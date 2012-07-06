/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MapperQueryParser.java 2012-3-29 15:04:16 l.xue.nong$$
 */


package org.apache.lucene.queryParser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;

import cn.com.rebirth.search.commons.io.FastStringReader;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.commons.lucene.search.Queries;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.mapper.internal.AllFieldMapper;
import cn.com.rebirth.search.core.index.query.QueryParseContext;
import cn.com.rebirth.search.core.index.query.support.QueryParsers;

import com.google.common.collect.ImmutableMap;


/**
 * The Class MapperQueryParser.
 *
 * @author l.xue.nong
 */
public class MapperQueryParser extends QueryParser {

	
	/** The Constant fieldQueryExtensions. */
	public static final ImmutableMap<String, FieldQueryExtension> fieldQueryExtensions;

	static {
		fieldQueryExtensions = ImmutableMap.<String, FieldQueryExtension> builder()
				.put(ExistsFieldQueryExtension.NAME, new ExistsFieldQueryExtension())
				.put(MissingFieldQueryExtension.NAME, new MissingFieldQueryExtension()).build();
	}

	
	/** The parse context. */
	private final QueryParseContext parseContext;

	
	/** The forced analyzer. */
	private boolean forcedAnalyzer;

	
	/** The current mapper. */
	private FieldMapper<?> currentMapper;

	
	/** The analyze wildcard. */
	private boolean analyzeWildcard;

	
	/**
	 * Instantiates a new mapper query parser.
	 *
	 * @param parseContext the parse context
	 */
	public MapperQueryParser(QueryParseContext parseContext) {
		super(Lucene.QUERYPARSER_VERSION, null, null);
		this.parseContext = parseContext;
	}

	
	/**
	 * Instantiates a new mapper query parser.
	 *
	 * @param settings the settings
	 * @param parseContext the parse context
	 */
	public MapperQueryParser(QueryParserSettings settings, QueryParseContext parseContext) {
		super(Lucene.QUERYPARSER_VERSION, settings.defaultField(), settings.defaultAnalyzer());
		this.parseContext = parseContext;
		reset(settings);
	}

	
	/**
	 * Reset.
	 *
	 * @param settings the settings
	 */
	public void reset(QueryParserSettings settings) {
		this.field = settings.defaultField();
		this.forcedAnalyzer = settings.forcedAnalyzer() != null;
		this.analyzer = forcedAnalyzer ? settings.forcedAnalyzer() : settings.defaultAnalyzer();
		setMultiTermRewriteMethod(settings.rewriteMethod());
		setEnablePositionIncrements(settings.enablePositionIncrements());
		setAutoGeneratePhraseQueries(settings.autoGeneratePhraseQueries());
		setAllowLeadingWildcard(settings.allowLeadingWildcard());
		setLowercaseExpandedTerms(settings.lowercaseExpandedTerms());
		setPhraseSlop(settings.phraseSlop());
		setDefaultOperator(settings.defaultOperator());
		setFuzzyMinSim(settings.fuzzyMinSim());
		setFuzzyPrefixLength(settings.fuzzyPrefixLength());
		this.analyzeWildcard = settings.analyzeWildcard();
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.queryParser.QueryParser#newTermQuery(org.apache.lucene.index.Term)
	 */
	@Override
	protected Query newTermQuery(Term term) {
		if (currentMapper != null) {
			Query termQuery = currentMapper.queryStringTermQuery(term);
			if (termQuery != null) {
				return termQuery;
			}
		}
		return super.newTermQuery(term);
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.queryParser.QueryParser#newMatchAllDocsQuery()
	 */
	@Override
	protected Query newMatchAllDocsQuery() {
		return Queries.MATCH_ALL_QUERY;
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.queryParser.QueryParser#getFieldQuery(java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public Query getFieldQuery(String field, String queryText, boolean quoted) throws ParseException {
		FieldQueryExtension fieldQueryExtension = fieldQueryExtensions.get(field);
		if (fieldQueryExtension != null) {
			return fieldQueryExtension.query(parseContext, queryText);
		}
		currentMapper = null;
		Analyzer oldAnalyzer = analyzer;
		try {
			MapperService.SmartNameFieldMappers fieldMappers = parseContext.smartFieldMappers(field);
			if (fieldMappers != null) {
				if (!forcedAnalyzer) {
					analyzer = fieldMappers.searchAnalyzer();
				}
				currentMapper = fieldMappers.fieldMappers().mapper();
				if (currentMapper != null) {
					Query query = null;
					if (currentMapper.useFieldQueryWithQueryString()) {
						if (fieldMappers.explicitTypeInNameWithDocMapper()) {
							String[] previousTypes = QueryParseContext.setTypesWithPrevious(new String[] { fieldMappers
									.docMapper().type() });
							try {
								query = currentMapper.fieldQuery(queryText, parseContext);
							} finally {
								QueryParseContext.setTypes(previousTypes);
							}
						} else {
							query = currentMapper.fieldQuery(queryText, parseContext);
						}
					}
					if (query == null) {
						query = super.getFieldQuery(currentMapper.names().indexName(), queryText, quoted);
					}
					return QueryParsers.wrapSmartNameQuery(query, fieldMappers, parseContext);
				}
			}
			return super.getFieldQuery(field, queryText, quoted);
		} finally {
			analyzer = oldAnalyzer;
		}
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.queryParser.QueryParser#getRangeQuery(java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	protected Query getRangeQuery(String field, String part1, String part2, boolean inclusive) throws ParseException {
		if ("*".equals(part1)) {
			part1 = null;
		}
		if ("*".equals(part2)) {
			part2 = null;
		}
		currentMapper = null;
		MapperService.SmartNameFieldMappers fieldMappers = parseContext.smartFieldMappers(field);
		if (fieldMappers != null) {
			currentMapper = fieldMappers.fieldMappers().mapper();
			if (currentMapper != null) {
				Query rangeQuery = currentMapper.rangeQuery(part1, part2, inclusive, inclusive, parseContext);
				return QueryParsers.wrapSmartNameQuery(rangeQuery, fieldMappers, parseContext);
			}
		}
		return newRangeQuery(field, part1, part2, inclusive);
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.queryParser.QueryParser#getFuzzyQuery(java.lang.String, java.lang.String, float)
	 */
	@Override
	protected Query getFuzzyQuery(String field, String termStr, float minSimilarity) throws ParseException {
		currentMapper = null;
		MapperService.SmartNameFieldMappers fieldMappers = parseContext.smartFieldMappers(field);
		if (fieldMappers != null) {
			currentMapper = fieldMappers.fieldMappers().mapper();
			if (currentMapper != null) {
				Query fuzzyQuery = currentMapper.fuzzyQuery(termStr, minSimilarity, fuzzyPrefixLength,
						FuzzyQuery.defaultMaxExpansions);
				return QueryParsers.wrapSmartNameQuery(fuzzyQuery, fieldMappers, parseContext);
			}
		}
		return super.getFuzzyQuery(field, termStr, minSimilarity);
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.queryParser.QueryParser#getPrefixQuery(java.lang.String, java.lang.String)
	 */
	@Override
	protected Query getPrefixQuery(String field, String termStr) throws ParseException {
		currentMapper = null;
		Analyzer oldAnalyzer = analyzer;
		try {
			MapperService.SmartNameFieldMappers fieldMappers = parseContext.smartFieldMappers(field);
			if (fieldMappers != null) {
				if (!forcedAnalyzer) {
					analyzer = fieldMappers.searchAnalyzer();
				}
				currentMapper = fieldMappers.fieldMappers().mapper();
				if (currentMapper != null) {
					Query query = null;
					if (currentMapper.useFieldQueryWithQueryString()) {
						if (fieldMappers.explicitTypeInNameWithDocMapper()) {
							String[] previousTypes = QueryParseContext.setTypesWithPrevious(new String[] { fieldMappers
									.docMapper().type() });
							try {
								query = currentMapper.prefixQuery(termStr, multiTermRewriteMethod, parseContext);
							} finally {
								QueryParseContext.setTypes(previousTypes);
							}
						} else {
							query = currentMapper.prefixQuery(termStr, multiTermRewriteMethod, parseContext);
						}
					}
					if (query == null) {
						query = getPossiblyAnalyzedPrefixQuery(currentMapper.names().indexName(), termStr);
					}
					return QueryParsers.wrapSmartNameQuery(query, fieldMappers, parseContext);
				}
			}
			return getPossiblyAnalyzedPrefixQuery(field, termStr);
		} finally {
			analyzer = oldAnalyzer;
		}
	}

	
	/**
	 * Gets the possibly analyzed prefix query.
	 *
	 * @param field the field
	 * @param termStr the term str
	 * @return the possibly analyzed prefix query
	 * @throws ParseException the parse exception
	 */
	private Query getPossiblyAnalyzedPrefixQuery(String field, String termStr) throws ParseException {
		if (!analyzeWildcard) {
			return super.getPrefixQuery(field, termStr);
		}
		
		TokenStream source;
		try {
			source = getAnalyzer().reusableTokenStream(field, new StringReader(termStr));
		} catch (IOException e) {
			return super.getPrefixQuery(field, termStr);
		}
		List<String> tlist = new ArrayList<String>();
		CharTermAttribute termAtt = source.addAttribute(CharTermAttribute.class);

		while (true) {
			try {
				if (!source.incrementToken())
					break;
			} catch (IOException e) {
				break;
			}
			tlist.add(termAtt.toString());
		}

		try {
			source.close();
		} catch (IOException e) {
			
		}

		if (tlist.size() == 1) {
			return super.getPrefixQuery(field, tlist.get(0));
		} else {
			
			List<BooleanClause> clauses = new ArrayList<BooleanClause>();
			for (String token : tlist) {
				clauses.add(new BooleanClause(super.getPrefixQuery(field, token), BooleanClause.Occur.SHOULD));
			}
			return getBooleanQuery(clauses, true);
			
		}

	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.queryParser.QueryParser#getWildcardQuery(java.lang.String, java.lang.String)
	 */
	@Override
	protected Query getWildcardQuery(String field, String termStr) throws ParseException {
		if (AllFieldMapper.NAME.equals(field) && termStr.equals("*")) {
			return newMatchAllDocsQuery();
		}
		String indexedNameField = field;
		currentMapper = null;
		Analyzer oldAnalyzer = analyzer;
		try {
			MapperService.SmartNameFieldMappers fieldMappers = parseContext.smartFieldMappers(field);
			if (fieldMappers != null) {
				if (!forcedAnalyzer) {
					analyzer = fieldMappers.searchAnalyzer();
				}
				currentMapper = fieldMappers.fieldMappers().mapper();
				if (currentMapper != null) {
					indexedNameField = currentMapper.names().indexName();
				}
				return QueryParsers.wrapSmartNameQuery(getPossiblyAnalyzedWildcardQuery(indexedNameField, termStr),
						fieldMappers, parseContext);
			}
			return getPossiblyAnalyzedWildcardQuery(indexedNameField, termStr);
		} finally {
			analyzer = oldAnalyzer;
		}
	}

	
	/**
	 * Gets the possibly analyzed wildcard query.
	 *
	 * @param field the field
	 * @param termStr the term str
	 * @return the possibly analyzed wildcard query
	 * @throws ParseException the parse exception
	 */
	private Query getPossiblyAnalyzedWildcardQuery(String field, String termStr) throws ParseException {
		if (!analyzeWildcard) {
			return super.getWildcardQuery(field, termStr);
		}
		boolean isWithinToken = (!termStr.startsWith("?") && !termStr.startsWith("*"));
		StringBuilder aggStr = new StringBuilder();
		StringBuilder tmp = new StringBuilder();
		for (int i = 0; i < termStr.length(); i++) {
			char c = termStr.charAt(i);
			if (c == '?' || c == '*') {
				if (isWithinToken) {
					try {
						TokenStream source = getAnalyzer().reusableTokenStream(field,
								new FastStringReader(tmp.toString()));
						CharTermAttribute termAtt = source.addAttribute(CharTermAttribute.class);
						if (source.incrementToken()) {
							String term = termAtt.toString();
							if (term.length() == 0) {
								
								aggStr.append(tmp);
							} else {
								aggStr.append(term);
							}
						} else {
							
							aggStr.append(tmp);
						}
						source.close();
					} catch (IOException e) {
						aggStr.append(tmp);
					}
					tmp.setLength(0);
				}
				isWithinToken = false;
				aggStr.append(c);
			} else {
				tmp.append(c);
				isWithinToken = true;
			}
		}
		if (isWithinToken) {
			try {
				TokenStream source = getAnalyzer().reusableTokenStream(field, new FastStringReader(tmp.toString()));
				CharTermAttribute termAtt = source.addAttribute(CharTermAttribute.class);
				if (source.incrementToken()) {
					String term = termAtt.toString();
					if (term.length() == 0) {
						
						aggStr.append(tmp);
					} else {
						aggStr.append(term);
					}
				} else {
					
					aggStr.append(tmp);
				}
				source.close();
			} catch (IOException e) {
				aggStr.append(tmp);
			}
		}

		return super.getWildcardQuery(field, aggStr.toString());
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.queryParser.QueryParser#getBooleanQuery(java.util.List, boolean)
	 */
	@Override
	protected Query getBooleanQuery(List<BooleanClause> clauses, boolean disableCoord) throws ParseException {
		Query q = super.getBooleanQuery(clauses, disableCoord);
		if (q == null) {
			return null;
		}
		return Queries.optimizeQuery(Queries.fixNegativeQueryIfNeeded(q));
	}
}
