/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AliasAction.java 2012-3-29 15:02:25 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.metadata;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentType;
import cn.com.rebirth.search.core.RestartGenerationException;
import cn.com.rebirth.search.core.index.query.FilterBuilder;


/**
 * The Class AliasAction.
 *
 * @author l.xue.nong
 */
public class AliasAction implements Streamable {

	
	/**
	 * The Enum Type.
	 *
	 * @author l.xue.nong
	 */
	public static enum Type {

		
		/** The ADD. */
		ADD((byte) 0),

		
		/** The REMOVE. */
		REMOVE((byte) 1);

		
		/** The value. */
		private final byte value;

		
		/**
		 * Instantiates a new type.
		 *
		 * @param value the value
		 */
		Type(byte value) {
			this.value = value;
		}

		
		/**
		 * Value.
		 *
		 * @return the byte
		 */
		public byte value() {
			return value;
		}

		
		/**
		 * From value.
		 *
		 * @param value the value
		 * @return the type
		 */
		public static Type fromValue(byte value) {
			if (value == 0) {
				return ADD;
			} else if (value == 1) {
				return REMOVE;
			} else {
				throw new RestartIllegalArgumentException("No type for action [" + value + "]");
			}
		}
	}

	
	/** The action type. */
	private Type actionType;

	
	/** The index. */
	private String index;

	
	/** The alias. */
	private String alias;

	
	/** The filter. */
	@Nullable
	private String filter;

	
	/** The index routing. */
	@Nullable
	private String indexRouting;

	
	/** The search routing. */
	@Nullable
	private String searchRouting;

	
	/**
	 * Instantiates a new alias action.
	 */
	private AliasAction() {

	}

	
	/**
	 * Instantiates a new alias action.
	 *
	 * @param actionType the action type
	 * @param index the index
	 * @param alias the alias
	 */
	public AliasAction(Type actionType, String index, String alias) {
		this.actionType = actionType;
		this.index = index;
		this.alias = alias;
	}

	
	/**
	 * Instantiates a new alias action.
	 *
	 * @param actionType the action type
	 * @param index the index
	 * @param alias the alias
	 * @param filter the filter
	 */
	public AliasAction(Type actionType, String index, String alias, String filter) {
		this.actionType = actionType;
		this.index = index;
		this.alias = alias;
		this.filter = filter;
	}

	
	/**
	 * Action type.
	 *
	 * @return the type
	 */
	public Type actionType() {
		return actionType;
	}

	
	/**
	 * Index.
	 *
	 * @return the string
	 */
	public String index() {
		return index;
	}

	
	/**
	 * Alias.
	 *
	 * @return the string
	 */
	public String alias() {
		return alias;
	}

	
	/**
	 * Filter.
	 *
	 * @return the string
	 */
	public String filter() {
		return filter;
	}

	
	/**
	 * Filter.
	 *
	 * @param filter the filter
	 * @return the alias action
	 */
	public AliasAction filter(String filter) {
		this.filter = filter;
		return this;
	}

	
	/**
	 * Filter.
	 *
	 * @param filter the filter
	 * @return the alias action
	 */
	public AliasAction filter(Map<String, Object> filter) {
		if (filter == null || filter.isEmpty()) {
			this.filter = null;
			return this;
		}
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON);
			builder.map(filter);
			this.filter = builder.string();
			return this;
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + filter + "]", e);
		}
	}

	
	/**
	 * Filter.
	 *
	 * @param filterBuilder the filter builder
	 * @return the alias action
	 */
	public AliasAction filter(FilterBuilder filterBuilder) {
		if (filterBuilder == null) {
			this.filter = null;
			return this;
		}
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder();
			filterBuilder.toXContent(builder, ToXContent.EMPTY_PARAMS);
			builder.close();
			this.filter = builder.string();
			return this;
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to build json for alias request", e);
		}
	}

	
	/**
	 * Routing.
	 *
	 * @param routing the routing
	 * @return the alias action
	 */
	public AliasAction routing(String routing) {
		this.indexRouting = routing;
		this.searchRouting = routing;
		return this;
	}

	
	/**
	 * Index routing.
	 *
	 * @return the string
	 */
	public String indexRouting() {
		return indexRouting;
	}

	
	/**
	 * Index routing.
	 *
	 * @param indexRouting the index routing
	 * @return the alias action
	 */
	public AliasAction indexRouting(String indexRouting) {
		this.indexRouting = indexRouting;
		return this;
	}

	
	/**
	 * Search routing.
	 *
	 * @return the string
	 */
	public String searchRouting() {
		return searchRouting;
	}

	
	/**
	 * Search routing.
	 *
	 * @param searchRouting the search routing
	 * @return the alias action
	 */
	public AliasAction searchRouting(String searchRouting) {
		this.searchRouting = searchRouting;
		return this;
	}

	
	/**
	 * Read alias action.
	 *
	 * @param in the in
	 * @return the alias action
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static AliasAction readAliasAction(StreamInput in) throws IOException {
		AliasAction aliasAction = new AliasAction();
		aliasAction.readFrom(in);
		return aliasAction;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		actionType = Type.fromValue(in.readByte());
		index = in.readUTF();
		alias = in.readUTF();
		if (in.readBoolean()) {
			filter = in.readUTF();
		}
		if (in.readBoolean()) {
			indexRouting = in.readUTF();
		}
		if (in.readBoolean()) {
			searchRouting = in.readUTF();
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeByte(actionType.value());
		out.writeUTF(index);
		out.writeUTF(alias);
		if (filter == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(filter);
		}
		if (indexRouting == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(indexRouting);
		}
		if (searchRouting == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(searchRouting);
		}
	}

	
	/**
	 * New add alias action.
	 *
	 * @param index the index
	 * @param alias the alias
	 * @return the alias action
	 */
	public static AliasAction newAddAliasAction(String index, String alias) {
		return new AliasAction(Type.ADD, index, alias);
	}

	
	/**
	 * New remove alias action.
	 *
	 * @param index the index
	 * @param alias the alias
	 * @return the alias action
	 */
	public static AliasAction newRemoveAliasAction(String index, String alias) {
		return new AliasAction(Type.REMOVE, index, alias);
	}

}
