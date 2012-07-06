/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core NativeScriptEngineService.java 2012-3-29 15:02:03 l.xue.nong$$
 */


package cn.com.rebirth.search.core.script;

import java.util.Map;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.search.lookup.SearchLookup;

import com.google.common.collect.ImmutableMap;


/**
 * The Class NativeScriptEngineService.
 *
 * @author l.xue.nong
 */
public class NativeScriptEngineService extends AbstractComponent implements ScriptEngineService {

	
	/** The scripts. */
	private final ImmutableMap<String, NativeScriptFactory> scripts;

	
	/**
	 * Instantiates a new native script engine service.
	 *
	 * @param settings the settings
	 * @param scripts the scripts
	 */
	@Inject
	public NativeScriptEngineService(Settings settings, Map<String, NativeScriptFactory> scripts) {
		super(settings);
		this.scripts = ImmutableMap.copyOf(scripts);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.ScriptEngineService#types()
	 */
	@Override
	public String[] types() {
		return new String[] { "native" };
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.ScriptEngineService#extensions()
	 */
	@Override
	public String[] extensions() {
		return new String[0];
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.ScriptEngineService#compile(java.lang.String)
	 */
	@Override
	public Object compile(String script) {
		NativeScriptFactory scriptFactory = scripts.get(script);
		if (scriptFactory != null) {
			return scriptFactory;
		}
		throw new RestartIllegalArgumentException("Native script [" + script + "] not found");
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.ScriptEngineService#executable(java.lang.Object, java.util.Map)
	 */
	@Override
	public ExecutableScript executable(Object compiledScript, @Nullable Map<String, Object> vars) {
		NativeScriptFactory scriptFactory = (NativeScriptFactory) compiledScript;
		return scriptFactory.newScript(vars);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.ScriptEngineService#search(java.lang.Object, cn.com.summall.search.core.search.lookup.SearchLookup, java.util.Map)
	 */
	@Override
	public SearchScript search(Object compiledScript, SearchLookup lookup, @Nullable Map<String, Object> vars) {
		NativeScriptFactory scriptFactory = (NativeScriptFactory) compiledScript;
		AbstractSearchScript script = (AbstractSearchScript) scriptFactory.newScript(vars);
		script.setLookup(lookup);
		return script;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.ScriptEngineService#execute(java.lang.Object, java.util.Map)
	 */
	@Override
	public Object execute(Object compiledScript, Map<String, Object> vars) {
		return executable(compiledScript, vars).run();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.ScriptEngineService#unwrap(java.lang.Object)
	 */
	@Override
	public Object unwrap(Object value) {
		return value;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.ScriptEngineService#close()
	 */
	@Override
	public void close() {
	}
}