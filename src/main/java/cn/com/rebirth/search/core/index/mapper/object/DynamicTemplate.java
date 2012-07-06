/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DynamicTemplate.java 2012-7-6 14:30:25 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.regex.Regex;
import cn.com.rebirth.search.core.index.mapper.ContentPath;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;

import com.google.common.collect.Maps;

/**
 * The Class DynamicTemplate.
 *
 * @author l.xue.nong
 */
public class DynamicTemplate {

	/**
	 * The Enum MatchType.
	 *
	 * @author l.xue.nong
	 */
	public static enum MatchType {

		/** The simple. */
		SIMPLE,

		/** The regex. */
		REGEX;

		/**
		 * From string.
		 *
		 * @param value the value
		 * @return the match type
		 */
		public static MatchType fromString(String value) {
			if ("simple".equals(value)) {
				return SIMPLE;
			} else if ("regex".equals(value)) {
				return REGEX;
			}
			throw new RebirthIllegalArgumentException("No matching pattern matched on [" + value + "]");
		}
	}

	/**
	 * Parses the.
	 *
	 * @param name the name
	 * @param conf the conf
	 * @return the dynamic template
	 * @throws MapperParsingException the mapper parsing exception
	 */
	public static DynamicTemplate parse(String name, Map<String, Object> conf) throws MapperParsingException {
		String match = null;
		String pathMatch = null;
		String unmatch = null;
		String pathUnmatch = null;
		Map<String, Object> mapping = null;
		String matchMappingType = null;
		String matchPattern = "simple";

		for (Map.Entry<String, Object> entry : conf.entrySet()) {
			String propName = Strings.toUnderscoreCase(entry.getKey());
			if ("match".equals(propName)) {
				match = entry.getValue().toString();
			} else if ("path_match".equals(propName)) {
				pathMatch = entry.getValue().toString();
			} else if ("unmatch".equals(propName)) {
				unmatch = entry.getValue().toString();
			} else if ("path_unmatch".equals(propName)) {
				pathUnmatch = entry.getValue().toString();
			} else if ("match_mapping_type".equals(propName)) {
				matchMappingType = entry.getValue().toString();
			} else if ("match_pattern".equals(propName)) {
				matchPattern = entry.getValue().toString();
			} else if ("mapping".equals(propName)) {
				mapping = (Map<String, Object>) entry.getValue();
			}
		}

		if (match == null && pathMatch == null) {
			throw new MapperParsingException("template must have match or path_match set");
		}
		if (mapping == null) {
			throw new MapperParsingException("template must have mapping set");
		}
		return new DynamicTemplate(name, conf, pathMatch, pathUnmatch, match, unmatch, matchMappingType,
				MatchType.fromString(matchPattern), mapping);
	}

	/** The name. */
	private final String name;

	/** The conf. */
	private final Map<String, Object> conf;

	/** The path match. */
	private final String pathMatch;

	/** The path unmatch. */
	private final String pathUnmatch;

	/** The match. */
	private final String match;

	/** The unmatch. */
	private final String unmatch;

	/** The match type. */
	private final MatchType matchType;

	/** The match mapping type. */
	private final String matchMappingType;

	/** The mapping. */
	private final Map<String, Object> mapping;

	/**
	 * Instantiates a new dynamic template.
	 *
	 * @param name the name
	 * @param conf the conf
	 * @param pathMatch the path match
	 * @param pathUnmatch the path unmatch
	 * @param match the match
	 * @param unmatch the unmatch
	 * @param matchMappingType the match mapping type
	 * @param matchType the match type
	 * @param mapping the mapping
	 */
	public DynamicTemplate(String name, Map<String, Object> conf, String pathMatch, String pathUnmatch, String match,
			String unmatch, String matchMappingType, MatchType matchType, Map<String, Object> mapping) {
		this.name = name;
		this.conf = new TreeMap<String, Object>(conf);
		this.pathMatch = pathMatch;
		this.pathUnmatch = pathUnmatch;
		this.match = match;
		this.unmatch = unmatch;
		this.matchType = matchType;
		this.matchMappingType = matchMappingType;
		this.mapping = mapping;
	}

	/**
	 * Name.
	 *
	 * @return the string
	 */
	public String name() {
		return this.name;
	}

