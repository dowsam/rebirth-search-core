/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IntDocFieldData.java 2012-3-29 15:02:34 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data.ints;

import cn.com.rebirth.search.core.index.field.data.NumericDocFieldData;


/**
 * The Class IntDocFieldData.
 *
 * @author l.xue.nong
 */
public class IntDocFieldData extends NumericDocFieldData<IntFieldData> {

    
    /**
     * Instantiates a new int doc field data.
     *
     * @param fieldData the field data
     */
    public IntDocFieldData(IntFieldData fieldData) {
        super(fieldData);
    }

    
    /**
     * Gets the value.
     *
     * @return the value
     */
    public int getValue() {
        return fieldData.value(docId);
    }

    
    /**
     * Gets the values.
     *
     * @return the values
     */
    public int[] getValues() {
        return fieldData.values(docId);
    }
}