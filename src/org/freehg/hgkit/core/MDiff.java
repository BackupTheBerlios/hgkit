package org.freehg.hgkit.core;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


class Frag {
	int start; 
	int end;
	// May differ from stirng data lenght
	int mlen = -1;
	byte[] data;
	
	int len() {
		if(mlen == -1) {
			mlen = data.length;
		}
		return mlen;
	}
	
	public void len(int len) {
		mlen = len;
	}

}


public class MDiff {

	// static PyObject *
	// patches(PyObject *self, PyObject *args)
	// {
	public static String patches(String text, List<String> bins) {

		// PyObject *text, *bins, *result;
		// struct flist *patch;
		// const char *in;
		// char *out;
		// int len, outlen;
		// Py_ssize_t inlen;
		//
		// if (!PyArg_ParseTuple(args, "OO:mpatch", &text, &bins))
		// return NULL;
		//
		// len = PyList_Size(bins);
		// if (!len) {
		// /* nothing to do */
		// Py_INCREF(text);
		// }
		if (bins.size() < 1) {
			return text;
		}
		// TODO Whatdoes these do? In which case do I have to return null?
		// it would seem as it converts text to a charbuffer named "in"
		// if (PyObject_AsCharBuffer(text, &in, &inlen))
		// 		return NULL;
		//
		// patch = fold(bins, 0, len);
		// if (!patch)
		// 		return NULL;
		//
		List<Frag> patch = fold(bins,0, bins.size());
		if (patch == null) {
			return null;
		}
		// outlen = calcsize(inlen, patch);
		// if (outlen < 0) {
		// 		result = NULL;
		// 		goto cleanup;
		// }
		// result = PyString_FromStringAndSize(NULL, outlen);
		// if (!result) {
		// 		result = NULL;
		// 		goto cleanup;
		// }
		// out = PyString_AsString(result);
		// apply patch to in and store in out
		// if (!apply(out, in, inlen, patch)) {
		// 		Py_DECREF(result);
		// 		result = NULL;
		// }
		String result = apply(text, patch);
		return result;
		// cleanup:
		// lfree(patch);
		// return result;
		// }

	}


	private static List<Frag> fold(List<String> bins, int start, int end) {
		/* recursively generate a patch of all bins between start and end */

			if (start + 1 == end) {
				/* trivial case, output a decoded list */
				byte[] bytes = bins.get(start).getBytes();
                return decode(bytes, bytes.length);
			}

			/* divide and conquer, memory management is elsewhere */
			int len = (end - start) / 2;
			return combine(fold(bins, start, start + len),
						   fold(bins, start + len, end));
		}

	private static List<Frag> combine(List<Frag> a, List<Frag> b) {
		/* combine hunk lists a and b, while adjusting b for offset changes in a/
	   this deletes a and b and returns the resultant list. */

		if (a == null || b == null) {
			return null;
		}
		// struct flist *c = NULL;
		// c = lalloc((lsize(a) + lsize(b)) * 2);
		List<Frag> combination = new ArrayList<Frag>(2 * (a.size() + b.size())); 
		// struct frag *bh, *ct;
		int offset = 0, post;
		// for (bh = b->head; bh != b->tail; bh++) {
		for (Frag frag : b) {
			
			/* save old hunks */
			offset = gather(combination, a, frag.start, offset);

			/* discard replaced hunks */
			post = discard(a, frag.end, offset);

			/* insert new hunk */
			// ct = c->tail;
			
			Frag ct = new Frag();
			ct.start = frag.start - offset;
			ct.end = frag.end - post;
			ct.data = frag.data;
			// c.tail++;
			combination.add(ct);
			
			offset = post;
		}

		/* hold on to tail from a */
		// // void * memcpy ( void * destination, const void * source, size_t num );
		// memcpy(c->tail, a->head, sizeof(struct frag) * lsize(a));
		// c->tail += lsize(a);
		combination.addAll(a);
		return combination;

		
	}

