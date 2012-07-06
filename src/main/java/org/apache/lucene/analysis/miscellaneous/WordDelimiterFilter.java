/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core WordDelimiterFilter.java 2012-3-29 15:04:16 l.xue.nong$$
 */


package org.apache.lucene.analysis.miscellaneous;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.RamUsageEstimator;

import java.io.IOException;



/**
 * The Class WordDelimiterFilter.
 *
 * @author l.xue.nong
 */
public final class WordDelimiterFilter extends TokenFilter {

    /** The Constant LOWER. */
    public static final int LOWER = 0x01;
    
    /** The Constant UPPER. */
    public static final int UPPER = 0x02;
    
    /** The Constant DIGIT. */
    public static final int DIGIT = 0x04;
    
    /** The Constant SUBWORD_DELIM. */
    public static final int SUBWORD_DELIM = 0x08;

    
    /** The Constant ALPHA. */
    public static final int ALPHA = 0x03;
    
    /** The Constant ALPHANUM. */
    public static final int ALPHANUM = 0x07;

    
    /** The generate word parts. */
    final boolean generateWordParts;

    
    /** The generate number parts. */
    final boolean generateNumberParts;

    
    /** The catenate words. */
    final boolean catenateWords;

    
    /** The catenate numbers. */
    final boolean catenateNumbers;

    
    /** The catenate all. */
    final boolean catenateAll;

    
    /** The preserve original. */
    final boolean preserveOriginal;

    
    /** The prot words. */
    final CharArraySet protWords;

    /** The term attribute. */
    private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
    
    /** The offset attribute. */
    private final OffsetAttribute offsetAttribute = addAttribute(OffsetAttribute.class);
    
    /** The pos inc attribute. */
    private final PositionIncrementAttribute posIncAttribute = addAttribute(PositionIncrementAttribute.class);
    
