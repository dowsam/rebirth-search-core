/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CreateIndexRequest.java 2012-7-6 14:29:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.create;

import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentType;
import cn.com.rebirth.search.core.RestartGenerationException;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest;

/**
 * The Class CreateIndexRequest.
 *
 * @author l.xue.nong
 */
public class CreateIndexRequest extends MasterNodeOperationRequest {

	/** The cause. */
	private String cause = "";

	/** The index. */
	private String index;

	/** The settings. */
	private Settings settings = ImmutableSettings.Builder.EMPTY_SETTINGS;

	/** The mappings. */
	private Map<String, String> mappings = newHashMap();

	/** The timeout. */
	private TimeValue timeout = new TimeValue(10, TimeUnit.SECONDS);

	/**
	 * Instantiates a new creates the index request.
	 */
	CreateIndexRequest() {
	}

	/**
	 * Instantiates a new creates the index request.
	 *
	 * @param index the index
	 */
	public CreateIndexRequest(String index) {
		this(index, ImmutableSettings.Builder.EMPTY_SETTINGS);
	}

	/**
	 * Instantiates a new creates the index request.
	 *
	 * @param index the index
	 * @param settings the settings
	 */
	public CreateIndexRequest(String index, Settings settings) {
		this.index = index;
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = null;
		if (index == null) {
			validationException = ValidateActions.addValidationError("index is missing", validationException);
		}
		return validationException;
	}

	/**
	 * Index.
	 *
	 * @return the string
	 */
	String index() {
		return index;
	}

	/**
	 * Index.
	 *
	 * @param index the index
	 * @return the creates the index request
	 */
	public CreateIndexRequest index(String index) {
		this.index = index;
		return this;
	}

	/**
	 * Settings.
	 *
	 * @return the settings
	 */
	Settings settings() {
		return settings;
	}

	/**
	 * Cause.
	 *
	 * @return the string
	 */
	String cause() {
		return cause;
	}

	/**
	 * Settings.
	 *
	 * @param settings the settings
	 * @return the creates the index request
	 */
	public CreateIndexRequest settings(Settings settings) {
		this.settings = settings;
		return this;
	}

	/**
	 * Settings.
	 *
	 * @param settings the settings
	 * @return the creates the index request
	 */
	public CreateIndexRequest settings(Settings.Builder settings) {
		this.settings = settings.build();
		return this;
	}

	/**
	 * Settings.
	 *
	 * @param source the source
	 * @return the creates the index request
	 */
	public CreateIndexRequest settings(String source) {
		this.settings = ImmutableSettings.settingsBuilder().loadFromSource(source).build();
		return this;
	}

	/**
	 * Settings.
	 *
	 * @param builder the builder
	 * @return the creates the index request
	 */
	public CreateIndexRequest settings(XContentBuilder builder) {
		try {
			settings(builder.string());
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate json settings from builder", e);
		}
		return this;
	}

	/**
	 * Settings.
	 *
	 * @param source the source
	 * @return the creates the index request
	 */
	public CreateIndexRequest settings(Map source) {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON);
			builder.map(source);
			settings(builder.string());
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + source + "]", e);
		}
		return this;
	}

	/**
	 * Mapping.
	 *
	 * @param type the type
	 * @param source the source
	 * @return the creates the index request
	 */
	public CreateIndexRequest mapping(String type, String source) {
		mappings.put(type, source);
		return this;
	}

	/**
	 * Cause.
	 *
	 * @param cause the cause
	 * @return the creates the index request
	 */
	public CreateIndexRequest cause(String cause) {
		this.cause = cause;
		return this;
	}

	/**
	 * Mapping.
	 *
	 * @param type the type
	 * @param source the source
	 * @return the creates the index request
	 */
	public CreateIndexRequest mapping(String type, XContentBuilder source) {
		try {
			mappings.put(type, source.string());
		} catch (IOException e) {
			throw new RebirthIllegalArgumentException("Failed to build json for mapping request", e);
		}
		return this;
	}

	/**
	 * Mapping.
	 *
	 * @param type the type
	 * @param source the source
	 * @return the creates the index request
	 */
	public CreateIndexRequest mapping(String type, Map source) {

		if (source.size() != 1 || !source.containsKey(type)) {
			source = MapBuilder.<String, Object> newMapBuilder().put(type, source).map();
		}
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON);
			builder.map(source);
			return mapping(type, builder.string());
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + source + "]", e);
		}
	}

	/**
	 * Mappings.
	 *
	 * @return the map
	 */
	Map<String, String> mappings() {
		return this.mappings;
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
	 * @return the creates the index request
	 */
	public CreateIndexRequest timeout(TimeValue timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the creates the index request
	 */
	public CreateIndexRequest timeout(String timeout) {
		return timeout(TimeValue.parseTimeValue(timeout, null));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest#masterNodeTimeout(cn.com.rebirth.commons.unit.TimeValue)
	 */
	@Override
	public CreateIndexRequest masterNodeTimeout(TimeValue timeout) {
		this.masterNodeTimeout = timeout;
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		cause = in.readUTF();
		index = in.readUTF();
		settings = ImmutableSettings.readSettingsFromStream(in);
		timeout = TimeValue.readTimeValue(in);
		int size = in.readVInt();
		for (int i = 0; i < size; i++) {
			mappings.put(in.readUTF(), in.readUTF());
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeUTF(cause);
		out.writeUTF(index);
		ImmutableSettings.writeSettingsToStream(settings, out);
		timeout.writeTo(out);
		out.writeVInt(mappings.size());
		for (Map.Entry<String, String> entry : mappings.entrySet()) {
			out.writeUTF(entry.getKey());
			out.writeUTF(entry.getValue());
		}
	}
}