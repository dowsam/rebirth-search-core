/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CacheKeyFilter.java 2012-7-6 14:28:51 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.filter.support;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;

import cn.com.rebirth.commons.Unicode;

/**
 * The Interface CacheKeyFilter.
 *
 * @author l.xue.nong
 */
public interface CacheKeyFilter {

	/**
	 * The Class Key.
	 *
	 * @author l.xue.nong
	 */
	public static class Key {

		/** The bytes. */
		private final byte[] bytes;

		/** The hash code. */
		private final int hashCode;

		/**
		 * Instantiates a new key.
		 *
		 * @param bytes the bytes
		 */
		public Key(byte[] bytes) {
			this.bytes = bytes;
			this.hashCode = Arrays.hashCode(bytes);
		}

		/**
		 * Instantiates a new key.
		 *
		 * @param str the str
		 */
		public Key(String str) {
			this(Unicode.fromStringAsBytes(str));
		}

		/**
		 * Bytes.
		 *
		 * @return the byte[]
		 */
		public byte[] bytes() {
			return this.bytes;
		}

		/**
		 * Utf8 to string.
		 *
		 * @return the string
		 */
		public String utf8ToString() {
			return Unicode.fromBytes(bytes);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o.getClass() != this.getClass()) {
				return false;
			}
			Key bytesWrap = (Key) o;
			return Arrays.equals(bytes, bytesWrap.bytes);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return hashCode;
		}
	}

	/**
	 * The Class Wrapper.
	 *
	 * @author l.xue.nong
	 */
	public static class Wrapper extends Filter implements CacheKeyFilter {

		/** The filter. */
		private final Filter filter;

		/** The key. */
		private final Key key;

		/**
		 * Instantiates a new wrapper.
		 *
		 * @param filter the filter
		 * @param key the key
		 */
		public Wrapper(Filter filter, Key key) {
			this.filter = filter;
			this.key = key;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.cache.filter.support.CacheKeyFilter#cacheKey()
		 */
		@Override
		public Key cacheKey() {
			return key;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Filter#getDocIdSet(org.apache.lucene.index.IndexReader)
		 */
		@Override
		public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
			return filter.getDocIdSet(reader);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return filter.hashCode();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			return filter.equals(obj);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return filter.toString();
		}
	}

	/**
	 * Cache key.
	 *
	 * @return the key
	 */
	Key cacheKey();
}