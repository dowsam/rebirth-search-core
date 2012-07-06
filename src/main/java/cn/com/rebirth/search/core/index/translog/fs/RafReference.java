/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RafReference.java 2012-7-6 14:29:19 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.translog.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Class RafReference.
 *
 * @author l.xue.nong
 */
public class RafReference {

	/** The file. */
	private final File file;

	/** The raf. */
	private final RandomAccessFile raf;

	/** The channel. */
	private final FileChannel channel;

	/** The ref count. */
	private final AtomicInteger refCount = new AtomicInteger();

	/**
	 * Instantiates a new raf reference.
	 *
	 * @param file the file
	 * @throws FileNotFoundException the file not found exception
	 */
	public RafReference(File file) throws FileNotFoundException {
		this.file = file;
		this.raf = new RandomAccessFile(file, "rw");
		this.channel = raf.getChannel();
		this.refCount.incrementAndGet();
	}

	/**
	 * File.
	 *
	 * @return the file
	 */
	public File file() {
		return this.file;
	}

	/**
	 * Channel.
	 *
	 * @return the file channel
	 */
	public FileChannel channel() {
		return this.channel;
	}

	/**
	 * Raf.
	 *
	 * @return the random access file
	 */
	public RandomAccessFile raf() {
		return this.raf;
	}

	/**
	 * Increase ref count.
	 *
	 * @return true, if successful
	 */
	public boolean increaseRefCount() {
		return refCount.incrementAndGet() > 1;
	}

	/**
	 * Decrease ref count.
	 *
	 * @param delete the delete
	 */
	public void decreaseRefCount(boolean delete) {
		if (refCount.decrementAndGet() <= 0) {
			try {
				raf.close();
				if (delete) {
					file.delete();
				}
			} catch (IOException e) {

			}
		}
	}
}
