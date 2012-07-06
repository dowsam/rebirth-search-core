/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ScriptService.java 2012-7-6 14:30:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.concurrent.ConcurrentCollections;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.io.Streams;
import cn.com.rebirth.search.core.env.Environment;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.script.mvel.MvelScriptEngineService;
import cn.com.rebirth.search.core.search.lookup.SearchLookup;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * The Class ScriptService.
 *
 * @author l.xue.nong
 */
public class ScriptService extends AbstractComponent {

	/** The default lang. */
	private final String defaultLang;

	/** The script engines. */
	private final ImmutableMap<String, ScriptEngineService> scriptEngines;

	/** The static cache. */
	private final ConcurrentMap<String, CompiledScript> staticCache = ConcurrentCollections.newConcurrentMap();

	/** The cache. */
	private final Cache<CacheKey, CompiledScript> cache = CacheBuilder.newBuilder().build();

	/** The disable dynamic. */
	private final boolean disableDynamic;

	/**
	 * Instantiates a new script service.
	 *
	 * @param settings the settings
	 */
	public ScriptService(Settings settings) {
		this(settings, new Environment(), ImmutableSet.<ScriptEngineService> builder()
				.add(new MvelScriptEngineService(settings)).build());
	}

	/**
	 * Instantiates a new script service.
	 *
	 * @param settings the settings
	 * @param env the env
	 * @param scriptEngines the script engines
	 */
	@Inject
	public ScriptService(Settings settings, Environment env, Set<ScriptEngineService> scriptEngines) {
		super(settings);

		this.defaultLang = componentSettings.get("default_lang", "mvel");
		this.disableDynamic = componentSettings.getAsBoolean("disable_dynamic", false);

		ImmutableMap.Builder<String, ScriptEngineService> builder = ImmutableMap.builder();
		for (ScriptEngineService scriptEngine : scriptEngines) {
			for (String type : scriptEngine.types()) {
				builder.put(type, scriptEngine);
			}
		}
		this.scriptEngines = builder.build();

		staticCache.put("doc.score", new CompiledScript("native", new DocScoreNativeScriptFactory()));

		File scriptsFile = new File(env.configFile(), "scripts");
		if (scriptsFile.exists()) {
			processScriptsDirectory("", scriptsFile);
		}
	}

