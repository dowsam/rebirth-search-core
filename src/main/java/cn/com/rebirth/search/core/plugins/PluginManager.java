/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PluginManager.java 2012-7-6 14:28:53 l.xue.nong$$
 */

package cn.com.rebirth.search.core.plugins;

import java.io.File;
import java.io.IOException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.com.rebirth.search.commons.io.FileSystemUtils;
import cn.com.rebirth.search.core.env.Environment;

/**
 * The Class PluginManager.
 *
 * @author l.xue.nong
 */
public class PluginManager {

	/** The environment. */
	private final Environment environment;

	/** The url. */
	private String url;

	/**
	 * Instantiates a new plugin manager.
	 *
	 * @param environment the environment
	 * @param url the url
	 */
	public PluginManager(Environment environment, String url) {
		this.environment = environment;
		this.url = url;

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };

		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Removes the plugin.
	 *
	 * @param name the name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void removePlugin(String name) throws IOException {
		File pluginToDelete = new File(environment.pluginsFile(), name);
		if (pluginToDelete.exists()) {
			FileSystemUtils.deleteRecursively(pluginToDelete, true);
		}
		pluginToDelete = new File(environment.pluginsFile(), name + ".zip");
		if (pluginToDelete.exists()) {
			pluginToDelete.delete();
		}
		File binLocation = new File(new File(environment.homeFile(), "bin"), name);
		if (binLocation.exists()) {
			FileSystemUtils.deleteRecursively(binLocation);
		}
	}

}
