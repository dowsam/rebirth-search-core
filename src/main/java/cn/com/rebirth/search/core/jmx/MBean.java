/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MBean.java 2012-3-29 15:02:01 l.xue.nong$$
 */


package cn.com.rebirth.search.core.jmx;

import java.lang.annotation.*;


/**
 * The Interface MBean.
 *
 * @author l.xue.nong
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface MBean {
    
    /**
     * Description.
     *
     * @return the string
     */
    String description() default "";

    /**
     * Object name.
     *
     * @return the string
     */
    String objectName() default "";
}
