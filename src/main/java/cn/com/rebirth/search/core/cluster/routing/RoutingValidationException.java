/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RoutingValidationException.java 2012-7-6 14:30:42 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing;

/**
 * The Class RoutingValidationException.
 *
 * @author l.xue.nong
 */
public class RoutingValidationException extends RoutingException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6168202052986842259L;

	/** The validation. */
	private final RoutingTableValidation validation;

	/**
	 * Instantiates a new routing validation exception.
	 *
	 * @param validation the validation
	 */
	public RoutingValidationException(RoutingTableValidation validation) {
		super(validation.toString());
		this.validation = validation;
	}

	/**
	 * Validation.
	 *
	 * @return the routing table validation
	 */
	public RoutingTableValidation validation() {
		return this.validation;
	}
}