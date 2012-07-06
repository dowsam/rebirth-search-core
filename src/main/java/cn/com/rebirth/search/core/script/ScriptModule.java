/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ScriptModule.java 2012-3-29 15:01:10 l.xue.nong$$
 */


package cn.com.rebirth.search.core.script;

import java.util.List;
import java.util.Map;

import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.AbstractModule;
import cn.com.rebirth.search.commons.inject.multibindings.MapBinder;
import cn.com.rebirth.search.commons.inject.multibindings.Multibinder;
import cn.com.rebirth.search.core.script.mvel.MvelScriptEngineService;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * The Class ScriptModule.
 *
 * @author l.xue.nong
 */
public class ScriptModule extends AbstractModule {

	
	/** The settings. */
	private final Settings settings;

	
	/** The script engines. */
	private final List<Class<? extends ScriptEngineService>> scriptEngines = Lists.newArrayList();

	
	/** The scripts. */
	private final Map<String, Class<? extends NativeScriptFactory>> scripts = Maps.newHashMap();

	
	/**
	 * Instantiates a new script module.
	 *
	 * @param settings the settings
	 */
	public ScriptModule(Settings settings) {
		this.settings = settings;
	}

	
	/**
	 * Adds the script engine.
	 *
	 * @param scriptEngine the script engine
	 */
	public void addScriptEngine(Class<? extends ScriptEngineService> scriptEngine) {
		scriptEngines.add(scriptEngine);
	}

	
	/**
	 * Register script.
	 *
	 * @param name the name
	 * @param script the script
	 */
	public void registerScript(String name, Class<? extends NativeScriptFactory> script) {
		scripts.put(name, script);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		MapBinder<String, NativeScriptFactory> scriptsBinder = MapBinder.newMapBinder(binder(), String.class,
				NativeScriptFactory.class);
		for (Map.Entry<String, Class<? extends NativeScriptFactory>> entry : scripts.entrySet()) {
			scriptsBinder.addBinding(entry.getKey()).to(entry.getValue());
		}

		
		Map<String, Settings> nativeSettings = settings.getGroups("script.native");
		for (Map.Entry<String, Settings> entry : nativeSettings.entrySet()) {
			String name = entry.getKey();
			Class<? extends NativeScriptFactory> type = entry.getValue().getAsClass("type", NativeScriptFactory.class);
			if (type == NativeScriptFactory.class) {
				throw new RestartIllegalArgumentException("type is missing for native script [" + name + "]");
			}
			scriptsBinder.addBinding(name).to(type);
		}

		Multibinder<ScriptEngineService> multibinder = Multibinder.newSetBinder(binder(), ScriptEngineService.class);
		multibinder.addBinding().to(NativeScriptEngineService.class);
		try {
			multibinder.addBinding().to(MvelScriptEngineService.class);
		} catch (Throwable t) {
			
		}
		for (Class<? extends ScriptEngineService> scriptEngine : scriptEngines) {
			multibinder.addBinding().to(scriptEngine);
		}

		bind(ScriptService.class).asEagerSingleton();
	}
}
