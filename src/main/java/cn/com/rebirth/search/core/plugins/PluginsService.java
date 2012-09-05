/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PluginsService.java 2012-7-6 14:29:49 l.xue.nong$$
 */

package cn.com.rebirth.search.core.plugins;

import static com.google.common.collect.Maps.newHashMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.component.AbstractComponent;
import cn.com.rebirth.commons.component.LifecycleComponent;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.ImmutableSettings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.inject.Module;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.CloseableIndexComponent;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * The Class PluginsService.
 *
 * @author l.xue.nong
 */
public class PluginsService extends AbstractComponent {

	/** The environment. */
	private final Environment environment;

	/** The plugins. */
	private final ImmutableMap<String, Plugin> plugins;

	/** The on module references. */
	private final ImmutableMap<Plugin, List<OnModuleReference>> onModuleReferences;

	/**
	 * The Class OnModuleReference.
	 *
	 * @author l.xue.nong
	 */
	static class OnModuleReference {

		/** The module class. */
		public final Class<? extends Module> moduleClass;

		/** The on module method. */
		public final Method onModuleMethod;

		/**
		 * Instantiates a new on module reference.
		 *
		 * @param moduleClass the module class
		 * @param onModuleMethod the on module method
		 */
		OnModuleReference(Class<? extends Module> moduleClass, Method onModuleMethod) {
			this.moduleClass = moduleClass;
			this.onModuleMethod = onModuleMethod;
		}
	}

	/**
	 * Instantiates a new plugins service.
	 *
	 * @param settings the settings
	 * @param environment the environment
	 */
	@Inject
	public PluginsService(Settings settings, Environment environment) {
		super(settings);
		this.environment = environment;

		loadPluginsIntoClassLoader();

		Map<String, Plugin> plugins = Maps.newHashMap();
		plugins.putAll(loadPluginsFromClasspath(settings));
		Set<String> sitePlugins = sitePlugins();

		String[] mandatoryPlugins = settings.getAsArray("plugin.mandatory", null);
		if (mandatoryPlugins != null) {
			Set<String> missingPlugins = Sets.newHashSet();
			for (String mandatoryPlugin : mandatoryPlugins) {
				if (!plugins.containsKey(mandatoryPlugin) && !sitePlugins.contains(mandatoryPlugin)
						&& !missingPlugins.contains(mandatoryPlugin)) {
					missingPlugins.add(mandatoryPlugin);
				}
			}
			if (!missingPlugins.isEmpty()) {
				throw new RebirthException("Missing mandatory plugins " + missingPlugins);
			}
		}

		logger.info("loaded {}, sites {}", plugins.keySet(), sitePlugins);

		this.plugins = ImmutableMap.copyOf(plugins);

		MapBuilder<Plugin, List<OnModuleReference>> onModuleReferences = MapBuilder.newMapBuilder();
		for (Plugin plugin : plugins.values()) {
			List<OnModuleReference> list = Lists.newArrayList();
			for (Method method : plugin.getClass().getDeclaredMethods()) {
				if (!method.getName().equals("onModule")) {
					continue;
				}
				if (method.getParameterTypes().length == 0 || method.getParameterTypes().length > 1) {
					logger.warn("Plugin: {} implementing onModule with no parameters or more than one parameter",
							plugin.name());
					continue;
				}
				Class moduleClass = method.getParameterTypes()[0];
				if (!Module.class.isAssignableFrom(moduleClass)) {
					logger.warn("Plugin: {} implementing onModule by the type is not of Module type {}", plugin.name(),
							moduleClass);
					continue;
				}
				method.setAccessible(true);
				list.add(new OnModuleReference(moduleClass, method));
			}
			if (!list.isEmpty()) {
				onModuleReferences.put(plugin, list);
			}
		}
		this.onModuleReferences = onModuleReferences.immutableMap();
	}

	/**
	 * Plugins.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, Plugin> plugins() {
		return plugins;
	}

	/**
	 * Process modules.
	 *
	 * @param modules the modules
	 */
	public void processModules(Iterable<Module> modules) {
		for (Module module : modules) {
			processModule(module);
		}
	}

