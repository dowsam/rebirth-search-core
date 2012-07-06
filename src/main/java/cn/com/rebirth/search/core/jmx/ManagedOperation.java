/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ManagedOperation.java 2012-3-29 15:01:41 l.xue.nong$$
 */


package cn.com.rebirth.search.core.jmx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * The Interface ManagedOperation.
 *
 * @author l.xue.nong
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ManagedOperation {
    
    /**
     * Description.
     *
     * @return the string
     */
    String description() default "";
}
