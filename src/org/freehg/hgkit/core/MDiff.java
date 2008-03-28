package org.freehg.hgkit.core;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
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
            txt = txt.substring(0,80) + "...";
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
        // convert to fragments
        List<Fragment> patch = fold(bins, 0, bins.size());
        if (patch == null) {
            return null;
        }
        // apply all delta fragments to in
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

    private static List<Fragment> combine(List<Fragment> a, List<Fragment> b) {
        /*
         * combine hunk lists a and b, while adjusting b for offset changes in
         * a this deletes a and b and returns the resultant list.
         */

        if (a == null || b == null) {
            return null;
        }
        List<Fragment> combination = new ArrayList<Fragment>(2 * (a.size() + b.size()));
        int offset = 0;
        for (Fragment frag : b) {
            /* save old hunks */
            offset = gather(combination, a, frag.start, offset);

            /* discard replaced hunks */
            int post = discard(a, frag.end, offset);

            // create a new fragment from an existing with ajustments
            Fragment ct = new Fragment();
            ct.start = frag.start - offset;
            ct.end = frag.end - post;
            ct.data = frag.data;
            combination.add(ct);

            offset = post;
        }

        /* hold on to tail from a */
        combination.addAll(a);
        return combination;
    }

    // static int discard(struct flist *src, int cut, int offset) {
    private static int discard(List<Fragment> src, int cut, int poffset) {
        // struct frag *s = src->head;
        int offset = poffset;
        Fragment s = null;
        int postend, c, l;
        // while (s != src->tail) {
        for (Fragment frag : src) {
            
            s = frag;
            if (s == null  
                || (s.start + offset >= cut)) {
             
                break;
            }

            postend = offset + s.start + s.len();
            if (postend <= cut) {
                offset += s.start + s.len() - s.end;
            } else {
                c = cut - offset;
                if (s.end < c)
                    c = s.end;
                l = cut - offset - s.start;
                if (s.len() < l)
                    l = s.len();

                offset += s.start + l - c;
                s.start = c;
                s.len(s.len() - l);

                // s.data = s.data + l;
                s.data = Arrays.copyOfRange(s.data, l, s.data.length);

                break;
            }
        }

        // src.head = s;
        // seems like a no-op? no, it clear src, pointing at null
        src.clear();
        return offset;
    }

    private static int gather(List<Fragment> dest, 
            List<Fragment> src, 
            int cut,
            int poffset) {
        
        /*
         * move hunks in source that are less cut to dest, compensating for
         * changes in offset. the last hunk may be split if necessary.
         */
        // struct frag *d = dest->tail, *s = src->head;
        int offset = poffset;
        

        Fragment s = null;
        // while (s != src->tail) {
        for (Fragment frag : src) {
            s = frag;
            if (s.start + offset >= cut)
                break; /* we've gone far enough */

            int postend = offset + s.start + s.len();
            if (postend <= cut) {
                /* save this hunk as it is */
                offset += s.start + s.len() - s.end;
                // *d++ = *s++;
                dest.add(s);
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

        // dest->tail = d;
        // src->head = s;
        // this is
        if (src.size() < 1) {
            src.add(s);
        } else {
            src.set(0, s);
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
            int backupPos = wrap.position();
            wrap.get(decode, binPtr, 12);
            wrap.position(backupPos);

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
        // TODO It is very likely this is erroneuos

        // network order is bigendian, as is java
        ByteBuffer buffer = ByteBuffer.wrap(decode);
        buffer.position(offset);
        int x = buffer.getInt();

        long uint32 = ((x & 0x000000ffL) << 24) | ((x & 0x0000ff00L) << 8)
                | ((x & 0x00ff0000L) >> 8) | ((x & 0xff000000L) >> 24);
        if (true) {
            return x;
        }
        return uint32;

    }

    // TODO Unused?
    static void memcpy(byte[] dest, 
            byte[] src,
            int srcOffset,
            int length) {

        if (dest.length != length || src.length < length) {

            throw new IllegalArgumentException(
                    "Destination length must == length");
        }
        for (int byteIndex = 0; byteIndex < length; byteIndex++) {
            dest[byteIndex] = src[byteIndex + srcOffset];
        }
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
                // throw new IllegalStateException("invalid patch");
            }
            
            int writeLength = f.start - last;
            writeLength = Math.min(writeLength, len - last);
            if( 0 < writeLength ) {
                p.write(orig, last, writeLength);
            }
            p.write(f.data, 0, f.len());
            last = f.end;
        }
        int wrl = len - last;
        if(0 < wrl && (last + wrl) < orig.length ) {
            p.write(orig, last, wrl);
        }
        return p.toByteArray();
    }
}