	/**
	 * Process module.
	 *
	 * @param module the module
	 */
	public void processModule(Module module) {
		for (Plugin plugin : plugins().values()) {
			plugin.processModule(module);

			List<OnModuleReference> references = onModuleReferences.get(plugin);
			if (references != null) {
				for (OnModuleReference reference : references) {
					if (reference.moduleClass.isAssignableFrom(module.getClass())) {
						try {
							reference.onModuleMethod.invoke(plugin, module);
						} catch (Exception e) {
							logger.warn("plugin {}, failed to invoke custom onModule method", e, plugin.name());
						}
					}
				}
			}
		}
	}

	/**
	 * Updated settings.
	 *
	 * @return the settings
	 */
	public Settings updatedSettings() {
		ImmutableSettings.Builder builder = ImmutableSettings.settingsBuilder().put(this.settings);
		for (Plugin plugin : plugins.values()) {
			builder.put(plugin.additionalSettings());
		}
		return builder.build();
	}

	/**
	 * Modules.
	 *
	 * @return the collection< class<? extends module>>
	 */
	public Collection<Class<? extends Module>> modules() {
		List<Class<? extends Module>> modules = Lists.newArrayList();
		for (Plugin plugin : plugins.values()) {
			modules.addAll(plugin.modules());
		}
		return modules;
	}

	/**
	 * Modules.
	 *
	 * @param settings the settings
	 * @return the collection
	 */
	public Collection<Module> modules(Settings settings) {
		List<Module> modules = Lists.newArrayList();
		for (Plugin plugin : plugins.values()) {
			modules.addAll(plugin.modules(settings));
		}
		return modules;
	}

	/**
	 * Services.
	 *
	 * @return the collection< class<? extends lifecycle component>>
	 */
	public Collection<Class<? extends LifecycleComponent>> services() {
		List<Class<? extends LifecycleComponent>> services = Lists.newArrayList();
		for (Plugin plugin : plugins.values()) {
			services.addAll(plugin.services());
		}
		return services;
	}

	/**
	 * Index modules.
	 *
	 * @return the collection< class<? extends module>>
	 */
	public Collection<Class<? extends Module>> indexModules() {
		List<Class<? extends Module>> modules = Lists.newArrayList();
		for (Plugin plugin : plugins.values()) {
			modules.addAll(plugin.indexModules());
		}
		return modules;
	}

	/**
	 * Index modules.
	 *
	 * @param settings the settings
	 * @return the collection
	 */
	public Collection<Module> indexModules(Settings settings) {
		List<Module> modules = Lists.newArrayList();
		for (Plugin plugin : plugins.values()) {
			modules.addAll(plugin.indexModules(settings));
		}
		return modules;
	}

	/**
	 * Index services.
	 *
	 * @return the collection< class<? extends closeable index component>>
	 */
	public Collection<Class<? extends CloseableIndexComponent>> indexServices() {
		List<Class<? extends CloseableIndexComponent>> services = Lists.newArrayList();
		for (Plugin plugin : plugins.values()) {
			services.addAll(plugin.indexServices());
		}
		return services;
	}

	/**
	 * Shard modules.
	 *
	 * @return the collection< class<? extends module>>
	 */
	public Collection<Class<? extends Module>> shardModules() {
		List<Class<? extends Module>> modules = Lists.newArrayList();
		for (Plugin plugin : plugins.values()) {
			modules.addAll(plugin.shardModules());
		}
		return modules;
	}

	/**
	 * Shard modules.
	 *
	 * @param settings the settings
	 * @return the collection
	 */
	public Collection<Module> shardModules(Settings settings) {
		List<Module> modules = Lists.newArrayList();
		for (Plugin plugin : plugins.values()) {
			modules.addAll(plugin.shardModules(settings));
		}
		return modules;
	}

	/**
	 * Shard services.
	 *
	 * @return the collection< class<? extends closeable index component>>
	 */
	public Collection<Class<? extends CloseableIndexComponent>> shardServices() {
		List<Class<? extends CloseableIndexComponent>> services = Lists.newArrayList();
		for (Plugin plugin : plugins.values()) {
			services.addAll(plugin.shardServices());
		}
		return services;
	}

