/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core JmxService.java 2012-3-29 15:01:50 l.xue.nong$$
 */
package cn.com.rebirth.search.core.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.network.NetworkService;
import cn.com.rebirth.search.commons.transport.PortsRange;

/**
 * The Class JmxService.
 *
 * @author l.xue.nong
 */
public class JmxService {

	/**
	 * The Class SettingsConstants.
	 *
	 * @author l.xue.nong
	 */
	public static class SettingsConstants {

		/** The Constant EXPORT. */
		public static final String EXPORT = "jmx.export";

		/** The Constant CREATE_CONNECTOR. */
		public static final String CREATE_CONNECTOR = "jmx.create_connector";
	}

	/**
	 * Should export.
	 *
	 * @param settings the settings
	 * @return true, if successful
	 */
	public static boolean shouldExport(Settings settings) {
		return settings.getAsBoolean(SettingsConstants.CREATE_CONNECTOR, false)
				|| settings.getAsBoolean(SettingsConstants.EXPORT, false);
	}

	/** The Constant JMXRMI_URI_PATTERN. */
	public static final String JMXRMI_URI_PATTERN = "service:jmx:rmi:///jndi/rmi://:{jmx.port}/jmxrmi";

	/** The Constant JMXRMI_PUBLISH_URI_PATTERN. */
	public static final String JMXRMI_PUBLISH_URI_PATTERN = "service:jmx:rmi:///jndi/rmi://{jmx.host}:{jmx.port}/jmxrmi";

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/** The settings. */
	private final Settings settings;

	/** The jmx domain. */
	private final String jmxDomain;

	/** The service url. */
	private String serviceUrl;

	/** The publish url. */
	private String publishUrl;

	/** The m bean server. */
	private final MBeanServer mBeanServer;

	/** The connector server. */
	private JMXConnectorServer connectorServer;

	/** The construction m beans. */
	private final CopyOnWriteArrayList<ResourceDMBean> constructionMBeans = new CopyOnWriteArrayList<ResourceDMBean>();

	/** The registered m beans. */
	private final CopyOnWriteArrayList<ResourceDMBean> registeredMBeans = new CopyOnWriteArrayList<ResourceDMBean>();

	/** The started. */
	private volatile boolean started = false;

	/**
	 * Instantiates a new jmx service.
	 *
	 * @param settings the settings
	 */
	public JmxService(final Settings settings) {
		this.settings = settings;
		this.jmxDomain = settings.get("jmx.domain", "cn.com.summall");
		this.mBeanServer = ManagementFactory.getPlatformMBeanServer();
	}

	/**
	 * Service url.
	 *
	 * @return the string
	 */
	public String serviceUrl() {
		return this.serviceUrl;
	}

	/**
	 * Publish url.
	 *
	 * @return the string
	 */
	public String publishUrl() {
		return this.publishUrl;
	}

	/**
	 * Connect and register.
	 *
	 * @param nodeDescription the node description
	 * @param networkService the network service
	 */
	public void connectAndRegister(String nodeDescription, final NetworkService networkService) {
		if (started) {
			return;
		}
		started = true;
		if (settings.getAsBoolean(SettingsConstants.CREATE_CONNECTOR, false)) {

			try {
				if (System.getProperty("sun.rmi.dgc.client.gcInterval") == null)
					System.setProperty("sun.rmi.dgc.client.gcInterval", "36000000");
				if (System.getProperty("sun.rmi.dgc.server.gcInterval") == null)
					System.setProperty("sun.rmi.dgc.server.gcInterval", "36000000");
			} catch (Exception secExc) {
				logger.warn("Failed to set sun.rmi.dgc.xxx system properties", secExc);
			}

			final String port = settings.get("jmx.port", "9400-9500");

			PortsRange portsRange = new PortsRange(port);
			final AtomicReference<Exception> lastException = new AtomicReference<Exception>();
			boolean success = portsRange.iterate(new PortsRange.PortCallback() {
				@Override
				public boolean onPortNumber(int portNumber) {
					try {
						LocateRegistry.createRegistry(portNumber);
						serviceUrl = settings.get("jmx.service_url", JMXRMI_URI_PATTERN).replace("{jmx.port}",
								Integer.toString(portNumber));

						JMXServiceURL url = new JMXServiceURL(serviceUrl);

						connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, settings.getAsMap(),
								mBeanServer);
						connectorServer.start();

						String publishHost = networkService.resolvePublishHostAddress(settings.get("jmx.publish_host"))
								.getHostAddress();
						publishUrl = settings.get("jmx.publish_url", JMXRMI_PUBLISH_URI_PATTERN)
								.replace("{jmx.port}", Integer.toString(portNumber)).replace("{jmx.host}", publishHost);
					} catch (Exception e) {
						lastException.set(e);
						return false;
					}
					return true;
				}
			});
			if (!success) {
				throw new JmxConnectorCreationException("Failed to bind to [" + port + "]", lastException.get());
			}
			logger.info("bound_address {{}}, publish_address {{}}", serviceUrl, publishUrl);
		}

