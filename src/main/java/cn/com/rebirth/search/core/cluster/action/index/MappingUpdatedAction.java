/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MappingUpdatedAction.java 2012-3-29 15:01:53 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.action.index;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import cn.com.rebirth.commons.compress.CompressedString;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.support.master.MasterNodeOperationRequest;
import cn.com.rebirth.search.core.action.support.master.TransportMasterNodeOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.metadata.MetaDataMappingService;
import cn.com.rebirth.search.core.threadpool.ThreadPool;
import cn.com.rebirth.search.core.transport.TransportService;

/**
 * The Class MappingUpdatedAction.
 *
 * @author l.xue.nong
 */
public class MappingUpdatedAction
		extends
		TransportMasterNodeOperationAction<MappingUpdatedAction.MappingUpdatedRequest, MappingUpdatedAction.MappingUpdatedResponse> {

	/** The meta data mapping service. */
	private final MetaDataMappingService metaDataMappingService;

	/**
	 * Instantiates a new mapping updated action.
	 *
	 * @param settings the settings
	 * @param transportService the transport service
	 * @param clusterService the cluster service
	 * @param threadPool the thread pool
	 * @param metaDataMappingService the meta data mapping service
	 */
	@Inject
	public MappingUpdatedAction(Settings settings, TransportService transportService, ClusterService clusterService,
			ThreadPool threadPool, MetaDataMappingService metaDataMappingService) {
		super(settings, transportService, clusterService, threadPool);
		this.metaDataMappingService = metaDataMappingService;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return "cluster/mappingUpdated";
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.GENERIC;
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#newRequest()
	 */
	@Override
	protected MappingUpdatedRequest newRequest() {
		return new MappingUpdatedRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#newResponse()
	 */
	@Override
	protected MappingUpdatedResponse newResponse() {
		return new MappingUpdatedResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.master.TransportMasterNodeOperationAction#masterOperation(cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest, cn.com.summall.search.core.cluster.ClusterState)
	 */
	@Override
	protected MappingUpdatedResponse masterOperation(MappingUpdatedRequest request, ClusterState state)
			throws RestartException {
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
		try {
			metaDataMappingService.updateMapping(request.index(), request.type(), request.mappingSource(),
					new MetaDataMappingService.Listener() {
						@Override
						public void onResponse(MetaDataMappingService.Response response) {
							latch.countDown();
						}

						@Override
						public void onFailure(Throwable t) {
							failure.set(t);
							latch.countDown();
						}
					});
		} catch (Exception e) {
			failure.set(e);
		}
		if (failure.get() != null) {
			if (failure.get() instanceof RestartException) {
				throw (RestartException) failure.get();
			} else {
				throw new RestartException("failed to update mapping", failure.get());
			}
		}
		return new MappingUpdatedResponse();
	}

	/**
	 * The Class MappingUpdatedResponse.
	 *
	 * @author l.xue.nong
	 */
	public static class MappingUpdatedResponse implements ActionResponse {

		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
		}

		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
		}
	}

	/**
	 * The Class MappingUpdatedRequest.
	 *
	 * @author l.xue.nong
	 */
	public static class MappingUpdatedRequest extends MasterNodeOperationRequest {

		/** The index. */
		private String index;

		/** The type. */
		private String type;

		/** The mapping source. */
		private CompressedString mappingSource;

		/**
		 * Instantiates a new mapping updated request.
		 */
		MappingUpdatedRequest() {
		}

		/**
		 * Instantiates a new mapping updated request.
		 *
		 * @param index the index
		 * @param type the type
		 * @param mappingSource the mapping source
		 */
		public MappingUpdatedRequest(String index, String type, CompressedString mappingSource) {
			this.index = index;
			this.type = type;
			this.mappingSource = mappingSource;
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
		 * Type.
		 *
		 * @return the string
		 */
		public String type() {
			return type;
		}

		/**
		 * Mapping source.
		 *
		 * @return the compressed string
		 */
		public CompressedString mappingSource() {
			return mappingSource;
		}

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.action.ActionRequest#validate()
		 */
		@Override
		public ActionRequestValidationException validate() {
			return null;
		}

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			super.readFrom(in);
			index = in.readUTF();
			type = in.readUTF();
			mappingSource = CompressedString.readCompressedString(in);
		}

		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.action.support.master.MasterNodeOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			super.writeTo(out);
			out.writeUTF(index);
			out.writeUTF(type);
			mappingSource.writeTo(out);
		}
	}
}