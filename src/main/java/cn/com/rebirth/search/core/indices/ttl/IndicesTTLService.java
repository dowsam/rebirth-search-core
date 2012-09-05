/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesTTLService.java 2012-7-6 14:30:08 l.xue.nong$$
 */

package cn.com.rebirth.search.core.indices.ttl;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;

import cn.com.rebirth.commons.component.AbstractLifecycleComponent;
import cn.com.rebirth.commons.concurrent.EsExecutors;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.commons.lucene.uid.UidField;
import cn.com.rebirth.search.core.action.ActionListener;
import cn.com.rebirth.search.core.action.bulk.BulkRequestBuilder;
import cn.com.rebirth.search.core.action.bulk.BulkResponse;
import cn.com.rebirth.search.core.action.delete.DeleteRequest;
import cn.com.rebirth.search.core.client.Client;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.metadata.IndexMetaData;
import cn.com.rebirth.search.core.cluster.metadata.MetaData;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.FieldMappers;
import cn.com.rebirth.search.core.index.mapper.Uid;
import cn.com.rebirth.search.core.index.mapper.internal.RoutingFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.TTLFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.UidFieldMapper;
import cn.com.rebirth.search.core.index.mapper.selector.UidAndRoutingFieldSelector;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.shard.IndexShardState;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.node.settings.NodeSettingsService;

/**
 * The Class IndicesTTLService.
 *
 * @author l.xue.nong
 */
public class IndicesTTLService extends AbstractLifecycleComponent<IndicesTTLService> {

	static {
		MetaData.addDynamicSettings("indices.ttl.interval", "index.ttl.disable_purge");
	}

	/** The cluster service. */
	private final ClusterService clusterService;

	/** The indices service. */
	private final IndicesService indicesService;

	/** The client. */
	private final Client client;

	/** The interval. */
	private volatile TimeValue interval;

	/** The bulk size. */
	private final int bulkSize;

	/** The purger thread. */
	private PurgerThread purgerThread;

