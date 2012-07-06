/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesAliasesRequest.java 2012-7-6 14:29:32 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.alias;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentType;
import cn.com.rebirth.search.core.RestartGenerationException;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest;
import cn.com.rebirth.search.core.cluster.metadata.AliasAction;
import cn.com.rebirth.search.core.index.query.FilterBuilder;

import com.google.common.collect.Lists;

/**
 * The Class IndicesAliasesRequest.
 *
 * @author l.xue.nong
 */
public class IndicesAliasesRequest extends MasterNodeOperationRequest {

	/** The alias actions. */
	private List<AliasAction> aliasActions = Lists.newArrayList();

	/** The timeout. */
	private TimeValue timeout = TimeValue.timeValueSeconds(10);

	/**
	 * Instantiates a new indices aliases request.
	 */
	public IndicesAliasesRequest() {

	}

	/**
	 * Adds the alias.
	 *
	 * @param index the index
	 * @param alias the alias
	 * @return the indices aliases request
	 */
	public IndicesAliasesRequest addAlias(String index, String alias) {
		aliasActions.add(new AliasAction(AliasAction.Type.ADD, index, alias));
		return this;
	}

	/**
	 * Adds the alias.
	 *
	 * @param index the index
	 * @param alias the alias
	 * @param filter the filter
	 * @return the indices aliases request
	 */
	public IndicesAliasesRequest addAlias(String index, String alias, String filter) {
		aliasActions.add(new AliasAction(AliasAction.Type.ADD, index, alias, filter));
		return this;
	}

	/**
	 * Adds the alias.
	 *
	 * @param index the index
	 * @param alias the alias
	 * @param filter the filter
	 * @return the indices aliases request
	 */
	public IndicesAliasesRequest addAlias(String index, String alias, Map<String, Object> filter) {
		if (filter == null || filter.isEmpty()) {
			aliasActions.add(new AliasAction(AliasAction.Type.ADD, index, alias));
			return this;
		}
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON);
			builder.map(filter);
			aliasActions.add(new AliasAction(AliasAction.Type.ADD, index, alias, builder.string()));
			return this;
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + filter + "]", e);
		}
	}

	/**
	 * Adds the alias.
	 *
	 * @param index the index
	 * @param alias the alias
	 * @param filterBuilder the filter builder
	 * @return the indices aliases request
	 */
	public IndicesAliasesRequest addAlias(String index, String alias, FilterBuilder filterBuilder) {
		if (filterBuilder == null) {
			aliasActions.add(new AliasAction(AliasAction.Type.ADD, index, alias));
			return this;
		}
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder();
			filterBuilder.toXContent(builder, ToXContent.EMPTY_PARAMS);
			builder.close();
			return addAlias(index, alias, builder.string());
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to build json for alias request", e);
		}
	}

	/**
	 * Removes the alias.
	 *
	 * @param index the index
	 * @param alias the alias
	 * @return the indices aliases request
	 */
	public IndicesAliasesRequest removeAlias(String index, String alias) {
		aliasActions.add(new AliasAction(AliasAction.Type.REMOVE, index, alias));
		return this;
	}

	/**
	 * Adds the alias action.
	 *
	 * @param action the action
	 * @return the indices aliases request
	 */
	public IndicesAliasesRequest addAliasAction(AliasAction action) {
		aliasActions.add(action);
		return this;
	}

	/**
	 * Alias actions.
	 *
	 * @return the list
	 */
	List<AliasAction> aliasActions() {
		return this.aliasActions;
	}

	/**
	 * Timeout.
	 *
	 * @return the time value
	 */
	TimeValue timeout() {
		return timeout;
	}

	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the indices aliases request
	 */
	public IndicesAliasesRequest timeout(TimeValue timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the indices aliases request
	 */
	public IndicesAliasesRequest timeout(String timeout) {
		return timeout(TimeValue.parseTimeValue(timeout, TimeValue.timeValueSeconds(10)));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = null;
		if (aliasActions.isEmpty()) {
			validationException = ValidateActions.addValidationError("Must specify at least one alias action",
					validationException);
		}
		return validationException;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		int size = in.readVInt();
		for (int i = 0; i < size; i++) {
			aliasActions.add(AliasAction.readAliasAction(in));
		}
		timeout = TimeValue.readTimeValue(in);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeVInt(aliasActions.size());
		for (AliasAction aliasAction : aliasActions) {
			aliasAction.writeTo(out);
		}
		timeout.writeTo(out);
	}
}
