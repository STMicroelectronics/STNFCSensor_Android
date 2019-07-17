package com.st.smarTag.cloud

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.content.IntentFilter
import androidx.preference.PreferenceManager
import android.util.Log
import com.st.smarTag.cloud.config.CloudConfigFragment
import com.st.smarTag.cloud.provider.CloudProvider
import com.st.smartaglib.model.DataSample
import com.st.smartaglib.model.TagExtreme

private const val ACTION_SYNC_EVENTS = "com.st.smarTag.cloud.action.ACTION_SYNC_EVENTS"
private const val EXTRA_EVENTS_TO_SYNC = "com.st.smarTag.cloud.extra.EXTRA_EVENTS_TO_SYNC"

private const val ACTION_SYNC_EXTREMES = "com.st.smarTag.cloud.action.ACTION_SYNC_EXTREMES"
private const val EXTRA_EXTREMES_TO_SYNC = "com.st.smarTag.cloud.extra.EXTRA_EXTREMES_TO_SYNC"

private const val EXTRA_TAG_ID = "com.st.smarTag.cloud.extra.EXTRA_TAG_ID"

/**
 * This service will syncronize the data read from the SmarTag to a cloud provider
 * the provider and its configuration is stored in a sharedPreference object
 */
class SmarTagCloudSync : IntentService("SmarTagCloudSync") {

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_SYNC_EVENTS -> {
                val tagId = intent.getStringExtra(EXTRA_TAG_ID)!!
                val sampleData = (intent.getSerializableExtra(EXTRA_EVENTS_TO_SYNC) as? ArrayList<DataSample>)!!
                uploadData(tagId,sampleData)
            }
            ACTION_SYNC_EXTREMES -> {
                val tagId = intent.getStringExtra(EXTRA_TAG_ID)!!
                val extremeData = (intent.getSerializableExtra(EXTRA_EXTREMES_TO_SYNC) as? TagExtreme)!!
                uploadExtremeData(tagId,extremeData)
            }
        }
    }

    /* broadcast manager used to notify the service results*/
    private lateinit var broadcast: androidx.localbroadcastmanager.content.LocalBroadcastManager

    override fun onCreate() {
        super.onCreate()
        broadcast = androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)
    }

    private fun notifyConnectionError(error:Throwable){
        val notification = Intent(CONNECTION_ERROR_ACTION).apply {
            putExtra(EXTRA_CONNECTION_ERROR_DESCRIPTION,error.toString())
        }

        broadcast.sendBroadcast(notification)
    }

    private fun notifyCloudSyncStart(){
        val notification = Intent(CLOUD_SYNC_STARTED_ACTION)

        broadcast.sendBroadcast(notification)
    }

    private fun notifyCloudSyncCompleted(){
        val notification = Intent(CLOUD_SYNC_COMPLETED_ACTION)

        broadcast.sendBroadcast(notification)
    }

    private fun uploadData(tagId: String, sampleData: List<DataSample>) {
        if(sampleData.isEmpty())
            return
        Log.d("uploadData","uploadData: Tag:"+tagId+" Sample: "+sampleData.size)

        val cloudProvider = buildCloudProvider(tagId)
        cloudProvider?.connect(object : CloudProvider.ConnectionCallback{
            override fun onSuccess(conn: CloudProvider.Connection) {
                Log.i("AWS", "connect ok")
                notifyCloudSyncStart()
                cloudProvider.uploadSamples(conn,sampleData, object : CloudProvider.PublishCallback{
                    override fun onSuccess() {
                        cloudProvider.disconnect(conn)
                        notifyCloudSyncCompleted()
                    }

                    override fun onFail(error: Throwable?) {
                        cloudProvider.disconnect(conn)
                        if(error!=null)
                            notifyConnectionError(error)
                    }
                })
            }

            override fun onFail(error: Throwable) {
                notifyConnectionError(error)
            }
        })
    }

    private fun uploadExtremeData(tagId: String, extremeData: TagExtreme) {
        val cloudProvider = buildCloudProvider(tagId)
        cloudProvider?.connect(object : CloudProvider.ConnectionCallback{
            override fun onSuccess(conn: CloudProvider.Connection) {
                notifyCloudSyncStart()
                cloudProvider.uploadExtreme(conn,extremeData, object : CloudProvider.PublishCallback{
                    override fun onSuccess() {
                        cloudProvider.disconnect(conn)
                        notifyCloudSyncCompleted()
                    }

                    override fun onFail(error: Throwable?) {
                        cloudProvider.disconnect(conn)
                        if(error!=null)
                            notifyConnectionError(error)
                    }

                })

            }

            override fun onFail(error: Throwable) {
                notifyConnectionError(error)
            }
        })
    }


    /**
     * create a cloud provider object using the info stored in the sharedPreference object.
     * if the logging is disabled or an unknonw provider is selecte this method will return null.
     */
    private fun buildCloudProvider(tagId: String):CloudProvider?{
        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        //if not enable
        if(!preference.getBoolean(CloudConfigFragment.SERVICE_ENABLE_KEY,false))
            return null

        val serviceKey = preference.getString(CloudConfigFragment.SERVICE_SELECTOR_KEY,null)
        return if (serviceKey!=null)
            CloudServices.getServiceForKey(serviceKey)?.builder?.invoke(this,tagId)
        else
            null
    }

    companion object {
        /**
         * Start a service that will upload the Data sample into the cloud
         * [tagId] id used as identifier for the board that produced the data
         * [data] list of sample to upload
         * [context] context used to start the service
         */
        @JvmStatic
        fun startDataSync(context: Context, tagId: String, data:List<DataSample>) {
            Log.d("StartService",tagId)
            val intent = Intent(context, SmarTagCloudSync::class.java).apply {
                action = ACTION_SYNC_EVENTS
                putExtra(EXTRA_EVENTS_TO_SYNC, ArrayList(data))
                putExtra(EXTRA_TAG_ID,tagId)
            }
            context.startService(intent)
        }

        /**
         * Start a service that will upload the extremes data into the cloud
         * [tagId] id used as identifier for the board that produced the data
         * [data] extremes data to upload
         * [context] context used to start the service
         */
        @JvmStatic
        fun startExtremeSync(context: Context, tagId: String, data:TagExtreme) {
            Log.d("StartService",tagId)
            val intent = Intent(context, SmarTagCloudSync::class.java).apply {
                action = ACTION_SYNC_EXTREMES
                putExtra(EXTRA_EXTREMES_TO_SYNC, data)
                putExtra(EXTRA_TAG_ID,tagId)
            }
            context.startService(intent)
        }

        /**
         * action sent when an error happen during the upload
         */
        val CONNECTION_ERROR_ACTION = SmarTagCloudSync::class.java.name + ".CONNECTION_ERROR_ACTION"
        /**
         * extra parameters containing a description error
         */
        val EXTRA_CONNECTION_ERROR_DESCRIPTION = SmarTagCloudSync::class.java.name + ".EXTRA_CONNECTION_ERROR_DESCRIPTION"

        /**
         * action sent when the service successfully open a connection with the server
         */
        val CLOUD_SYNC_STARTED_ACTION = SmarTagCloudSync::class.java.name + ".CLOUD_SYNC_STARTED"

        /**
         * action sent when the service end to send data to the cloud
         */
        val CLOUD_SYNC_COMPLETED_ACTION = SmarTagCloudSync::class.java.name + ".CLOUD_SYNC_COMPLETED_ACTION"

        /**
         * utility function that return an intent filter to capture all the broadcast message sent by
         * thi service
         */
        fun getDataSyncIntentFilter(): IntentFilter {
            return IntentFilter().apply {
                addAction(CONNECTION_ERROR_ACTION)
                addAction(CLOUD_SYNC_STARTED_ACTION)
                addAction(CLOUD_SYNC_COMPLETED_ACTION)
            }
        }

    }
}
