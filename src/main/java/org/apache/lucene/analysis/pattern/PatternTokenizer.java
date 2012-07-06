/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core PatternTokenizer.java 2012-3-29 15:04:16 l.xue.nong$$
 */


package org.apache.lucene.analysis.pattern;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The Class PatternTokenizer.
 *
 * @author l.xue.nong
 */
public final class PatternTokenizer extends Tokenizer {

    /** The term att. */
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    
    /** The offset att. */
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    /** The str. */
    private final StringBuilder str = new StringBuilder();
    
    /** The index. */
    private int index;

    /** The group. */
    private final int group;
    
    /** The matcher. */
    private final Matcher matcher;

    
    /**
     * Instantiates a new pattern tokenizer.
     *
     * @param input the input
     * @param pattern the pattern
     * @param group the group
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public PatternTokenizer(Reader input, Pattern pattern, int group) throws IOException {
        super(input);
        this.group = group;
        fillBuffer(str, input);
        matcher = pattern.matcher(str);
        index = 0;
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.analysis.TokenStream#incrementToken()
     */
    @Override
    public boolean incrementToken() throws IOException {
        if (index >= str.length()) return false;
        clearAttributes();
        if (group >= 0) {

            
            while (matcher.find()) {
                index = matcher.start(group);
                final int endIndex = matcher.end(group);
                if (index == endIndex) continue;
                termAtt.setEmpty().append(str, index, endIndex);
                offsetAtt.setOffset(correctOffset(index), correctOffset(endIndex));
                return true;
            }

            index = Integer.MAX_VALUE; 
            return false;

        } else {

            
            while (matcher.find()) {
                if (matcher.start() - index > 0) {
                    
                    termAtt.setEmpty().append(str, index, matcher.start());
                    offsetAtt.setOffset(correctOffset(index), correctOffset(matcher.start()));
                    index = matcher.end();
                    return true;
                }

                index = matcher.end();
            }

            if (str.length() - index == 0) {
                index = Integer.MAX_VALUE; 
                return false;
            }

            termAtt.setEmpty().append(str, index, str.length());
            offsetAtt.setOffset(correctOffset(index), correctOffset(str.length()));
            index = Integer.MAX_VALUE; 
            return true;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.analysis.TokenStream#end()
     */
    @Override
    public void end() throws IOException {
        final int ofs = correctOffset(str.length());
        offsetAtt.setOffset(ofs, ofs);
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.analysis.Tokenizer#reset(java.io.Reader)
     */
    @Override
    public void reset(Reader input) throws IOException {
        super.reset(input);
        fillBuffer(str, input);
        matcher.reset(str);
        index = 0;
    }

    
    
    /** The buffer. */
    final char[] buffer = new char[8192];

    /**
     * Fill buffer.
     *
     * @param sb the sb
     * @param input the input
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void fillBuffer(StringBuilder sb, Reader input) throws IOException {
        int len;
        sb.setLength(0);
        while ((len = input.read(buffer)) > 0) {
            sb.append(buffer, 0, len);
        }
    }
}
