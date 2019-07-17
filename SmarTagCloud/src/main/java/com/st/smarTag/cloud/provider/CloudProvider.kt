package com.st.smarTag.cloud.provider

import com.st.smartaglib.model.DataSample
import com.st.smartaglib.model.TagExtreme

interface CloudProvider {

    interface Connection

    fun connect( callback: ConnectionCallback)

    fun uploadSamples(conn:Connection, samples:List<DataSample> , callback: PublishCallback)

    fun uploadExtreme(conn: Connection, extremeData: TagExtreme, callback: PublishCallback)

    fun disconnect(connection: Connection)


    interface ConnectionCallback{
        fun onSuccess(conn:Connection)
        fun onFail(error:Throwable)
    }

    interface PublishCallback{
        fun onSuccess()
        fun onFail(error:Throwable?)
    }

}