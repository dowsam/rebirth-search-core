/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TrimFilter.java 2012-3-29 15:04:17 l.xue.nong$$
 */
package org.apache.lucene.analysis.miscellaneous;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;



/**
 * The Class TrimFilter.
 *
 * @author l.xue.nong
 */
public final class TrimFilter extends TokenFilter {

    /** The update offsets. */
    final boolean updateOffsets;
    
    /** The term att. */
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    
    /** The offset att. */
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);


    /**
     * Instantiates a new trim filter.
     *
     * @param in the in
     * @param updateOffsets the update offsets
     */
    public TrimFilter(TokenStream in, boolean updateOffsets) {
        super(in);
        this.updateOffsets = updateOffsets;
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.analysis.TokenStream#incrementToken()
     */
    @Override
    public boolean incrementToken() throws IOException {
        if (!input.incrementToken()) return false;

        char[] termBuffer = termAtt.buffer();
        int len = termAtt.length();
        
        
        if (len == 0) {
            return true;
        }
        int start = 0;
        int end = 0;
        int endOff = 0;

        
        
        for (start = 0; start < len && termBuffer[start] <= ' '; start++) {
        }
        
        for (end = len; end >= start && termBuffer[end - 1] <= ' '; end--) {
            endOff++;
        }
        if (start > 0 || end < len) {
            if (start < end) {
                termAtt.copyBuffer(termBuffer, start, (end - start));
            } else {
                termAtt.setEmpty();
            }
            if (updateOffsets) {
                int newStart = offsetAtt.startOffset() + start;
                int newEnd = offsetAtt.endOffset() - (start < end ? endOff : 0);
                offsetAtt.setOffset(newStart, newEnd);
            }
        }

        return true;
    }
}
