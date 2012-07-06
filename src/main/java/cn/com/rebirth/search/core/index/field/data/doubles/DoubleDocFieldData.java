/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core DoubleDocFieldData.java 2012-3-29 15:00:47 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.data.doubles;

import cn.com.rebirth.search.core.index.field.data.NumericDocFieldData;


/**
 * The Class DoubleDocFieldData.
 *
 * @author l.xue.nong
 */
public class DoubleDocFieldData extends NumericDocFieldData<DoubleFieldData> {

    
    /**
     * Instantiates a new double doc field data.
     *
     * @param fieldData the field data
     */
    public DoubleDocFieldData(DoubleFieldData fieldData) {
        super(fieldData);
    }

    
    /**
     * Gets the value.
     *
     * @return the value
     */
    public double getValue() {
        return fieldData.value(docId);
    }

    
    /**
     * Gets the values.
     *
     * @return the values
     */
    public double[] getValues() {
        return fieldData.values(docId);
    }
}