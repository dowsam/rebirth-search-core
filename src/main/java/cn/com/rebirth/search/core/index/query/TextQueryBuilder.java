/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TextQueryBuilder.java 2012-3-29 15:02:35 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;


/**
 * The Class TextQueryBuilder.
 *
 * @author l.xue.nong
 */
public class TextQueryBuilder extends BaseQueryBuilder {

	
	/**
	 * The Enum Operator.
	 *
	 * @author l.xue.nong
	 */
	public static enum Operator {

		
		/** The OR. */
		OR,

		
		/** The AND. */
		AND
	}

	
	/**
	 * The Enum Type.
	 *
	 * @author l.xue.nong
	 */
	public static enum Type {

		
		/** The BOOLEAN. */
		BOOLEAN,

		
		/** The PHRASE. */
		PHRASE,

		
		/** The PHRAS e_ prefix. */
		PHRASE_PREFIX
	}

	
	/** The name. */
	private final String name;

	
	/** The text. */
	private final Object text;

	
	/** The type. */
	private Type type;

	
	/** The operator. */
	private Operator operator;

	
	/** The analyzer. */
	private String analyzer;

	
	/** The boost. */
	private Float boost;

	
	/** The slop. */
	private Integer slop;

	
	/** The fuzziness. */
	private String fuzziness;

	
	/** The prefix length. */
	private Integer prefixLength;

	
	/** The max expansions. */
	private Integer maxExpansions;

	
	/**
	 * Instantiates a new text query builder.
	 *
	 * @param name the name
	 * @param text the text
	 */
	public TextQueryBuilder(String name, Object text) {
		this.name = name;
		this.text = text;
	}

	
	/**
	 * Type.
	 *
	 * @param type the type
	 * @return the text query builder
	 */
	public TextQueryBuilder type(Type type) {
		this.type = type;
		return this;
	}

	
	/**
	 * Operator.
	 *
	 * @param operator the operator
	 * @return the text query builder
	 */
	public TextQueryBuilder operator(Operator operator) {
		this.operator = operator;
		return this;
	}

	
	/**
	 * Analyzer.
	 *
	 * @param analyzer the analyzer
	 * @return the text query builder
	 */
	public TextQueryBuilder analyzer(String analyzer) {
		this.analyzer = analyzer;
		return this;
	}

	
	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the text query builder
	 */
	public TextQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	
	/**
	 * Slop.
	 *
	 * @param slop the slop
	 * @return the text query builder
	 */
	public TextQueryBuilder slop(int slop) {
		this.slop = slop;
		return this;
	}

	
	/**
	 * Fuzziness.
	 *
	 * @param fuzziness the fuzziness
	 * @return the text query builder
	 */
	public TextQueryBuilder fuzziness(Object fuzziness) {
		this.fuzziness = fuzziness.toString();
		return this;
	}

	
	/**
	 * Max expansions.
	 *
	 * @param maxExpansions the max expansions
	 * @return the text query builder
	 */
	public TextQueryBuilder maxExpansions(int maxExpansions) {
		this.maxExpansions = maxExpansions;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(TextQueryParser.NAME);
		builder.startObject(name);

		builder.field("query", text);
		if (type != null) {
			builder.field("type", type.toString().toLowerCase());
		}
		if (operator != null) {
			builder.field("operator", operator.toString());
		}
		if (analyzer != null) {
			builder.field("analyzer", analyzer);
		}
		if (boost != null) {
			builder.field("boost", boost);
		}
		if (slop != null) {
			builder.field("slop", slop);
		}
		if (fuzziness != null) {
			builder.field("fuzziness", fuzziness);
		}
		if (prefixLength != null) {
			builder.field("prefix_length", prefixLength);
		}
		if (maxExpansions != null) {
			builder.field("max_expansions", maxExpansions);
		}

		builder.endObject();
		builder.endObject();
	}
}