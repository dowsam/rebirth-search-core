/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DiscoveryNodeService.java 2012-3-29 15:02:31 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.node;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;

import com.google.common.collect.Maps;


/**
 * The Class DiscoveryNodeService.
 *
 * @author l.xue.nong
 */
public class DiscoveryNodeService extends AbstractComponent {

	/** The custom attributes providers. */
	private final List<CustomAttributesProvider> customAttributesProviders = new CopyOnWriteArrayList<CustomAttributesProvider>();

	/**
	 * Instantiates a new discovery node service.
	 *
	 * @param settings the settings
	 */
	@Inject
	public DiscoveryNodeService(Settings settings) {
		super(settings);
	}

	/**
	 * Adds the custom attribute provider.
	 *
	 * @param customAttributesProvider the custom attributes provider
	 * @return the discovery node service
	 */
	public DiscoveryNodeService addCustomAttributeProvider(CustomAttributesProvider customAttributesProvider) {
		customAttributesProviders.add(customAttributesProvider);
		return this;
	}

	/**
	 * Builds the attributes.
	 *
	 * @return the map
	 */
	public Map<String, String> buildAttributes() {
		Map<String, String> attributes = Maps.newHashMap(settings.getByPrefix("node.").getAsMap());
		attributes.remove("name"); 
		if (attributes.containsKey("client")) {
			if (attributes.get("client").equals("false")) {
				attributes.remove("client"); 
			} else {
				
				attributes.put("data", "false");
			}
		}
		if (attributes.containsKey("data")) {
			if (attributes.get("data").equals("true")) {
				attributes.remove("data");
			}
		}

		for (CustomAttributesProvider provider : customAttributesProviders) {
			try {
				Map<String, String> customAttributes = provider.buildAttributes();
				if (customAttributes != null) {
					for (Map.Entry<String, String> entry : customAttributes.entrySet()) {
						if (!attributes.containsKey(entry.getKey())) {
							attributes.put(entry.getKey(), entry.getValue());
						}
					}
				}
			} catch (Exception e) {
				logger.warn("failed to build custom attributes from provider [{}]", e, provider);
			}
		}

		return attributes;
	}

	/**
	 * The Interface CustomAttributesProvider.
	 *
	 * @author l.xue.nong
	 */
	public static interface CustomAttributesProvider {

		/**
		 * Builds the attributes.
		 *
		 * @return the map
		 */
		Map<String, String> buildAttributes();
	}
}
