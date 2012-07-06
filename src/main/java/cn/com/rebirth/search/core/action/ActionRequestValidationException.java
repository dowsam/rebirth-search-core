/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ActionRequestValidationException.java 2012-7-6 14:30:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action;

import java.util.ArrayList;
import java.util.List;

import cn.com.rebirth.commons.exception.RebirthException;

/**
 * The Class ActionRequestValidationException.
 *
 * @author l.xue.nong
 */
public class ActionRequestValidationException extends RebirthException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8763216879359554632L;

	/** The validation errors. */
	private final List<String> validationErrors = new ArrayList<String>();

	/**
	 * Instantiates a new action request validation exception.
	 */
	public ActionRequestValidationException() {
		super(null);
	}

	/**
	 * Adds the validation error.
	 *
	 * @param error the error
	 */
	public void addValidationError(String error) {
		validationErrors.add(error);
	}

	/**
	 * Adds the validation errors.
	 *
	 * @param errors the errors
	 */
	public void addValidationErrors(Iterable<String> errors) {
		for (String error : errors) {
			validationErrors.add(error);
		}
	}

	/**
	 * Validation errors.
	 *
	 * @return the list
	 */
	public List<String> validationErrors() {
		return validationErrors;
	}

	/* (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("Validation Failed: ");
		int index = 0;
		for (String error : validationErrors) {
			sb.append(++index).append(": ").append(error).append(";");
		}
		return sb.toString();
	}
}
