/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClusterUpdateSettingsRequest.java 2012-7-6 14:29:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.cluster.settings;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentType;
import cn.com.rebirth.search.core.RestartGenerationException;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest;

/**
 * The Class ClusterUpdateSettingsRequest.
 *
 * @author l.xue.nong
 */
public class ClusterUpdateSettingsRequest extends MasterNodeOperationRequest {

	/** The transient settings. */
	private Settings transientSettings = ImmutableSettings.Builder.EMPTY_SETTINGS;

	/** The persistent settings. */
	private Settings persistentSettings = ImmutableSettings.Builder.EMPTY_SETTINGS;

	/**
	 * Instantiates a new cluster update settings request.
	 */
	public ClusterUpdateSettingsRequest() {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = null;
		if (transientSettings.getAsMap().isEmpty() && persistentSettings.getAsMap().isEmpty()) {
			validationException = ValidateActions.addValidationError("no settings to update", validationException);
		}
		return validationException;
	}

	/**
	 * Transient settings.
	 *
	 * @return the settings
	 */
	Settings transientSettings() {
		return transientSettings;
	}

	/**
	 * Persistent settings.
	 *
	 * @return the settings
	 */
	Settings persistentSettings() {
		return persistentSettings;
	}

	/**
	 * Transient settings.
	 *
	 * @param settings the settings
	 * @return the cluster update settings request
	 */
	public ClusterUpdateSettingsRequest transientSettings(Settings settings) {
		this.transientSettings = settings;
		return this;
	}

	/**
	 * Transient settings.
	 *
	 * @param settings the settings
	 * @return the cluster update settings request
	 */
	public ClusterUpdateSettingsRequest transientSettings(Settings.Builder settings) {
		this.transientSettings = settings.build();
		return this;
	}

	/**
	 * Transient settings.
	 *
	 * @param source the source
	 * @return the cluster update settings request
	 */
	public ClusterUpdateSettingsRequest transientSettings(String source) {
		this.transientSettings = ImmutableSettings.settingsBuilder().loadFromSource(source).build();
		return this;
	}

	/**
	 * Transient settings.
	 *
	 * @param source the source
	 * @return the cluster update settings request
	 */
	public ClusterUpdateSettingsRequest transientSettings(Map source) {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON);
			builder.map(source);
			transientSettings(builder.string());
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + source + "]", e);
		}
		return this;
	}

	/**
	 * Persistent settings.
	 *
	 * @param settings the settings
	 * @return the cluster update settings request
	 */
	public ClusterUpdateSettingsRequest persistentSettings(Settings settings) {
		this.persistentSettings = settings;
		return this;
	}

	/**
	 * Persistent settings.
	 *
	 * @param settings the settings
	 * @return the cluster update settings request
	 */
	public ClusterUpdateSettingsRequest persistentSettings(Settings.Builder settings) {
		this.persistentSettings = settings.build();
		return this;
	}

	/**
	 * Persistent settings.
	 *
	 * @param source the source
	 * @return the cluster update settings request
	 */
	public ClusterUpdateSettingsRequest persistentSettings(String source) {
		this.persistentSettings = ImmutableSettings.settingsBuilder().loadFromSource(source).build();
		return this;
	}

	/**
	 * Persistent settings.
	 *
	 * @param source the source
	 * @return the cluster update settings request
	 */
	public ClusterUpdateSettingsRequest persistentSettings(Map source) {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON);
			builder.map(source);
			persistentSettings(builder.string());
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
		transientSettings = ImmutableSettings.readSettingsFromStream(in);
		persistentSettings = ImmutableSettings.readSettingsFromStream(in);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		ImmutableSettings.writeSettingsToStream(transientSettings, out);
		ImmutableSettings.writeSettingsToStream(persistentSettings, out);
	}
}