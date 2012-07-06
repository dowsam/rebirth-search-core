/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ValidateActions.java 2012-7-6 14:30:14 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action;

/**
 * The Class ValidateActions.
 *
 * @author l.xue.nong
 */
public class ValidateActions {

	/**
	 * Adds the validation error.
	 *
	 * @param error the error
	 * @param validationException the validation exception
	 * @return the action request validation exception
	 */
	public static ActionRequestValidationException addValidationError(String error,
			ActionRequestValidationException validationException) {
		if (validationException == null) {
			validationException = new ActionRequestValidationException();
		}
		validationException.addValidationError(error);
		return validationException;
	}
}
