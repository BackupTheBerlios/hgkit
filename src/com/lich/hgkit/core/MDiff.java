package com.lich.hgkit.core;

import java.util.ArrayList;
import java.util.List;


class Frag {
	int start; 
	int end;	
	int len() {
		return data.length();
	}
	String data;

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
			StringBuilder buffer = new StringBuilder();

			if (start + 1 == end) {
				/* trivial case, output a decoded list */
				String hunk = bins.get(start);
				buffer.append(hunk);
				return decode(buffer, buffer.length());
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
		List<Frag> c = new ArrayList<Frag>(2 * (a.size() + b.size())); 
		// struct frag *bh, *ct;
		int offset = 0, post;
		// for (bh = b->head; bh != b->tail; bh++) {
		for (Frag frag : b) {
			
			/* save old hunks */
			offset = gather(c, a, frag.start, offset);

			/* discard replaced hunks */
			post = discard(a, frag.end, offset);

			/* insert new hunk */
			// ct = c->tail;
			Frag ct = c.get(c.size() - 1);
			ct.start = frag.start - offset;
			ct.end = frag.end - post;
			ct.data = frag.data;
			// c.tail++;
			offset = post;
		}

		/* hold on to tail from a */
		// // void * memcpy ( void * destination, const void * source, size_t num );
		// memcpy(c->tail, a->head, sizeof(struct frag) * lsize(a));
		// c->tail += lsize(a);
		c.addAll(a);
		return c;

		
		throw new RuntimeException("NIE");
	}

	private static List<Frag> decode(StringBuilder buffer, int length) {
		throw new RuntimeException("NIE");
	}

	private static String apply(String text, Object patch) {
		throw new RuntimeException("NIE");
	}
}
