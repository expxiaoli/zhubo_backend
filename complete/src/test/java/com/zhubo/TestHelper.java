package com.zhubo;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.zhubo.helper.GeneralHelper;
import com.zhubo.task.processdata.TimeUnit;

public class TestHelper {
    @Test
    public void testAggregateDate() {
        Assert.assertEquals(GeneralHelper.getAggregateDate(new Date(2016-1900, 9, 9), TimeUnit.WEEK), new Date(2016-1900, 9, 3));
        Assert.assertEquals(GeneralHelper.getAggregateDate(new Date(2016-1900, 9, 10), TimeUnit.WEEK), new Date(2016-1900, 9, 10));
        Assert.assertEquals(GeneralHelper.getAggregateDate(new Date(2016-1900, 9, 15), TimeUnit.WEEK), new Date(2016-1900, 9, 10));
        Assert.assertEquals(GeneralHelper.getAggregateDate(new Date(2016-1900, 9, 16), TimeUnit.WEEK), new Date(2016-1900, 9, 10));
        Assert.assertEquals(GeneralHelper.getAggregateDate(new Date(2016-1900, 9, 17), TimeUnit.WEEK), new Date(2016-1900, 9, 17));
    }
    
    @Test
    public void testGetIntegerFromComplexString() {
        Assert.assertEquals(new Integer(15000), GeneralHelper.getIntegerFromComplextString("1.5万"));
        Assert.assertEquals(new Integer(15000), GeneralHelper.getIntegerFromComplextString("15000"));
    }
}
