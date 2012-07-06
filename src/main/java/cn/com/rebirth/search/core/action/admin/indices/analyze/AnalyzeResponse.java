/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AnalyzeResponse.java 2012-3-29 15:01:55 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.analyze;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.action.ActionResponse;


/**
 * The Class AnalyzeResponse.
 *
 * @author l.xue.nong
 */
public class AnalyzeResponse implements ActionResponse, Iterable<AnalyzeResponse.AnalyzeToken>, ToXContent {

	
	/**
	 * The Class AnalyzeToken.
	 *
	 * @author l.xue.nong
	 */
	public static class AnalyzeToken implements Streamable {

		
		/** The term. */
		private String term;

		
		/** The start offset. */
		private int startOffset;

		
		/** The end offset. */
		private int endOffset;

		
		/** The position. */
		private int position;

		
		/** The type. */
		private String type;

		
		/**
		 * Instantiates a new analyze token.
		 */
		AnalyzeToken() {
		}

		
		/**
		 * Instantiates a new analyze token.
		 *
		 * @param term the term
		 * @param position the position
		 * @param startOffset the start offset
		 * @param endOffset the end offset
		 * @param type the type
		 */
		public AnalyzeToken(String term, int position, int startOffset, int endOffset, String type) {
			this.term = term;
			this.position = position;
			this.startOffset = startOffset;
			this.endOffset = endOffset;
			this.type = type;
		}

		
		/**
		 * Term.
		 *
		 * @return the string
		 */
		public String term() {
			return this.term;
		}

		
		/**
		 * Gets the term.
		 *
		 * @return the term
		 */
		public String getTerm() {
			return term();
		}

		
		/**
		 * Start offset.
		 *
		 * @return the int
		 */
		public int startOffset() {
			return this.startOffset;
		}

		
		/**
		 * Gets the start offset.
		 *
		 * @return the start offset
		 */
		public int getStartOffset() {
			return startOffset();
		}

		
		/**
		 * End offset.
		 *
		 * @return the int
		 */
		public int endOffset() {
			return this.endOffset;
		}

		
		/**
		 * Gets the end offset.
		 *
		 * @return the end offset
		 */
		public int getEndOffset() {
			return endOffset();
		}

		
		/**
		 * Position.
		 *
		 * @return the int
		 */
		public int position() {
			return this.position;
		}

		
		/**
		 * Gets the position.
		 *
		 * @return the position
		 */
		public int getPosition() {
			return position();
		}

		
		/**
		 * Type.
		 *
		 * @return the string
		 */
		public String type() {
			return this.type;
		}

		
		/**
		 * Gets the type.
		 *
		 * @return the type
		 */
		public String getType() {
			return this.type;
		}

		
		/**
		 * Read analyze token.
		 *
		 * @param in the in
		 * @return the analyze token
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static AnalyzeToken readAnalyzeToken(StreamInput in) throws IOException {
			AnalyzeToken analyzeToken = new AnalyzeToken();
			analyzeToken.readFrom(in);
			return analyzeToken;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			term = in.readUTF();
			startOffset = in.readInt();
			endOffset = in.readInt();
			position = in.readVInt();
			if (in.readBoolean()) {
				type = in.readUTF();
			}
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeUTF(term);
			out.writeInt(startOffset);
			out.writeInt(endOffset);
			out.writeVInt(position);
			if (type == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				out.writeUTF(type);
			}
		}
	}

	
	/** The tokens. */
	private List<AnalyzeToken> tokens;

	
	/**
	 * Instantiates a new analyze response.
	 */
	AnalyzeResponse() {
	}

	
	/**
	 * Instantiates a new analyze response.
	 *
	 * @param tokens the tokens
	 */
	public AnalyzeResponse(List<AnalyzeToken> tokens) {
		this.tokens = tokens;
	}

	
	/**
	 * Tokens.
	 *
	 * @return the list
	 */
	public List<AnalyzeToken> tokens() {
		return this.tokens;
	}

	
	/**
	 * Gets the tokens.
	 *
	 * @return the tokens
	 */
	public List<AnalyzeToken> getTokens() {
		return tokens();
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<AnalyzeToken> iterator() {
		return tokens.iterator();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		String format = params.param("format", "detailed");
		if ("detailed".equals(format)) {
			builder.startArray("tokens");
			for (AnalyzeToken token : tokens) {
				builder.startObject();
				builder.field("token", token.term());
				builder.field("start_offset", token.startOffset());
				builder.field("end_offset", token.endOffset());
				builder.field("type", token.type());
				builder.field("position", token.position());
				builder.endObject();
			}
			builder.endArray();
		} else if ("text".equals(format)) {
			StringBuilder sb = new StringBuilder();
			int lastPosition = 0;
			for (AnalyzeToken token : tokens) {
				if (lastPosition != token.position()) {
					if (lastPosition != 0) {
						sb.append("\n").append(token.position()).append(": \n");
					}
					lastPosition = token.position();
				}
				sb.append('[').append(token.term()).append(":").append(token.startOffset()).append("->")
						.append(token.endOffset()).append(":").append(token.type()).append("]\n");
			}
			builder.field("tokens", sb);
		}
		return builder;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		int size = in.readVInt();
		tokens = new ArrayList<AnalyzeToken>(size);
		for (int i = 0; i < size; i++) {
			tokens.add(AnalyzeToken.readAnalyzeToken(in));
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVInt(tokens.size());
		for (AnalyzeToken token : tokens) {
			token.writeTo(out);
		}
	}
}
