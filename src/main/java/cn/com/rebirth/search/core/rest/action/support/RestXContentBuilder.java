/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RestXContentBuilder.java 2012-7-6 14:29:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.rest.action.support;

import java.io.IOException;

import cn.com.rebirth.commons.compress.lzf.LZF;
import cn.com.rebirth.commons.io.stream.CachedStreamInput;
import cn.com.rebirth.commons.io.stream.LZFStreamInput;
import cn.com.rebirth.search.commons.io.stream.BytesStreamInput;
import cn.com.rebirth.search.commons.io.stream.CachedStreamOutput;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.commons.xcontent.XContentType;
import cn.com.rebirth.search.core.rest.RestRequest;

/**
 * The Class RestXContentBuilder.
 *
 * @author l.xue.nong
 */
public class RestXContentBuilder {

	/**
	 * Rest content builder.
	 *
	 * @param request the request
	 * @return the x content builder
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static XContentBuilder restContentBuilder(RestRequest request) throws IOException {
		XContentType contentType = XContentType.fromRestContentType(request.header("Content-Type"));
		if (contentType == null) {

			if (request.hasContent()) {
				contentType = XContentFactory.xContentType(request.contentByteArray(),
						request.contentByteArrayOffset(), request.contentLength());
			}
		}
		if (contentType == null) {

			contentType = XContentType.JSON;
		}
		CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
		XContentBuilder builder = new XContentBuilder(XContentFactory.xContent(contentType), cachedEntry.cachedBytes(),
				cachedEntry);
		if (request.paramAsBoolean("pretty", false)) {
			builder.prettyPrint();
		}
		String casing = request.param("case");
		if (casing != null && "camelCase".equals(casing)) {
			builder.fieldCaseConversion(XContentBuilder.FieldCaseConversion.CAMELCASE);
		} else {

			builder.fieldCaseConversion(XContentBuilder.FieldCaseConversion.NONE);
		}
		return builder;
	}

	/**
	 * Rest document source.
	 *
	 * @param source the source
	 * @param builder the builder
	 * @param params the params
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void restDocumentSource(byte[] source, XContentBuilder builder, ToXContent.Params params)
			throws IOException {
		restDocumentSource(source, 0, source.length, builder, params);
	}

	/**
	 * Rest document source.
	 *
	 * @param source the source
	 * @param offset the offset
	 * @param length the length
	 * @param builder the builder
	 * @param params the params
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void restDocumentSource(byte[] source, int offset, int length, XContentBuilder builder,
			ToXContent.Params params) throws IOException {
		if (LZF.isCompressed(source, offset, length)) {
			BytesStreamInput siBytes = new BytesStreamInput(source, offset, length, false);
			LZFStreamInput siLzf = CachedStreamInput.cachedLzf(siBytes);
			XContentType contentType = XContentFactory.xContentType(siLzf);
			siLzf.resetToBufferStart();
			if (contentType == builder.contentType()) {
				builder.rawField("_source", siLzf);
			} else {
				XContentParser parser = XContentFactory.xContent(contentType).createParser(siLzf);
				try {
					parser.nextToken();
					builder.field("_source");
					builder.copyCurrentStructure(parser);
				} finally {
					parser.close();
				}
			}
		} else {
			XContentType contentType = XContentFactory.xContentType(source, offset, length);
			if (contentType == builder.contentType()) {
				builder.rawField("_source", source, offset, length);
			} else {
				XContentParser parser = XContentFactory.xContent(contentType).createParser(source);
				try {
					parser.nextToken();
					builder.field("_source");
					builder.copyCurrentStructure(parser);
				} finally {
					parser.close();
				}
			}
		}
	}
}
