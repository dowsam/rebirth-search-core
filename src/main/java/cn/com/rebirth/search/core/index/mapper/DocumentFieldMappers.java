/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DocumentFieldMappers.java 2012-7-6 14:30:41 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;

import cn.com.rebirth.commons.regex.Regex;
import cn.com.rebirth.search.core.index.analysis.FieldNameAnalyzer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;

/**
 * The Class DocumentFieldMappers.
 *
 * @author l.xue.nong
 */
public class DocumentFieldMappers implements Iterable<FieldMapper> {

	/** The field mappers. */
	private final ImmutableList<FieldMapper> fieldMappers;

	/** The full name field mappers. */
	private final Map<String, FieldMappers> fullNameFieldMappers;

	/** The name field mappers. */
	private final Map<String, FieldMappers> nameFieldMappers;

	/** The index name field mappers. */
	private final Map<String, FieldMappers> indexNameFieldMappers;

	/** The index analyzer. */
	private final FieldNameAnalyzer indexAnalyzer;

	/** The search analyzer. */
	private final FieldNameAnalyzer searchAnalyzer;

	/**
	 * Instantiates a new document field mappers.
	 *
	 * @param docMapper the doc mapper
	 * @param fieldMappers the field mappers
	 */
	public DocumentFieldMappers(DocumentMapper docMapper, Iterable<FieldMapper> fieldMappers) {
		final Map<String, FieldMappers> tempNameFieldMappers = newHashMap();
		final Map<String, FieldMappers> tempIndexNameFieldMappers = newHashMap();
		final Map<String, FieldMappers> tempFullNameFieldMappers = newHashMap();

		final Map<String, Analyzer> indexAnalyzers = newHashMap();
		final Map<String, Analyzer> searchAnalyzers = newHashMap();

		for (FieldMapper fieldMapper : fieldMappers) {
			FieldMappers mappers = tempNameFieldMappers.get(fieldMapper.names().name());
			if (mappers == null) {
				mappers = new FieldMappers(fieldMapper);
			} else {
				mappers = mappers.concat(fieldMapper);
			}
			tempNameFieldMappers.put(fieldMapper.names().name(), mappers);

			mappers = tempIndexNameFieldMappers.get(fieldMapper.names().indexName());
			if (mappers == null) {
				mappers = new FieldMappers(fieldMapper);
			} else {
				mappers = mappers.concat(fieldMapper);
			}
			tempIndexNameFieldMappers.put(fieldMapper.names().indexName(), mappers);

			mappers = tempFullNameFieldMappers.get(fieldMapper.names().fullName());
			if (mappers == null) {
				mappers = new FieldMappers(fieldMapper);
			} else {
				mappers = mappers.concat(fieldMapper);
			}
			tempFullNameFieldMappers.put(fieldMapper.names().fullName(), mappers);

			if (fieldMapper.indexAnalyzer() != null) {
				indexAnalyzers.put(fieldMapper.names().indexName(), fieldMapper.indexAnalyzer());
			}
			if (fieldMapper.searchAnalyzer() != null) {
				searchAnalyzers.put(fieldMapper.names().indexName(), fieldMapper.searchAnalyzer());
			}
		}
		this.fieldMappers = ImmutableList.copyOf(fieldMappers);
		this.nameFieldMappers = ImmutableMap.copyOf(tempNameFieldMappers);
		this.indexNameFieldMappers = ImmutableMap.copyOf(tempIndexNameFieldMappers);
		this.fullNameFieldMappers = ImmutableMap.copyOf(tempFullNameFieldMappers);

		this.indexAnalyzer = new FieldNameAnalyzer(indexAnalyzers, docMapper.indexAnalyzer());
		this.searchAnalyzer = new FieldNameAnalyzer(searchAnalyzers, docMapper.searchAnalyzer());
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public UnmodifiableIterator<FieldMapper> iterator() {
		return fieldMappers.iterator();
	}

	/**
	 * Mappers.
	 *
	 * @return the immutable list
	 */
	public ImmutableList<FieldMapper> mappers() {
		return this.fieldMappers;
	}

	/**
	 * Checks for mapper.
	 *
	 * @param fieldMapper the field mapper
	 * @return true, if successful
	 */
	public boolean hasMapper(FieldMapper fieldMapper) {
		return fieldMappers.contains(fieldMapper);
	}

	/**
	 * Name.
	 *
	 * @param name the name
	 * @return the field mappers
	 */
	public FieldMappers name(String name) {
		return nameFieldMappers.get(name);
	}

	/**
	 * Index name.
	 *
	 * @param indexName the index name
	 * @return the field mappers
	 */
	public FieldMappers indexName(String indexName) {
		return indexNameFieldMappers.get(indexName);
	}

	/**
	 * Full name.
	 *
	 * @param fullName the full name
	 * @return the field mappers
	 */
	public FieldMappers fullName(String fullName) {
		return fullNameFieldMappers.get(fullName);
	}

	/**
	 * Simple match to index names.
	 *
	 * @param pattern the pattern
	 * @return the sets the
	 */
	public Set<String> simpleMatchToIndexNames(String pattern) {
		Set<String> fields = Sets.newHashSet();
		for (FieldMapper fieldMapper : fieldMappers) {
			if (Regex.simpleMatch(pattern, fieldMapper.names().fullName())) {
				fields.add(fieldMapper.names().indexName());
			} else if (Regex.simpleMatch(pattern, fieldMapper.names().indexName())) {
				fields.add(fieldMapper.names().name());
			} else if (Regex.simpleMatch(pattern, fieldMapper.names().name())) {
				fields.add(fieldMapper.names().indexName());
			}
		}
		return fields;
	}

	/**
	 * Smart name.
	 *
	 * @param name the name
	 * @return the field mappers
	 */
	public FieldMappers smartName(String name) {
		FieldMappers fieldMappers = fullName(name);
		if (fieldMappers != null) {
			return fieldMappers;
		}
		fieldMappers = indexName(name);
		if (fieldMappers != null) {
			return fieldMappers;
		}
		return name(name);
	}

	/**
	 * Smart name field mapper.
	 *
	 * @param name the name
	 * @return the field mapper
	 */
	public FieldMapper smartNameFieldMapper(String name) {
		FieldMappers fieldMappers = smartName(name);
		if (fieldMappers == null) {
			return null;
		}
		return fieldMappers.mapper();
	}

	/**
	 * Index analyzer.
	 *
	 * @return the analyzer
	 */
	public Analyzer indexAnalyzer() {
		return this.indexAnalyzer;
	}

	/**
	 * Index analyzer.
	 *
	 * @param defaultAnalyzer the default analyzer
	 * @return the analyzer
	 */
	public Analyzer indexAnalyzer(Analyzer defaultAnalyzer) {
		return new FieldNameAnalyzer(indexAnalyzer.analyzers(), defaultAnalyzer);
	}

	/**
	 * Search analyzer.
	 *
	 * @return the analyzer
	 */
	public Analyzer searchAnalyzer() {
		return this.searchAnalyzer;
	}

	/**
	 * Concat.
	 *
	 * @param docMapper the doc mapper
	 * @param fieldMappers the field mappers
	 * @return the document field mappers
	 */
	public DocumentFieldMappers concat(DocumentMapper docMapper, FieldMapper... fieldMappers) {
		return concat(docMapper, newArrayList(fieldMappers));
	}

	/**
	 * Concat.
	 *
	 * @param docMapper the doc mapper
	 * @param fieldMappers the field mappers
	 * @return the document field mappers
	 */
	public DocumentFieldMappers concat(DocumentMapper docMapper, Iterable<FieldMapper> fieldMappers) {
		return new DocumentFieldMappers(docMapper, Iterables.concat(this.fieldMappers, fieldMappers));
	}
}
