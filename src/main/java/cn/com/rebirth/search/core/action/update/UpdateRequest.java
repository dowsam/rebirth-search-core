/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core UpdateRequest.java 2012-7-6 14:30:35 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.update;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.WriteConsistencyLevel;
import cn.com.rebirth.search.core.action.support.replication.ReplicationType;
import cn.com.rebirth.search.core.action.support.single.instance.InstanceShardOperationRequest;

import com.google.common.collect.Maps;

/**
 * The Class UpdateRequest.
 *
 * @author l.xue.nong
 */
public class UpdateRequest extends InstanceShardOperationRequest {

	/** The type. */
	private String type;

	/** The id. */
	private String id;

	/** The routing. */
	@Nullable
	private String routing;

	/** The script. */
	String script;

	/** The script lang. */
	@Nullable
	String scriptLang;

	/** The script params. */
	@Nullable
	Map<String, Object> scriptParams;

	/** The retry on conflict. */
	int retryOnConflict = 0;

	/** The percolate. */
	private String percolate;

	/** The refresh. */
	private boolean refresh = false;

	/** The replication type. */
	private ReplicationType replicationType = ReplicationType.DEFAULT;

	/** The consistency level. */
	private WriteConsistencyLevel consistencyLevel = WriteConsistencyLevel.DEFAULT;

	/**
	 * Instantiates a new update request.
	 */
	UpdateRequest() {

	}

	/**
	 * Instantiates a new update request.
	 *
	 * @param index the index
	 * @param type the type
	 * @param id the id
	 */
	public UpdateRequest(String index, String type, String id) {
		this.index = index;
		this.type = type;
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.instance.InstanceShardOperationRequest#validate()
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
		if (script == null) {
			validationException = ValidateActions.addValidationError("script is missing", validationException);
		}
		return validationException;
	}

