package org.freehg.hgkit.core;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


class Fragment {
    int start;
    int end;
    byte[] data;

    // Maybe this can differ from data length
    int mlength = -1;
    int len() {
        if (mlength == -1) {
            mlength = data.length;
        }
        return mlength;
    }

    public void len(int len) {
        mlength = len;
    }
    
    @Override
    public String toString() {
        String txt = new String(this.data);
        if(80 < txt.length() ) {
            int max = Math.min(80, len());
            txt = txt.substring(0,max);
            if(len() > 80) {
                txt += "...";
            }
        }
        return start + " " + end + " " + len() + " " + txt;
    }

}

public class MDiff {
    
    public static byte[] patches(byte[] in, 
            List<byte[]> bins) {
        
        // if there are no fragments we dont have to do anything
        if (bins.size() < 1) {
            return in;
        }
        // convert binary to fragments
        List<Fragment> patch = fold(bins, 0, bins.size());
        if (patch == null) {
            return null;
        }
        // apply all fragments to in
        return apply(in, in.length, patch);
    }

    private static List<Fragment> fold(List<byte[]> bins, 
            int start, 
            int end) {
        
        /* recursively generate a patch of all bins between 
         * start and end */
        if (start + 1 == end) {
            /* trivial case, output a decoded list */
            byte[] bytes = bins.get(start);
            return decode(bytes, bytes.length);
        }

        /* divide and conquer, memory management is elsewhere */
        int len = (end - start) / 2;
        List<Fragment> left = fold(bins, start, start + len);
        List<Fragment> right = fold(bins, start + len, end);
        return combine(left, right);
    }

    /*
     * combine hunk lists a and b, while adjusting b for offset changes in
     * a this deletes a and b and returns the resultant list.
     */
    private static List<Fragment> combine(List<Fragment> a, 
    			List<Fragment> b) {

        if (a == null || b == null) {
            return null;
        }
        List<Fragment> combination = new ArrayList<Fragment>(2 * (a.size() + b.size()));
        int offset = 0;
        for (Fragment bFrag : b) {
            /* save old hunks */
            offset = gather(combination, 
            		a, 
            		bFrag.start, 
            		offset);

            /* discard replaced hunks */
            int post = discard(a, bFrag.end, offset);

            // create a new fragment from an existing with ajustments
            Fragment ct = new Fragment();
            ct.start = bFrag.start - offset;
            ct.end = bFrag.end - post;
            ct.data = bFrag.data;
            ct.len( bFrag.len());
            combination.add(ct);

            offset = post;
        }

        /* hold on to tail from a */
        combination.addAll(a);
        a.clear();
        b.clear();
        return combination;
    }

    // static int discard(struct flist *src, int cut, int offset) {
    private static int discard(List<Fragment> src, 
    		int cut, 
    		int poffset) {

    	int offset = poffset;
        int postend, c, l;
//         i think this discards everything up to "cut"
//         a fragment may have to be split if it 
//         overlaps "cut"
        for(Iterator<Fragment> iter = src.iterator(); iter.hasNext(); ) {
            final Fragment s = iter.next();
            if (cut <= s.start + offset) {
                break;
            }
            
            postend = offset + s.start + s.len();
            if (postend <= cut) {
                // this one is discarded
                offset += s.start + s.len() - s.end;
                iter.remove();
            } else {
                // partial discarding
                c = cut - offset;
                if (s.end < c) {
                    c = s.end;
                }
                l = cut - offset - s.start;
                if (s.len() < l)
                    l = s.len();

                offset += s.start + l - c;
                s.start = c;
                s.len(s.len() - l);

                // s.data = s.data + l;
                // this should work, but doesnt, bug? 
                // s.data = Arrays.copyOfRange(s.data, l, s.len());
                s.data = Arrays.copyOfRange(s.data, l, s.data.length);
                // no more needs to be discarded
                break;
            }
        }
        return offset;
    }

