/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PercolatorService.java 2012-3-29 15:01:41 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.percolator;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;

import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.inject.Inject;
import cn.com.rebirth.search.commons.lucene.search.DeletionAwareConstantScoreQuery;
import cn.com.rebirth.search.commons.lucene.search.TermFilter;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.indexing.IndexingOperationListener;
import cn.com.rebirth.search.core.index.mapper.Uid;
import cn.com.rebirth.search.core.index.mapper.internal.SourceFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.TypeFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.UidFieldMapper;
import cn.com.rebirth.search.core.index.mapper.selector.UidAndSourceFieldSelector;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.IndexShardState;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.indices.IndicesLifecycle.Listener;

import com.google.common.collect.Maps;


/**
 * The Class PercolatorService.
 *
 * @author l.xue.nong
 */
public class PercolatorService extends AbstractIndexComponent {

	
	/** The Constant INDEX_NAME. */
	public static final String INDEX_NAME = "_percolator";

	
	/** The indices service. */
	private final IndicesService indicesService;

	
	/** The percolator. */
	private final PercolatorExecutor percolator;

	
	/** The shard lifecycle listener. */
	private final ShardLifecycleListener shardLifecycleListener;

	
	/** The real time percolator operation listener. */
	private final RealTimePercolatorOperationListener realTimePercolatorOperationListener = new RealTimePercolatorOperationListener();

	
	/** The mutex. */
	private final Object mutex = new Object();

	
	/** The initial queries fetch done. */
	private boolean initialQueriesFetchDone = false;

	
	/**
	 * Instantiates a new percolator service.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 * @param indicesService the indices service
	 * @param percolator the percolator
	 */
	@Inject
	public PercolatorService(Index index, @IndexSettings Settings indexSettings, IndicesService indicesService,
			PercolatorExecutor percolator) {
		super(index, indexSettings);
		this.indicesService = indicesService;
		this.percolator = percolator;
		this.shardLifecycleListener = new ShardLifecycleListener();
		this.indicesService.indicesLifecycle().addListener(shardLifecycleListener);
		this.percolator.setIndicesService(indicesService);

		
		if (percolatorAllocated()) {
			IndexService percolatorIndexService = percolatorIndexService();
			if (percolatorIndexService != null) {
				for (IndexShard indexShard : percolatorIndexService) {
					try {
						indexShard.indexingService().addListener(realTimePercolatorOperationListener);
					} catch (Exception e) {
						
					}
				}
			}
		}
	}

	
	/**
	 * Close.
	 */
	public void close() {
		this.indicesService.indicesLifecycle().removeListener(shardLifecycleListener);

		
		IndexService percolatorIndexService = percolatorIndexService();
		if (percolatorIndexService != null) {
			for (IndexShard indexShard : percolatorIndexService) {
				try {
					indexShard.indexingService().removeListener(realTimePercolatorOperationListener);
				} catch (Exception e) {
					
				}
			}
		}
	}

	
	/**
	 * Percolate.
	 *
	 * @param request the request
	 * @return the percolator executor. response
	 * @throws PercolatorException the percolator exception
	 */
	public PercolatorExecutor.Response percolate(PercolatorExecutor.SourceRequest request) throws PercolatorException {
		return percolator.percolate(request);
	}

	
	/**
	 * Percolate.
	 *
	 * @param request the request
	 * @return the percolator executor. response
	 * @throws PercolatorException the percolator exception
	 */
	public PercolatorExecutor.Response percolate(PercolatorExecutor.DocAndSourceQueryRequest request)
			throws PercolatorException {
		return percolator.percolate(request);
	}

	
	/**
	 * Load queries.
	 *
	 * @param indexName the index name
	 */
	private void loadQueries(String indexName) {
		IndexService indexService = percolatorIndexService();
		IndexShard shard = indexService.shard(0);
		shard.refresh(new Engine.Refresh(true));
		Engine.Searcher searcher = shard.searcher();
		try {
			
			
			Query query = new DeletionAwareConstantScoreQuery(indexQueriesFilter(indexName));
			QueriesLoaderCollector queries = new QueriesLoaderCollector();
			searcher.searcher().search(query, queries);
			percolator.addQueries(queries.queries());
		} catch (IOException e) {
			throw new PercolatorException(index, "failed to load queries from percolator index");
		} finally {
			searcher.release();
		}
	}

	
	/**
	 * Index queries filter.
	 *
	 * @param indexName the index name
	 * @return the filter
	 */
	private Filter indexQueriesFilter(String indexName) {
		return percolatorIndexService().cache().filter()
				.cache(new TermFilter(TypeFieldMapper.TERM_FACTORY.createTerm(indexName)));
	}

	
	/**
	 * Percolator allocated.
	 *
	 * @return true, if successful
	 */
	private boolean percolatorAllocated() {
		if (!indicesService.hasIndex(INDEX_NAME)) {
			return false;
		}
		if (percolatorIndexService().numberOfShards() == 0) {
			return false;
		}
		if (percolatorIndexService().shard(0).state() != IndexShardState.STARTED) {
			return false;
		}
		return true;
	}

	
	/**
	 * Percolator index service.
	 *
	 * @return the index service
	 */
	private IndexService percolatorIndexService() {
		return indicesService.indexService(INDEX_NAME);
	}

	
	/**
	 * The Class QueriesLoaderCollector.
	 *
	 * @author l.xue.nong
	 */
	class QueriesLoaderCollector extends Collector {

		
		/** The reader. */
		private IndexReader reader;

		
		/** The queries. */
		private Map<String, Query> queries = Maps.newHashMap();

		
		/**
		 * Queries.
		 *
		 * @return the map
		 */
		public Map<String, Query> queries() {
			return this.queries;
		}

		
		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Collector#setScorer(org.apache.lucene.search.Scorer)
		 */
		@Override
		public void setScorer(Scorer scorer) throws IOException {
		}

		
		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Collector#collect(int)
		 */
		@Override
		public void collect(int doc) throws IOException {
			
			Document document = reader.document(doc, new UidAndSourceFieldSelector());
			String id = Uid.createUid(document.get(UidFieldMapper.NAME)).id();
			try {
				Fieldable sourceField = document.getFieldable(SourceFieldMapper.NAME);
				queries.put(id, percolator.parseQuery(id, sourceField.getBinaryValue(), sourceField.getBinaryOffset(),
						sourceField.getBinaryLength()));
			} catch (Exception e) {
				logger.warn("failed to add query [{}]", e, id);
			}
		}

		
		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Collector#setNextReader(org.apache.lucene.index.IndexReader, int)
		 */
		@Override
		public void setNextReader(IndexReader reader, int docBase) throws IOException {
			this.reader = reader;
		}

		
		/* (non-Javadoc)
		 * @see org.apache.lucene.search.Collector#acceptsDocsOutOfOrder()
		 */
		@Override
		public boolean acceptsDocsOutOfOrder() {
			return true;
		}
	}

	
	/**
	 * The listener interface for receiving shardLifecycle events.
	 * The class that is interested in processing a shardLifecycle
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addShardLifecycleListener<code> method. When
	 * the shardLifecycle event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see ShardLifecycleEvent
	 */
	class ShardLifecycleListener extends Listener {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.indices.IndicesLifecycle.Listener#afterIndexShardCreated(cn.com.summall.search.core.index.shard.service.IndexShard)
		 */
		@Override
		public void afterIndexShardCreated(IndexShard indexShard) {
			
			
			if (indexShard.shardId().index().name().equals(INDEX_NAME)) {
				indexShard.indexingService().addListener(realTimePercolatorOperationListener);
			}
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.indices.IndicesLifecycle.Listener#afterIndexShardStarted(cn.com.summall.search.core.index.shard.service.IndexShard)
		 */
		@Override
		public void afterIndexShardStarted(IndexShard indexShard) {
			if (indexShard.shardId().index().name().equals(INDEX_NAME)) {
				
				
				synchronized (mutex) {
					if (initialQueriesFetchDone) {
						return;
					}
					
					for (IndexService indexService : indicesService) {
						
						if (indexService.index().equals(index())) {
							logger.debug("loading percolator queries for index [{}]...", indexService.index().name());
							loadQueries(indexService.index().name());
							logger.trace("done loading percolator queries for index [{}]", indexService.index().name());
						}
					}
					initialQueriesFetchDone = true;
				}
			}
			if (!indexShard.shardId().index().equals(index())) {
				
				return;
			}
			if (!percolatorAllocated()) {
				return;
			}
			
			
			IndexService indexService = indicesService.indexService(indexShard.shardId().index().name());
			if (indexService == null) {
				return;
			}
			if (indexService.numberOfShards() != 1) {
				return;
			}
			synchronized (mutex) {
				if (initialQueriesFetchDone) {
					return;
				}
				
				logger.debug("loading percolator queries for index [{}]...", indexService.index().name());
				loadQueries(index.name());
				logger.trace("done loading percolator queries for index [{}]", indexService.index().name());
				initialQueriesFetchDone = true;
			}
		}
	}

	
	/**
	 * The listener interface for receiving realTimePercolatorOperation events.
	 * The class that is interested in processing a realTimePercolatorOperation
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addRealTimePercolatorOperationListener<code> method. When
	 * the realTimePercolatorOperation event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see RealTimePercolatorOperationEvent
	 */
	class RealTimePercolatorOperationListener extends IndexingOperationListener {

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.indexing.IndexingOperationListener#preCreate(cn.com.summall.search.core.index.engine.Engine.Create)
		 */
		@Override
		public Engine.Create preCreate(Engine.Create create) {
			if (create.type().equals(index().name())) {
				percolator.addQuery(create.id(), create.source(), create.sourceOffset(), create.sourceLength());
			}
			return create;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.indexing.IndexingOperationListener#preIndex(cn.com.summall.search.core.index.engine.Engine.Index)
		 */
		@Override
		public Engine.Index preIndex(Engine.Index index) {
			if (index.type().equals(index().name())) {
				percolator.addQuery(index.id(), index.source(), index.sourceOffset(), index.sourceLength());
			}
			return index;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.indexing.IndexingOperationListener#preDelete(cn.com.summall.search.core.index.engine.Engine.Delete)
		 */
		@Override
		public Engine.Delete preDelete(Engine.Delete delete) {
			if (delete.type().equals(index().name())) {
				percolator.removeQuery(delete.id());
			}
			return delete;
		}
	}
}
