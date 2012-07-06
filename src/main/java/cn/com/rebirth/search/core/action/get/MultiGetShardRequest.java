/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MultiGetShardRequest.java 2012-7-6 14:29:01 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.get;

import gnu.trove.list.array.TIntArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.single.shard.SingleShardOperationRequest;

/**
 * The Class MultiGetShardRequest.
 *
 * @author l.xue.nong
 */
public class MultiGetShardRequest extends SingleShardOperationRequest {

	/** The shard id. */
	private int shardId;

	/** The preference. */
	private String preference;

	/** The realtime. */
	Boolean realtime;

	/** The refresh. */
	boolean refresh;

	/** The locations. */
	TIntArrayList locations;

	/** The types. */
	List<String> types;

	/** The ids. */
	List<String> ids;

	/** The fields. */
	List<String[]> fields;

	/**
	 * Instantiates a new multi get shard request.
	 */
	MultiGetShardRequest() {

	}

	/**
	 * Instantiates a new multi get shard request.
	 *
	 * @param index the index
	 * @param shardId the shard id
	 */
	MultiGetShardRequest(String index, int shardId) {
		super(index);
		this.shardId = shardId;
		locations = new TIntArrayList();
		types = new ArrayList<String>();
		ids = new ArrayList<String>();
		fields = new ArrayList<String[]>();
	}

	/**
	 * Shard id.
	 *
	 * @return the int
	 */
	public int shardId() {
		return this.shardId;
	}

	/**
	 * Preference.
	 *
	 * @param preference the preference
	 * @return the multi get shard request
	 */
	public MultiGetShardRequest preference(String preference) {
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
	 * @return the multi get shard request
	 */
	public MultiGetShardRequest realtime(Boolean realtime) {
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
	 * @return the multi get shard request
	 */
	public MultiGetShardRequest refresh(boolean refresh) {
		this.refresh = refresh;
		return this;
	}

	/**
	 * Adds the.
	 *
	 * @param location the location
	 * @param type the type
	 * @param id the id
	 * @param fields the fields
	 */
	public void add(int location, @Nullable String type, String id, String[] fields) {
		this.locations.add(location);
		this.types.add(type);
		this.ids.add(id);
		this.fields.add(fields);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.shard.SingleShardOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		int size = in.readVInt();
		locations = new TIntArrayList(size);
		types = new ArrayList<String>(size);
		ids = new ArrayList<String>(size);
		fields = new ArrayList<String[]>(size);
		for (int i = 0; i < size; i++) {
			locations.add(in.readVInt());
			if (in.readBoolean()) {
				types.add(in.readUTF());
			} else {
				types.add(null);
			}
			ids.add(in.readUTF());
			int size1 = in.readVInt();
			if (size1 > 0) {
				String[] fields = new String[size1];
				for (int j = 0; j < size1; j++) {
					fields[j] = in.readUTF();
				}
				this.fields.add(fields);
			} else {
				fields.add(null);
			}
		}

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
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.shard.SingleShardOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeVInt(types.size());
		for (int i = 0; i < types.size(); i++) {
			out.writeVInt(locations.get(i));
			if (types.get(i) == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				out.writeUTF(types.get(i));
			}
			out.writeUTF(ids.get(i));
			if (fields.get(i) == null) {
				out.writeVInt(0);
			} else {
				out.writeVInt(fields.get(i).length);
				for (String field : fields.get(i)) {
					out.writeUTF(field);
				}
			}
		}

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
	}
}