/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MvelScriptEngineService.java 2012-3-29 15:01:43 l.xue.nong$$
 */


package cn.com.rebirth.search.core.script.mvel;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;
import org.mvel2.MVEL;
import org.mvel2.ParserConfiguration;
import org.mvel2.ParserContext;
import org.mvel2.compiler.ExecutableStatement;
import org.mvel2.integration.impl.MapVariableResolverFactory;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.math.UnboxedMathUtils;
import cn.com.rebirth.search.core.script.ExecutableScript;
import cn.com.rebirth.search.core.script.ScriptEngineService;
import cn.com.rebirth.search.core.script.SearchScript;
import cn.com.rebirth.search.core.search.lookup.SearchLookup;


/**
 * The Class MvelScriptEngineService.
 *
 * @author l.xue.nong
 */
public class MvelScriptEngineService extends AbstractComponent implements ScriptEngineService {

	
	/** The parser configuration. */
	private final ParserConfiguration parserConfiguration;

	
	/**
	 * Instantiates a new mvel script engine service.
	 *
	 * @param settings the settings
	 */
	@Inject
	public MvelScriptEngineService(Settings settings) {
		super(settings);

		parserConfiguration = new ParserConfiguration();
		parserConfiguration.addPackageImport("java.util");
		parserConfiguration.addPackageImport("gnu.trove");
		parserConfiguration.addPackageImport("org.joda");
		parserConfiguration.addImport("time", MVEL.getStaticMethod(System.class, "currentTimeMillis", new Class[0]));
		
		for (Method m : UnboxedMathUtils.class.getMethods()) {
			if ((m.getModifiers() & Modifier.STATIC) > 0) {
				parserConfiguration.addImport(m.getName(), m);
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.ScriptEngineService#close()
	 */
	@Override
	public void close() {
		
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.ScriptEngineService#types()
	 */
	@Override
	public String[] types() {
		return new String[] { "mvel" };
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.ScriptEngineService#extensions()
	 */
	@Override
	public String[] extensions() {
		return new String[] { "mvel" };
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.ScriptEngineService#compile(java.lang.String)
	 */
	@Override
	public Object compile(String script) {
		return MVEL.compileExpression(script, new ParserContext(parserConfiguration));
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.ScriptEngineService#execute(java.lang.Object, java.util.Map)
	 */
	@Override
	public Object execute(Object compiledScript, Map vars) {
		return MVEL.executeExpression(compiledScript, vars);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.ScriptEngineService#executable(java.lang.Object, java.util.Map)
	 */
	@Override
	public ExecutableScript executable(Object compiledScript, Map vars) {
		return new MvelExecutableScript(compiledScript, vars);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.ScriptEngineService#search(java.lang.Object, cn.com.summall.search.core.search.lookup.SearchLookup, java.util.Map)
	 */
	@Override
	public SearchScript search(Object compiledScript, SearchLookup lookup, @Nullable Map<String, Object> vars) {
		return new MvelSearchScript(compiledScript, lookup, vars);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.script.ScriptEngineService#unwrap(java.lang.Object)
	 */
	@Override
	public Object unwrap(Object value) {
		return value;
	}

	
	/**
	 * The Class MvelExecutableScript.
	 *
	 * @author l.xue.nong
	 */
	public static class MvelExecutableScript implements ExecutableScript {

		
		/** The script. */
		private final ExecutableStatement script;

		
		/** The resolver. */
		private final MapVariableResolverFactory resolver;

		
		/**
		 * Instantiates a new mvel executable script.
		 *
		 * @param script the script
		 * @param vars the vars
		 */
		public MvelExecutableScript(Object script, Map vars) {
			this.script = (ExecutableStatement) script;
			if (vars != null) {
				this.resolver = new MapVariableResolverFactory(vars);
			} else {
				this.resolver = new MapVariableResolverFactory(new HashMap());
			}
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.script.ExecutableScript#setNextVar(java.lang.String, java.lang.Object)
		 */
		@Override
		public void setNextVar(String name, Object value) {
			resolver.createVariable(name, value);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.script.ExecutableScript#run()
		 */
		@Override
		public Object run() {
			return script.getValue(null, resolver);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.script.ExecutableScript#unwrap(java.lang.Object)
		 */
		@Override
		public Object unwrap(Object value) {
			return value;
		}
	}

	
	/**
	 * The Class MvelSearchScript.
	 *
	 * @author l.xue.nong
	 */
	public static class MvelSearchScript implements SearchScript {

		
		/** The script. */
		private final ExecutableStatement script;

		
		/** The lookup. */
		private final SearchLookup lookup;

		
		/** The resolver. */
		private final MapVariableResolverFactory resolver;

		
		/**
		 * Instantiates a new mvel search script.
		 *
		 * @param script the script
		 * @param lookup the lookup
		 * @param vars the vars
		 */
		public MvelSearchScript(Object script, SearchLookup lookup, Map<String, Object> vars) {
			this.script = (ExecutableStatement) script;
			this.lookup = lookup;
			if (vars != null) {
				this.resolver = new MapVariableResolverFactory(vars);
			} else {
				this.resolver = new MapVariableResolverFactory(new HashMap());
			}
			for (Map.Entry<String, Object> entry : lookup.asMap().entrySet()) {
				resolver.createVariable(entry.getKey(), entry.getValue());
			}
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.script.SearchScript#setScorer(org.apache.lucene.search.Scorer)
		 */
		@Override
		public void setScorer(Scorer scorer) {
			lookup.setScorer(scorer);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.script.SearchScript#setNextReader(org.apache.lucene.index.IndexReader)
		 */
		@Override
		public void setNextReader(IndexReader reader) {
			lookup.setNextReader(reader);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.script.SearchScript#setNextDocId(int)
		 */
		@Override
		public void setNextDocId(int doc) {
			lookup.setNextDocId(doc);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.script.SearchScript#setNextScore(float)
		 */
		@Override
		public void setNextScore(float score) {
			resolver.createVariable("_score", score);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.script.ExecutableScript#setNextVar(java.lang.String, java.lang.Object)
		 */
		@Override
		public void setNextVar(String name, Object value) {
			resolver.createVariable(name, value);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.script.SearchScript#setNextSource(java.util.Map)
		 */
		@Override
		public void setNextSource(Map<String, Object> source) {
			lookup.source().setNextSource(source);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.script.ExecutableScript#run()
		 */
		@Override
		public Object run() {
			return script.getValue(null, resolver);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.script.SearchScript#runAsFloat()
		 */
		@Override
		public float runAsFloat() {
			return ((Number) run()).floatValue();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.script.SearchScript#runAsLong()
		 */
		@Override
		public long runAsLong() {
			return ((Number) run()).longValue();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.script.SearchScript#runAsDouble()
		 */
		@Override
		public double runAsDouble() {
			return ((Number) run()).doubleValue();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.script.ExecutableScript#unwrap(java.lang.Object)
		 */
		@Override
		public Object unwrap(Object value) {
			return value;
		}
	}
}
