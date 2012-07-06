/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GetRequest.java 2012-7-6 14:30:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.get;

import java.io.IOException;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.commons.Required;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.support.single.shard.SingleShardOperationRequest;

/**
 * The Class GetRequest.
 *
 * @author l.xue.nong
 */
public class GetRequest extends SingleShardOperationRequest {

	/** The type. */
	protected String type;

	/** The id. */
	protected String id;

	/** The routing. */
	protected String routing;

	/** The preference. */
	protected String preference;

	/** The fields. */
	private String[] fields;

	/** The refresh. */
	private boolean refresh = false;

	/** The realtime. */
	Boolean realtime;

	/**
	 * Instantiates a new gets the request.
	 */
	GetRequest() {
		type = "_all";
	}

	/**
	 * Instantiates a new gets the request.
	 *
	 * @param index the index
	 */
	public GetRequest(String index) {
		super(index);
		this.type = "_all";
	}

	/**
	 * Instantiates a new gets the request.
	 *
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 */
	public GetRequest(String index, String type, String id) {
		super(index);
		this.type = type;
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.shard.SingleShardOperationRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = super.validate();
		if (type == null) {
			validationException = ValidateActions.addValidationError("type is missing", validationException);
		}
		if (id == null) {
			validationException = ValidateActions.addValidationError("id is missing", validationException);
		}
		return validationException;
	}

	/**
	 * Index.
	 *
	 * @param index the index
	 * @return the gets the request
	 */
	@Required
	public GetRequest index(String index) {
		this.index = index;
		return this;
	}

	/**
	 * Type.
	 *
	 * @param type the type
	 * @return the gets the request
	 */
	public GetRequest type(@Nullable String type) {
		if (type == null) {
			type = "_all";
		}
		this.type = type;
		return this;
	}

	/**
	 * Id.
	 *
	 * @param id the id
	 * @return the gets the request
	 */
	@Required
	public GetRequest id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Routing.
	 *
	 * @param routing the routing
	 * @return the gets the request
	 */
	public GetRequest routing(String routing) {
		this.routing = routing;
		return this;
	}

	/**
	 * Preference.
	 *
	 * @param preference the preference
	 * @return the gets the request
	 */
	public GetRequest preference(String preference) {
		this.preference = preference;
		return this;
	}

	/**
	 * Type.
	 *
	 * @return the string
	 */
	public String type() {
		return type;
	}

	/**
	 * Id.
	 *
	 * @return the string
	 */
	public String id() {
		return id;
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
	 * Preference.
	 *
	 * @return the string
	 */
	public String preference() {
		return this.preference;
	}

	/**
	 * Fields.
	 *
	 * @param fields the fields
	 * @return the gets the request
	 */
	public GetRequest fields(String... fields) {
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
	 * Refresh.
	 *
	 * @param refresh the refresh
	 * @return the gets the request
	 */
	public GetRequest refresh(boolean refresh) {
		this.refresh = refresh;
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
	 * @return the gets the request
	 */
	public GetRequest realtime(Boolean realtime) {
		this.realtime = realtime;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.shard.SingleShardOperationRequest#listenerThreaded(boolean)
	 */
	@Override
	public GetRequest listenerThreaded(boolean threadedListener) {
		super.listenerThreaded(threadedListener);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.shard.SingleShardOperationRequest#operationThreaded(boolean)
	 */
	@Override
	public GetRequest operationThreaded(boolean threadedOperation) {
		super.operationThreaded(threadedOperation);
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.shard.SingleShardOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);

		type = in.readUTF();
		id = in.readUTF();
		if (in.readBoolean()) {
			routing = in.readUTF();
		}
		if (in.readBoolean()) {
			preference = in.readUTF();
		}

		refresh = in.readBoolean();
		int size = in.readInt();
		if (size >= 0) {
			fields = new String[size];
			for (int i = 0; i < size; i++) {
				fields[i] = in.readUTF();
			}
		}
		byte realtime = in.readByte();
		if (realtime == 0) {
			this.realtime = false;
		} else if (realtime == 1) {
			this.realtime = true;
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.shard.SingleShardOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);

		out.writeUTF(type);
		out.writeUTF(id);
		if (routing == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(routing);
		}
		if (preference == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(preference);
		}

		out.writeBoolean(refresh);
		if (fields == null) {
			out.writeInt(-1);
		} else {
			out.writeInt(fields.length);
			for (String field : fields) {
				out.writeUTF(field);
			}
		}
		if (realtime == null) {
			out.writeByte((byte) -1);
		} else if (realtime == false) {
			out.writeByte((byte) 0);
		} else {
			out.writeByte((byte) 1);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + index + "][" + type + "][" + id + "]: routing [" + routing + "]";
	}
}
