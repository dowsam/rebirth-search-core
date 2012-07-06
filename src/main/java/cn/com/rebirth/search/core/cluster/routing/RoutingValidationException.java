/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RoutingValidationException.java 2012-3-29 15:02:14 l.xue.nong$$
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