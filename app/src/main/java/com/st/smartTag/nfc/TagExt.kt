package com.st.smartTag.nfc

import android.nfc.Tag

val Tag.stringId :String?
    get() = this.id.joinToString(separator = "") { String.format("%02X",it) }
