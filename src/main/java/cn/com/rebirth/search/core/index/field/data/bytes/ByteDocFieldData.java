/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ByteDocFieldData.java 2012-3-29 15:01:56 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data.bytes;

import cn.com.rebirth.search.core.index.field.data.NumericDocFieldData;


/**
 * The Class ByteDocFieldData.
 *
 * @author l.xue.nong
 */
public class ByteDocFieldData extends NumericDocFieldData<ByteFieldData> {

    
    /**
     * Instantiates a new byte doc field data.
     *
     * @param fieldData the field data
     */
    public ByteDocFieldData(ByteFieldData fieldData) {
        super(fieldData);
    }

    
    /**
     * Gets the value.
     *
     * @return the value
     */
    public byte getValue() {
        return fieldData.value(docId);
    }

    
    /**
     * Gets the values.
     *
     * @return the values
     */
    public byte[] getValues() {
        return fieldData.values(docId);
    }
}
