/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TextQueryParser.java 2012-7-6 14:30:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.search;

import static cn.com.rebirth.search.core.index.query.support.QueryParsers.wrapSmartNameQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.exception.RebirthIllegalStateException;
import cn.com.rebirth.search.commons.io.FastStringReader;
import cn.com.rebirth.search.commons.lucene.search.MatchNoDocsQuery;
import cn.com.rebirth.search.commons.lucene.search.MultiPhrasePrefixQuery;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.query.QueryParseContext;

/**
 * The Class TextQueryParser.
 *
 * @author l.xue.nong
 */
public class TextQueryParser {

	/**
	 * The Enum Type.
	 *
	 * @author l.xue.nong
	 */
	public static enum Type {

		/** The boolean. */
		BOOLEAN,

		/** The phrase. */
		PHRASE,

		/** The phrase prefix. */
		PHRASE_PREFIX
	}

	/** The parse context. */
	private final QueryParseContext parseContext;

	/** The field name. */
	private final String fieldName;

	/** The text. */
	private final String text;

	/** The analyzer. */
	private String analyzer;

	/** The occur. */
	private BooleanClause.Occur occur = BooleanClause.Occur.SHOULD;

	/** The enable position increments. */
	private boolean enablePositionIncrements = true;

	/** The phrase slop. */
	private int phraseSlop = 0;

	/** The fuzziness. */
	private String fuzziness = null;

	/** The fuzzy prefix length. */
	private int fuzzyPrefixLength = FuzzyQuery.defaultPrefixLength;

	/** The max expansions. */
	private int maxExpansions = FuzzyQuery.defaultMaxExpansions;

	/**
	 * Instantiates a new text query parser.
	 *
	 * @param parseContext the parse context
	 * @param fieldName the field name
	 * @param text the text
	 */
	public TextQueryParser(QueryParseContext parseContext, String fieldName, String text) {
		this.parseContext = parseContext;
		this.fieldName = fieldName;
		this.text = text;
	}

	/**
	 * Sets the analyzer.
	 *
	 * @param analyzer the new analyzer
	 */
	public void setAnalyzer(String analyzer) {
		this.analyzer = analyzer;
	}

	/**
	 * Sets the occur.
	 *
	 * @param occur the new occur
	 */
	public void setOccur(BooleanClause.Occur occur) {
		this.occur = occur;
	}

	/**
	 * Sets the enable position increments.
	 *
	 * @param enablePositionIncrements the new enable position increments
	 */
	public void setEnablePositionIncrements(boolean enablePositionIncrements) {
		this.enablePositionIncrements = enablePositionIncrements;
	}

	/**
	 * Sets the phrase slop.
	 *
	 * @param phraseSlop the new phrase slop
	 */
	public void setPhraseSlop(int phraseSlop) {
		this.phraseSlop = phraseSlop;
	}

	/**
	 * Sets the fuzziness.
	 *
	 * @param fuzziness the new fuzziness
	 */
	public void setFuzziness(String fuzziness) {
		this.fuzziness = fuzziness;
	}

	/**
	 * Sets the fuzzy prefix length.
	 *
	 * @param fuzzyPrefixLength the new fuzzy prefix length
	 */
	public void setFuzzyPrefixLength(int fuzzyPrefixLength) {
		this.fuzzyPrefixLength = fuzzyPrefixLength;
	}

	/**
	 * Sets the max expansions.
	 *
	 * @param maxExpansions the new max expansions
	 */
	public void setMaxExpansions(int maxExpansions) {
		this.maxExpansions = maxExpansions;
	}

