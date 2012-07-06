/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MetaDataIndexTemplateService.java 2012-3-29 15:02:04 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.metadata;

import java.util.Map;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.component.AbstractComponent;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.ProcessedClusterStateUpdateTask;
import cn.com.rebirth.search.core.indices.IndexTemplateAlreadyExistsException;
import cn.com.rebirth.search.core.indices.IndexTemplateMissingException;
import cn.com.rebirth.search.core.indices.InvalidIndexTemplateException;

import com.google.common.collect.Maps;


/**
 * The Class MetaDataIndexTemplateService.
 *
 * @author l.xue.nong
 */
public class MetaDataIndexTemplateService extends AbstractComponent {

	/** The cluster service. */
	private final ClusterService clusterService;

	/**
	 * Instantiates a new meta data index template service.
	 *
	 * @param settings the settings
	 * @param clusterService the cluster service
	 */
	@Inject
	public MetaDataIndexTemplateService(Settings settings, ClusterService clusterService) {
		super(settings);
		this.clusterService = clusterService;
	}

	/**
	 * Removes the template.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	public void removeTemplate(final RemoveRequest request, final RemoveListener listener) {
		clusterService.submitStateUpdateTask("remove-index-template [" + request.name + "]",
				new ProcessedClusterStateUpdateTask() {
					@Override
					public ClusterState execute(ClusterState currentState) {
						if (!currentState.metaData().templates().containsKey(request.name)) {
							listener.onFailure(new IndexTemplateMissingException(request.name));
							return currentState;
						}
						MetaData.Builder metaData = MetaData.builder().metaData(currentState.metaData())
								.removeTemplate(request.name);

						return ClusterState.builder().state(currentState).metaData(metaData).build();
					}

					@Override
					public void clusterStateProcessed(ClusterState clusterState) {
						listener.onResponse(new RemoveResponse(true));
					}
				});
	}

	/**
	 * Put template.
	 *
	 * @param request the request
	 * @param listener the listener
	 */
	public void putTemplate(final PutRequest request, final PutListener listener) {
		ImmutableSettings.Builder updatedSettingsBuilder = ImmutableSettings.settingsBuilder();
		for (Map.Entry<String, String> entry : request.settings.getAsMap().entrySet()) {
			if (!entry.getKey().startsWith("index.")) {
				updatedSettingsBuilder.put("index." + entry.getKey(), entry.getValue());
			} else {
				updatedSettingsBuilder.put(entry.getKey(), entry.getValue());
			}
		}
		request.settings(updatedSettingsBuilder.build());

		if (request.name == null) {
			listener.onFailure(new RestartIllegalArgumentException("index_template must provide a name"));
			return;
		}
		if (request.template == null) {
			listener.onFailure(new RestartIllegalArgumentException("index_template must provide a template"));
			return;
		}

		try {
			validate(request);
		} catch (Exception e) {
			listener.onFailure(e);
			return;
		}

		IndexTemplateMetaData.Builder templateBuilder;
		try {
			templateBuilder = IndexTemplateMetaData.builder(request.name);
			templateBuilder.order(request.order);
			templateBuilder.template(request.template);
			templateBuilder.settings(request.settings);
			for (Map.Entry<String, String> entry : request.mappings.entrySet()) {
				templateBuilder.putMapping(entry.getKey(), entry.getValue());
			}
		} catch (Exception e) {
			listener.onFailure(e);
			return;
		}
		final IndexTemplateMetaData template = templateBuilder.build();

		clusterService.submitStateUpdateTask("create-index-template [" + request.name + "], cause [" + request.cause
				+ "]", new ProcessedClusterStateUpdateTask() {
			@Override
			public ClusterState execute(ClusterState currentState) {
				if (request.create && currentState.metaData().templates().containsKey(request.name)) {
					listener.onFailure(new IndexTemplateAlreadyExistsException(request.name));
					return currentState;
				}
				MetaData.Builder builder = MetaData.builder().metaData(currentState.metaData()).put(template);

				return ClusterState.builder().state(currentState).metaData(builder).build();
			}

			@Override
			public void clusterStateProcessed(ClusterState clusterState) {
				listener.onResponse(new PutResponse(true, template));
			}
		});
	}

	/**
	 * Validate.
	 *
	 * @param request the request
	 * @throws SumMallSearchException the sum mall search exception
	 */
	private void validate(PutRequest request) throws RestartException {
		if (request.name.contains(" ")) {
			throw new InvalidIndexTemplateException(request.name, "name must not contain a space");
		}
		if (request.name.contains(",")) {
			throw new InvalidIndexTemplateException(request.name, "name must not contain a ','");
		}
		if (request.name.contains("#")) {
			throw new InvalidIndexTemplateException(request.name, "name must not contain a '#'");
		}
		if (request.name.startsWith("_")) {
			throw new InvalidIndexTemplateException(request.name, "name must not start with '_'");
		}
		if (!request.name.toLowerCase().equals(request.name)) {
			throw new InvalidIndexTemplateException(request.name, "name must be lower cased");
		}
		if (request.template.contains(" ")) {
			throw new InvalidIndexTemplateException(request.name, "template must not contain a space");
		}
		if (request.template.contains(",")) {
			throw new InvalidIndexTemplateException(request.name, "template must not contain a ','");
		}
		if (request.template.contains("#")) {
			throw new InvalidIndexTemplateException(request.name, "template must not contain a '#'");
		}
		if (request.template.startsWith("_")) {
			throw new InvalidIndexTemplateException(request.name, "template must not start with '_'");
		}
		if (!Strings.validFileNameExcludingAstrix(request.template)) {
			throw new InvalidIndexTemplateException(request.name,
					"template must not container the following characters " + Strings.INVALID_FILENAME_CHARS);
		}
	}

