/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core HighlightPhase.java 2012-7-6 14:30:40 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.highlight;

import static com.google.common.collect.Maps.newHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.DefaultEncoder;
import org.apache.lucene.search.highlight.Encoder;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.NullFragmenter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.vectorhighlight.BoundaryScanner;
import org.apache.lucene.search.vectorhighlight.CustomFieldQuery;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.search.vectorhighlight.FragListBuilder;
import org.apache.lucene.search.vectorhighlight.FragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.ScoreOrderFragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.SimpleFragListBuilder;
import org.apache.lucene.search.vectorhighlight.SimpleFragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.SingleFragListBuilder;

import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.search.commons.io.FastStringReader;
import cn.com.rebirth.search.commons.lucene.document.SingleFieldSelector;
import cn.com.rebirth.search.commons.lucene.search.function.FiltersFunctionScoreQuery;
import cn.com.rebirth.search.commons.lucene.search.function.FunctionScoreQuery;
import cn.com.rebirth.search.commons.lucene.search.vectorhighlight.SimpleBoundaryScanner2;
import cn.com.rebirth.search.core.index.mapper.DocumentMapper;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.fetch.FetchPhaseExecutionException;
import cn.com.rebirth.search.core.search.fetch.FetchSubPhase;
import cn.com.rebirth.search.core.search.highlight.vectorhighlight.SourceScoreOrderFragmentsBuilder;
import cn.com.rebirth.search.core.search.highlight.vectorhighlight.SourceSimpleFragmentsBuilder;
import cn.com.rebirth.search.core.search.internal.InternalSearchHit;
import cn.com.rebirth.search.core.search.internal.SearchContext;
import cn.com.rebirth.search.core.search.lookup.SearchLookup;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * The Class HighlightPhase.
 *
 * @author l.xue.nong
 */
public class HighlightPhase implements FetchSubPhase {

	/**
	 * The Class Encoders.
	 *
	 * @author l.xue.nong
	 */
	public static class Encoders {

		/** The default. */
		public static Encoder DEFAULT = new DefaultEncoder();

