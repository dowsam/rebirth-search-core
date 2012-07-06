/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core Environment.java 2012-7-6 14:28:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.env;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.io.Streams;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.core.cluster.ClusterName;

/**
 * The Class Environment.
 *
 * @author l.xue.nong
 */
public class Environment {

	/** The settings. */
	private final Settings settings;

	/** The home file. */
	private final File homeFile;

	/** The work file. */
	private final File workFile;

	/** The work with cluster file. */
	private final File workWithClusterFile;

	/** The data files. */
	private final File[] dataFiles;

	/** The data with cluster files. */
	private final File[] dataWithClusterFiles;

	/** The config file. */
	private final File configFile;

	/** The plugins file. */
	private final File pluginsFile;

	/** The logs file. */
	private final File logsFile;

	/**
	 * Instantiates a new environment.
	 */
	public Environment() {
		this(ImmutableSettings.Builder.EMPTY_SETTINGS);
	}

	/**
	 * Instantiates a new environment.
	 *
	 * @param settings the settings
	 */
	public Environment(Settings settings) {
		this.settings = settings;
		if (settings.get("path.home") != null) {
			homeFile = new File(Strings.cleanPath(settings.get("path.home")));
		} else {
			homeFile = new File(System.getProperty("user.dir"));
		}

		if (settings.get("path.conf") != null) {
			configFile = new File(Strings.cleanPath(settings.get("path.conf")));
		} else {
			configFile = new File(homeFile, "config");
		}

		if (settings.get("path.plugins") != null) {
			pluginsFile = new File(Strings.cleanPath(settings.get("path.plugins")));
		} else {
			pluginsFile = new File(homeFile, "plugins");
		}

		if (settings.get("path.work") != null) {
			workFile = new File(Strings.cleanPath(settings.get("path.work")));
		} else {
			workFile = new File(homeFile, "work");
		}
		workWithClusterFile = new File(workFile, ClusterName.clusterNameFromSettings(settings).value());

		String[] dataPaths = settings.getAsArray("path.data");
		if (dataPaths.length > 0) {
			dataFiles = new File[dataPaths.length];
			dataWithClusterFiles = new File[dataPaths.length];
			for (int i = 0; i < dataPaths.length; i++) {
				dataFiles[i] = new File(dataPaths[i]);
				dataWithClusterFiles[i] = new File(dataFiles[i], ClusterName.clusterNameFromSettings(settings).value());
			}
		} else {
			dataFiles = new File[] { new File(homeFile, "data") };
			dataWithClusterFiles = new File[] { new File(new File(homeFile, "data"), ClusterName
					.clusterNameFromSettings(settings).value()) };
		}

		if (settings.get("path.logs") != null) {
			logsFile = new File(Strings.cleanPath(settings.get("path.logs")));
		} else {
			logsFile = new File(homeFile, "logs");
		}
	}

	/**
	 * Settings.
	 *
	 * @return the settings
	 */
	public Settings settings() {
		return this.settings;
	}

	/**
	 * Home file.
	 *
	 * @return the file
	 */
	public File homeFile() {
		return homeFile;
	}

	/**
	 * Work file.
	 *
	 * @return the file
	 */
	public File workFile() {
		return workFile;
	}

	/**
	 * Work with cluster file.
	 *
	 * @return the file
	 */
	public File workWithClusterFile() {
		return workWithClusterFile;
	}

	/**
	 * Data files.
	 *
	 * @return the file[]
	 */
	public File[] dataFiles() {
		return dataFiles;
	}

	/**
	 * Data with cluster files.
	 *
	 * @return the file[]
	 */
	public File[] dataWithClusterFiles() {
		return dataWithClusterFiles;
	}

	/**
	 * Config file.
	 *
	 * @return the file
	 */
	public File configFile() {
		return configFile;
	}

	/**
	 * Plugins file.
	 *
	 * @return the file
	 */
	public File pluginsFile() {
		return pluginsFile;
	}

	/**
	 * Logs file.
	 *
	 * @return the file
	 */
	public File logsFile() {
		return logsFile;
	}

	/**
	 * Resolve config and load to string.
	 *
	 * @param path the path
	 * @return the string
	 * @throws FailedToResolveConfigException the failed to resolve config exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String resolveConfigAndLoadToString(String path) throws FailedToResolveConfigException, IOException {
		return Streams.copyToString(new InputStreamReader(resolveConfig(path).openStream(), "UTF-8"));
	}

	/**
	 * Resolve config.
	 *
	 * @param path the path
	 * @return the url
	 * @throws FailedToResolveConfigException the failed to resolve config exception
	 */
	public URL resolveConfig(String path) throws FailedToResolveConfigException {
		String origPath = path;

		File f1 = new File(path);
		if (f1.exists()) {
			try {
				return f1.toURI().toURL();
			} catch (MalformedURLException e) {
				throw new FailedToResolveConfigException("Failed to resolve path [" + f1 + "]", e);
			}
		}
		if (path.startsWith("/")) {
			path = path.substring(1);
		}

		File f2 = new File(configFile, path);
		if (f2.exists()) {
			try {
				return f2.toURI().toURL();
			} catch (MalformedURLException e) {
				throw new FailedToResolveConfigException("Failed to resolve path [" + f2 + "]", e);
			}
		}

		URL resource = settings.getClassLoader().getResource(path);
		if (resource != null) {
			return resource;
		}

		if (!path.startsWith("config/")) {
			resource = settings.getClassLoader().getResource("config/" + path);
			if (resource != null) {
				return resource;
			}
		}
		throw new FailedToResolveConfigException("Failed to resolve config path [" + origPath + "], tried file path ["
				+ f1 + "], path file [" + f2 + "], and classpath");
	}
}
