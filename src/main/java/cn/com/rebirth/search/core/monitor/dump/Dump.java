/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core Dump.java 2012-7-6 14:30:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.monitor.dump;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;

/**
 * The Interface Dump.
 *
 * @author l.xue.nong
 */
public interface Dump {

	/**
	 * Timestamp.
	 *
	 * @return the long
	 */
	long timestamp();

	/**
	 * Context.
	 *
	 * @return the map
	 */
	Map<String, Object> context();

	/**
	 * Cause.
	 *
	 * @return the string
	 */
	String cause();

	/**
	 * Creates the file.
	 *
	 * @param name the name
	 * @return the file
	 * @throws DumpException the dump exception
	 */
	File createFile(String name) throws DumpException;

	/**
	 * Creates the file writer.
	 *
	 * @param name the name
	 * @return the writer
	 * @throws DumpException the dump exception
	 */
	Writer createFileWriter(String name) throws DumpException;

	/**
	 * Creates the file output stream.
	 *
	 * @param name the name
	 * @return the output stream
	 * @throws DumpException the dump exception
	 */
	OutputStream createFileOutputStream(String name) throws DumpException;

	/**
	 * Files.
	 *
	 * @return the file[]
	 */
	File[] files();

	/**
	 * Finish.
	 *
	 * @throws DumpException the dump exception
	 */
	void finish() throws DumpException;
}
