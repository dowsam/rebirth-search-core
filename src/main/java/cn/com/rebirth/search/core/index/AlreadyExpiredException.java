/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AlreadyExpiredException.java 2012-3-29 15:00:51 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index;

import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.search.core.index.engine.IgnoreOnRecoveryEngineException;


/**
 * The Class AlreadyExpiredException.
 *
 * @author l.xue.nong
 */
public class AlreadyExpiredException extends RestartException implements IgnoreOnRecoveryEngineException {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8301140662841658275L;

	
	/** The index. */
	private String index;

	
	/** The type. */
	private String type;

	
	/** The id. */
	private String id;

	
	/** The timestamp. */
	private final long timestamp;

	
	/** The ttl. */
	private final long ttl;

	
	/** The now. */
	private final long now;

	
	/**
	 * Instantiates a new already expired exception.
	 *
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 * @param timestamp the timestamp
	 * @param ttl the ttl
	 * @param now the now
	 */
	public AlreadyExpiredException(String index, String type, String id, long timestamp, long ttl, long now) {
		super("already expired [" + index + "]/[" + type + "]/[" + id + "] due to expire at [" + (timestamp + ttl)
				+ "] and was processed at [" + now + "]");
		this.index = index;
		this.type = type;
		this.id = id;
		this.timestamp = timestamp;
		this.ttl = ttl;
		this.now = now;
	}

	
	/**
	 * Index.
	 *
	 * @return the string
	 */
	public String index() {
		return index;
	}

	
	/**
	 * Type.
	 *
	 * @return the string
	 */
	public String type() {
		return type;
	}

	
	/**
	 * Id.
	 *
	 * @return the string
	 */
	public String id() {
		return id;
	}

	
	/**
	 * Timestamp.
	 *
	 * @return the long
	 */
	public long timestamp() {
		return timestamp;
	}

	
	/**
	 * Ttl.
	 *
	 * @return the long
	 */
	public long ttl() {
		return ttl;
	}

	
	/**
	 * Now.
	 *
	 * @return the long
	 */
	public long now() {
		return now;
	}
}