    private static int gather(List<Fragment> dest, 
            List<Fragment> src, 
            int cut,
            int poffset) {
        
        /*
         * move hunks in source that are less than cut to dest, 
         * but compensate for changes in offset. 
         * The last hunk may be split if necessary (oberlaps cut).
         */
        int offset = poffset;
        Fragment s = null;

        for(Iterator<Fragment> iter = src.iterator(); 
                iter.hasNext(); ) {
            s = iter.next();
            if (cut <= s.start + offset)
                break; /* we've gone far enough */

            int postend = offset + s.start + s.len();
            if (postend <= cut) {
                /* save this hunk as it is */
                offset += s.start + s.len() - s.end;
                // *d++ = *s++;
                dest.add(s);
                // at the endf of this algorithm
                // src should have s as head, this one could 
                // be safely removed if there is another s here
                if( iter.hasNext()) {
                    iter.remove();
                }
            } else {
                /* This hunk must be broken up */
                int cutAt = cut - offset;
                if (s.end < cutAt) {
                    cutAt = s.end;
                }
                int length = cut - offset - s.start;
                if (s.len() < length) {
                    length = s.len();
                }

                offset += s.start + length - cutAt;
                Fragment d = new Fragment();
                d.start = s.start;
                d.end = cutAt;
                d.data = s.data;
                d.len(length);
                // d++;
                dest.add(d);

                s.start = cutAt;
                s.len(s.len() - length);
                // s.data = s.data + l;
                s.data = Arrays.copyOfRange(s.data, length, s.data.length);
                break;
            }
        }
        if(0 < src.size() && s != src.get(0)) {
            throw new IllegalStateException("src head should be s");
        }
        return offset;
    }

    /**
     * decode a binary patch into a fragment list
     * 
     * @param bin the binary patch
     * @param length the length of the patch
     * @return a list of fragments
     */
    static List<Fragment> decode(byte[] bin, int length) {
        // 12 is some sort of magic number
        // Seems like a hunk-header size
        List<Fragment> result = new ArrayList<Fragment>();
        ByteBuffer wrap = ByteBuffer.wrap(bin);

        int binPtr = 0;
        int dataPtr = 12;


        byte decode[] = new byte[12];
        while (dataPtr <= length) {
            // Read the fragment header without moving position
            wrap.position(binPtr);
            wrap.get(decode, 0, 12);
            wrap.position(binPtr);

            Fragment lt = new Fragment();
            result.add(lt);

            lt.start = (int) ntohl(decode, 0);
            lt.end = (int) ntohl(decode, 4);
            lt.len((int) ntohl(decode, 8));
            
            if (lt.start > lt.end) {
                break; /* sanity check */
            }

            binPtr = dataPtr + lt.len();

            if (binPtr < dataPtr) {
                throw new IllegalStateException(
                        "Programmer Unsure of what  'big data + big (bogus) len can wrap around' means");
                // break; /* big data + big (bogus) len can wrap around */
            }

            lt.data = new byte[length - dataPtr];
            if (0 < lt.data.length) {
                wrap.position(dataPtr);
                wrap.get(lt.data, 0, lt.data.length);
            }
            // data = bin + 12;
            dataPtr = binPtr + 12;
        }

        if( binPtr != length) {
            throw new IllegalStateException("patch cannot be decoded");
        }
        return result;
    }

    static long ntohl(byte[] decode, int offset) {
        // network order is bigendian, as is java by native
        ByteBuffer buffer = ByteBuffer.wrap(decode);
        buffer.position(offset);
        int x = buffer.getInt();

        if( x < -20000) {
        	throw new IllegalStateException("Assumptions are wrong");
        }
        
        long uint32 = ((x & 0x000000ffL) << 24) | ((x & 0x0000ff00L) << 8)
                | ((x & 0x00ff0000L) >> 8) | ((x & 0xff000000L) >> 24);
        if (true) {
            return x;
        }
        return uint32;

    }

    
    private static byte[] apply(byte[] orig, 
            int len, 
            List<Fragment> patch) {

        // FIXME This seems wrong
        int last = 0;
        ByteArrayOutputStream p = new ByteArrayOutputStream(len);
        
        for (Fragment f : patch) {
            // if this fragment is not within the bounds
            if (f.start < last || len < f.end) {
            	throw new IllegalStateException("invalid patch");
            }
            p.write(orig, last, f.start - last);
            p.write(f.data, 0, f.len());
            last = f.end;
            
            
        }
        // memcpy(p, orig + last, len - last);
        p.write(orig, last, len - last);
        return p.toByteArray();
    }

	public static byte[] patches(byte[] bytes, byte[] patch) {
		ArrayList<byte[]> list = new ArrayList<byte[]>(1);
		list.add(patch);
		return patches(bytes,list);
		
	}
}
