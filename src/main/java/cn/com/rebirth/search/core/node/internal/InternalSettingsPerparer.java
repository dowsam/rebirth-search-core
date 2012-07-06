/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core InternalSettingsPerparer.java 2012-3-29 15:21:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.node.internal;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.collect.Tuple;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.Names;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.core.cluster.ClusterName;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.env.FailedToResolveConfigException;

/**
 * The Class InternalSettingsPerparer.
 *
 * @author l.xue.nong
 */
public class InternalSettingsPerparer {

	/**
	 * Prepare settings.
	 *
	 * @param pSettings the settings
	 * @param loadConfigSettings the load config settings
	 * @return the tuple
	 */
	public static Tuple<Settings, Environment> prepareSettings(Settings pSettings, boolean loadConfigSettings) {

		ImmutableSettings.Builder settingsBuilder = ImmutableSettings.settingsBuilder().put(pSettings)
				.putProperties("summallsearch.", System.getProperties()).putProperties("es.", System.getProperties())
				.replacePropertyPlaceholders();

		Environment environment = new Environment(settingsBuilder.build());

		if (loadConfigSettings) {
			boolean explicitSettingsProvided = false;
			if (System.getProperty("es.config") != null) {
				explicitSettingsProvided = true;
				settingsBuilder.loadFromUrl(environment.resolveConfig(System.getProperty("es.config")));
			}
			if (System.getProperty("summallsearch.config") != null) {
				explicitSettingsProvided = true;
				settingsBuilder.loadFromUrl(environment.resolveConfig(System.getProperty("summallsearch.config")));
			}
			if (!explicitSettingsProvided) {
				try {
					settingsBuilder.loadFromUrl(environment.resolveConfig("summallsearch.yml"));
				} catch (FailedToResolveConfigException e) {

				} catch (NoClassDefFoundError e) {

				}
				try {
					settingsBuilder.loadFromUrl(environment.resolveConfig("summallsearch.json"));
				} catch (FailedToResolveConfigException e) {

				}
				try {
					settingsBuilder.loadFromUrl(environment.resolveConfig("summallsearch.properties"));
				} catch (FailedToResolveConfigException e) {

				}
			}
		}

		settingsBuilder.put(pSettings).putProperties("summallsearch.", System.getProperties())
				.putProperties("es.", System.getProperties()).replacePropertyPlaceholders();

		if (settingsBuilder.get("name") == null) {
			String name = System.getProperty("name");
			if (name == null || name.isEmpty()) {
				name = settingsBuilder.get("node.name");
				if (name == null || name.isEmpty()) {
					name = Names.randomNodeName(environment.resolveConfig("names.txt"));
				}
			}

			if (name != null) {
				settingsBuilder.put("name", name);
			}
		}

		if (settingsBuilder.get(ClusterName.SETTING) == null) {
			settingsBuilder.put(ClusterName.SETTING, ClusterName.DEFAULT.value());
		}

		Settings v1 = settingsBuilder.build();
		environment = new Environment(v1);

		settingsBuilder = ImmutableSettings.settingsBuilder().put(v1);

		settingsBuilder.put("path.logs", Strings.cleanPath(environment.logsFile().getAbsolutePath()));

		v1 = settingsBuilder.build();

		return new Tuple<Settings, Environment>(v1, environment);
	}
}
