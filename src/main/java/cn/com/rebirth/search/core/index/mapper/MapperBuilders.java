/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MapperBuilders.java 2012-7-6 14:29:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.core.index.mapper.core.BinaryFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.BooleanFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.ByteFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.DateFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.DoubleFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.FloatFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.IntegerFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.LongFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.ShortFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.StringFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.AllFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.AnalyzerMapper;
import cn.com.rebirth.search.core.index.mapper.internal.BoostFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.IdFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.IndexFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.RoutingFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.SourceFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.TimestampFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.TypeFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.UidFieldMapper;
import cn.com.rebirth.search.core.index.mapper.ip.IpFieldMapper;
import cn.com.rebirth.search.core.index.mapper.multifield.MultiFieldMapper;
import cn.com.rebirth.search.core.index.mapper.object.ObjectMapper;
import cn.com.rebirth.search.core.index.mapper.object.RootObjectMapper;

/**
 * The Class MapperBuilders.
 *
 * @author l.xue.nong
 */
public final class MapperBuilders {

	/**
	 * Instantiates a new mapper builders.
	 */
	private MapperBuilders() {

	}

	/**
	 * Doc.
	 *
	 * @param index the index
	 * @param objectBuilder the object builder
	 * @return the document mapper. builder
	 */
	public static DocumentMapper.Builder doc(String index, RootObjectMapper.Builder objectBuilder) {
		return new DocumentMapper.Builder(index, null, objectBuilder);
	}

	/**
	 * Doc.
	 *
	 * @param index the index
	 * @param settings the settings
	 * @param objectBuilder the object builder
	 * @return the document mapper. builder
	 */
	public static DocumentMapper.Builder doc(String index, @Nullable Settings settings,
			RootObjectMapper.Builder objectBuilder) {
		return new DocumentMapper.Builder(index, settings, objectBuilder);
	}

	/**
	 * Source.
	 *
	 * @return the source field mapper. builder
	 */
	public static SourceFieldMapper.Builder source() {
		return new SourceFieldMapper.Builder();
	}

	/**
	 * Id.
	 *
	 * @return the id field mapper. builder
	 */
	public static IdFieldMapper.Builder id() {
		return new IdFieldMapper.Builder();
	}

	/**
	 * Routing.
	 *
	 * @return the routing field mapper. builder
	 */
	public static RoutingFieldMapper.Builder routing() {
		return new RoutingFieldMapper.Builder();
	}

	/**
	 * Uid.
	 *
	 * @return the uid field mapper. builder
	 */
	public static UidFieldMapper.Builder uid() {
		return new UidFieldMapper.Builder();
	}

	/**
	 * Type.
	 *
	 * @return the type field mapper. builder
	 */
	public static TypeFieldMapper.Builder type() {
		return new TypeFieldMapper.Builder();
	}

	/**
	 * Index.
	 *
	 * @return the index field mapper. builder
	 */
	public static IndexFieldMapper.Builder index() {
		return new IndexFieldMapper.Builder();
	}

	/**
	 * Timestamp.
	 *
	 * @return the timestamp field mapper. builder
	 */
	public static TimestampFieldMapper.Builder timestamp() {
		return new TimestampFieldMapper.Builder();
	}

	/**
	 * Boost.
	 *
	 * @param name the name
	 * @return the boost field mapper. builder
	 */
	public static BoostFieldMapper.Builder boost(String name) {
		return new BoostFieldMapper.Builder(name);
	}

	/**
	 * All.
	 *
	 * @return the all field mapper. builder
	 */
	public static AllFieldMapper.Builder all() {
		return new AllFieldMapper.Builder();
	}

	/**
	 * Analyzer.
	 *
	 * @return the analyzer mapper. builder
	 */
	public static AnalyzerMapper.Builder analyzer() {
		return new AnalyzerMapper.Builder();
	}

	/**
	 * Multi field.
	 *
	 * @param name the name
	 * @return the multi field mapper. builder
	 */
	public static MultiFieldMapper.Builder multiField(String name) {
		return new MultiFieldMapper.Builder(name);
	}

	/**
	 * Root object.
	 *
	 * @param name the name
	 * @return the root object mapper. builder
	 */
	public static RootObjectMapper.Builder rootObject(String name) {
		return new RootObjectMapper.Builder(name);
	}

	/**
	 * Object.
	 *
	 * @param name the name
	 * @return the object mapper. builder
	 */
	public static ObjectMapper.Builder object(String name) {
		return new ObjectMapper.Builder(name);
	}

	/**
	 * Boolean field.
	 *
	 * @param name the name
	 * @return the boolean field mapper. builder
	 */
	public static BooleanFieldMapper.Builder booleanField(String name) {
		return new BooleanFieldMapper.Builder(name);
	}

	/**
	 * String field.
	 *
	 * @param name the name
	 * @return the string field mapper. builder
	 */
	public static StringFieldMapper.Builder stringField(String name) {
		return new StringFieldMapper.Builder(name);
	}

	/**
	 * Binary field.
	 *
	 * @param name the name
	 * @return the binary field mapper. builder
	 */
	public static BinaryFieldMapper.Builder binaryField(String name) {
		return new BinaryFieldMapper.Builder(name);
	}

	/**
	 * Date field.
	 *
	 * @param name the name
	 * @return the date field mapper. builder
	 */
	public static DateFieldMapper.Builder dateField(String name) {
		return new DateFieldMapper.Builder(name);
	}

	/**
	 * Ip field.
	 *
	 * @param name the name
	 * @return the ip field mapper. builder
	 */
	public static IpFieldMapper.Builder ipField(String name) {
		return new IpFieldMapper.Builder(name);
	}

	/**
	 * Short field.
	 *
	 * @param name the name
	 * @return the short field mapper. builder
	 */
	public static ShortFieldMapper.Builder shortField(String name) {
		return new ShortFieldMapper.Builder(name);
	}

	/**
	 * Byte field.
	 *
	 * @param name the name
	 * @return the byte field mapper. builder
	 */
	public static ByteFieldMapper.Builder byteField(String name) {
		return new ByteFieldMapper.Builder(name);
	}

	/**
	 * Integer field.
	 *
	 * @param name the name
	 * @return the integer field mapper. builder
	 */
	public static IntegerFieldMapper.Builder integerField(String name) {
		return new IntegerFieldMapper.Builder(name);
	}

	/**
	 * Long field.
	 *
	 * @param name the name
	 * @return the long field mapper. builder
	 */
	public static LongFieldMapper.Builder longField(String name) {
		return new LongFieldMapper.Builder(name);
	}

	/**
	 * Float field.
	 *
	 * @param name the name
	 * @return the float field mapper. builder
	 */
	public static FloatFieldMapper.Builder floatField(String name) {
		return new FloatFieldMapper.Builder(name);
	}

	/**
	 * Double field.
	 *
	 * @param name the name
	 * @return the double field mapper. builder
	 */
	public static DoubleFieldMapper.Builder doubleField(String name) {
		return new DoubleFieldMapper.Builder(name);
	}
}
