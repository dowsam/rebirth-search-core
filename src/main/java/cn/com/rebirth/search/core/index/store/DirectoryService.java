/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DirectoryService.java 2012-3-29 15:00:59 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.store;

import java.io.IOException;

import org.apache.lucene.store.Directory;


/**
 * The Interface DirectoryService.
 *
 * @author l.xue.nong
 */
public interface DirectoryService {

	
	/**
	 * Builds the.
	 *
	 * @return the directory[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	Directory[] build() throws IOException;

	
	/**
	 * Rename file.
	 *
	 * @param dir the dir
	 * @param from the from
	 * @param to the to
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void renameFile(Directory dir, String from, String to) throws IOException;

	
	/**
	 * Full delete.
	 *
	 * @param dir the dir
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void fullDelete(Directory dir) throws IOException;
}
