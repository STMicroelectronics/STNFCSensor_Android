package com.st.smartaglib.model.io

import org.junit.Assert
import org.junit.Test

class PresAccDataTest{

    @Test
    fun MaxMinPresAccCellAreCorrectlyUnpacked(){
        val origData = MaxMinPresAccCell(1210.0f, 810.0f, 256.0f)
        val data = unpackMaxMinPresAccCell(origData.pack())
        Assert.assertEquals(origData,data)
    }

    @Test
    fun MaxMinTempHumCellAreCorrectlyUnpacked(){
        val origData = MaxMinTempHumCell(1.0f, 2.0f, 3.0f, 4.0f)
        val data = unpackMaxMinTempHumCell(origData.pack())
        Assert.assertEquals(origData,data)
    }

}