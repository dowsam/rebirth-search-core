/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ManagedAttribute.java 2012-7-6 14:29:40 l.xue.nong$$
 */

package cn.com.rebirth.search.core.jmx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Interface ManagedAttribute.
 *
 * @author l.xue.nong
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface ManagedAttribute {

	/**
	 * Description.
	 *
	 * @return the string
	 */
	String description() default "";

	/**
	 * Writable.
	 *
	 * @return true, if successful
	 */
	boolean writable() default false;
}
