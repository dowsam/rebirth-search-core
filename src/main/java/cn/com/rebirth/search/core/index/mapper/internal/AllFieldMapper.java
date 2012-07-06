/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AllFieldMapper.java 2012-3-29 15:01:03 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper.internal;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.commons.lucene.all.AllField;
import cn.com.rebirth.search.commons.lucene.all.AllTermQuery;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.support.XContentMapValues;
import cn.com.rebirth.search.core.index.mapper.InternalMapper;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperBuilders;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.mapper.RootMapper;
import cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.TypeParsers;
import cn.com.rebirth.search.core.index.query.QueryParseContext;
import cn.com.rebirth.search.index.analysis.NamedAnalyzer;


/**
 * The Class AllFieldMapper.
 *
 * @author l.xue.nong
 */
public class AllFieldMapper extends AbstractFieldMapper<Void> implements InternalMapper, RootMapper {

	
	/**
	 * The Interface IncludeInAll.
	 *
	 * @author l.xue.nong
	 */
	public interface IncludeInAll extends Mapper {

		
		/**
		 * Include in all.
		 *
		 * @param includeInAll the include in all
		 */
		void includeInAll(Boolean includeInAll);

		
		/**
		 * Include in all if not set.
		 *
		 * @param includeInAll the include in all
		 */
		void includeInAllIfNotSet(Boolean includeInAll);
	}

	
	/** The Constant NAME. */
	public static final String NAME = "_all";

	
	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "_all";

	
	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults extends AbstractFieldMapper.Defaults {

		
		/** The Constant NAME. */
		public static final String NAME = AllFieldMapper.NAME;

		
		/** The Constant INDEX_NAME. */
		public static final String INDEX_NAME = AllFieldMapper.NAME;

		
		/** The Constant ENABLED. */
		public static final boolean ENABLED = true;
	}

	
	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends AbstractFieldMapper.Builder<Builder, AllFieldMapper> {

		
		/** The enabled. */
		private boolean enabled = Defaults.ENABLED;

		
		/**
		 * Instantiates a new builder.
		 */
		public Builder() {
			super(Defaults.NAME);
			builder = this;
			indexName = Defaults.INDEX_NAME;
		}

		
		/**
		 * Enabled.
		 *
		 * @param enabled the enabled
		 * @return the builder
		 */
		public Builder enabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper.Builder#store(org.apache.lucene.document.Field.Store)
		 */
		@Override
		public Builder store(Field.Store store) {
			return super.store(store);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper.Builder#termVector(org.apache.lucene.document.Field.TermVector)
		 */
		@Override
		public Builder termVector(Field.TermVector termVector) {
			return super.termVector(termVector);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper.Builder#indexAnalyzer(cn.com.summall.search.index.analysis.NamedAnalyzer)
		 */
		@Override
		protected Builder indexAnalyzer(NamedAnalyzer indexAnalyzer) {
			return super.indexAnalyzer(indexAnalyzer);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper.Builder#searchAnalyzer(cn.com.summall.search.index.analysis.NamedAnalyzer)
		 */
		@Override
		protected Builder searchAnalyzer(NamedAnalyzer searchAnalyzer) {
			return super.searchAnalyzer(searchAnalyzer);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.mapper.Mapper.Builder#build(cn.com.summall.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public AllFieldMapper build(BuilderContext context) {
			return new AllFieldMapper(name, store, termVector, omitNorms, omitTermFreqAndPositions, indexAnalyzer,
					searchAnalyzer, enabled);
		}
	}

	
	/**
	 * The Class TypeParser.
	 *
	 * @author l.xue.nong
	 */
	public static class TypeParser implements Mapper.TypeParser {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.mapper.Mapper.TypeParser#parse(java.lang.String, java.util.Map, cn.com.summall.search.core.index.mapper.Mapper.TypeParser.ParserContext)
		 */
		@Override
		public Mapper.Builder parse(String name, Map<String, Object> node, ParserContext parserContext)
				throws MapperParsingException {
			AllFieldMapper.Builder builder = MapperBuilders.all();
			TypeParsers.parseField(builder, builder.name, node, parserContext);
			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String fieldName = Strings.toUnderscoreCase(entry.getKey());
				Object fieldNode = entry.getValue();
				if (fieldName.equals("enabled")) {
					builder.enabled(XContentMapValues.nodeBooleanValue(fieldNode));
				}
			}
			return builder;
		}
	}

	
	/** The enabled. */
	private boolean enabled;

	
	/**
	 * Instantiates a new all field mapper.
	 */
	public AllFieldMapper() {
		this(Defaults.NAME, Defaults.STORE, Defaults.TERM_VECTOR, Defaults.OMIT_NORMS,
				Defaults.OMIT_TERM_FREQ_AND_POSITIONS, null, null, Defaults.ENABLED);
	}

	
	/**
	 * Instantiates a new all field mapper.
	 *
	 * @param name the name
	 * @param store the store
	 * @param termVector the term vector
	 * @param omitNorms the omit norms
	 * @param omitTermFreqAndPositions the omit term freq and positions
	 * @param indexAnalyzer the index analyzer
	 * @param searchAnalyzer the search analyzer
	 * @param enabled the enabled
	 */
	protected AllFieldMapper(String name, Field.Store store, Field.TermVector termVector, boolean omitNorms,
			boolean omitTermFreqAndPositions, NamedAnalyzer indexAnalyzer, NamedAnalyzer searchAnalyzer, boolean enabled) {
		super(new Names(name, name, name, name), Field.Index.ANALYZED, store, termVector, 1.0f, omitNorms,
				omitTermFreqAndPositions, indexAnalyzer, searchAnalyzer);
		this.enabled = enabled;
	}

	
	/**
	 * Enabled.
	 *
	 * @return true, if successful
	 */
	public boolean enabled() {
		return this.enabled;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#queryStringTermQuery(org.apache.lucene.index.Term)
	 */
	@Override
	public Query queryStringTermQuery(Term term) {
		return new AllTermQuery(term);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#fieldQuery(java.lang.String, cn.com.summall.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query fieldQuery(String value, QueryParseContext context) {
		return new AllTermQuery(names().createIndexNameTerm(value));
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.RootMapper#preParse(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void preParse(ParseContext context) throws IOException {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.RootMapper#postParse(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void postParse(ParseContext context) throws IOException {
		super.parse(context);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#parse(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void parse(ParseContext context) throws IOException {
		
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.RootMapper#validate(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void validate(ParseContext context) throws MapperParsingException {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.RootMapper#includeInObject()
	 */
	@Override
	public boolean includeInObject() {
		return true;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#parseCreateField(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	protected Fieldable parseCreateField(ParseContext context) throws IOException {
		if (!enabled) {
			return null;
		}
		
		context.allEntries().reset();

		Analyzer analyzer = findAnalyzer(context);
		return new AllField(names.indexName(), store, termVector, context.allEntries(), analyzer);
	}

	
	/**
	 * Find analyzer.
	 *
	 * @param context the context
	 * @return the analyzer
	 */
	private Analyzer findAnalyzer(ParseContext context) {
		Analyzer analyzer = indexAnalyzer;
		if (analyzer == null) {
			analyzer = context.analyzer();
			if (analyzer == null) {
				analyzer = context.docMapper().indexAnalyzer();
				if (analyzer == null) {
					
					analyzer = Lucene.STANDARD_ANALYZER;
				}
			}
		}
		return analyzer;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.FieldMapper#value(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public Void value(Fieldable field) {
		return null;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.FieldMapper#valueFromString(java.lang.String)
	 */
	@Override
	public Void valueFromString(String value) {
		return null;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.FieldMapper#valueAsString(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public String valueAsString(Fieldable field) {
		return null;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#valueForSearch(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public Object valueForSearch(Fieldable field) {
		return null;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#contentType()
	 */
	@Override
	protected String contentType() {
		return CONTENT_TYPE;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		
		if (enabled == Defaults.ENABLED && store == Defaults.STORE && termVector == Defaults.TERM_VECTOR
				&& indexAnalyzer == null && searchAnalyzer == null) {
			return builder;
		}
		builder.startObject(CONTENT_TYPE);
		if (enabled != Defaults.ENABLED) {
			builder.field("enabled", enabled);
		}
		if (store != Defaults.STORE) {
			builder.field("store", store.name().toLowerCase());
		}
		if (termVector != Defaults.TERM_VECTOR) {
			builder.field("term_vector", termVector.name().toLowerCase());
		}
		if (indexAnalyzer != null && searchAnalyzer != null && indexAnalyzer.name().equals(searchAnalyzer.name())
				&& !indexAnalyzer.name().startsWith("_")) {
			
			builder.field("analyzer", indexAnalyzer.name());
		} else {
			if (indexAnalyzer != null && !indexAnalyzer.name().startsWith("_")) {
				builder.field("index_analyzer", indexAnalyzer.name());
			}
			if (searchAnalyzer != null && !searchAnalyzer.name().startsWith("_")) {
				builder.field("search_analyzer", searchAnalyzer.name());
			}
		}
		builder.endObject();
		return builder;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#merge(cn.com.summall.search.core.index.mapper.Mapper, cn.com.summall.search.core.index.mapper.MergeContext)
	 */
	@Override
	public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
		
	}
}