	/**
	 * Conf.
	 *
	 * @return the map
	 */
	public Map<String, Object> conf() {
		return this.conf;
	}

	/**
	 * Match.
	 *
	 * @param path the path
	 * @param name the name
	 * @param dynamicType the dynamic type
	 * @return true, if successful
	 */
	public boolean match(ContentPath path, String name, String dynamicType) {
		if (pathMatch != null && !patternMatch(pathMatch, path.fullPathAsText(name))) {
			return false;
		}
		if (match != null && !patternMatch(match, name)) {
			return false;
		}
		if (pathUnmatch != null && patternMatch(pathUnmatch, path.fullPathAsText(name))) {
			return false;
		}
		if (unmatch != null && patternMatch(unmatch, name)) {
			return false;
		}
		if (matchMappingType != null) {
			if (dynamicType == null) {
				return false;
			}
			if (!patternMatch(matchMappingType, dynamicType)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks for type.
	 *
	 * @return true, if successful
	 */
	public boolean hasType() {
		return mapping.containsKey("type");
	}

	/**
	 * Mapping type.
	 *
	 * @param dynamicType the dynamic type
	 * @return the string
	 */
	public String mappingType(String dynamicType) {
		return mapping.containsKey("type") ? mapping.get("type").toString() : dynamicType;
	}

	/**
	 * Pattern match.
	 *
	 * @param pattern the pattern
	 * @param str the str
	 * @return true, if successful
	 */
	private boolean patternMatch(String pattern, String str) {
		if (matchType == MatchType.SIMPLE) {
			return Regex.simpleMatch(pattern, str);
		}
		return str.matches(pattern);
	}

	/**
	 * Mapping for name.
	 *
	 * @param name the name
	 * @param dynamicType the dynamic type
	 * @return the map
	 */
	public Map<String, Object> mappingForName(String name, String dynamicType) {
		return processMap(mapping, name, dynamicType);
	}

	/**
	 * Process map.
	 *
	 * @param map the map
	 * @param name the name
	 * @param dynamicType the dynamic type
	 * @return the map
	 */
	private Map<String, Object> processMap(Map<String, Object> map, String name, String dynamicType) {
		Map<String, Object> processedMap = Maps.newHashMap();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey().replace("{name}", name).replace("{dynamic_type}", dynamicType)
					.replace("{dynamicType}", dynamicType);
			Object value = entry.getValue();
			if (value instanceof Map) {
				value = processMap((Map<String, Object>) value, name, dynamicType);
			} else if (value instanceof List) {
				value = processList((List) value, name, dynamicType);
			} else if (value instanceof String) {
				value = value.toString().replace("{name}", name).replace("{dynamic_type}", dynamicType)
						.replace("{dynamicType}", dynamicType);
			}
			processedMap.put(key, value);
		}
		return processedMap;
	}

	/**
	 * Process list.
	 *
	 * @param list the list
	 * @param name the name
	 * @param dynamicType the dynamic type
	 * @return the list
	 */
	private List processList(List list, String name, String dynamicType) {
		List processedList = new ArrayList();
		for (Object value : list) {
			if (value instanceof Map) {
				value = processMap((Map<String, Object>) value, name, dynamicType);
			} else if (value instanceof List) {
				value = processList((List) value, name, dynamicType);
			} else if (value instanceof String) {
				value = value.toString().replace("{name}", name).replace("{dynamic_type}", dynamicType)
						.replace("{dynamicType}", dynamicType);
			}
			processedList.add(value);
		}
		return processedList;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		DynamicTemplate that = (DynamicTemplate) o;

		if (match != null ? !match.equals(that.match) : that.match != null)
			return false;
		if (matchMappingType != null ? !matchMappingType.equals(that.matchMappingType) : that.matchMappingType != null)
			return false;
		if (matchType != that.matchType)
			return false;
		if (unmatch != null ? !unmatch.equals(that.unmatch) : that.unmatch != null)
			return false;

		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		int result = match != null ? match.hashCode() : 0;
		result = 31 * result + (unmatch != null ? unmatch.hashCode() : 0);
		result = 31 * result + (matchType != null ? matchType.hashCode() : 0);
		result = 31 * result + (matchMappingType != null ? matchMappingType.hashCode() : 0);
		return result;
	}
}