	// static int discard(struct flist *src, int cut, int offset) {
	private static int discard(List<Frag> src, int cut, int poffset) {
		// struct frag *s = src->head;
	    int offset = poffset;
		Frag s = null;
		int postend, c, l;
		// while (s != src->tail) {
		for(Frag frag : src) {
			s = frag;
			if (s.start + offset >= cut)
				break;

			postend = offset + s.start + s.len();
			if (postend <= cut) {
				offset += s.start + s.len() - s.end;
			}
			else {
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


	private static int gather(List<Frag> dest, List<Frag> src, int cut,
			int poffset) {
		/*
		 * move hunks in source that are less cut to dest, compensating for
		 * changes in offset. the last hunk may be split if necessary.
		 */
		// struct frag *d = dest->tail, *s = src->head;
	    int offset = poffset;
		int postend, c, l;

		Frag s = null;
		// while (s != src->tail) {
		for(Frag frag : src) {
			s = frag;
			if (s.start + offset >= cut)
				break; /* we've gone far enough */

			postend = offset + s.start + s.len();
			if (postend <= cut) {
				/* save this hunk */
				offset += s.start + s.len() - s.end;
				// *d++ = *s++;
				dest.add(s);
			} else {
				/* break up this hunk */
				c = cut - offset;
				if (s.end < c)
					c = s.end;
				l = cut - offset - s.start;
				if (s.len() < l)
					l = s.len();

				offset += s.start + l - c;
				Frag d  = new Frag();
				d.start = s.start;
				d.end = c;
				d.data = s.data;
				d.len(l);
				// d++;
				dest.add(d);

				s.start = c;
				s.len(s.len() - l);
				// s.data = s.data + l;
				s.data = Arrays.copyOfRange(s.data, l, s.data.length);
				break;
			}
		}

		// dest->tail = d;
		// src->head = s;
		// this is
		src.set(0, s);
		return offset;
	}


	// static struct flist *decode(const char *bin, int len) {
	private static List<Frag> decode(byte[] bin, int length) {

	        // struct flist *l;
	        // struct frag *lt;
			List<Frag> result = new ArrayList<Frag>();
			Frag lt = null;
			
			// const char *data = bin + 12;
			// const char *end = bin + len;
			byte[] data = Arrays.copyOfRange(bin, 12, bin.length);
			
			// char decode[12]; /* for dealing with alignment issues */
			byte decode[] = new byte[12];

			/* assume worst case size, we won't have many of these lists */
            //	result = lalloc(len / 12);
            //	if (!result)
            //		return NULL;

			// lt = l->tail;
			
			int binp = 0;
			int datap = 0;
			int end = length;

			ByteBuffer wrap = ByteBuffer.wrap(bin);

			while (datap <= end) {
				memcpy(decode, bin, 12);
				lt = new Frag();
				result.add(lt);
				lt.start = ntohl(decode,0);
				lt.end = ntohl(decode,  4);
				lt.len( ntohl(decode, 8) );
				if (lt.start > lt.end)
					break; /* sanity check */
				binp = datap + lt.len();
				if (binp < datap)
					break; /* big data + big (bogus) len can wrap around */
				lt.data = data;
				// data = bin + 12;
				data = Arrays.copyOfRange(bin, 12, bin.length);
				
				// lt++;
			}
			
//			if (bin != end) {
//				if (!PyErr_Occurred())
//					PyErr_SetString(mpatch_Error, "patch cannot be decoded");
//				lfree(l);
//				return NULL;
//			}

			// l->tail = lt;
			return result;	
	}


    private static int ntohl(byte[] decode, int offset) {
        throw new RuntimeException("NIE");
    }


    private static void memcpy(byte[] dest, byte[] src, int length) {
        if( dest.length != length
            || src.length < length) {
            
            throw new IllegalArgumentException("Destination length must == length");
        }
        for(int byteIndex = 0; byteIndex < length; byteIndex++) {
            dest[byteIndex] = src[byteIndex];
        }
    }


    private static String apply(String text, Object patch) {
		throw new RuntimeException("NIE");
	}
}