		for (ResourceDMBean resource : constructionMBeans) {
			register(resource);
		}
	}

	/**
	 * Register m bean.
	 *
	 * @param instance the instance
	 */
	public void registerMBean(Object instance) {
		ResourceDMBean resourceDMBean = new ResourceDMBean(instance);
		if (!resourceDMBean.isManagedResource()) {
			return;
		}
		if (!started) {
			constructionMBeans.add(resourceDMBean);
			return;
		}
		register(resourceDMBean);
	}

	/**
	 * Unregister group.
	 *
	 * @param groupName the group name
	 */
	public void unregisterGroup(String groupName) {
		for (ResourceDMBean resource : registeredMBeans) {
			if (!groupName.equals(resource.getGroupName())) {
				continue;
			}

			registeredMBeans.remove(resource);

			String resourceName = resource.getFullObjectName();
			try {
				ObjectName objectName = new ObjectName(getObjectName(resourceName));
				if (mBeanServer.isRegistered(objectName)) {
					mBeanServer.unregisterMBean(objectName);
					if (logger.isTraceEnabled()) {
						logger.trace("Unregistered " + objectName);
					}
				}
			} catch (Exception e) {
				logger.warn("Failed to unregister " + resource.getFullObjectName());
			}
		}
	}

	/**
	 * Close.
	 */
	public void close() {
		if (!started) {
			return;
		}
		started = false;

		for (ResourceDMBean resource : registeredMBeans) {
			String resourceName = resource.getFullObjectName();
			try {
				ObjectName objectName = new ObjectName(getObjectName(resourceName));
				if (mBeanServer.isRegistered(objectName)) {
					mBeanServer.unregisterMBean(objectName);
					if (logger.isTraceEnabled()) {
						logger.trace("Unregistered " + objectName);
					}
				}
			} catch (Exception e) {
				logger.warn("Failed to unregister " + resource.getFullObjectName());
			}
		}
		if (connectorServer != null) {
			try {
				connectorServer.stop();
			} catch (IOException e) {
				logger.debug("Failed to close connector", e);
			}
		}
	}

	/**
	 * Register.
	 *
	 * @param resourceDMBean the resource dm bean
	 * @throws JmxRegistrationException the jmx registration exception
	 */
	private void register(ResourceDMBean resourceDMBean) throws JmxRegistrationException {
		try {
			String resourceName = resourceDMBean.getFullObjectName();
			ObjectName objectName = new ObjectName(getObjectName(resourceName));
			if (!mBeanServer.isRegistered(objectName)) {
				try {
					mBeanServer.registerMBean(resourceDMBean, objectName);
					registeredMBeans.add(resourceDMBean);
					if (logger.isTraceEnabled()) {
						logger.trace("Registered " + resourceDMBean + " under " + objectName);
					}
				} catch (InstanceAlreadyExistsException e) {

					logger.debug("Could not register object with name:" + objectName + "(" + e.getMessage() + ")");
				}
			} else {
				logger.debug("Could not register object with name: " + objectName + ", already registered");
			}
		} catch (Exception e) {
			logger.warn("Could not register object with name: " + resourceDMBean.getFullObjectName() + "("
					+ e.getMessage() + ")");
		}
	}

	/**
	 * Gets the object name.
	 *
	 * @param resourceName the resource name
	 * @return the object name
	 */
	private String getObjectName(String resourceName) {
		return getObjectName(jmxDomain, resourceName);
	}

	/**
	 * Gets the object name.
	 *
	 * @param jmxDomain the jmx domain
	 * @param resourceName the resource name
	 * @return the object name
	 */
	private String getObjectName(String jmxDomain, String resourceName) {
		return jmxDomain + ":" + resourceName;
	}
}
