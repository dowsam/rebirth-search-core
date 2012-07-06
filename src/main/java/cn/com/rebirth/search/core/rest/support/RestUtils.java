/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RestUtils.java 2012-3-29 15:02:33 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest.support;

import java.nio.charset.Charset;
import java.util.Map;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.commons.path.PathTrie;

import com.google.common.base.Charsets;


/**
 * The Class RestUtils.
 *
 * @author l.xue.nong
 */
public class RestUtils {

	
	/** The RES t_ decoder. */
	public static PathTrie.Decoder REST_DECODER = new PathTrie.Decoder() {
		@Override
		public String decode(String value) {
			return RestUtils.decodeComponent(value);
		}
	};

	
	/**
	 * Checks if is browser.
	 *
	 * @param userAgent the user agent
	 * @return true, if is browser
	 */
	public static boolean isBrowser(@Nullable String userAgent) {
		if (userAgent == null) {
			return false;
		}
		
		if (userAgent.startsWith("Mozilla")) {
			return true;
		}
		return false;
	}

	
	/**
	 * Decode query string.
	 *
	 * @param s the s
	 * @param fromIndex the from index
	 * @param params the params
	 */
	public static void decodeQueryString(String s, int fromIndex, Map<String, String> params) {
		if (fromIndex < 0) {
			return;
		}
		if (fromIndex >= s.length()) {
			return;
		}

		String name = null;
		int pos = fromIndex; 
		int i; 
		char c = 0; 
		for (i = fromIndex; i < s.length(); i++) {
			c = s.charAt(i);
			if (c == '=' && name == null) {
				if (pos != i) {
					name = decodeComponent(s.substring(pos, i));
				}
				pos = i + 1;
			} else if (c == '&') {
				if (name == null && pos != i) {
					
					
					
					addParam(params, decodeComponent(s.substring(pos, i)), "");
				} else if (name != null) {
					addParam(params, name, decodeComponent(s.substring(pos, i)));
					name = null;
				}
				pos = i + 1;
			}
		}

		if (pos != i) { 
			if (name == null) { 
				addParam(params, decodeComponent(s.substring(pos, i)), "");
			} else { 
				addParam(params, name, decodeComponent(s.substring(pos, i)));
			}
		} else if (name != null) { 
			addParam(params, name, "");
		}
	}

	
	/**
	 * Adds the param.
	 *
	 * @param params the params
	 * @param name the name
	 * @param value the value
	 */
	private static void addParam(Map<String, String> params, String name, String value) {
		params.put(name, value);
	}

	
	/**
	 * Decode component.
	 *
	 * @param s the s
	 * @return the string
	 */
	public static String decodeComponent(final String s) {
		return decodeComponent(s, Charsets.UTF_8);
	}

	
	/**
	 * Decode component.
	 *
	 * @param s the s
	 * @param charset the charset
	 * @return the string
	 */
	@SuppressWarnings("fallthrough")
	public static String decodeComponent(final String s, final Charset charset) {
		if (s == null) {
			return "";
		}
		final int size = s.length();
		boolean modified = false;
		for (int i = 0; i < size; i++) {
			final char c = s.charAt(i);
			switch (c) {
			case '%':
				i++; 
				
			case '+':
				modified = true;
				break;
			}
		}
		if (!modified) {
			return s;
		}
		final byte[] buf = new byte[size];
		int pos = 0; 
		for (int i = 0; i < size; i++) {
			char c = s.charAt(i);
			switch (c) {
			case '+':
				buf[pos++] = ' '; 
				break;
			case '%':
				if (i == size - 1) {
					throw new IllegalArgumentException("unterminated escape" + " sequence at end of string: " + s);
				}
				c = s.charAt(++i);
				if (c == '%') {
					buf[pos++] = '%'; 
					break;
				} else if (i == size - 1) {
					throw new IllegalArgumentException("partial escape" + " sequence at end of string: " + s);
				}
				c = decodeHexNibble(c);
				final char c2 = decodeHexNibble(s.charAt(++i));
				if (c == Character.MAX_VALUE || c2 == Character.MAX_VALUE) {
					throw new IllegalArgumentException("invalid escape sequence `%" + s.charAt(i - 1) + s.charAt(i)
							+ "' at index " + (i - 2) + " of: " + s);
				}
				c = (char) (c * 16 + c2);
				
			default:
				buf[pos++] = (byte) c;
				break;
			}
		}
		return new String(buf, 0, pos, charset);
	}

	
	/**
	 * Decode hex nibble.
	 *
	 * @param c the c
	 * @return the char
	 */
	private static char decodeHexNibble(final char c) {
		if ('0' <= c && c <= '9') {
			return (char) (c - '0');
		} else if ('a' <= c && c <= 'f') {
			return (char) (c - 'a' + 10);
		} else if ('A' <= c && c <= 'F') {
			return (char) (c - 'A' + 10);
		} else {
			return Character.MAX_VALUE;
		}
	}
}
