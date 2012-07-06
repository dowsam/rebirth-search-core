/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ThreadingModel.java 2012-3-29 15:01:14 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action;

import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;



/**
 * The Enum ThreadingModel.
 *
 * @author l.xue.nong
 */
public enum ThreadingModel {

	
	/** The NONE. */
	NONE((byte) 0),

	
	/** The OPERATION. */
	OPERATION((byte) 1),

	
	/** The LISTENER. */
	LISTENER((byte) 2),

	
	/** The OPERATIO n_ listener. */
	OPERATION_LISTENER((byte) 3);

	
	/** The id. */
	private byte id;

	
	/**
	 * Instantiates a new threading model.
	 *
	 * @param id the id
	 */
	ThreadingModel(byte id) {
		this.id = id;
	}

	
	/**
	 * Id.
	 *
	 * @return the byte
	 */
	public byte id() {
		return this.id;
	}

	
	/**
	 * Threaded operation.
	 *
	 * @return true, if successful
	 */
	public boolean threadedOperation() {
		return this == OPERATION || this == OPERATION_LISTENER;
	}

	
	/**
	 * Threaded listener.
	 *
	 * @return true, if successful
	 */
	public boolean threadedListener() {
		return this == LISTENER || this == OPERATION_LISTENER;
	}

	
	/**
	 * Adds the listener.
	 *
	 * @return the threading model
	 */
	public ThreadingModel addListener() {
		if (this == NONE) {
			return LISTENER;
		}
		if (this == OPERATION) {
			return OPERATION_LISTENER;
		}
		return this;
	}

	
	/**
	 * Removes the listener.
	 *
	 * @return the threading model
	 */
	public ThreadingModel removeListener() {
		if (this == LISTENER) {
			return NONE;
		}
		if (this == OPERATION_LISTENER) {
			return OPERATION;
		}
		return this;
	}

	
	/**
	 * Adds the operation.
	 *
	 * @return the threading model
	 */
	public ThreadingModel addOperation() {
		if (this == NONE) {
			return OPERATION;
		}
		if (this == LISTENER) {
			return OPERATION_LISTENER;
		}
		return this;
	}

	
	/**
	 * Removes the operation.
	 *
	 * @return the threading model
	 */
	public ThreadingModel removeOperation() {
		if (this == OPERATION) {
			return NONE;
		}
		if (this == OPERATION_LISTENER) {
			return LISTENER;
		}
		return this;
	}

	
	/**
	 * From id.
	 *
	 * @param id the id
	 * @return the threading model
	 */
	public static ThreadingModel fromId(byte id) {
		if (id == 0) {
			return NONE;
		} else if (id == 1) {
			return OPERATION;
		} else if (id == 2) {
			return LISTENER;
		} else if (id == 3) {
			return OPERATION_LISTENER;
		} else {
			throw new RestartIllegalArgumentException("No threading model for [" + id + "]");
		}
	}
}
