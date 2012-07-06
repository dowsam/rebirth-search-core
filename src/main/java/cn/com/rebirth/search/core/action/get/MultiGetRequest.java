/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MultiGetRequest.java 2012-3-29 15:00:44 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.get;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;


/**
 * The Class MultiGetRequest.
 *
 * @author l.xue.nong
 */
public class MultiGetRequest implements ActionRequest {

	
	/**
	 * The Class Item.
	 *
	 * @author l.xue.nong
	 */
	public static class Item implements Streamable {

		
		/** The index. */
		private String index;

		
		/** The type. */
		private String type;

		
		/** The id. */
		private String id;

		
		/** The routing. */
		private String routing;

		
		/** The fields. */
		private String[] fields;

		
		/**
		 * Instantiates a new item.
		 */
		Item() {

		}

		
		/**
		 * Instantiates a new item.
		 *
		 * @param index the index
		 * @param type the type
		 * @param id the id
		 */
		public Item(String index, @Nullable String type, String id) {
			this.index = index;
			this.type = type;
			this.id = id;
		}

		
		/**
		 * Index.
		 *
		 * @return the string
		 */
		public String index() {
			return this.index;
		}

		
		/**
		 * Index.
		 *
		 * @param index the index
		 * @return the item
		 */
		public Item index(String index) {
			this.index = index;
			return this;
		}

		
		/**
		 * Type.
		 *
		 * @return the string
		 */
		public String type() {
			return this.type;
		}

		
		/**
		 * Id.
		 *
		 * @return the string
		 */
		public String id() {
			return this.id;
		}

		
		/**
		 * Routing.
		 *
		 * @param routing the routing
		 * @return the item
		 */
		public Item routing(String routing) {
			this.routing = routing;
			return this;
		}

		
		/**
		 * Routing.
		 *
		 * @return the string
		 */
		public String routing() {
			return this.routing;
		}

		
		/**
		 * Fields.
		 *
		 * @param fields the fields
		 * @return the item
		 */
		public Item fields(String... fields) {
			this.fields = fields;
			return this;
		}

		
		/**
		 * Fields.
		 *
		 * @return the string[]
		 */
		public String[] fields() {
			return this.fields;
		}

		
		/**
		 * Read item.
		 *
		 * @param in the in
		 * @return the item
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static Item readItem(StreamInput in) throws IOException {
			Item item = new Item();
			item.readFrom(in);
			return item;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			index = in.readUTF();
			if (in.readBoolean()) {
				type = in.readUTF();
			}
			id = in.readUTF();
			if (in.readBoolean()) {
				routing = in.readUTF();
			}
			int size = in.readVInt();
			if (size > 0) {
				fields = new String[size];
				for (int i = 0; i < size; i++) {
					fields[i] = in.readUTF();
				}
			}
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeUTF(index);
			if (type == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				out.writeUTF(type);
			}
			out.writeUTF(id);
			if (routing == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				out.writeUTF(routing);
			}
			if (fields == null) {
				out.writeVInt(0);
			} else {
				out.writeVInt(fields.length);
				for (String field : fields) {
					out.writeUTF(field);
				}
			}
		}
	}

	
	/** The listener threaded. */
	private boolean listenerThreaded = false;

	
	/** The preference. */
	String preference;

	
	/** The realtime. */
	Boolean realtime;

	
	/** The refresh. */
	boolean refresh;

	
	/** The items. */
	List<Item> items = new ArrayList<Item>();

	
	/**
	 * Adds the.
	 *
	 * @param item the item
	 * @return the multi get request
	 */
	public MultiGetRequest add(Item item) {
		items.add(item);
		return this;
	}

	
	/**
	 * Adds the.
	 *
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 * @return the multi get request
	 */
	public MultiGetRequest add(String index, @Nullable String type, String id) {
		items.add(new Item(index, type, id));
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#listenerThreaded()
	 */
	@Override
	public boolean listenerThreaded() {
		return listenerThreaded;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#listenerThreaded(boolean)
	 */
	@Override
	public MultiGetRequest listenerThreaded(boolean listenerThreaded) {
		this.listenerThreaded = listenerThreaded;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = null;
		if (items.isEmpty()) {
			validationException = ValidateActions.addValidationError("no documents to get", validationException);
		} else {
			for (int i = 0; i < items.size(); i++) {
				Item item = items.get(i);
				if (item.index() == null) {
					validationException = ValidateActions.addValidationError("index is missing for doc " + i,
							validationException);
				}
				if (item.id() == null) {
					validationException = ValidateActions.addValidationError("id is missing for doc " + i,
							validationException);
				}
			}
		}
		return validationException;
	}

	
	/**
	 * Preference.
	 *
	 * @param preference the preference
	 * @return the multi get request
	 */
	public MultiGetRequest preference(String preference) {
		this.preference = preference;
		return this;
	}

	
	/**
	 * Preference.
	 *
	 * @return the string
	 */
	public String preference() {
		return this.preference;
	}

	
	/**
	 * Realtime.
	 *
	 * @return true, if successful
	 */
	public boolean realtime() {
		return this.realtime == null ? true : this.realtime;
	}

	
	/**
	 * Realtime.
	 *
	 * @param realtime the realtime
	 * @return the multi get request
	 */
	public MultiGetRequest realtime(Boolean realtime) {
		this.realtime = realtime;
		return this;
	}

	
	/**
	 * Refresh.
	 *
	 * @return true, if successful
	 */
	public boolean refresh() {
		return this.refresh;
	}

	
	/**
	 * Refresh.
	 *
	 * @param refresh the refresh
	 * @return the multi get request
	 */
	public MultiGetRequest refresh(boolean refresh) {
		this.refresh = refresh;
		return this;
	}

	
	/**
	 * Adds the.
	 *
	 * @param defaultIndex the default index
	 * @param defaultType the default type
	 * @param defaultFields the default fields
	 * @param data the data
	 * @param from the from
	 * @param length the length
	 * @throws Exception the exception
	 */
	public void add(@Nullable String defaultIndex, @Nullable String defaultType, @Nullable String[] defaultFields,
			byte[] data, int from, int length) throws Exception {
		XContentParser parser = XContentFactory.xContent(data, from, length).createParser(data, from, length);
		try {
			XContentParser.Token token;
			String currentFieldName = null;
			while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
				if (token == XContentParser.Token.FIELD_NAME) {
					currentFieldName = parser.currentName();
				} else if (token == XContentParser.Token.START_ARRAY) {
					if ("docs".equals(currentFieldName)) {
						while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
							if (token != XContentParser.Token.START_OBJECT) {
								throw new RestartIllegalArgumentException(
										"docs array element should include an object");
							}
							String index = defaultIndex;
							String type = defaultType;
							String id = null;
							String routing = null;
							List<String> fields = null;
							while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
								if (token == XContentParser.Token.FIELD_NAME) {
									currentFieldName = parser.currentName();
								} else if (token.isValue()) {
									if ("_index".equals(currentFieldName)) {
										index = parser.text();
									} else if ("_type".equals(currentFieldName)) {
										type = parser.text();
									} else if ("_id".equals(currentFieldName)) {
										id = parser.text();
									} else if ("_routing".equals(currentFieldName)
											|| "routing".equals(currentFieldName)) {
										routing = parser.text();
									}
								} else if (token == XContentParser.Token.START_ARRAY) {
									if ("fields".equals(currentFieldName)) {
										fields = new ArrayList<String>();
										while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
											fields.add(parser.text());
										}
									}
								}
							}
							String[] aFields;
							if (fields != null) {
								aFields = fields.toArray(new String[fields.size()]);
							} else {
								aFields = defaultFields;
							}
							add(new Item(index, type, id).routing(routing).fields(aFields));
						}
					} else if ("ids".equals(currentFieldName)) {
						while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
							if (!token.isValue()) {
								throw new RestartIllegalArgumentException(
										"ids array element should only contain ids");
							}
							add(new Item(defaultIndex, defaultType, parser.text()).fields(defaultFields));
						}
					}
				}
			}
		} finally {
			parser.close();
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		if (in.readBoolean()) {
			preference = in.readUTF();
		}
		refresh = in.readBoolean();
		byte realtime = in.readByte();
		if (realtime == 0) {
			this.realtime = false;
		} else if (realtime == 1) {
			this.realtime = true;
		}

		int size = in.readVInt();
		items = new ArrayList<Item>(size);
		for (int i = 0; i < size; i++) {
			items.add(Item.readItem(in));
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		if (preference == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(preference);
		}
		out.writeBoolean(refresh);
		if (realtime == null) {
			out.writeByte((byte) -1);
		} else if (realtime == false) {
			out.writeByte((byte) 0);
		} else {
			out.writeByte((byte) 1);
		}

		out.writeVInt(items.size());
		for (Item item : items) {
			item.writeTo(out);
		}
	}
}
