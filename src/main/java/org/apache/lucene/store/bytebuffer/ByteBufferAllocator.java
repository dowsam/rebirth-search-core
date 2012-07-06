/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ByteBufferAllocator.java 2012-7-6 14:28:44 l.xue.nong$$
 */

package org.apache.lucene.store.bytebuffer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * The Interface ByteBufferAllocator.
 *
 * @author l.xue.nong
 */
public interface ByteBufferAllocator {

	/**
	 * The Class Cleaner.
	 *
	 * @author l.xue.nong
	 */
	public static class Cleaner {

		/** The Constant CLEAN_SUPPORTED. */
		public static final boolean CLEAN_SUPPORTED;

		/** The Constant directBufferCleaner. */
		private static final Method directBufferCleaner;

		/** The Constant directBufferCleanerClean. */
		private static final Method directBufferCleanerClean;

		static {
			Method directBufferCleanerX = null;
			Method directBufferCleanerCleanX = null;
			boolean v;
			try {
				directBufferCleanerX = Class.forName("java.nio.DirectByteBuffer").getMethod("cleaner");
				directBufferCleanerX.setAccessible(true);
				directBufferCleanerCleanX = Class.forName("sun.misc.Cleaner").getMethod("clean");
				directBufferCleanerCleanX.setAccessible(true);
				v = true;
			} catch (Exception e) {
				v = false;
			}
			CLEAN_SUPPORTED = v;
			directBufferCleaner = directBufferCleanerX;
			directBufferCleanerClean = directBufferCleanerCleanX;
		}

		/**
		 * Clean.
		 *
		 * @param buffer the buffer
		 */
		public static void clean(ByteBuffer buffer) {
			if (CLEAN_SUPPORTED && buffer.isDirect()) {
				try {
					Object cleaner = directBufferCleaner.invoke(buffer);
					directBufferCleanerClean.invoke(cleaner);
				} catch (Exception e) {

				}
			}
		}
	}

	/**
	 * The Enum Type.
	 *
	 * @author l.xue.nong
	 */
	public static enum Type {

		/** The small. */
		SMALL,

		/** The large. */
		LARGE
	}

	/**
	 * Size in bytes.
	 *
	 * @param type the type
	 * @return the int
	 */
	int sizeInBytes(Type type);

	/**
	 * Allocate.
	 *
	 * @param type the type
	 * @return the byte buffer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	ByteBuffer allocate(Type type) throws IOException;

	/**
	 * Release.
	 *
	 * @param buffer the buffer
	 */
	void release(ByteBuffer buffer);

	/**
	 * Close.
	 */
	void close();
}
