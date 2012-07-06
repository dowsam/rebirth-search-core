/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PutIndexTemplateRequest.java 2012-7-6 14:29:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.template.put;

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
 * The Class PutIndexTemplateRequest.
 *
 * @author l.xue.nong
 */
public class PutIndexTemplateRequest extends MasterNodeOperationRequest {

	/** The name. */
	private String name;

	/** The cause. */
	private String cause = "";

	/** The template. */
	private String template;

	/** The order. */
	private int order;

	/** The create. */
	private boolean create;

	/** The settings. */
	private Settings settings = ImmutableSettings.Builder.EMPTY_SETTINGS;

	/** The mappings. */
	private Map<String, String> mappings = newHashMap();

	/** The timeout. */
	private TimeValue timeout = new TimeValue(10, TimeUnit.SECONDS);

	/**
	 * Instantiates a new put index template request.
	 */
	PutIndexTemplateRequest() {
	}

	/**
	 * Instantiates a new put index template request.
	 *
	 * @param name the name
	 */
	public PutIndexTemplateRequest(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = null;
		if (name == null) {
			validationException = ValidateActions.addValidationError("name is missing", validationException);
		}
		if (template == null) {
			validationException = ValidateActions.addValidationError("template is missing", validationException);
		}
		return validationException;
	}

	/**
	 * Name.
	 *
	 * @param name the name
	 * @return the put index template request
	 */
	public PutIndexTemplateRequest name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Name.
	 *
	 * @return the string
	 */
	public String name() {
		return this.name;
	}

	/**
	 * Template.
	 *
	 * @param template the template
	 * @return the put index template request
	 */
	public PutIndexTemplateRequest template(String template) {
		this.template = template;
		return this;
	}

	/**
	 * Template.
	 *
	 * @return the string
	 */
	public String template() {
		return this.template;
	}

	/**
	 * Order.
	 *
	 * @param order the order
	 * @return the put index template request
	 */
	public PutIndexTemplateRequest order(int order) {
		this.order = order;
		return this;
	}

	/**
	 * Order.
	 *
	 * @return the int
	 */
	public int order() {
		return this.order;
	}

	/**
	 * Creates the.
	 *
	 * @param create the create
	 * @return the put index template request
	 */
	public PutIndexTemplateRequest create(boolean create) {
		this.create = create;
		return this;
	}

	/**
	 * Creates the.
	 *
	 * @return true, if successful
	 */
	public boolean create() {
		return create;
	}

	/**
	 * Settings.
	 *
	 * @param settings the settings
	 * @return the put index template request
	 */
	public PutIndexTemplateRequest settings(Settings settings) {
		this.settings = settings;
		return this;
	}

	/**
	 * Settings.
	 *
	 * @param settings the settings
	 * @return the put index template request
	 */
	public PutIndexTemplateRequest settings(Settings.Builder settings) {
		this.settings = settings.build();
		return this;
	}

	/**
	 * Settings.
	 *
	 * @param source the source
	 * @return the put index template request
	 */
	public PutIndexTemplateRequest settings(String source) {
		this.settings = ImmutableSettings.settingsBuilder().loadFromSource(source).build();
		return this;
	}

	/**
	 * Settings.
	 *
	 * @param source the source
	 * @return the put index template request
	 */
	public PutIndexTemplateRequest settings(Map<String, Object> source) {
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
	 * Settings.
	 *
	 * @return the settings
	 */
	Settings settings() {
		return this.settings;
	}

	/**
	 * Mapping.
	 *
	 * @param type the type
	 * @param source the source
	 * @return the put index template request
	 */
	public PutIndexTemplateRequest mapping(String type, String source) {
		mappings.put(type, source);
		return this;
	}

	/**
	 * Cause.
	 *
	 * @param cause the cause
	 * @return the put index template request
	 */
	public PutIndexTemplateRequest cause(String cause) {
		this.cause = cause;
		return this;
	}

	/**
	 * Cause.
	 *
	 * @return the string
	 */
	public String cause() {
		return this.cause;
	}

	/**
	 * Mapping.
	 *
	 * @param type the type
	 * @param source the source
	 * @return the put index template request
	 */
	public PutIndexTemplateRequest mapping(String type, XContentBuilder source) {
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
	 * @return the put index template request
	 */
	public PutIndexTemplateRequest mapping(String type, Map<String, Object> source) {

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
	 * @return the put index template request
	 */
	public PutIndexTemplateRequest timeout(TimeValue timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * Timeout.
	 *
	 * @param timeout the timeout
	 * @return the put index template request
	 */
	public PutIndexTemplateRequest timeout(String timeout) {
		return timeout(TimeValue.parseTimeValue(timeout, null));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		cause = in.readUTF();
		name = in.readUTF();
		template = in.readUTF();
		order = in.readInt();
		create = in.readBoolean();
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
		out.writeUTF(name);
		out.writeUTF(template);
		out.writeInt(order);
		out.writeBoolean(create);
		ImmutableSettings.writeSettingsToStream(settings, out);
		timeout.writeTo(out);
		out.writeVInt(mappings.size());
		for (Map.Entry<String, String> entry : mappings.entrySet()) {
			out.writeUTF(entry.getKey());
			out.writeUTF(entry.getValue());
		}
	}
}