	/**
	 * Instantiates a new indices ttl service.
	 *
	 * @param settings the settings
	 * @param clusterService the cluster service
	 * @param indicesService the indices service
	 * @param nodeSettingsService the node settings service
	 * @param client the client
	 */
	@Inject
	public IndicesTTLService(Settings settings, ClusterService clusterService, IndicesService indicesService,
			NodeSettingsService nodeSettingsService, Client client) {
		super(settings);
		this.clusterService = clusterService;
		this.indicesService = indicesService;
		this.client = client;
		this.interval = componentSettings.getAsTime("interval", TimeValue.timeValueSeconds(60));
		this.bulkSize = componentSettings.getAsInt("bulk_size", 10000);

		nodeSettingsService.addListener(new ApplySettings());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStart()
	 */
	@Override
	protected void doStart() throws RebirthException {
		this.purgerThread = new PurgerThread(EsExecutors.threadName(settings, "[ttl_expire]"));
		this.purgerThread.start();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doStop()
	 */
	@Override
	protected void doStop() throws RebirthException {
		this.purgerThread.doStop();
		this.purgerThread.interrupt();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.AbstractLifecycleComponent#doClose()
	 */
	@Override
	protected void doClose() throws RebirthException {
	}

	/**
	 * The Class PurgerThread.
	 *
	 * @author l.xue.nong
	 */
	private class PurgerThread extends Thread {

		/** The running. */
		volatile boolean running = true;

		/**
		 * Instantiates a new purger thread.
		 *
		 * @param name the name
		 */
		public PurgerThread(String name) {
			super(name);
			setDaemon(true);
		}

		/**
		 * Do stop.
		 */
		public void doStop() {
			running = false;
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			while (running) {
				try {
					List<IndexShard> shardsToPurge = getShardsToPurge();
					purgeShards(shardsToPurge);
				} catch (Throwable e) {
					if (running) {
						logger.warn("failed to execute ttl purge", e);
					}
				}
				try {
					Thread.sleep(interval.millis());
				} catch (InterruptedException e) {

				}

			}
		}

		/**
		 * Gets the shards to purge.
		 *
		 * @return the shards to purge
		 */
		private List<IndexShard> getShardsToPurge() {
			List<IndexShard> shardsToPurge = new ArrayList<IndexShard>();
			MetaData metaData = clusterService.state().metaData();
			for (IndexService indexService : indicesService) {

				IndexMetaData indexMetaData = metaData.index(indexService.index().name());
				if (indexMetaData == null) {
					continue;
				}
				boolean disablePurge = indexMetaData.settings().getAsBoolean("index.ttl.disable_purge", false);
				if (disablePurge) {
					continue;
				}

				FieldMappers ttlFieldMappers = indexService.mapperService().name(TTLFieldMapper.NAME);
				if (ttlFieldMappers == null) {
					continue;
				}

				boolean hasTTLEnabled = false;
				for (FieldMapper ttlFieldMapper : ttlFieldMappers) {
					if (((TTLFieldMapper) ttlFieldMapper).enabled()) {
						hasTTLEnabled = true;
						break;
					}
				}
				if (hasTTLEnabled) {
					for (IndexShard indexShard : indexService) {
						if (indexShard.state() == IndexShardState.STARTED && indexShard.routingEntry().primary()
								&& indexShard.routingEntry().started()) {
							shardsToPurge.add(indexShard);
						}
					}
				}
			}
			return shardsToPurge;
		}
	}

	/**
	 * Purge shards.
	 *
	 * @param shardsToPurge the shards to purge
	 */
	private void purgeShards(List<IndexShard> shardsToPurge) {
		for (IndexShard shardToPurge : shardsToPurge) {
			Query query = NumericRangeQuery.newLongRange(TTLFieldMapper.NAME, null, System.currentTimeMillis(), false,
					true);
			Engine.Searcher searcher = shardToPurge.searcher();
			try {
				logger.debug("[{}][{}] purging shard", shardToPurge.routingEntry().index(), shardToPurge.routingEntry()
						.id());
				ExpiredDocsCollector expiredDocsCollector = new ExpiredDocsCollector();
				searcher.searcher().search(query, expiredDocsCollector);
				List<DocToPurge> docsToPurge = expiredDocsCollector.getDocsToPurge();
				BulkRequestBuilder bulkRequest = client.prepareBulk();
				for (DocToPurge docToPurge : docsToPurge) {
					bulkRequest.add(new DeleteRequest().index(shardToPurge.routingEntry().index())
							.type(docToPurge.type).id(docToPurge.id).version(docToPurge.version)
							.routing(docToPurge.routing));
					bulkRequest = processBulkIfNeeded(bulkRequest, false);
				}
				processBulkIfNeeded(bulkRequest, true);
			} catch (Exception e) {
				logger.warn("failed to purge", e);
			} finally {
				searcher.release();
			}
		}
	}

	/**
	 * The Class DocToPurge.
	 *
	 * @author l.xue.nong
	 */
	private static class DocToPurge {

		/** The type. */
		public final String type;

		/** The id. */
		public final String id;

		/** The version. */
		public final long version;

		/** The routing. */
		public final String routing;

		/**
		 * Instantiates a new doc to purge.
		 *
		 * @param type the type
		 * @param id the id
		 * @param version the version
		 * @param routing the routing
		 */
		public DocToPurge(String type, String id, long version, String routing) {
			this.type = type;
			this.id = id;
			this.version = version;
			this.routing = routing;
		}
	}

	/**
	 * The Class ExpiredDocsCollector.
	 *
	 * @author l.xue.nong
	 */
	private class ExpiredDocsCollector extends Collector {

		/** The index reader. */
		private IndexReader indexReader;

		/** The docs to purge. */
		private List<DocToPurge> docsToPurge = new ArrayList<DocToPurge>();

		/**
		 * Instantiates a new expired docs collector.
		 */
		public ExpiredDocsCollector() {
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Collector#setScorer(org.apache.lucene.search.Scorer)
		 */
		public void setScorer(Scorer scorer) {
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Collector#acceptsDocsOutOfOrder()
		 */
		public boolean acceptsDocsOutOfOrder() {
			return true;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Collector#collect(int)
		 */
		public void collect(int doc) {
			try {
				Document document = indexReader.document(doc, new UidAndRoutingFieldSelector());
				String uid = document.getFieldable(UidFieldMapper.NAME).stringValue();
				long version = UidField.loadVersion(indexReader, UidFieldMapper.TERM_FACTORY.createTerm(uid));
				docsToPurge.add(new DocToPurge(Uid.typeFromUid(uid), Uid.idFromUid(uid), version, document
						.get(RoutingFieldMapper.NAME)));
			} catch (Exception e) {
				logger.trace("failed to collect doc", e);
			}
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Collector#setNextReader(org.apache.lucene.index.IndexReader, int)
		 */
		public void setNextReader(IndexReader reader, int docBase) {
			this.indexReader = reader;
		}

		/**
		 * Gets the docs to purge.
		 *
		 * @return the docs to purge
		 */
		public List<DocToPurge> getDocsToPurge() {
			return this.docsToPurge;
		}
	}

	/**
	 * Process bulk if needed.
	 *
	 * @param bulkRequest the bulk request
	 * @param force the force
	 * @return the bulk request builder
	 */
	private BulkRequestBuilder processBulkIfNeeded(BulkRequestBuilder bulkRequest, boolean force) {
		if ((force && bulkRequest.numberOfActions() > 0) || bulkRequest.numberOfActions() >= bulkSize) {
			try {
				bulkRequest.execute(new ActionListener<BulkResponse>() {
					@Override
					public void onResponse(BulkResponse bulkResponse) {
						logger.trace("bulk took " + bulkResponse.getTookInMillis() + "ms");
					}

					@Override
					public void onFailure(Throwable e) {
						logger.warn("failed to execute bulk");
					}
				});
			} catch (Exception e) {
				logger.warn("failed to process bulk", e);
			}
			bulkRequest = client.prepareBulk();
		}
		return bulkRequest;
	}

	/**
	 * The Class ApplySettings.
	 *
	 * @author l.xue.nong
	 */
	class ApplySettings implements NodeSettingsService.Listener {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.node.settings.NodeSettingsService.Listener#onRefreshSettings(cn.com.rebirth.commons.settings.Settings)
		 */
		@Override
		public void onRefreshSettings(Settings settings) {
			TimeValue interval = settings.getAsTime("indices.ttl.interval", IndicesTTLService.this.interval);
			if (!interval.equals(IndicesTTLService.this.interval)) {
				logger.info("updating indices.ttl.interval from [{}] to [{}]", IndicesTTLService.this.interval,
						interval);
				IndicesTTLService.this.interval = interval;
			}
		}
	}
}