	/**
	 * Parses the.
	 *
	 * @param type the type
	 * @return the query
	 */
	public Query parse(Type type) {
		FieldMapper mapper = null;
		Term fieldTerm;
		MapperService.SmartNameFieldMappers smartNameFieldMappers = parseContext.smartFieldMappers(fieldName);
		if (smartNameFieldMappers != null && smartNameFieldMappers.hasMapper()) {
			mapper = smartNameFieldMappers.mapper();
			fieldTerm = mapper.names().indexNameTerm();
		} else {
			fieldTerm = new Term(fieldName);
		}

		if (mapper != null && mapper.useFieldQueryWithQueryString()) {
			if (smartNameFieldMappers.explicitTypeInNameWithDocMapper()) {
				String[] previousTypes = QueryParseContext.setTypesWithPrevious(new String[] { smartNameFieldMappers
						.docMapper().type() });
				try {
					return wrapSmartNameQuery(mapper.fieldQuery(text, parseContext), smartNameFieldMappers,
							parseContext);
				} finally {
					QueryParseContext.setTypes(previousTypes);
				}
			} else {
				return wrapSmartNameQuery(mapper.fieldQuery(text, parseContext), smartNameFieldMappers, parseContext);
			}
		}

		Analyzer analyzer = null;
		if (this.analyzer == null) {
			if (mapper != null) {
				analyzer = mapper.searchAnalyzer();
			}
			if (analyzer == null && smartNameFieldMappers != null) {
				analyzer = smartNameFieldMappers.searchAnalyzer();
			}
			if (analyzer == null) {
				analyzer = parseContext.mapperService().searchAnalyzer();
			}
		} else {
			analyzer = parseContext.mapperService().analysisService().analyzer(this.analyzer);
			if (analyzer == null) {
				throw new RebirthIllegalArgumentException("No analyzer found for [" + this.analyzer + "]");
			}
		}

		TokenStream source;
		try {
			source = analyzer.reusableTokenStream(fieldTerm.field(), new FastStringReader(text));
			source.reset();
		} catch (IOException e) {
			source = analyzer.tokenStream(fieldTerm.field(), new FastStringReader(text));
		}
		CachingTokenFilter buffer = new CachingTokenFilter(source);
		CharTermAttribute termAtt = null;
		PositionIncrementAttribute posIncrAtt = null;
		int numTokens = 0;

		boolean success = false;
		try {
			buffer.reset();
			success = true;
		} catch (IOException e) {

		}
		if (success) {
			if (buffer.hasAttribute(CharTermAttribute.class)) {
				termAtt = buffer.getAttribute(CharTermAttribute.class);
			}
			if (buffer.hasAttribute(PositionIncrementAttribute.class)) {
				posIncrAtt = buffer.getAttribute(PositionIncrementAttribute.class);
			}
		}

		int positionCount = 0;
		boolean severalTokensAtSamePosition = false;

		boolean hasMoreTokens = false;
		if (termAtt != null) {
			try {
				hasMoreTokens = buffer.incrementToken();
				while (hasMoreTokens) {
					numTokens++;
					int positionIncrement = (posIncrAtt != null) ? posIncrAtt.getPositionIncrement() : 1;
					if (positionIncrement != 0) {
						positionCount += positionIncrement;
					} else {
						severalTokensAtSamePosition = true;
					}
					hasMoreTokens = buffer.incrementToken();
				}
			} catch (IOException e) {

			}
		}
		try {

			buffer.reset();

			source.close();
		} catch (IOException e) {

		}

		if (numTokens == 0) {
			return MatchNoDocsQuery.INSTANCE;
		} else if (type == Type.BOOLEAN) {
			if (numTokens == 1) {
				String term = null;
				try {
					boolean hasNext = buffer.incrementToken();
					assert hasNext == true;
					term = termAtt.toString();
				} catch (IOException e) {

				}
				Query q = newTermQuery(mapper, fieldTerm.createTerm(term));
				return wrapSmartNameQuery(q, smartNameFieldMappers, parseContext);
			}
			BooleanQuery q = new BooleanQuery(positionCount == 1);
			for (int i = 0; i < numTokens; i++) {
				String term = null;
				try {
					boolean hasNext = buffer.incrementToken();
					assert hasNext == true;
					term = termAtt.toString();
				} catch (IOException e) {

				}

				Query currentQuery = newTermQuery(mapper, fieldTerm.createTerm(term));
				q.add(currentQuery, occur);
			}
			return wrapSmartNameQuery(q, smartNameFieldMappers, parseContext);
		} else if (type == Type.PHRASE) {
			if (severalTokensAtSamePosition) {
				MultiPhraseQuery mpq = new MultiPhraseQuery();
				mpq.setSlop(phraseSlop);
				List<Term> multiTerms = new ArrayList<Term>();
				int position = -1;
				for (int i = 0; i < numTokens; i++) {
					String term = null;
					int positionIncrement = 1;
					try {
						boolean hasNext = buffer.incrementToken();
						assert hasNext == true;
						term = termAtt.toString();
						if (posIncrAtt != null) {
							positionIncrement = posIncrAtt.getPositionIncrement();
						}
					} catch (IOException e) {

					}

					if (positionIncrement > 0 && multiTerms.size() > 0) {
						if (enablePositionIncrements) {
							mpq.add(multiTerms.toArray(new Term[multiTerms.size()]), position);
						} else {
							mpq.add(multiTerms.toArray(new Term[multiTerms.size()]));
						}
						multiTerms.clear();
					}
					position += positionIncrement;
					multiTerms.add(fieldTerm.createTerm(term));
				}
				if (enablePositionIncrements) {
					mpq.add(multiTerms.toArray(new Term[multiTerms.size()]), position);
				} else {
					mpq.add(multiTerms.toArray(new Term[multiTerms.size()]));
				}
				return wrapSmartNameQuery(mpq, smartNameFieldMappers, parseContext);
			} else {
				PhraseQuery pq = new PhraseQuery();
				pq.setSlop(phraseSlop);
				int position = -1;

				for (int i = 0; i < numTokens; i++) {
					String term = null;
					int positionIncrement = 1;

					try {
						boolean hasNext = buffer.incrementToken();
						assert hasNext == true;
						term = termAtt.toString();
						if (posIncrAtt != null) {
							positionIncrement = posIncrAtt.getPositionIncrement();
						}
					} catch (IOException e) {

					}

					if (enablePositionIncrements) {
						position += positionIncrement;
						pq.add(fieldTerm.createTerm(term), position);
					} else {
						pq.add(fieldTerm.createTerm(term));
					}
				}
				return wrapSmartNameQuery(pq, smartNameFieldMappers, parseContext);
			}
		} else if (type == Type.PHRASE_PREFIX) {
			MultiPhrasePrefixQuery mpq = new MultiPhrasePrefixQuery();
			mpq.setSlop(phraseSlop);
			mpq.setMaxExpansions(maxExpansions);
			List<Term> multiTerms = new ArrayList<Term>();
			int position = -1;
			for (int i = 0; i < numTokens; i++) {
				String term = null;
				int positionIncrement = 1;
				try {
					boolean hasNext = buffer.incrementToken();
					assert hasNext == true;
					term = termAtt.toString();
					if (posIncrAtt != null) {
						positionIncrement = posIncrAtt.getPositionIncrement();
					}
				} catch (IOException e) {

				}

				if (positionIncrement > 0 && multiTerms.size() > 0) {
					if (enablePositionIncrements) {
						mpq.add(multiTerms.toArray(new Term[multiTerms.size()]), position);
					} else {
						mpq.add(multiTerms.toArray(new Term[multiTerms.size()]));
					}
					multiTerms.clear();
				}
				position += positionIncrement;
				multiTerms.add(fieldTerm.createTerm(term));
			}
			if (enablePositionIncrements) {
				mpq.add(multiTerms.toArray(new Term[multiTerms.size()]), position);
			} else {
				mpq.add(multiTerms.toArray(new Term[multiTerms.size()]));
			}
			return wrapSmartNameQuery(mpq, smartNameFieldMappers, parseContext);
		}

		throw new RebirthIllegalStateException("No type found for [" + type + "]");
	}

	/**
	 * New term query.
	 *
	 * @param mapper the mapper
	 * @param term the term
	 * @return the query
	 */
	private Query newTermQuery(@Nullable FieldMapper mapper, Term term) {
		if (fuzziness != null) {
			if (mapper != null) {
				return mapper.fuzzyQuery(term.text(), fuzziness, fuzzyPrefixLength, maxExpansions);
			}
			return new FuzzyQuery(term, Float.parseFloat(fuzziness), fuzzyPrefixLength, maxExpansions);
		}
		if (mapper != null) {
			Query termQuery = mapper.queryStringTermQuery(term);
			if (termQuery != null) {
				return termQuery;
			}
		}
		return new TermQuery(term);
	}
}