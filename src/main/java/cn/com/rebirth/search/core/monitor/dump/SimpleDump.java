/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SimpleDump.java 2012-3-29 15:01:20 l.xue.nong$$
 */


package cn.com.rebirth.search.core.monitor.dump;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import cn.com.rebirth.commons.Nullable;


/**
 * The Class SimpleDump.
 *
 * @author l.xue.nong
 */
public class SimpleDump extends AbstractDump {

	
	/** The location. */
	private final File location;

	
	/**
	 * Instantiates a new simple dump.
	 *
	 * @param timestamp the timestamp
	 * @param cause the cause
	 * @param context the context
	 * @param location the location
	 * @throws FileNotFoundException the file not found exception
	 */
	public SimpleDump(long timestamp, String cause, @Nullable Map<String, Object> context, File location)
			throws FileNotFoundException {
		super(timestamp, cause, context);
		this.location = location;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.monitor.dump.AbstractDump#doCreateFile(java.lang.String)
	 */
	@Override
	protected File doCreateFile(String name) throws DumpException {
		return new File(location, name);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.monitor.dump.Dump#finish()
	 */
	@Override
	public void finish() throws DumpException {

	}
}
