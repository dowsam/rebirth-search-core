/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DiscoveryNodeFilters.java 2012-7-6 14:29:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.node;

import java.util.HashMap;
import java.util.Map;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.regex.Regex;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.transport.InetSocketTransportAddress;

import com.google.common.collect.ImmutableMap;

/**
 * The Class DiscoveryNodeFilters.
 *
 * @author l.xue.nong
 */
public class DiscoveryNodeFilters {

	/** The Constant NO_FILTERS. */
	public static final DiscoveryNodeFilters NO_FILTERS = new DiscoveryNodeFilters(ImmutableMap.<String, String[]> of());

	/**
	 * Builds the from settings.
	 *
	 * @param prefix the prefix
	 * @param settings the settings
	 * @return the discovery node filters
	 */
	public static DiscoveryNodeFilters buildFromSettings(String prefix, Settings settings) {
		return buildFromKeyValue(settings.getByPrefix(prefix).getAsMap());
	}

	/**
	 * Builds the from key value.
	 *
	 * @param filters the filters
	 * @return the discovery node filters
	 */
	public static DiscoveryNodeFilters buildFromKeyValue(Map<String, String> filters) {
		Map<String, String[]> bFilters = new HashMap<String, String[]>();
		for (Map.Entry<String, String> entry : filters.entrySet()) {
			bFilters.put(entry.getKey(), Strings.splitStringByCommaToArray(entry.getValue()));
		}
		if (bFilters.isEmpty()) {
			return NO_FILTERS;
		}
		return new DiscoveryNodeFilters(bFilters);
	}

	/** The filters. */
	private final Map<String, String[]> filters;

	/**
	 * Instantiates a new discovery node filters.
	 *
	 * @param filters the filters
	 */
	DiscoveryNodeFilters(Map<String, String[]> filters) {
		this.filters = filters;
	}

	/**
	 * Match.
	 *
	 * @param node the node
	 * @return true, if successful
	 */
	public boolean match(DiscoveryNode node) {
		if (filters.isEmpty()) {
			return true;
		}
		for (Map.Entry<String, String[]> entry : filters.entrySet()) {
			String attr = entry.getKey();
			String[] values = entry.getValue();
			if ("_ip".equals(attr)) {
				if (!(node.address() instanceof InetSocketTransportAddress)) {
					return false;
				}
				InetSocketTransportAddress inetAddress = (InetSocketTransportAddress) node.address();
				for (String value : values) {
					if (Regex.simpleMatch(value, inetAddress.address().getAddress().getHostAddress())) {
						return true;
					}
				}
				return false;
			} else if ("_id".equals(attr)) {
				for (String value : values) {
					if (node.id().equals(value)) {
						return true;
					}
				}
				return false;
			} else if ("_name".equals(attr) || "name".equals(attr)) {
				for (String value : values) {
					if (Regex.simpleMatch(value, node.name())) {
						return true;
					}
				}
				return false;
			} else {
				String nodeAttributeValue = node.attributes().get(attr);
				if (nodeAttributeValue == null) {
					return false;
				}
				for (String value : values) {
					if (Regex.simpleMatch(value, nodeAttributeValue)) {
						return true;
					}
				}
				return false;
			}
		}
		return true;
	}
}