    /** The type attribute. */
    private final TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);

    
    /** The iterator. */
    private final WordDelimiterIterator iterator;

    
    /** The concat. */
    private final WordDelimiterConcatenation concat = new WordDelimiterConcatenation();
    
    /** The last concat count. */
    private int lastConcatCount = 0;

    
    /** The concat all. */
    private final WordDelimiterConcatenation concatAll = new WordDelimiterConcatenation();

    
    /** The accum pos inc. */
    private int accumPosInc = 0;

    /** The saved buffer. */
    private char savedBuffer[] = new char[1024];
    
    /** The saved start offset. */
    private int savedStartOffset;
    
    /** The saved end offset. */
    private int savedEndOffset;
    
    /** The saved type. */
    private String savedType;
    
    /** The has saved state. */
    private boolean hasSavedState = false;
    
    
    /** The has illegal offsets. */
    private boolean hasIllegalOffsets = false;

    
    /** The has output token. */
    private boolean hasOutputToken = false;
    
    
    /** The has output following original. */
    private boolean hasOutputFollowingOriginal = false;

    
    /**
     * Instantiates a new word delimiter filter.
     *
     * @param in the in
     * @param charTypeTable the char type table
     * @param generateWordParts the generate word parts
     * @param generateNumberParts the generate number parts
     * @param catenateWords the catenate words
     * @param catenateNumbers the catenate numbers
     * @param catenateAll the catenate all
     * @param splitOnCaseChange the split on case change
     * @param preserveOriginal the preserve original
     * @param splitOnNumerics the split on numerics
     * @param stemEnglishPossessive the stem english possessive
     * @param protWords the prot words
     */
    public WordDelimiterFilter(TokenStream in,
                               byte[] charTypeTable,
                               int generateWordParts,
                               int generateNumberParts,
                               int catenateWords,
                               int catenateNumbers,
                               int catenateAll,
                               int splitOnCaseChange,
                               int preserveOriginal,
                               int splitOnNumerics,
                               int stemEnglishPossessive,
                               CharArraySet protWords) {
        super(in);
        this.generateWordParts = generateWordParts != 0;
        this.generateNumberParts = generateNumberParts != 0;
        this.catenateWords = catenateWords != 0;
        this.catenateNumbers = catenateNumbers != 0;
        this.catenateAll = catenateAll != 0;
        this.preserveOriginal = preserveOriginal != 0;
        this.protWords = protWords;
        this.iterator = new WordDelimiterIterator(charTypeTable, splitOnCaseChange != 0, splitOnNumerics != 0, stemEnglishPossessive != 0);
    }

    
    /**
     * Instantiates a new word delimiter filter.
     *
     * @param in the in
     * @param generateWordParts the generate word parts
     * @param generateNumberParts the generate number parts
     * @param catenateWords the catenate words
     * @param catenateNumbers the catenate numbers
     * @param catenateAll the catenate all
     * @param splitOnCaseChange the split on case change
     * @param preserveOriginal the preserve original
     * @param splitOnNumerics the split on numerics
     * @param stemEnglishPossessive the stem english possessive
     * @param protWords the prot words
     */
    public WordDelimiterFilter(TokenStream in,
                               int generateWordParts,
                               int generateNumberParts,
                               int catenateWords,
                               int catenateNumbers,
                               int catenateAll,
                               int splitOnCaseChange,
                               int preserveOriginal,
                               int splitOnNumerics,
                               int stemEnglishPossessive,
                               CharArraySet protWords) {
        this(in, WordDelimiterIterator.DEFAULT_WORD_DELIM_TABLE, generateWordParts, generateNumberParts, catenateWords, catenateNumbers, catenateAll, splitOnCaseChange, preserveOriginal, splitOnNumerics, stemEnglishPossessive, protWords);
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.analysis.TokenStream#incrementToken()
     */
    public boolean incrementToken() throws IOException {
        while (true) {
            if (!hasSavedState) {
                
                if (!input.incrementToken()) {
                    return false;
                }

                int termLength = termAttribute.length();
                char[] termBuffer = termAttribute.buffer();

                accumPosInc += posIncAttribute.getPositionIncrement();

                iterator.setText(termBuffer, termLength);
                iterator.next();

                
                if ((iterator.current == 0 && iterator.end == termLength) ||
                        (protWords != null && protWords.contains(termBuffer, 0, termLength))) {
                    posIncAttribute.setPositionIncrement(accumPosInc);
                    accumPosInc = 0;
                    return true;
                }

                
                if (iterator.end == WordDelimiterIterator.DONE && !preserveOriginal) {
                    
                    if (posIncAttribute.getPositionIncrement() == 1) {
                        accumPosInc--;
                    }
                    continue;
                }

                saveState();

                hasOutputToken = false;
                hasOutputFollowingOriginal = !preserveOriginal;
                lastConcatCount = 0;

                if (preserveOriginal) {
                    posIncAttribute.setPositionIncrement(accumPosInc);
                    accumPosInc = 0;
                    return true;
                }
            }

            
            if (iterator.end == WordDelimiterIterator.DONE) {
                if (!concat.isEmpty()) {
                    if (flushConcatenation(concat)) {
                        return true;
                    }
                }

                if (!concatAll.isEmpty()) {
                    
                    if (concatAll.subwordCount > lastConcatCount) {
                        concatAll.writeAndClear();
                        return true;
                    }
                    concatAll.clear();
                }

                
                hasSavedState = false;
                continue;
            }

            
            if (iterator.isSingleWord()) {
                generatePart(true);
                iterator.next();
                return true;
            }

            int wordType = iterator.type();

            
            if (!concat.isEmpty() && (concat.type & wordType) == 0) {
                if (flushConcatenation(concat)) {
                    hasOutputToken = false;
                    return true;
                }
                hasOutputToken = false;
            }

            
            if (shouldConcatenate(wordType)) {
                if (concat.isEmpty()) {
                    concat.type = wordType;
                }
                concatenate(concat);
            }

            
            if (catenateAll) {
                concatenate(concatAll);
            }

            
            if (shouldGenerateParts(wordType)) {
                generatePart(false);
                iterator.next();
                return true;
            }

            iterator.next();
        }
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.analysis.TokenFilter#reset()
     */
    @Override
    public void reset() throws IOException {
        super.reset();
        hasSavedState = false;
        concat.clear();
        concatAll.clear();
        accumPosInc = 0;
    }

    

    
    /**
     * Save state.
     */
    private void saveState() {
        
        savedStartOffset = offsetAttribute.startOffset();
        savedEndOffset = offsetAttribute.endOffset();
        
        hasIllegalOffsets = (savedEndOffset - savedStartOffset != termAttribute.length());
        savedType = typeAttribute.type();

        if (savedBuffer.length < termAttribute.length()) {
            savedBuffer = new char[ArrayUtil.oversize(termAttribute.length(), RamUsageEstimator.NUM_BYTES_CHAR)];
        }

        System.arraycopy(termAttribute.buffer(), 0, savedBuffer, 0, termAttribute.length());
        iterator.text = savedBuffer;

        hasSavedState = true;
    }

    
    /**
     * Flush concatenation.
     *
     * @param concatenation the concatenation
     * @return true, if successful
     */
    private boolean flushConcatenation(WordDelimiterConcatenation concatenation) {
        lastConcatCount = concatenation.subwordCount;
        if (concatenation.subwordCount != 1 || !shouldGenerateParts(concatenation.type)) {
            concatenation.writeAndClear();
            return true;
        }
        concatenation.clear();
        return false;
    }

    
    /**
     * Should concatenate.
     *
     * @param wordType the word type
     * @return true, if successful
     */
    private boolean shouldConcatenate(int wordType) {
        return (catenateWords && isAlpha(wordType)) || (catenateNumbers && isDigit(wordType));
    }

    
    /**
     * Should generate parts.
     *
     * @param wordType the word type
     * @return true, if successful
     */
    private boolean shouldGenerateParts(int wordType) {
        return (generateWordParts && isAlpha(wordType)) || (generateNumberParts && isDigit(wordType));
    }

    
    /**
     * Concatenate.
     *
     * @param concatenation the concatenation
     */
    private void concatenate(WordDelimiterConcatenation concatenation) {
        if (concatenation.isEmpty()) {
            concatenation.startOffset = savedStartOffset + iterator.current;
        }
        concatenation.append(savedBuffer, iterator.current, iterator.end - iterator.current);
        concatenation.endOffset = savedStartOffset + iterator.end;
    }

    
    /**
     * Generate part.
     *
     * @param isSingleWord the is single word
     */
    private void generatePart(boolean isSingleWord) {
        clearAttributes();
        termAttribute.copyBuffer(savedBuffer, iterator.current, iterator.end - iterator.current);

        int startOffSet = (isSingleWord || !hasIllegalOffsets) ? savedStartOffset + iterator.current : savedStartOffset;
        int endOffSet = (hasIllegalOffsets) ? savedEndOffset : savedStartOffset + iterator.end;

        offsetAttribute.setOffset(startOffSet, endOffSet);
        posIncAttribute.setPositionIncrement(position(false));
        typeAttribute.setType(savedType);
    }

    
    /**
     * Position.
     *
     * @param inject the inject
     * @return the int
     */
    private int position(boolean inject) {
        int posInc = accumPosInc;

        if (hasOutputToken) {
            accumPosInc = 0;
            return inject ? 0 : Math.max(1, posInc);
        }

        hasOutputToken = true;

        if (!hasOutputFollowingOriginal) {
            
            hasOutputFollowingOriginal = true;
            return 0;
        }
        
        accumPosInc = 0;
        return Math.max(1, posInc);
    }

    
    /**
     * Checks if is alpha.
     *
     * @param type the type
     * @return true, if is alpha
     */
    static boolean isAlpha(int type) {
        return (type & ALPHA) != 0;
    }

    
    /**
     * Checks if is digit.
     *
     * @param type the type
     * @return true, if is digit
     */
    static boolean isDigit(int type) {
        return (type & DIGIT) != 0;
    }

    
    /**
     * Checks if is subword delim.
     *
     * @param type the type
     * @return true, if is subword delim
     */
    static boolean isSubwordDelim(int type) {
        return (type & SUBWORD_DELIM) != 0;
    }

    
    /**
     * Checks if is upper.
     *
     * @param type the type
     * @return true, if is upper
     */
    static boolean isUpper(int type) {
        return (type & UPPER) != 0;
    }

    

    
    /**
     * The Class WordDelimiterConcatenation.
     *
     * @author l.xue.nong
     */
    final class WordDelimiterConcatenation {
        
        /** The buffer. */
        final StringBuilder buffer = new StringBuilder();
        
        /** The start offset. */
        int startOffset;
        
        /** The end offset. */
        int endOffset;
        
        /** The type. */
        int type;
        
        /** The subword count. */
        int subwordCount;

        
        /**
         * Append.
         *
         * @param text the text
         * @param offset the offset
         * @param length the length
         */
        void append(char text[], int offset, int length) {
            buffer.append(text, offset, length);
            subwordCount++;
        }

        
        /**
         * Write.
         */
        void write() {
            clearAttributes();
            if (termAttribute.length() < buffer.length()) {
                termAttribute.resizeBuffer(buffer.length());
            }
            char termbuffer[] = termAttribute.buffer();

            buffer.getChars(0, buffer.length(), termbuffer, 0);
            termAttribute.setLength(buffer.length());

            if (hasIllegalOffsets) {
                offsetAttribute.setOffset(savedStartOffset, savedEndOffset);
            } else {
                offsetAttribute.setOffset(startOffset, endOffset);
            }
            posIncAttribute.setPositionIncrement(position(true));
            typeAttribute.setType(savedType);
            accumPosInc = 0;
        }

        
        /**
         * Checks if is empty.
         *
         * @return true, if is empty
         */
        boolean isEmpty() {
            return buffer.length() == 0;
        }

        
        /**
         * Clear.
         */
        void clear() {
            buffer.setLength(0);
            startOffset = endOffset = type = subwordCount = 0;
        }

        
        /**
         * Write and clear.
         */
        void writeAndClear() {
            write();
            clear();
        }
    }
    
    
    
    
    
}
