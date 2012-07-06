/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexSettings.java 2012-3-29 15:01:40 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.settings;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import cn.com.rebirth.search.commons.inject.BindingAnnotation;



/**
 * The Interface IndexSettings.
 *
 * @author l.xue.nong
 */
@BindingAnnotation
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
@Documented
public @interface IndexSettings {
}
