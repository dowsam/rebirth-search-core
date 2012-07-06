/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MoreLikeThisRequest.java 2012-3-29 15:00:49 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.mlt;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.lucene.util.UnicodeUtil;

import cn.com.rebirth.commons.Bytes;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.Unicode;
import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.commons.Required;
import cn.com.rebirth.search.commons.io.BytesStream;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentType;
import cn.com.rebirth.search.core.RestartGenerationException;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.action.search.SearchType;
import cn.com.rebirth.search.core.client.Requests;
import cn.com.rebirth.search.core.search.Scroll;
import cn.com.rebirth.search.core.search.builder.SearchSourceBuilder;


/**
 * The Class MoreLikeThisRequest.
 *
 * @author l.xue.nong
 */
public class MoreLikeThisRequest implements ActionRequest {

	
	/** The Constant contentType. */
	private static final XContentType contentType = Requests.CONTENT_TYPE;

	
	/** The index. */
	private String index;

	
	/** The type. */
	private String type;

	
	/** The id. */
	private String id;

	
	/** The fields. */
	private String[] fields;

	
	/** The percent terms to match. */
	private float percentTermsToMatch = -1;

	
	/** The min term freq. */
	private int minTermFreq = -1;

	
	/** The max query terms. */
	private int maxQueryTerms = -1;

	
	/** The stop words. */
	private String[] stopWords = null;

	
	/** The min doc freq. */
	private int minDocFreq = -1;

	
	/** The max doc freq. */
	private int maxDocFreq = -1;

	
	/** The min word len. */
	private int minWordLen = -1;

	
	/** The max word len. */
	private int maxWordLen = -1;

	
	/** The boost terms. */
	private float boostTerms = -1;

	
	/** The search type. */
	private SearchType searchType = SearchType.DEFAULT;

	
	/** The search size. */
	private int searchSize = 0;

	
	/** The search from. */
	private int searchFrom = 0;

	
	/** The search query hint. */
	private String searchQueryHint;

	
	/** The search indices. */
	private String[] searchIndices;

	
	/** The search types. */
	private String[] searchTypes;

	
	/** The search scroll. */
	private Scroll searchScroll;

	
	/** The search source. */
	private byte[] searchSource;

	
	/** The search source offset. */
	private int searchSourceOffset;

	
	/** The search source length. */
	private int searchSourceLength;

	
	/** The search source unsafe. */
	private boolean searchSourceUnsafe;

	
	/** The threaded listener. */
	private boolean threadedListener = false;

	
	/**
	 * Instantiates a new more like this request.
	 */
	MoreLikeThisRequest() {
	}

	
	/**
	 * Instantiates a new more like this request.
	 *
	 * @param index the index
	 */
	public MoreLikeThisRequest(String index) {
		this.index = index;
	}

	
	/**
	 * Index.
	 *
	 * @return the string
	 */
	public String index() {
		return index;
	}

	
	/**
	 * Type.
	 *
	 * @return the string
	 */
	public String type() {
		return type;
	}

	
	/**
	 * Index.
	 *
	 * @param index the index
	 */
	void index(String index) {
		this.index = index;
	}

	
	/**
	 * Type.
	 *
	 * @param type the type
	 * @return the more like this request
	 */
	@Required
	public MoreLikeThisRequest type(String type) {
		this.type = type;
		return this;
	}

	
	/**
	 * Id.
	 *
	 * @return the string
	 */
	public String id() {
		return id;
	}

	
	/**
	 * Id.
	 *
	 * @param id the id
	 * @return the more like this request
	 */
	@Required
	public MoreLikeThisRequest id(String id) {
		this.id = id;
		return this;
	}

	
	/**
	 * Fields.
	 *
	 * @return the string[]
	 */
	public String[] fields() {
		return this.fields;
	}

	
	/**
	 * Fields.
	 *
	 * @param fields the fields
	 * @return the more like this request
	 */
	public MoreLikeThisRequest fields(String... fields) {
		this.fields = fields;
		return this;
	}

	
	/**
	 * Percent terms to match.
	 *
	 * @param percentTermsToMatch the percent terms to match
	 * @return the more like this request
	 */
	public MoreLikeThisRequest percentTermsToMatch(float percentTermsToMatch) {
		this.percentTermsToMatch = percentTermsToMatch;
		return this;
	}

	
	/**
	 * Percent terms to match.
	 *
	 * @return the float
	 */
	public float percentTermsToMatch() {
		return this.percentTermsToMatch;
	}

	
	/**
	 * Min term freq.
	 *
	 * @param minTermFreq the min term freq
	 * @return the more like this request
	 */
	public MoreLikeThisRequest minTermFreq(int minTermFreq) {
		this.minTermFreq = minTermFreq;
		return this;
	}

	
	/**
	 * Min term freq.
	 *
	 * @return the int
	 */
	public int minTermFreq() {
		return this.minTermFreq;
	}

	
	/**
	 * Max query terms.
	 *
	 * @param maxQueryTerms the max query terms
	 * @return the more like this request
	 */
	public MoreLikeThisRequest maxQueryTerms(int maxQueryTerms) {
		this.maxQueryTerms = maxQueryTerms;
		return this;
	}

	
	/**
	 * Max query terms.
	 *
	 * @return the int
	 */
	public int maxQueryTerms() {
		return this.maxQueryTerms;
	}

	
	/**
	 * Stop words.
	 *
	 * @param stopWords the stop words
	 * @return the more like this request
	 */
	public MoreLikeThisRequest stopWords(String... stopWords) {
		this.stopWords = stopWords;
		return this;
	}

	
	/**
	 * Stop words.
	 *
	 * @return the string[]
	 */
	public String[] stopWords() {
		return this.stopWords;
	}

	
	/**
	 * Min doc freq.
	 *
	 * @param minDocFreq the min doc freq
	 * @return the more like this request
	 */
	public MoreLikeThisRequest minDocFreq(int minDocFreq) {
		this.minDocFreq = minDocFreq;
		return this;
	}

	
	/**
	 * Min doc freq.
	 *
	 * @return the int
	 */
	public int minDocFreq() {
		return this.minDocFreq;
	}

	
	/**
	 * Max doc freq.
	 *
	 * @param maxDocFreq the max doc freq
	 * @return the more like this request
	 */
	public MoreLikeThisRequest maxDocFreq(int maxDocFreq) {
		this.maxDocFreq = maxDocFreq;
		return this;
	}

	
	/**
	 * Max doc freq.
	 *
	 * @return the int
	 */
	public int maxDocFreq() {
		return this.maxDocFreq;
	}

	
	/**
	 * Min word len.
	 *
	 * @param minWordLen the min word len
	 * @return the more like this request
	 */
	public MoreLikeThisRequest minWordLen(int minWordLen) {
		this.minWordLen = minWordLen;
		return this;
	}

	
	/**
	 * Min word len.
	 *
	 * @return the int
	 */
	public int minWordLen() {
		return this.minWordLen;
	}

	
	/**
	 * Max word len.
	 *
	 * @param maxWordLen the max word len
	 * @return the more like this request
	 */
	public MoreLikeThisRequest maxWordLen(int maxWordLen) {
		this.maxWordLen = maxWordLen;
		return this;
	}

	
	/**
	 * Max word len.
	 *
	 * @return the int
	 */
	public int maxWordLen() {
		return this.maxWordLen;
	}

	
	/**
	 * Boost terms.
	 *
	 * @param boostTerms the boost terms
	 * @return the more like this request
	 */
	public MoreLikeThisRequest boostTerms(float boostTerms) {
		this.boostTerms = boostTerms;
		return this;
	}

	
	/**
	 * Boost terms.
	 *
	 * @return the float
	 */
	public float boostTerms() {
		return this.boostTerms;
	}

	
	/**
	 * Before local fork.
	 */
	void beforeLocalFork() {
		if (searchSourceUnsafe) {
			searchSource = Arrays
					.copyOfRange(searchSource, searchSourceOffset, searchSourceOffset + searchSourceLength);
			searchSourceOffset = 0;
			searchSourceUnsafe = false;
		}
	}

	
	/**
	 * Search source.
	 *
	 * @param sourceBuilder the source builder
	 * @return the more like this request
	 */
	public MoreLikeThisRequest searchSource(SearchSourceBuilder sourceBuilder) {
		BytesStream bos = sourceBuilder.buildAsBytesStream(Requests.CONTENT_TYPE);
		this.searchSource = bos.underlyingBytes();
		this.searchSourceOffset = 0;
		this.searchSourceLength = bos.size();
		this.searchSourceUnsafe = true;
		return this;
	}

	
	/**
	 * Search source.
	 *
	 * @param searchSource the search source
	 * @return the more like this request
	 */
	public MoreLikeThisRequest searchSource(String searchSource) {
		UnicodeUtil.UTF8Result result = Unicode.fromStringAsUtf8(searchSource);
		this.searchSource = result.result;
		this.searchSourceOffset = 0;
		this.searchSourceLength = result.length;
		this.searchSourceUnsafe = true;
		return this;
	}

	
	/**
	 * Search source.
	 *
	 * @param searchSource the search source
	 * @return the more like this request
	 */
	public MoreLikeThisRequest searchSource(Map searchSource) {
		try {
			XContentBuilder builder = XContentFactory.contentBuilder(contentType);
			builder.map(searchSource);
			return searchSource(builder);
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + searchSource + "]", e);
		}
	}

	
	/**
	 * Search source.
	 *
	 * @param builder the builder
	 * @return the more like this request
	 */
	public MoreLikeThisRequest searchSource(XContentBuilder builder) {
		try {
			this.searchSource = builder.underlyingBytes();
			this.searchSourceOffset = 0;
			this.searchSourceLength = builder.underlyingBytesLength();
			this.searchSourceUnsafe = false;
			return this;
		} catch (IOException e) {
			throw new RestartGenerationException("Failed to generate [" + builder + "]", e);
		}
	}

	
	/**
	 * Search source.
	 *
	 * @param searchSource the search source
	 * @return the more like this request
	 */
	public MoreLikeThisRequest searchSource(byte[] searchSource) {
		return searchSource(searchSource, 0, searchSource.length, false);
	}

	
	/**
	 * Search source.
	 *
	 * @param searchSource the search source
	 * @param offset the offset
	 * @param length the length
	 * @param unsafe the unsafe
	 * @return the more like this request
	 */
	public MoreLikeThisRequest searchSource(byte[] searchSource, int offset, int length, boolean unsafe) {
		this.searchSource = searchSource;
		this.searchSourceOffset = offset;
		this.searchSourceLength = length;
		this.searchSourceUnsafe = unsafe;
		return this;
	}

	
	/**
	 * Search source.
	 *
	 * @return the byte[]
	 */
	public byte[] searchSource() {
		return this.searchSource;
	}

	
	/**
	 * Search source offset.
	 *
	 * @return the int
	 */
	public int searchSourceOffset() {
		return searchSourceOffset;
	}

	
	/**
	 * Search source length.
	 *
	 * @return the int
	 */
	public int searchSourceLength() {
		return searchSourceLength;
	}

	
	/**
	 * Search source unsafe.
	 *
	 * @return true, if successful
	 */
	public boolean searchSourceUnsafe() {
		return searchSourceUnsafe;
	}

	
	/**
	 * Search type.
	 *
	 * @param searchType the search type
	 * @return the more like this request
	 */
	public MoreLikeThisRequest searchType(SearchType searchType) {
		this.searchType = searchType;
		return this;
	}

	
	/**
	 * Search type.
	 *
	 * @param searchType the search type
	 * @return the more like this request
	 * @throws SumMallSearchIllegalArgumentException the sum mall search illegal argument exception
	 */
	public MoreLikeThisRequest searchType(String searchType) throws RestartIllegalArgumentException {
		return searchType(SearchType.fromString(searchType));
	}

	
	/**
	 * Search type.
	 *
	 * @return the search type
	 */
	public SearchType searchType() {
		return this.searchType;
	}

	
	/**
	 * Search indices.
	 *
	 * @param searchIndices the search indices
	 * @return the more like this request
	 */
	public MoreLikeThisRequest searchIndices(String... searchIndices) {
		this.searchIndices = searchIndices;
		return this;
	}

	
	/**
	 * Search indices.
	 *
	 * @return the string[]
	 */
	public String[] searchIndices() {
		return this.searchIndices;
	}

	
	/**
	 * Search types.
	 *
	 * @param searchTypes the search types
	 * @return the more like this request
	 */
	public MoreLikeThisRequest searchTypes(String... searchTypes) {
		this.searchTypes = searchTypes;
		return this;
	}

	
	/**
	 * Search types.
	 *
	 * @return the string[]
	 */
	public String[] searchTypes() {
		return this.searchTypes;
	}

	
	/**
	 * Search query hint.
	 *
	 * @param searchQueryHint the search query hint
	 * @return the more like this request
	 */
	public MoreLikeThisRequest searchQueryHint(String searchQueryHint) {
		this.searchQueryHint = searchQueryHint;
		return this;
	}

	
	/**
	 * Search query hint.
	 *
	 * @return the string
	 */
	public String searchQueryHint() {
		return this.searchQueryHint;
	}

	
	/**
	 * Search scroll.
	 *
	 * @param searchScroll the search scroll
	 * @return the more like this request
	 */
	public MoreLikeThisRequest searchScroll(Scroll searchScroll) {
		this.searchScroll = searchScroll;
		return this;
	}

	
	/**
	 * Search scroll.
	 *
	 * @return the scroll
	 */
	public Scroll searchScroll() {
		return this.searchScroll;
	}

	
	/**
	 * Search size.
	 *
	 * @param size the size
	 * @return the more like this request
	 */
	public MoreLikeThisRequest searchSize(int size) {
		this.searchSize = size;
		return this;
	}

	
	/**
	 * Search size.
	 *
	 * @return the int
	 */
	public int searchSize() {
		return this.searchSize;
	}

	
	/**
	 * Search from.
	 *
	 * @param from the from
	 * @return the more like this request
	 */
	public MoreLikeThisRequest searchFrom(int from) {
		this.searchFrom = from;
		return this;
	}

	
	/**
	 * Search from.
	 *
	 * @return the int
	 */
	public int searchFrom() {
		return this.searchFrom;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = null;
		if (index == null) {
			validationException = ValidateActions.addValidationError("index is missing", validationException);
		}
		if (type == null) {
			validationException = ValidateActions.addValidationError("type is missing", validationException);
		}
		if (id == null) {
			validationException = ValidateActions.addValidationError("id is missing", validationException);
		}
		return validationException;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#listenerThreaded()
	 */
	@Override
	public boolean listenerThreaded() {
		return threadedListener;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#listenerThreaded(boolean)
	 */
	@Override
	public ActionRequest listenerThreaded(boolean listenerThreaded) {
		this.threadedListener = listenerThreaded;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		index = in.readUTF();
		type = in.readUTF();
		id = in.readUTF();
		
		int size = in.readVInt();
		if (size == 0) {
			fields = Strings.EMPTY_ARRAY;
		} else {
			fields = new String[size];
			for (int i = 0; i < size; i++) {
				fields[i] = in.readUTF();
			}
		}

		percentTermsToMatch = in.readFloat();
		minTermFreq = in.readVInt();
		maxQueryTerms = in.readVInt();
		size = in.readVInt();
		if (size > 0) {
			stopWords = new String[size];
			for (int i = 0; i < size; i++) {
				stopWords[i] = in.readUTF();
			}
		}
		minDocFreq = in.readVInt();
		maxDocFreq = in.readVInt();
		minWordLen = in.readVInt();
		maxWordLen = in.readVInt();
		boostTerms = in.readFloat();
		searchType = SearchType.fromId(in.readByte());
		if (in.readBoolean()) {
			searchQueryHint = in.readUTF();
		}
		size = in.readVInt();
		if (size == 0) {
			searchIndices = null;
		} else if (size == 1) {
			searchIndices = Strings.EMPTY_ARRAY;
		} else {
			searchIndices = new String[size - 1];
			for (int i = 0; i < searchIndices.length; i++) {
				searchIndices[i] = in.readUTF();
			}
		}
		size = in.readVInt();
		if (size == 0) {
			searchTypes = null;
		} else if (size == 1) {
			searchTypes = Strings.EMPTY_ARRAY;
		} else {
			searchTypes = new String[size - 1];
			for (int i = 0; i < searchTypes.length; i++) {
				searchTypes[i] = in.readUTF();
			}
		}
		if (in.readBoolean()) {
			searchScroll = Scroll.readScroll(in);
		}

		searchSourceUnsafe = false;
		searchSourceOffset = 0;
		searchSourceLength = in.readVInt();
		if (searchSourceLength == 0) {
			searchSource = Bytes.EMPTY_ARRAY;
		} else {
			searchSource = new byte[searchSourceLength];
			in.readFully(searchSource);
		}

		searchSize = in.readVInt();
		searchFrom = in.readVInt();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(index);
		out.writeUTF(type);
		out.writeUTF(id);
		if (fields == null) {
			out.writeVInt(0);
		} else {
			out.writeVInt(fields.length);
			for (String field : fields) {
				out.writeUTF(field);
			}
		}

		out.writeFloat(percentTermsToMatch);
		out.writeVInt(minTermFreq);
		out.writeVInt(maxQueryTerms);
		if (stopWords == null) {
			out.writeVInt(0);
		} else {
			out.writeVInt(stopWords.length);
			for (String stopWord : stopWords) {
				out.writeUTF(stopWord);
			}
		}
		out.writeVInt(minDocFreq);
		out.writeVInt(maxDocFreq);
		out.writeVInt(minWordLen);
		out.writeVInt(maxWordLen);
		out.writeFloat(boostTerms);

		out.writeByte(searchType.id());
		if (searchQueryHint == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(searchQueryHint);
		}
		if (searchIndices == null) {
			out.writeVInt(0);
		} else {
			out.writeVInt(searchIndices.length + 1);
			for (String index : searchIndices) {
				out.writeUTF(index);
			}
		}
		if (searchTypes == null) {
			out.writeVInt(0);
		} else {
			out.writeVInt(searchTypes.length + 1);
			for (String type : searchTypes) {
				out.writeUTF(type);
			}
		}
		if (searchScroll == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			searchScroll.writeTo(out);
		}
		if (searchSource == null) {
			out.writeVInt(0);
		} else {
			out.writeVInt(searchSourceLength);
			out.writeBytes(searchSource, searchSourceOffset, searchSourceLength);
		}

		out.writeVInt(searchSize);
		out.writeVInt(searchFrom);
	}
}
