/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TransportAnalyzeAction.java 2012-7-6 14:30:14 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.analyze;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.io.FastStringReader;
import cn.com.rebirth.commons.settings.ImmutableSettings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.core.threadpool.ThreadPool;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.core.action.support.single.custom.TransportSingleCustomOperationAction;
import cn.com.rebirth.search.core.cluster.ClusterService;
import cn.com.rebirth.search.core.cluster.ClusterState;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockException;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.routing.ShardsIterator;
import cn.com.rebirth.search.core.index.analysis.CharFilterFactory;
import cn.com.rebirth.search.core.index.analysis.CustomAnalyzer;
import cn.com.rebirth.search.core.index.analysis.TokenFilterFactory;
import cn.com.rebirth.search.core.index.analysis.TokenFilterFactoryFactory;
import cn.com.rebirth.search.core.index.analysis.TokenizerFactory;
import cn.com.rebirth.search.core.index.analysis.TokenizerFactoryFactory;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.AllFieldMapper;
import cn.com.rebirth.search.core.index.service.IndexService;
import cn.com.rebirth.search.core.indices.IndicesService;
import cn.com.rebirth.search.core.indices.analysis.IndicesAnalysisService;
import cn.com.rebirth.search.core.transport.TransportService;

import com.google.common.collect.Lists;

/**
 * The Class TransportAnalyzeAction.
 *
 * @author l.xue.nong
 */
public class TransportAnalyzeAction extends TransportSingleCustomOperationAction<AnalyzeRequest, AnalyzeResponse> {

	/** The indices service. */
	private final IndicesService indicesService;

	/** The indices analysis service. */
	private final IndicesAnalysisService indicesAnalysisService;

