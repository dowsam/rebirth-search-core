/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core AbstractDump.java 2012-7-6 14:29:35 l.xue.nong$$
 */

package cn.com.rebirth.search.core.monitor.dump;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;

import cn.com.rebirth.commons.Nullable;

import com.google.common.collect.ImmutableMap;

/**
 * The Class AbstractDump.
 *
 * @author l.xue.nong
 */
public abstract class AbstractDump implements Dump {

	/** The timestamp. */
	private final long timestamp;

	/** The cause. */
	private final String cause;

	/** The context. */
	private final Map<String, Object> context;

	/** The files. */
	private final ArrayList<File> files = new ArrayList<File>();

	/**
	 * Instantiates a new abstract dump.
	 *
	 * @param timestamp the timestamp
	 * @param cause the cause
	 * @param context the context
	 */
	protected AbstractDump(long timestamp, String cause, @Nullable Map<String, Object> context) {
		this.timestamp = timestamp;
		this.cause = cause;
		if (context == null) {
			context = ImmutableMap.of();
		}
		this.context = context;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.monitor.dump.Dump#timestamp()
	 */
	@Override
	public long timestamp() {
		return timestamp;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.monitor.dump.Dump#context()
	 */
	@Override
	public Map<String, Object> context() {
		return this.context;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.monitor.dump.Dump#cause()
	 */
	@Override
	public String cause() {
		return cause;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.monitor.dump.Dump#files()
	 */
	@Override
	public File[] files() {
		return files.toArray(new File[files.size()]);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.monitor.dump.Dump#createFile(java.lang.String)
	 */
	@Override
	public File createFile(String name) throws DumpException {
		File file = doCreateFile(name);
		files.add(file);
		return file;
	}

	/**
	 * Do create file.
	 *
	 * @param name the name
	 * @return the file
	 * @throws DumpException the dump exception
	 */
	protected abstract File doCreateFile(String name) throws DumpException;

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.monitor.dump.Dump#createFileOutputStream(java.lang.String)
	 */
	@Override
	public OutputStream createFileOutputStream(String name) throws DumpException {
		try {
			return new FileOutputStream(createFile(name));
		} catch (FileNotFoundException e) {
			throw new DumpException("Failed to create file [" + name + "]", e);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.monitor.dump.Dump#createFileWriter(java.lang.String)
	 */
	@Override
	public Writer createFileWriter(String name) throws DumpException {
		try {
			return new FileWriter(createFile(name));
		} catch (IOException e) {
			throw new DumpException("Failed to create file [" + name + "]", e);
		}
	}
}
