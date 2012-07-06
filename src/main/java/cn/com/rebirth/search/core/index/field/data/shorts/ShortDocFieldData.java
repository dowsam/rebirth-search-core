/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShortDocFieldData.java 2012-3-29 15:01:07 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data.shorts;

import cn.com.rebirth.search.core.index.field.data.NumericDocFieldData;


/**
 * The Class ShortDocFieldData.
 *
 * @author l.xue.nong
 */
public class ShortDocFieldData extends NumericDocFieldData<ShortFieldData> {

    
    /**
     * Instantiates a new short doc field data.
     *
     * @param fieldData the field data
     */
    public ShortDocFieldData(ShortFieldData fieldData) {
        super(fieldData);
    }

    
    /**
     * Gets the value.
     *
     * @return the value
     */
    public short getValue() {
        return fieldData.value(docId);
    }

    
    /**
     * Gets the values.
     *
     * @return the values
     */
    public short[] getValues() {
        return fieldData.values(docId);
    }
}
