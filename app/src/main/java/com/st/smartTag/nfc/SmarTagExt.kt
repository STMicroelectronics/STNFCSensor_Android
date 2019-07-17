package com.st.smartTag.nfc

import android.nfc.Tag
import com.st.smartaglib.SmarTag

fun SmarTag.Companion.get(tag:Tag):SmarTag?{
    val tagIO = ST25DVTag.get(tag)
    return if(tagIO!=null){
        SmarTag(tagIO)
    }else {
        null
    }
}