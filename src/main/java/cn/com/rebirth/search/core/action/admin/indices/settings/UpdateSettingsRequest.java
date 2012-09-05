/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core UpdateSettingsRequest.java 2012-7-6 14:30:14 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.settings;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.settings.ImmutableSettings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentFactory;
import cn.com.rebirth.commons.xcontent.XContentType;
import cn.com.rebirth.search.core.RestartGenerationException;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest;

/**
 * The Class UpdateSettingsRequest.
 *
 * @author l.xue.nong
 */
public class UpdateSettingsRequest extends MasterNodeOperationRequest {

	/** The indices. */
	private String[] indices;

	/** The settings. */
	private Settings settings = ImmutableSettings.Builder.EMPTY_SETTINGS;

	/**
	 * Instantiates a new update settings request.
	 */
	UpdateSettingsRequest() {
	}

	/**
	 * Instantiates a new update settings request.
	 *
	 * @param indices the indices
	 */
	public UpdateSettingsRequest(String... indices) {
		this.indices = indices;
	}

	/**
	 * Instantiates a new update settings request.
	 *
	 * @param settings the settings
	 * @param indices the indices
	 */
	public UpdateSettingsRequest(Settings settings, String... indices) {
		this.indices = indices;
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = null;
		if (settings.getAsMap().isEmpty()) {
			validationException = ValidateActions.addValidationError("no settings to update", validationException);
		}
		return validationException;
	}

	/**
	 * Indices.
	 *
	 * @return the string[]
	 */
	String[] indices() {
		return indices;
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
	 * Indices.
	 *
	 * @param indices the indices
	 * @return the update settings request
	 */
	public UpdateSettingsRequest indices(String... indices) {
		this.indices = indices;
		return this;
	}

	/**
	 * Settings.
	 *
	 * @param settings the settings
	 * @return the update settings request
	 */
	public UpdateSettingsRequest settings(Settings settings) {
		this.settings = settings;
		return this;
	}

	/**
	 * Settings.
	 *
	 * @param settings the settings
	 * @return the update settings request
	 */
	public UpdateSettingsRequest settings(Settings.Builder settings) {
		this.settings = settings.build();
		return this;
	}

	/**
	 * Settings.
	 *
	 * @param source the source
	 * @return the update settings request
	 */
	public UpdateSettingsRequest settings(String source) {
		this.settings = ImmutableSettings.settingsBuilder().loadFromSource(source).build();
		return this;
	}

	/**
	 * Settings.
	 *
	 * @param source the source
	 * @return the update settings request
	 */
	public UpdateSettingsRequest settings(Map source) {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON);
			builder.map(source);
			settings(builder.string());
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + source + "]", e);
		}
		return this;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		indices = new String[in.readVInt()];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = in.readUTF();
		}
		settings = ImmutableSettings.readSettingsFromStream(in);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		if (indices == null) {
			out.writeVInt(0);
		} else {
			out.writeVInt(indices.length);
			for (String index : indices) {
				out.writeUTF(index);
			}
		}
		ImmutableSettings.writeSettingsToStream(settings, out);
	}
}