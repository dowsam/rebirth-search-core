/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ActionListener.java 2012-3-29 15:01:30 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action;

import java.awt.event.ActionEvent;


/**
 * The listener interface for receiving action events.
 * The class that is interested in processing a action
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addActionListener<code> method. When
 * the action event occurs, that object's appropriate
 * method is invoked.
 *
 * @param <Response> the generic type
 * @see ActionEvent
 */
public interface ActionListener<Response> {

	
	/**
	 * On response.
	 *
	 * @param response the response
	 */
	void onResponse(Response response);

	
	/**
	 * On failure.
	 *
	 * @param e the e
	 */
	void onFailure(Throwable e);
}