	/**
	 * The listener interface for receiving put events.
	 * The class that is interested in processing a put
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addPutListener<code> method. When
	 * the put event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see PutEvent
	 */
	public static interface PutListener {

		/**
		 * On response.
		 *
		 * @param response the response
		 */
		void onResponse(PutResponse response);

		/**
		 * On failure.
		 *
		 * @param t the t
		 */
		void onFailure(Throwable t);
	}

	/**
	 * The Class PutRequest.
	 *
	 * @author l.xue.nong
	 */
	public static class PutRequest {
		
		/** The name. */
		final String name;
		
		/** The cause. */
		final String cause;
		
		/** The create. */
		boolean create;
		
		/** The order. */
		int order;
		
		/** The template. */
		String template;
		
		/** The settings. */
		Settings settings = ImmutableSettings.Builder.EMPTY_SETTINGS;
		
		/** The mappings. */
		Map<String, String> mappings = Maps.newHashMap();

		/**
		 * Instantiates a new put request.
		 *
		 * @param cause the cause
		 * @param name the name
		 */
		public PutRequest(String cause, String name) {
			this.cause = cause;
			this.name = name;
		}

		/**
		 * Order.
		 *
		 * @param order the order
		 * @return the put request
		 */
		public PutRequest order(int order) {
			this.order = order;
			return this;
		}

		/**
		 * Template.
		 *
		 * @param template the template
		 * @return the put request
		 */
		public PutRequest template(String template) {
			this.template = template;
			return this;
		}

		/**
		 * Creates the.
		 *
		 * @param create the create
		 * @return the put request
		 */
		public PutRequest create(boolean create) {
			this.create = create;
			return this;
		}

		/**
		 * Settings.
		 *
		 * @param settings the settings
		 * @return the put request
		 */
		public PutRequest settings(Settings settings) {
			this.settings = settings;
			return this;
		}

		/**
		 * Mappings.
		 *
		 * @param mappings the mappings
		 * @return the put request
		 */
		public PutRequest mappings(Map<String, String> mappings) {
			this.mappings.putAll(mappings);
			return this;
		}

		/**
		 * Put mapping.
		 *
		 * @param mappingType the mapping type
		 * @param mappingSource the mapping source
		 * @return the put request
		 */
		public PutRequest putMapping(String mappingType, String mappingSource) {
			mappings.put(mappingType, mappingSource);
			return this;
		}
	}

	/**
	 * The Class PutResponse.
	 *
	 * @author l.xue.nong
	 */
	public static class PutResponse {
		
		/** The acknowledged. */
		private final boolean acknowledged;
		
		/** The template. */
		private final IndexTemplateMetaData template;

		/**
		 * Instantiates a new put response.
		 *
		 * @param acknowledged the acknowledged
		 * @param template the template
		 */
		public PutResponse(boolean acknowledged, IndexTemplateMetaData template) {
			this.acknowledged = acknowledged;
			this.template = template;
		}

		/**
		 * Acknowledged.
		 *
		 * @return true, if successful
		 */
		public boolean acknowledged() {
			return acknowledged;
		}

		/**
		 * Template.
		 *
		 * @return the index template meta data
		 */
		public IndexTemplateMetaData template() {
			return template;
		}
	}

	/**
	 * The Class RemoveRequest.
	 *
	 * @author l.xue.nong
	 */
	public static class RemoveRequest {
		
		/** The name. */
		final String name;

		/**
		 * Instantiates a new removes the request.
		 *
		 * @param name the name
		 */
		public RemoveRequest(String name) {
			this.name = name;
		}
	}

	/**
	 * The Class RemoveResponse.
	 *
	 * @author l.xue.nong
	 */
	public static class RemoveResponse {
		
		/** The acknowledged. */
		private final boolean acknowledged;

		/**
		 * Instantiates a new removes the response.
		 *
		 * @param acknowledged the acknowledged
		 */
		public RemoveResponse(boolean acknowledged) {
			this.acknowledged = acknowledged;
		}

		/**
		 * Acknowledged.
		 *
		 * @return true, if successful
		 */
		public boolean acknowledged() {
			return acknowledged;
		}
	}

	/**
	 * The listener interface for receiving remove events.
	 * The class that is interested in processing a remove
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addRemoveListener<code> method. When
	 * the remove event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see RemoveEvent
	 */
	public static interface RemoveListener {

		/**
		 * On response.
		 *
		 * @param response the response
		 */
		void onResponse(RemoveResponse response);

		/**
		 * On failure.
		 *
		 * @param t the t
		 */
		void onFailure(Throwable t);
	}
}