	/**
	 * Instantiates a new transport analyze action.
	 *
	 * @param settings the settings
	 * @param threadPool the thread pool
	 * @param clusterService the cluster service
	 * @param transportService the transport service
	 * @param indicesService the indices service
	 * @param indicesAnalysisService the indices analysis service
	 */
	@Inject
	public TransportAnalyzeAction(Settings settings, ThreadPool threadPool, ClusterService clusterService,
			TransportService transportService, IndicesService indicesService,
			IndicesAnalysisService indicesAnalysisService) {
		super(settings, threadPool, clusterService, transportService);
		this.indicesService = indicesService;
		this.indicesAnalysisService = indicesAnalysisService;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.custom.TransportSingleCustomOperationAction#executor()
	 */
	@Override
	protected String executor() {
		return ThreadPool.Names.INDEX;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.custom.TransportSingleCustomOperationAction#newRequest()
	 */
	@Override
	protected AnalyzeRequest newRequest() {
		return new AnalyzeRequest();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.custom.TransportSingleCustomOperationAction#newResponse()
	 */
	@Override
	protected AnalyzeResponse newResponse() {
		return new AnalyzeResponse();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.custom.TransportSingleCustomOperationAction#transportAction()
	 */
	@Override
	protected String transportAction() {
		return AnalyzeAction.NAME;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.custom.TransportSingleCustomOperationAction#checkGlobalBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.single.custom.SingleCustomOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state, AnalyzeRequest request) {
		return state.blocks().globalBlockedException(ClusterBlockLevel.READ);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.custom.TransportSingleCustomOperationAction#checkRequestBlock(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.single.custom.SingleCustomOperationRequest)
	 */
	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state, AnalyzeRequest request) {
		if (request.index() != null) {
			request.index(state.metaData().concreteIndex(request.index()));
			return state.blocks().indexBlockedException(ClusterBlockLevel.READ, request.index());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.custom.TransportSingleCustomOperationAction#shards(cn.com.rebirth.search.core.cluster.ClusterState, cn.com.rebirth.search.core.action.support.single.custom.SingleCustomOperationRequest)
	 */
	@Override
	protected ShardsIterator shards(ClusterState state, AnalyzeRequest request) {
		if (request.index() == null) {

			return null;
		}
		return state.routingTable().index(request.index()).randomAllActiveShardsIt();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.action.support.single.custom.TransportSingleCustomOperationAction#shardOperation(cn.com.rebirth.search.core.action.support.single.custom.SingleCustomOperationRequest, int)
	 */
	@Override
	protected AnalyzeResponse shardOperation(AnalyzeRequest request, int shardId) throws RebirthException {
		IndexService indexService = null;
		if (request.index() != null) {
			indexService = indicesService.indexServiceSafe(request.index());
		}
		Analyzer analyzer = null;
		boolean closeAnalyzer = false;
		String field = null;
		if (request.field() != null) {
			if (indexService == null) {
				throw new RebirthIllegalArgumentException(
						"No index provided, and trying to analyzer based on a specific field which requires the index parameter");
			}
			FieldMapper fieldMapper = indexService.mapperService().smartNameFieldMapper(request.field());
			if (fieldMapper != null) {
				analyzer = fieldMapper.indexAnalyzer();
				field = fieldMapper.names().indexName();
			}
		}
		if (field == null) {
			if (indexService != null) {
				field = indexService.queryParserService().defaultField();
			} else {
				field = AllFieldMapper.NAME;
			}
		}
		if (analyzer == null && request.analyzer() != null) {
			if (indexService == null) {
				analyzer = indicesAnalysisService.analyzer(request.analyzer());
			} else {
				analyzer = indexService.analysisService().analyzer(request.analyzer());
			}
			if (analyzer == null) {
				throw new RebirthIllegalArgumentException("failed to find analyzer [" + request.analyzer() + "]");
			}
		} else if (request.tokenizer() != null) {
			TokenizerFactory tokenizerFactory;
			if (indexService == null) {
				TokenizerFactoryFactory tokenizerFactoryFactory = indicesAnalysisService
						.tokenizerFactoryFactory(request.tokenizer());
				if (tokenizerFactoryFactory == null) {
					throw new RebirthIllegalArgumentException("failed to find global tokenizer under ["
							+ request.tokenizer() + "]");
				}
				tokenizerFactory = tokenizerFactoryFactory.create(request.tokenizer(),
						ImmutableSettings.Builder.EMPTY_SETTINGS);
			} else {
				tokenizerFactory = indexService.analysisService().tokenizer(request.tokenizer());
				if (tokenizerFactory == null) {
					throw new RebirthIllegalArgumentException("failed to find tokenizer under [" + request.tokenizer()
							+ "]");
				}
			}
			TokenFilterFactory[] tokenFilterFactories = new TokenFilterFactory[0];
			if (request.tokenFilters() != null && request.tokenFilters().length > 0) {
				tokenFilterFactories = new TokenFilterFactory[request.tokenFilters().length];
				for (int i = 0; i < request.tokenFilters().length; i++) {
					String tokenFilterName = request.tokenFilters()[i];
					if (indexService == null) {
						TokenFilterFactoryFactory tokenFilterFactoryFactory = indicesAnalysisService
								.tokenFilterFactoryFactory(tokenFilterName);
						if (tokenFilterFactoryFactory == null) {
							throw new RebirthIllegalArgumentException("failed to find global token filter under ["
									+ request.tokenizer() + "]");
						}
						tokenFilterFactories[i] = tokenFilterFactoryFactory.create(tokenFilterName,
								ImmutableSettings.Builder.EMPTY_SETTINGS);
					} else {
						tokenFilterFactories[i] = indexService.analysisService().tokenFilter(tokenFilterName);
						if (tokenFilterFactories[i] == null) {
							throw new RebirthIllegalArgumentException("failed to find token filter under ["
									+ request.tokenizer() + "]");
						}
					}
					if (tokenFilterFactories[i] == null) {
						throw new RebirthIllegalArgumentException("failed to find token filter under ["
								+ request.tokenizer() + "]");
					}
				}
			}
			analyzer = new CustomAnalyzer(tokenizerFactory, new CharFilterFactory[0], tokenFilterFactories);
			closeAnalyzer = true;
		} else if (analyzer == null) {
			if (indexService == null) {
				analyzer = Lucene.STANDARD_ANALYZER;
			} else {
				analyzer = indexService.analysisService().defaultIndexAnalyzer();
			}
		}
		if (analyzer == null) {
			throw new RebirthIllegalArgumentException("failed to find analyzer");
		}

		List<AnalyzeResponse.AnalyzeToken> tokens = Lists.newArrayList();
		TokenStream stream = null;
		try {
			stream = analyzer.reusableTokenStream(field, new FastStringReader(request.text()));
			stream.reset();
			CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
			PositionIncrementAttribute posIncr = stream.addAttribute(PositionIncrementAttribute.class);
			OffsetAttribute offset = stream.addAttribute(OffsetAttribute.class);
			TypeAttribute type = stream.addAttribute(TypeAttribute.class);

			int position = 0;
			while (stream.incrementToken()) {
				int increment = posIncr.getPositionIncrement();
				if (increment > 0) {
					position = position + increment;
				}
				tokens.add(new AnalyzeResponse.AnalyzeToken(term.toString(), position, offset.startOffset(), offset
						.endOffset(), type.type()));
			}
			stream.end();
		} catch (IOException e) {
			throw new RebirthException("failed to analyze", e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {

				}
			}
			if (closeAnalyzer) {
				analyzer.close();
			}
		}

		return new AnalyzeResponse(tokens);
	}
}
