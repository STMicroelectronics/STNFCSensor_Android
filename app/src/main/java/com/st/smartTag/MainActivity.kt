/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *    and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *    conditions and the following disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *    STMicroelectronics company nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *    in a directory whose title begins with st_images may only be used for internal purposes and
 *    shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *    icons, pictures, logos and other images that are provided with the source code in a directory
 *    whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package com.st.smartTag

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.st.smartTag.tagExtremeData.TagExtremeDataFragment
import com.st.smartTag.tagExtremeData.TagExtremeViewModel
import com.st.smartTag.tagPlotData.TagDataFragment
import com.st.smartTag.tagPlotData.TagDataViewModel
import com.st.smartTag.tagSettings.TagSettingsFragment
import com.st.smartTag.tagSettings.TagSettingsViewModel
import com.st.smartTag.tagSingleShot.TagSingleShotFragment

class MainActivity : AppCompatActivity() {

    private lateinit var navigationView: BottomNavigationView

    private val settingFragment = TagSettingsFragment()
    private val extremeDataFragment = TagExtremeDataFragment()
    private val plotFragment = TagDataFragment()
    private val singleShotFragment = TagSingleShotFragment()

    private lateinit var nfcTagHolder: NfcTagViewModel

    private lateinit var nfcAdapter: NfcAdapter

    private var errorToast: Toast? = null
    private lateinit var mainView: View

    private val exportResultReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ExportDataService.ACTION_EXPORT_ERROR -> {
                    val error = intent.getStringExtra(ExportDataService.EXTRA_EXPORT_ERROR)
                    Snackbar.make(mainView, error, Snackbar.LENGTH_SHORT).show()
                }
                ExportDataService.ACTION_EXPORT_SUCCESS -> {
                    val fileUri = intent.getParcelableExtra<Uri>(ExportDataService.EXTRA_EXPORTED_FILE)
                    val viewIntent = Intent(Intent.ACTION_VIEW)
                    viewIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    viewIntent.setDataAndType(fileUri, "text/csv")
                    val appChooser = Intent.createChooser(
                            viewIntent, getString(R.string.main_open_data_file_chooser))
                    appChooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK // need for android 4.4

                    Snackbar.make(mainView, getString(R.string.main_export_data_success), Snackbar.LENGTH_LONG)
                            .setAction(R.string.main_open, { context?.startActivity(appChooser)} )
                            .show()
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        setContentView(R.layout.activity_main)
        mainView = findViewById(R.id.main_root_view)
        navigationView = findViewById(R.id.main_navigation)
        navigationView.setOnNavigationItemSelectedListener({ this.handleNavigationItemSelected(it) })

        if(savedInstanceState==null)//first time
            showFragment(settingFragment)

        errorToast = Toast(this)

        nfcTagHolder = NfcTagViewModel.create(this)
        initializeNfcObserver()

        if (intent?.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            nfcTagHolder.nfcTagDiscovered(tag)
        }
    }

    private fun initializeNfcObserver() {
        nfcTagHolder.nfcTag.observe(this, Observer {
            if (it == null) {
                showSearchTag = true
                Snackbar.make(mainView, getString(R.string.main_tag_missing), Snackbar.LENGTH_SHORT).show()
            } else {
                showSearchTag = false
                Snackbar.make(mainView, R.string.main_tag_detected, Snackbar.LENGTH_SHORT).show()
            }
        })
        nfcTagHolder.ioError.observe(this, Observer { errorMsg ->
            if(errorMsg!=null)
                Log.e("SmartTag","error: "+errorMsg)
            /*errorToast?.cancel() // remove the previous one
            if (errorMsg == null) {
                errorToast = null // free the reference
            } else {
                errorToast = Toast.makeText(this,errorMsg,Toast.LENGTH_SHORT)
                errorToast?.show()
            }*/
        })
    }

    private var showSearchTag: Boolean
        get() = findViewById<View>(R.id.main_progressBar).visibility == View.VISIBLE
        set(value) {
            val visibility = if (value) View.VISIBLE else View.GONE
            findViewById<View>(R.id.main_progressBar).visibility = visibility
            findViewById<View>(R.id.main_progressMessage).visibility = visibility
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.activiy_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.main_menu_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            R.id.main_menu_exportXLSLog -> {
                exportDataToXlsFile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter.enableReaderMode(this,
                { foundTag -> nfcTagHolder.nfcTagDiscovered(foundTag) },
                NfcAdapter.FLAG_READER_NFC_V, Bundle())

        LocalBroadcastManager.getInstance(this).registerReceiver(exportResultReceiver,
                ExportDataService.getExportDataResponseFilter())
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableReaderMode(this)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(exportResultReceiver)
    }

    private fun handleNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_settings -> {
                showFragment(settingFragment)
                return true
            }
            R.id.navigation_data -> {
                showFragment(extremeDataFragment)
                return true
            }
            R.id.navigation_chart -> {
                showFragment(plotFragment)
                return true
            }
            R.id.navigation_singleShot -> {
                showFragment(singleShotFragment)
                return true
            }
        }
        return false
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.main_contentView, fragment)
                .commit()
    }

    private fun exportDataToXlsFile() {
        if (!isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestWritePermission()
        } else {
            val settings = TagSettingsViewModel.create(this).currentSettings.value
            val extreme = TagExtremeViewModel.create(this).dataExtreme.value
            val dataSample = TagDataViewModel.create(this).allSampleList.value
            ExportDataService.startExportCSVData(this, settings, extreme, dataSample)
        }
    }

    private fun requestWritePermission() {
        if (shouldShowPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Snackbar.make(mainView, R.string.main_request_write_permission,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, {
                        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_WRITE_PERMISSION)
                    })
                    .show()
        } else {
            // Camera permission has not been granted yet. Request it directly.
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_WRITE_PERMISSION)
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        val writePermissionIndex = permissions.indexOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (requestCode == REQUEST_WRITE_PERMISSION && writePermissionIndex >= 0) {
            // Check if the permission has been granted
            if (grantResults[writePermissionIndex] == PackageManager.PERMISSION_GRANTED) {
                exportDataToXlsFile()
            } else {
                Snackbar.make(mainView, R.string.main_write_permission_denied,
                        Snackbar.LENGTH_SHORT).show()
            }
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {

        /**
         * Id to identify a Write permission request.
         */
        const val REQUEST_WRITE_PERMISSION = 0

    }

    private fun AppCompatActivity.isPermissionGranted(permission: String) =
            ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun AppCompatActivity.shouldShowPermissionRationale(permission: String) =
            ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

    private fun AppCompatActivity.requestPermission(permission: String, requestId: Int) =
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestId)

}