	/**
	 * Site plugins.
	 *
	 * @return the sets the
	 */
	private Set<String> sitePlugins() {
		File pluginsFile = environment.pluginsFile();
		Set<String> sitePlugins = Sets.newHashSet();
		if (!pluginsFile.exists()) {
			return sitePlugins;
		}
		if (!pluginsFile.isDirectory()) {
			return sitePlugins;
		}
		File[] pluginsFiles = pluginsFile.listFiles();
		for (File pluginFile : pluginsFiles) {
			if (new File(pluginFile, "_site").exists()) {
				sitePlugins.add(pluginFile.getName());
			}
		}
		return sitePlugins;
	}

	/**
	 * Load plugins into class loader.
	 */
	private void loadPluginsIntoClassLoader() {
		File pluginsFile = environment.pluginsFile();
		if (!pluginsFile.exists()) {
			return;
		}
		if (!pluginsFile.isDirectory()) {
			return;
		}

		ClassLoader classLoader = settings.getClassLoader();
		Class classLoaderClass = classLoader.getClass();
		Method addURL = null;
		while (!classLoaderClass.equals(Object.class)) {
			try {
				addURL = classLoaderClass.getDeclaredMethod("addURL", URL.class);
				addURL.setAccessible(true);
				break;
			} catch (NoSuchMethodException e) {

				classLoaderClass = classLoaderClass.getSuperclass();
			}
		}
		if (addURL == null) {
			logger.debug("failed to find addURL method on classLoader [" + classLoader + "] to add methods");
			return;
		}

		File[] pluginsFiles = pluginsFile.listFiles();
		for (File pluginFile : pluginsFiles) {
			if (pluginFile.isDirectory()) {
				logger.trace("--- adding plugin [" + pluginFile.getAbsolutePath() + "]");
				try {

					addURL.invoke(classLoader, pluginFile.toURI().toURL());

					List<File> libFiles = Lists.newArrayList();
					if (pluginFile.listFiles() != null) {
						libFiles.addAll(Arrays.asList(pluginFile.listFiles()));
					}
					File libLocation = new File(pluginFile, "lib");
					if (libLocation.exists() && libLocation.isDirectory() && libLocation.listFiles() != null) {
						libFiles.addAll(Arrays.asList(libLocation.listFiles()));
					}

					for (File libFile : libFiles) {
						if (!(libFile.getName().endsWith(".jar") || libFile.getName().endsWith(".zip"))) {
							continue;
						}
						addURL.invoke(classLoader, libFile.toURI().toURL());
					}
				} catch (Exception e) {
					logger.warn("failed to add plugin [" + pluginFile + "]", e);
				}
			}
		}
	}

	/**
	 * Load plugins from classpath.
	 *
	 * @param settings the settings
	 * @return the map
	 */
	private Map<String, Plugin> loadPluginsFromClasspath(Settings settings) {
		Map<String, Plugin> plugins = newHashMap();
		Enumeration<URL> pluginUrls = null;
		try {
			pluginUrls = settings.getClassLoader().getResources("rebirth-search-plugin.properties");
		} catch (IOException e) {
			logger.warn("failed to find plugins from classpath", e);
			return ImmutableMap.of();
		}
		while (pluginUrls.hasMoreElements()) {
			URL pluginUrl = pluginUrls.nextElement();
			Properties pluginProps = new Properties();
			InputStream is = null;
			try {
				is = pluginUrl.openStream();
				pluginProps.load(is);
				String sPluginClass = pluginProps.getProperty("plugin");
				Class<? extends Plugin> pluginClass = (Class<? extends Plugin>) settings.getClassLoader().loadClass(
						sPluginClass);
				Plugin plugin;
				try {
					plugin = pluginClass.getConstructor(Settings.class).newInstance(settings);
				} catch (NoSuchMethodException e) {
					try {
						plugin = pluginClass.getConstructor().newInstance();
					} catch (NoSuchMethodException e1) {
						throw new RebirthException("No constructor for [" + pluginClass + "]");
					}
				}
				plugins.put(plugin.name(), plugin);
			} catch (Exception e) {
				logger.warn("failed to load plugin from [" + pluginUrl + "]", e);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {

					}
				}
			}
		}
		return plugins;
	}
}