	/**
	 * Index.
	 *
	 * @param index the index
	 * @return the update request
	 */
	public UpdateRequest index(String index) {
		this.index = index;
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
	 * Type.
	 *
	 * @param type the type
	 * @return the update request
	 */
	public UpdateRequest type(String type) {
		this.type = type;
		return this;
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
	 * Id.
	 *
	 * @param id the id
	 * @return the update request
	 */
	public UpdateRequest id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Routing.
	 *
	 * @param routing the routing
	 * @return the update request
	 */
	public UpdateRequest routing(String routing) {
		if (routing != null && routing.length() == 0) {
			this.routing = null;
		} else {
			this.routing = routing;
		}
		return this;
	}

	/**
	 * Parent.
	 *
	 * @param parent the parent
	 * @return the update request
	 */
	public UpdateRequest parent(String parent) {
		if (routing == null) {
			routing = parent;
		}
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
	 * Shard id.
	 *
	 * @return the int
	 */
	int shardId() {
		return this.shardId;
	}

	/**
	 * Script.
	 *
	 * @param script the script
	 * @return the update request
	 */
	public UpdateRequest script(String script) {
		this.script = script;
		return this;
	}

	/**
	 * Script lang.
	 *
	 * @param scriptLang the script lang
	 * @return the update request
	 */
	public UpdateRequest scriptLang(String scriptLang) {
		this.scriptLang = scriptLang;
		return this;
	}

	/**
	 * Adds the script param.
	 *
	 * @param name the name
	 * @param value the value
	 * @return the update request
	 */
	public UpdateRequest addScriptParam(String name, Object value) {
		if (scriptParams == null) {
			scriptParams = Maps.newHashMap();
		}
		scriptParams.put(name, value);
		return this;
	}

	/**
	 * Script params.
	 *
	 * @param scriptParams the script params
	 * @return the update request
	 */
	public UpdateRequest scriptParams(Map<String, Object> scriptParams) {
		if (this.scriptParams == null) {
			this.scriptParams = scriptParams;
		} else {
			this.scriptParams.putAll(scriptParams);
		}
		return this;
	}

	/**
	 * Script.
	 *
	 * @param script the script
	 * @param scriptParams the script params
	 * @return the update request
	 */
	public UpdateRequest script(String script, @Nullable Map<String, Object> scriptParams) {
		this.script = script;
		if (this.scriptParams != null) {
			this.scriptParams.putAll(scriptParams);
		} else {
			this.scriptParams = scriptParams;
		}
		return this;
	}

	/**
	 * Script.
	 *
	 * @param script the script
	 * @param scriptLang the script lang
	 * @param scriptParams the script params
	 * @return the update request
	 */
	public UpdateRequest script(String script, @Nullable String scriptLang, @Nullable Map<String, Object> scriptParams) {
		this.script = script;
		this.scriptLang = scriptLang;
		if (this.scriptParams != null) {
			this.scriptParams.putAll(scriptParams);
		} else {
			this.scriptParams = scriptParams;
		}
		return this;
	}

	/**
	 * Retry on conflict.
	 *
	 * @param retryOnConflict the retry on conflict
	 * @return the update request
	 */
	public UpdateRequest retryOnConflict(int retryOnConflict) {
		this.retryOnConflict = retryOnConflict;
		return this;
	}

	/**
	 * Retry on conflict.
	 *
	 * @return the int
	 */
	public int retryOnConflict() {
		return this.retryOnConflict;
	}

	/**
	 * Percolate.
	 *
	 * @param percolate the percolate
	 * @return the update request
	 */
	public UpdateRequest percolate(String percolate) {
		this.percolate = percolate;
		return this;
	}

	/**
	 * Percolate.
	 *
	 * @return the string
	 */
	public String percolate() {
		return this.percolate;
	}

	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the update request
	 */
	public UpdateRequest timeout(TimeValue timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the update request
	 */
	public UpdateRequest timeout(String timeout) {
		return timeout(TimeValue.parseTimeValue(timeout, null));
	}

	/**
	 * Refresh.
	 *
	 * @param refresh the refresh
	 * @return the update request
	 */
	public UpdateRequest refresh(boolean refresh) {
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
	 * Replication type.
	 *
	 * @return the replication type
	 */
	public ReplicationType replicationType() {
		return this.replicationType;
	}

	/**
	 * Replication type.
	 *
	 * @param replicationType the replication type
	 * @return the update request
	 */
	public UpdateRequest replicationType(ReplicationType replicationType) {
		this.replicationType = replicationType;
		return this;
	}

	/**
	 * Consistency level.
	 *
	 * @return the write consistency level
	 */
	public WriteConsistencyLevel consistencyLevel() {
		return this.consistencyLevel;
	}

	/**
	 * Consistency level.
	 *
	 * @param consistencyLevel the consistency level
	 * @return the update request
	 */
	public UpdateRequest consistencyLevel(WriteConsistencyLevel consistencyLevel) {
		this.consistencyLevel = consistencyLevel;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.instance.InstanceShardOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		replicationType = ReplicationType.fromId(in.readByte());
		consistencyLevel = WriteConsistencyLevel.fromId(in.readByte());
		type = in.readUTF();
		id = in.readUTF();
		if (in.readBoolean()) {
			routing = in.readUTF();
		}
		script = in.readUTF();
		if (in.readBoolean()) {
			scriptLang = in.readUTF();
		}
		scriptParams = in.readMap();
		retryOnConflict = in.readVInt();
		if (in.readBoolean()) {
			percolate = in.readUTF();
		}
		refresh = in.readBoolean();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.instance.InstanceShardOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeByte(replicationType.id());
		out.writeByte(consistencyLevel.id());
		out.writeUTF(type);
		out.writeUTF(id);
		if (routing == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(routing);
		}
		out.writeUTF(script);
		if (scriptLang == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(scriptLang);
		}
		out.writeMap(scriptParams);
		out.writeVInt(retryOnConflict);
		if (percolate == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(percolate);
		}
		out.writeBoolean(refresh);
	}
}