	/**
	 * Process scripts directory.
	 *
	 * @param prefix the prefix
	 * @param dir the dir
	 */
	private void processScriptsDirectory(String prefix, File dir) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				processScriptsDirectory(prefix + file.getName() + "_", file);
			} else {
				int extIndex = file.getName().lastIndexOf('.');
				if (extIndex != -1) {
					String ext = file.getName().substring(extIndex + 1);
					String scriptName = prefix + file.getName().substring(0, extIndex);
					boolean found = false;
					for (ScriptEngineService engineService : scriptEngines.values()) {
						for (String s : engineService.extensions()) {
							if (s.equals(ext)) {
								found = true;
								try {
									String script = Streams.copyToString(new InputStreamReader(
											new FileInputStream(file), "UTF-8"));
									staticCache.put(scriptName, new CompiledScript(engineService.types()[0],
											engineService.compile(script)));
								} catch (Exception e) {
									logger.warn("failed to load/compile script [{}]", e, scriptName);
								}
								break;
							}
						}
						if (found) {
							break;
						}
					}
					if (!found) {
						logger.warn("no script engine found for [{}]", ext);
					}
				}
			}
		}
	}

	/**
	 * Close.
	 */
	public void close() {
		for (ScriptEngineService engineService : scriptEngines.values()) {
			engineService.close();
		}
	}

	/**
	 * Compile.
	 *
	 * @param script the script
	 * @return the compiled script
	 */
	public CompiledScript compile(String script) {
		return compile(defaultLang, script);
	}

	/**
	 * Compile.
	 *
	 * @param lang the lang
	 * @param script the script
	 * @return the compiled script
	 */
	public CompiledScript compile(String lang, String script) {
		CompiledScript compiled = staticCache.get(script);
		if (compiled != null) {
			return compiled;
		}
		if (lang == null) {
			lang = defaultLang;
		}
		if (dynamicScriptDisabled(lang)) {
			throw new ScriptException("dynamic scripting disabled");
		}
		CacheKey cacheKey = new CacheKey(lang, script);
		compiled = cache.getIfPresent(cacheKey);
		if (compiled != null) {
			return compiled;
		}

		ScriptEngineService service = scriptEngines.get(lang);
		if (service == null) {
			throw new RebirthIllegalArgumentException("script_lang not supported [" + lang + "]");
		}
		compiled = new CompiledScript(lang, service.compile(script));
		cache.put(cacheKey, compiled);
		return compiled;
	}

	/**
	 * Executable.
	 *
	 * @param lang the lang
	 * @param script the script
	 * @param vars the vars
	 * @return the executable script
	 */
	public ExecutableScript executable(String lang, String script, Map vars) {
		return executable(compile(lang, script), vars);
	}

	/**
	 * Executable.
	 *
	 * @param compiledScript the compiled script
	 * @param vars the vars
	 * @return the executable script
	 */
	public ExecutableScript executable(CompiledScript compiledScript, Map vars) {
		return scriptEngines.get(compiledScript.lang()).executable(compiledScript.compiled(), vars);
	}

	/**
	 * Search.
	 *
	 * @param compiledScript the compiled script
	 * @param lookup the lookup
	 * @param vars the vars
	 * @return the search script
	 */
	public SearchScript search(CompiledScript compiledScript, SearchLookup lookup, @Nullable Map<String, Object> vars) {
		return scriptEngines.get(compiledScript.lang()).search(compiledScript.compiled(), lookup, vars);
	}

	/**
	 * Search.
	 *
	 * @param lookup the lookup
	 * @param lang the lang
	 * @param script the script
	 * @param vars the vars
	 * @return the search script
	 */
	public SearchScript search(SearchLookup lookup, String lang, String script, @Nullable Map<String, Object> vars) {
		return search(compile(lang, script), lookup, vars);
	}

	/**
	 * Search.
	 *
	 * @param mapperService the mapper service
	 * @param fieldDataCache the field data cache
	 * @param lang the lang
	 * @param script the script
	 * @param vars the vars
	 * @return the search script
	 */
	public SearchScript search(MapperService mapperService, FieldDataCache fieldDataCache, String lang, String script,
			@Nullable Map<String, Object> vars) {
		return search(compile(lang, script), new SearchLookup(mapperService, fieldDataCache), vars);
	}

	/**
	 * Execute.
	 *
	 * @param compiledScript the compiled script
	 * @param vars the vars
	 * @return the object
	 */
	public Object execute(CompiledScript compiledScript, Map vars) {
		return scriptEngines.get(compiledScript.lang()).execute(compiledScript.compiled(), vars);
	}

	/**
	 * Clear.
	 */
	public void clear() {
		cache.invalidateAll();
	}

	/**
	 * Dynamic script disabled.
	 *
	 * @param lang the lang
	 * @return true, if successful
	 */
	private boolean dynamicScriptDisabled(String lang) {
		if (!disableDynamic) {
			return false;
		}

		return !"native".equals(lang);
	}

	/**
	 * The Class CacheKey.
	 *
	 * @author l.xue.nong
	 */
	public static class CacheKey {

		/** The lang. */
		public final String lang;

		/** The script. */
		public final String script;

		/**
		 * Instantiates a new cache key.
		 *
		 * @param lang the lang
		 * @param script the script
		 */
		public CacheKey(String lang, String script) {
			this.lang = lang;
			this.script = script;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			CacheKey other = (CacheKey) o;
			return lang.equals(other.lang) && script.equals(other.script);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return lang.hashCode() + 31 * script.hashCode();
		}
	}

	/**
	 * A factory for creating DocScoreNativeScript objects.
	 */
	public static class DocScoreNativeScriptFactory implements NativeScriptFactory {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.script.NativeScriptFactory#newScript(java.util.Map)
		 */
		@Override
		public ExecutableScript newScript(@Nullable Map<String, Object> params) {
			return new DocScoreSearchScript();
		}
	}

	/**
	 * The Class DocScoreSearchScript.
	 *
	 * @author l.xue.nong
	 */
	public static class DocScoreSearchScript extends AbstractFloatSearchScript {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.script.AbstractFloatSearchScript#runAsFloat()
		 */
		@Override
		public float runAsFloat() {
			try {
				return doc().score();
			} catch (IOException e) {
				return 0;
			}
		}
	}
}
