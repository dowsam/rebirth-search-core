/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SpanNearQueryBuilder.java 2012-3-29 15:02:43 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.query;

import java.io.IOException;
import java.util.ArrayList;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;


/**
 * The Class SpanNearQueryBuilder.
 *
 * @author l.xue.nong
 */
public class SpanNearQueryBuilder extends BaseQueryBuilder implements SpanQueryBuilder {

	
	/** The clauses. */
	private ArrayList<SpanQueryBuilder> clauses = new ArrayList<SpanQueryBuilder>();

	
	/** The slop. */
	private int slop = -1;

	
	/** The in order. */
	private Boolean inOrder;

	
	/** The collect payloads. */
	private Boolean collectPayloads;

	
	/** The boost. */
	private float boost = -1;

	
	/**
	 * Clause.
	 *
	 * @param clause the clause
	 * @return the span near query builder
	 */
	public SpanNearQueryBuilder clause(SpanQueryBuilder clause) {
		clauses.add(clause);
		return this;
	}

	
	/**
	 * Slop.
	 *
	 * @param slop the slop
	 * @return the span near query builder
	 */
	public SpanNearQueryBuilder slop(int slop) {
		this.slop = slop;
		return this;
	}

	
	/**
	 * In order.
	 *
	 * @param inOrder the in order
	 * @return the span near query builder
	 */
	public SpanNearQueryBuilder inOrder(boolean inOrder) {
		this.inOrder = inOrder;
		return this;
	}

	
	/**
	 * Collect payloads.
	 *
	 * @param collectPayloads the collect payloads
	 * @return the span near query builder
	 */
	public SpanNearQueryBuilder collectPayloads(boolean collectPayloads) {
		this.collectPayloads = collectPayloads;
		return this;
	}

	
	/**
	 * Boost.
	 *
	 * @param boost the boost
	 * @return the span near query builder
	 */
	public SpanNearQueryBuilder boost(float boost) {
		this.boost = boost;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.query.BaseQueryBuilder#doXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		if (clauses.isEmpty()) {
			throw new QueryBuilderException("Must have at least one clause when building a spanNear query");
		}
		if (slop == -1) {
			throw new QueryBuilderException("Must set the slop when building a spanNear query");
		}
		builder.startObject(SpanNearQueryParser.NAME);
		builder.startArray("clauses");
		for (SpanQueryBuilder clause : clauses) {
			clause.toXContent(builder, params);
		}
		builder.endArray();
		builder.field("slop", slop);
		if (inOrder != null) {
			builder.field("in_order", inOrder);
		}
		if (collectPayloads != null) {
			builder.field("collect_payloads", collectPayloads);
		}
		if (boost != -1) {
			builder.field("boost", boost);
		}
		builder.endObject();
	}
}
