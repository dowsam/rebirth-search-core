/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AbstractRiverComponent.java 2012-3-29 15:01:03 l.xue.nong$$
 */


package cn.com.rebirth.search.core.river;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class AbstractRiverComponent.
 *
 * @author l.xue.nong
 */
public class AbstractRiverComponent implements RiverComponent {

	
	/** The logger. */
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	
	/** The river name. */
	protected final RiverName riverName;

	
	/** The settings. */
	protected final RiverSettings settings;

	
	/**
	 * Instantiates a new abstract river component.
	 *
	 * @param riverName the river name
	 * @param settings the settings
	 */
	protected AbstractRiverComponent(RiverName riverName, RiverSettings settings) {
		this.riverName = riverName;
		this.settings = settings;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.river.RiverComponent#riverName()
	 */
	@Override
	public RiverName riverName() {
		return riverName;
	}

	
	/**
	 * Node name.
	 *
	 * @return the string
	 */
	public String nodeName() {
		return settings.globalSettings().get("name", "");
	}
}