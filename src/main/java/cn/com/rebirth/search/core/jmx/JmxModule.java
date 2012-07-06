/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core JmxModule.java 2012-3-29 15:01:22 l.xue.nong$$
 */


package cn.com.rebirth.search.core.jmx;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.TypeLiteral;
import cn.com.rebirth.search.commons.inject.matcher.Matchers;
import cn.com.rebirth.search.commons.inject.spi.InjectionListener;
import cn.com.rebirth.search.commons.inject.spi.TypeEncounter;
import cn.com.rebirth.search.commons.inject.spi.TypeListener;
import cn.com.rebirth.search.core.jmx.action.GetJmxServiceUrlAction;


/**
 * The Class JmxModule.
 *
 * @author l.xue.nong
 */
public class JmxModule extends AbstractModule {

	
	/** The settings. */
	private final Settings settings;

	
	/**
	 * Instantiates a new jmx module.
	 *
	 * @param settings the settings
	 */
	public JmxModule(Settings settings) {
		this.settings = settings;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		JmxService jmxService = new JmxService(settings);
		bind(JmxService.class).toInstance(jmxService);
		bind(GetJmxServiceUrlAction.class).asEagerSingleton();
		if (JmxService.shouldExport(settings)) {
			bindListener(Matchers.any(), new JmxExporterTypeListener(jmxService));
		}
	}

	
	/**
	 * The listener interface for receiving jmxExporterType events.
	 * The class that is interested in processing a jmxExporterType
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addJmxExporterTypeListener<code> method. When
	 * the jmxExporterType event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see JmxExporterTypeEvent
	 */
	private static class JmxExporterTypeListener implements TypeListener {

		
		/** The jmx service. */
		private final JmxService jmxService;

		
		/**
		 * Instantiates a new jmx exporter type listener.
		 *
		 * @param jmxService the jmx service
		 */
		private JmxExporterTypeListener(JmxService jmxService) {
			this.jmxService = jmxService;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.inject.spi.TypeListener#hear(cn.com.summall.search.commons.inject.TypeLiteral, cn.com.summall.search.commons.inject.spi.TypeEncounter)
		 */
		@Override
		public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
			Class<? super I> type = typeLiteral.getRawType();
			if (type.isAnnotationPresent(MBean.class)) {
				typeEncounter.register(new JmxExporterInjectionListener<I>(jmxService));
			}
		}
	}

	
	/**
	 * The listener interface for receiving jmxExporterInjection events.
	 * The class that is interested in processing a jmxExporterInjection
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addJmxExporterInjectionListener<code> method. When
	 * the jmxExporterInjection event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @param <I> the generic type
	 * @see JmxExporterInjectionEvent
	 */
	private static class JmxExporterInjectionListener<I> implements InjectionListener<I> {

		
		/** The jmx service. */
		private final JmxService jmxService;

		
		/**
		 * Instantiates a new jmx exporter injection listener.
		 *
		 * @param jmxService the jmx service
		 */
		private JmxExporterInjectionListener(JmxService jmxService) {
			this.jmxService = jmxService;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.inject.spi.InjectionListener#afterInjection(java.lang.Object)
		 */
		@Override
		public void afterInjection(I instance) {
			jmxService.registerMBean(instance);
		}
	}
}
