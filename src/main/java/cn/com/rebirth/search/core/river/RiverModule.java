/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RiverModule.java 2012-7-6 14:29:31 l.xue.nong$$
 */

package cn.com.rebirth.search.core.river;

import java.util.Map;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.settings.NoClassSettingsException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.AbstractModule;
import cn.com.rebirth.core.inject.Module;
import cn.com.rebirth.core.inject.Modules;
import cn.com.rebirth.core.inject.SpawnModules;

import com.google.common.collect.ImmutableList;

/**
 * The Class RiverModule.
 *
 * @author l.xue.nong
 */
public class RiverModule extends AbstractModule implements SpawnModules {

	/** The river name. */
	private RiverName riverName;

	/** The global settings. */
	private final Settings globalSettings;

	/** The settings. */
	private final Map<String, Object> settings;

	/** The types registry. */
	private final RiversTypesRegistry typesRegistry;

	/**
	 * Instantiates a new river module.
	 *
	 * @param riverName the river name
	 * @param settings the settings
	 * @param globalSettings the global settings
	 * @param typesRegistry the types registry
	 */
	public RiverModule(RiverName riverName, Map<String, Object> settings, Settings globalSettings,
			RiversTypesRegistry typesRegistry) {
		this.riverName = riverName;
		this.globalSettings = globalSettings;
		this.settings = settings;
		this.typesRegistry = typesRegistry;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.SpawnModules#spawnModules()
	 */
	@Override
	public Iterable<? extends Module> spawnModules() {
		return ImmutableList.of(Modules.createModule(
				loadTypeModule(riverName.type(), "cn.com.rebirth.search.core.river.", "RiverModule"), globalSettings));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(RiverSettings.class).toInstance(new RiverSettings(globalSettings, settings));
	}

	/**
	 * Load type module.
	 *
	 * @param type the type
	 * @param prefixPackage the prefix package
	 * @param suffixClassName the suffix class name
	 * @return the class<? extends module>
	 */
	private Class<? extends Module> loadTypeModule(String type, String prefixPackage, String suffixClassName) {
		Class<? extends Module> registered = typesRegistry.type(type);
		if (registered != null) {
			return registered;
		}
		String fullClassName = type;
		try {
			return (Class<? extends Module>) globalSettings.getClassLoader().loadClass(fullClassName);
		} catch (ClassNotFoundException e) {
			fullClassName = prefixPackage + Strings.capitalize(Strings.toCamelCase(type)) + suffixClassName;
			try {
				return (Class<? extends Module>) globalSettings.getClassLoader().loadClass(fullClassName);
			} catch (ClassNotFoundException e1) {
				fullClassName = prefixPackage + Strings.toCamelCase(type) + "."
						+ Strings.capitalize(Strings.toCamelCase(type)) + suffixClassName;
				try {
					return (Class<? extends Module>) globalSettings.getClassLoader().loadClass(fullClassName);
				} catch (ClassNotFoundException e2) {
					fullClassName = prefixPackage + Strings.toCamelCase(type).toLowerCase() + "."
							+ Strings.capitalize(Strings.toCamelCase(type)) + suffixClassName;
					try {
						return (Class<? extends Module>) globalSettings.getClassLoader().loadClass(fullClassName);
					} catch (ClassNotFoundException e3) {
						throw new NoClassSettingsException("Failed to load class with value [" + type + "]", e);
					}
				}
			}
		}
	}
}