		/** The html. */
		public static Encoder HTML = new SimpleHTMLEncoder();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.fetch.FetchSubPhase#parseElements()
	 */
	@Override
	public Map<String, ? extends SearchParseElement> parseElements() {
		return ImmutableMap.of("highlight", new HighlighterParseElement());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.fetch.FetchSubPhase#hitsExecutionNeeded(cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public boolean hitsExecutionNeeded(SearchContext context) {
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.fetch.FetchSubPhase#hitsExecute(cn.com.rebirth.search.core.search.internal.SearchContext, cn.com.rebirth.search.core.search.internal.InternalSearchHit[])
	 */
	@Override
	public void hitsExecute(SearchContext context, InternalSearchHit[] hits) throws RebirthException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.fetch.FetchSubPhase#hitExecutionNeeded(cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public boolean hitExecutionNeeded(SearchContext context) {
		return context.highlight() != null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.fetch.FetchSubPhase#hitExecute(cn.com.rebirth.search.core.search.internal.SearchContext, cn.com.rebirth.search.core.search.fetch.FetchSubPhase.HitContext)
	 */
	@Override
	public void hitExecute(SearchContext context, HitContext hitContext) throws RebirthException {

		HighlighterEntry cache = (HighlighterEntry) hitContext.cache().get("highlight");
		if (cache == null) {
			cache = new HighlighterEntry();
			hitContext.cache().put("highlight", cache);
		}

		DocumentMapper documentMapper = context.mapperService().documentMapper(hitContext.hit().type());

		Map<String, HighlightField> highlightFields = newHashMap();
		for (SearchContextHighlight.Field field : context.highlight().fields()) {
			Encoder encoder;
			if (field.encoder().equals("html")) {
				encoder = Encoders.HTML;
			} else {
				encoder = Encoders.DEFAULT;
			}
			FieldMapper mapper = documentMapper.mappers().smartNameFieldMapper(field.field());
			if (mapper == null) {
				MapperService.SmartNameFieldMappers fullMapper = context.mapperService().smartName(field.field());
				if (fullMapper == null || !fullMapper.hasDocMapper()) {

					continue;
				}
				if (!fullMapper.docMapper().type().equals(hitContext.hit().type())) {
					continue;
				}
				mapper = fullMapper.mapper();
				if (mapper == null) {
					continue;
				}
			}

			if (mapper.termVector() != Field.TermVector.WITH_POSITIONS_OFFSETS) {
				MapperHighlightEntry entry = cache.mappers.get(mapper);
				if (entry == null) {

					Query query = context.parsedQuery().query();
					while (true) {
						boolean extracted = false;
						if (query instanceof FunctionScoreQuery) {
							query = ((FunctionScoreQuery) query).getSubQuery();
							extracted = true;
						} else if (query instanceof FiltersFunctionScoreQuery) {
							query = ((FiltersFunctionScoreQuery) query).getSubQuery();
							extracted = true;
						} else if (query instanceof ConstantScoreQuery) {
							ConstantScoreQuery q = (ConstantScoreQuery) query;
							if (q.getQuery() != null) {
								query = q.getQuery();
								extracted = true;
							}
						}
						if (!extracted) {
							break;
						}
					}

					QueryScorer queryScorer = new QueryScorer(query, field.requireFieldMatch() ? mapper.names()
							.indexName() : null);
					queryScorer.setExpandMultiTermQuery(true);
					Fragmenter fragmenter;
					if (field.numberOfFragments() == 0) {
						fragmenter = new NullFragmenter();
					} else {
						fragmenter = new SimpleSpanFragmenter(queryScorer, field.fragmentCharSize());
					}
					Formatter formatter = new SimpleHTMLFormatter(field.preTags()[0], field.postTags()[0]);

					entry = new MapperHighlightEntry();
					entry.highlighter = new Highlighter(formatter, encoder, queryScorer);
					entry.highlighter.setTextFragmenter(fragmenter);

					entry.highlighter.setMaxDocCharsToAnalyze(Integer.MAX_VALUE);

					cache.mappers.put(mapper, entry);
				}

				List<Object> textsToHighlight;
				if (mapper.stored()) {
					try {
						Document doc = hitContext.reader().document(hitContext.docId(),
								new SingleFieldSelector(mapper.names().indexName()));
						textsToHighlight = new ArrayList<Object>(doc.getFields().size());
						for (Fieldable docField : doc.getFields()) {
							if (docField.stringValue() != null) {
								textsToHighlight.add(docField.stringValue());
							}
						}
					} catch (Exception e) {
						throw new FetchPhaseExecutionException(context, "Failed to highlight field [" + field.field()
								+ "]", e);
					}
				} else {
					SearchLookup lookup = context.lookup();
					lookup.setNextReader(hitContext.reader());
					lookup.setNextDocId(hitContext.docId());
					textsToHighlight = lookup.source().extractRawValues(mapper.names().sourcePath());
				}

				int numberOfFragments = field.numberOfFragments() == 0 ? 1 : field.numberOfFragments();
				ArrayList<TextFragment> fragsList = new ArrayList<TextFragment>();
				try {
					for (Object textToHighlight : textsToHighlight) {
						String text = textToHighlight.toString();
						Analyzer analyzer = context.mapperService().documentMapper(hitContext.hit().type()).mappers()
								.indexAnalyzer();
						TokenStream tokenStream = analyzer.reusableTokenStream(mapper.names().indexName(),
								new FastStringReader(text));
						TextFragment[] bestTextFragments = entry.highlighter.getBestTextFragments(tokenStream, text,
								false, numberOfFragments);
						for (TextFragment bestTextFragment : bestTextFragments) {
							if (bestTextFragment != null && bestTextFragment.getScore() > 0) {
								fragsList.add(bestTextFragment);
							}
						}
					}
				} catch (Exception e) {
					throw new FetchPhaseExecutionException(context,
							"Failed to highlight field [" + field.field() + "]", e);
				}
				if (field.scoreOrdered()) {
					Collections.sort(fragsList, new Comparator<TextFragment>() {
						public int compare(TextFragment o1, TextFragment o2) {
							return Math.round(o2.getScore() - o1.getScore());
						}
					});
				}
				String[] fragments = null;

				if (field.numberOfFragments() == 0 && textsToHighlight.size() > 1 && fragsList.size() > 0) {
					fragments = new String[1];
					for (int i = 0; i < fragsList.size(); i++) {
						fragments[0] = (fragments[0] != null ? (fragments[0] + " ") : "") + fragsList.get(i).toString();
					}
				} else {

					numberOfFragments = fragsList.size() < numberOfFragments ? fragsList.size() : numberOfFragments;
					fragments = new String[numberOfFragments];
					for (int i = 0; i < fragments.length; i++) {
						fragments[i] = fragsList.get(i).toString();
					}
				}

				if (fragments != null && fragments.length > 0) {
					HighlightField highlightField = new HighlightField(field.field(), fragments);
					highlightFields.put(highlightField.name(), highlightField);
				}
			} else {
				try {
					MapperHighlightEntry entry = cache.mappers.get(mapper);
					FieldQuery fieldQuery = null;
					if (entry == null) {
						FragListBuilder fragListBuilder;
						FragmentsBuilder fragmentsBuilder;

						BoundaryScanner boundaryScanner = SimpleBoundaryScanner2.DEFAULT;
						if (field.boundaryMaxScan() != SimpleBoundaryScanner2.DEFAULT_MAX_SCAN
								|| field.boundaryChars() != SimpleBoundaryScanner2.DEFAULT_BOUNDARY_CHARS) {
							boundaryScanner = new SimpleBoundaryScanner2(field.boundaryMaxScan(), field.boundaryChars());
						}

						if (field.numberOfFragments() == 0) {
							fragListBuilder = new SingleFragListBuilder();

							if (mapper.stored()) {
								fragmentsBuilder = new SimpleFragmentsBuilder(field.preTags(), field.postTags(),
										boundaryScanner);
							} else {
								fragmentsBuilder = new SourceSimpleFragmentsBuilder(mapper, context, field.preTags(),
										field.postTags(), boundaryScanner);
							}
						} else {
							if (field.fragmentOffset() == -1)
								fragListBuilder = new SimpleFragListBuilder();
							else
								fragListBuilder = new SimpleFragListBuilder(field.fragmentOffset());

							if (field.scoreOrdered()) {
								if (mapper.stored()) {
									fragmentsBuilder = new ScoreOrderFragmentsBuilder(field.preTags(),
											field.postTags(), boundaryScanner);
								} else {
									fragmentsBuilder = new SourceScoreOrderFragmentsBuilder(mapper, context,
											field.preTags(), field.postTags(), boundaryScanner);
								}
							} else {
								if (mapper.stored()) {
									fragmentsBuilder = new SimpleFragmentsBuilder(field.preTags(), field.postTags(),
											boundaryScanner);
								} else {
									fragmentsBuilder = new SourceSimpleFragmentsBuilder(mapper, context,
											field.preTags(), field.postTags(), boundaryScanner);
								}
							}
						}
						entry = new MapperHighlightEntry();
						entry.fragListBuilder = fragListBuilder;
						entry.fragmentsBuilder = fragmentsBuilder;
						if (cache.fvh == null) {

							cache.fvh = new FastVectorHighlighter();
						}
						CustomFieldQuery.highlightFilters.set(field.highlightFilter());
						if (field.requireFieldMatch()) {
							if (cache.fieldMatchFieldQuery == null) {

								cache.fieldMatchFieldQuery = new CustomFieldQuery(context.parsedQuery().query(),
										hitContext.topLevelReader(), true, field.requireFieldMatch());
							}
							fieldQuery = cache.fieldMatchFieldQuery;
						} else {
							if (cache.noFieldMatchFieldQuery == null) {

								cache.noFieldMatchFieldQuery = new CustomFieldQuery(context.parsedQuery().query(),
										hitContext.topLevelReader(), true, field.requireFieldMatch());
							}
							fieldQuery = cache.noFieldMatchFieldQuery;
						}
						cache.mappers.put(mapper, entry);
					}

					String[] fragments;

					int numberOfFragments = field.numberOfFragments() == 0 ? 1 : field.numberOfFragments();

					fragments = cache.fvh.getBestFragments(fieldQuery, hitContext.reader(), hitContext.docId(), mapper
							.names().indexName(), field.fragmentCharSize(), numberOfFragments, entry.fragListBuilder,
							entry.fragmentsBuilder, field.preTags(), field.postTags(), encoder);

					if (fragments != null && fragments.length > 0) {
						HighlightField highlightField = new HighlightField(field.field(), fragments);
						highlightFields.put(highlightField.name(), highlightField);
					}
				} catch (Exception e) {
					throw new FetchPhaseExecutionException(context,
							"Failed to highlight field [" + field.field() + "]", e);
				}
			}
		}

		hitContext.hit().highlightFields(highlightFields);
	}

	/**
	 * The Class MapperHighlightEntry.
	 *
	 * @author l.xue.nong
	 */
	static class MapperHighlightEntry {

		/** The frag list builder. */
		public FragListBuilder fragListBuilder;

		/** The fragments builder. */
		public FragmentsBuilder fragmentsBuilder;

		/** The highlighter. */
		public Highlighter highlighter;
	}

	/**
	 * The Class HighlighterEntry.
	 *
	 * @author l.xue.nong
	 */
	static class HighlighterEntry {

		/** The fvh. */
		public FastVectorHighlighter fvh;

		/** The no field match field query. */
		public FieldQuery noFieldMatchFieldQuery;

		/** The field match field query. */
		public FieldQuery fieldMatchFieldQuery;

		/** The mappers. */
		public Map<FieldMapper, MapperHighlightEntry> mappers = Maps.newHashMap();
	}
}
