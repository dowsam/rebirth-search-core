/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ValidateActions.java 2012-3-29 15:02:12 l.xue.nong$$
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
    public static ActionRequestValidationException addValidationError(String error, ActionRequestValidationException validationException) {
        if (validationException == null) {
            validationException = new ActionRequestValidationException();
        }
        validationException.addValidationError(error);
        return validationException;
    }
}
