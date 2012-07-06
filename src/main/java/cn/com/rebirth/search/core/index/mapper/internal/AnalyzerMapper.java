/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AnalyzerMapper.java 2012-3-29 15:01:38 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper.internal;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.index.mapper.FieldMapperListener;
import cn.com.rebirth.search.core.index.mapper.InternalMapper;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperBuilders;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ObjectMapperListener;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.mapper.RootMapper;


/**
 * The Class AnalyzerMapper.
 *
 * @author l.xue.nong
 */
public class AnalyzerMapper implements Mapper, InternalMapper, RootMapper {

	
	/** The Constant NAME. */
	public static final String NAME = "_analyzer";

	
	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "_analyzer";

	
	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults {

		
		/** The Constant PATH. */
		public static final String PATH = "_analyzer";
	}

	
	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends Mapper.Builder<Builder, AnalyzerMapper> {

		
		/** The field. */
		private String field = Defaults.PATH;

		
		/**
		 * Instantiates a new builder.
		 */
		public Builder() {
			super(CONTENT_TYPE);
			this.builder = this;
		}

		
		/**
		 * Field.
		 *
		 * @param field the field
		 * @return the builder
		 */
		public Builder field(String field) {
			this.field = field;
			return this;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.mapper.Mapper.Builder#build(cn.com.summall.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public AnalyzerMapper build(BuilderContext context) {
			return new AnalyzerMapper(field);
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
			AnalyzerMapper.Builder builder = MapperBuilders.analyzer();
			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String fieldName = Strings.toUnderscoreCase(entry.getKey());
				Object fieldNode = entry.getValue();
				if (fieldName.equals("path")) {
					builder.field(fieldNode.toString());
				}
			}
			return builder;
		}
	}

	
	/** The path. */
	private final String path;

	
	/**
	 * Instantiates a new analyzer mapper.
	 */
	public AnalyzerMapper() {
		this(Defaults.PATH);
	}

	
	/**
	 * Instantiates a new analyzer mapper.
	 *
	 * @param path the path
	 */
	public AnalyzerMapper(String path) {
		this.path = path;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.Mapper#name()
	 */
	@Override
	public String name() {
		return CONTENT_TYPE;
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
		Analyzer analyzer = context.docMapper().mappers().indexAnalyzer();
		if (path != null) {
			String value = context.doc().get(path);
			if (value == null) {
				value = context.ignoredValue(path);
			}
			if (value != null) {
				analyzer = context.analysisService().analyzer(value);
				if (analyzer == null) {
					throw new MapperParsingException("No analyzer found for [" + value + "] from path [" + path + "]");
				}
				analyzer = context.docMapper().mappers().indexAnalyzer(analyzer);
			}
		}
		context.analyzer(analyzer);
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
		return false;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.Mapper#parse(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void parse(ParseContext context) throws IOException {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.Mapper#merge(cn.com.summall.search.core.index.mapper.Mapper, cn.com.summall.search.core.index.mapper.MergeContext)
	 */
	@Override
	public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.Mapper#traverse(cn.com.summall.search.core.index.mapper.FieldMapperListener)
	 */
	@Override
	public void traverse(FieldMapperListener fieldMapperListener) {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.Mapper#traverse(cn.com.summall.search.core.index.mapper.ObjectMapperListener)
	 */
	@Override
	public void traverse(ObjectMapperListener objectMapperListener) {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		if (path.equals(Defaults.PATH)) {
			return builder;
		}
		builder.startObject(CONTENT_TYPE);
		if (!path.equals(Defaults.PATH)) {
			builder.field("path", path);
		}
		builder.endObject();
		return builder;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.Mapper#close()
	 */
	@Override
	public void close() {

	}
}
