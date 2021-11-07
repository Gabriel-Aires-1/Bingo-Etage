package com.example.bingoetage.updater

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import androidx.core.app.ActivityCompat.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.bingoetage.BuildConfig

/**
 * Helper class for the Updater class
 */
class UpdaterHelper
{
    private fun checkPermissions(activity: FragmentActivity, context: Context, permission: String): Boolean
    {
        return when
        {
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                true
            }
            /*shouldShowRequestPermissionRationale(activity, Manifest.permission.INTERNET) -> {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            showInContextUI(...)
        }*/
            else -> {
                // You can directly ask for the permission.
                requestPermissions(activity,
                    arrayOf(permission),
                    10)
                false
            }
        }
    }

    companion object
    {
        /**
         * Check if the version in the UpdateSummaryContainer object is different than the current version
         * @param update    UpdateSummaryContainer containing the update information
         * @return Boolean: True if update is different than current version, false otherwise
         */
        fun isNewVersionAvailable(update: UpdateSummaryContainer) =
            BuildConfig.VERSION_NAME.lowercase() != update.versionNumber.lowercase()

        /**
         * Check if an update is available (wraps the updater method)
         * @param context           The application context
         * @param updater           The relevant updater
         * @param updateListener    The listener called when a response is received from GitHub servers
         */
        fun checkUpdate(context: Context, updater: Updater, updateListener: UpdateListener)
        {
            updater.checkUpdate(context, updateListener)
        }

        /**
         * Returns a VersionDialogListener starting the download with the positive button and doing nothing otherwise
         * @param context           The application context
         * @param update            UpdateSummaryContainer containing the update information
         * @param updater           The relevant updater
         * @return VersionDialogListener: The relevant VersionDialogListener
         */
        fun getVersionDialogListener(context: Context, update: UpdateSummaryContainer, updater: Updater): VersionDialogListener
        {
            return object: VersionDialogListener
            {
                override fun onClickPositiveButton()
                {
                    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                    val downloadID = updater.downloadUpdate(context, update)

                    context.registerReceiver(
                        setupBroadcastReceiver(
                            updater,
                            downloadID,
                            downloadManager
                        ),
                        IntentFilter(
                            DownloadManager.ACTION_DOWNLOAD_COMPLETE
                        )
                    )
                }

                override fun onClickNegativeButton()
                {
                    // Nothing to do
                }
            }
        }

        /**
         * Returns a BroadcastReceiver which starts the installation when the download with the downloadManager completes
         * @param updater           The relevant updater
         * @param downloadID        The download id of the download with the DownloadManager
         * @param downloadManager   An instance of the DownloadManager
         * @return BroadcastReceiver: The relevant BroadcastReceiver
         */
        private fun setupBroadcastReceiver(updater: Updater, downloadID: Long, downloadManager: DownloadManager): BroadcastReceiver
        {
            val receiver: BroadcastReceiver = object : BroadcastReceiver()
            {
                override fun onReceive(context: Context, intent: Intent)
                {
                    val action = intent.action
                    if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action)
                    {
                        val query = DownloadManager.Query()
                        query.setFilterById(downloadID)
                        val cursor: Cursor = downloadManager.query(query)
                        if (cursor.moveToFirst())
                        {
                            val columnIndex: Int = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            when(cursor.getInt(columnIndex))
                            {
                                DownloadManager.STATUS_SUCCESSFUL ->
                                {
                                    val localPath: String = cursor.getString(
                                        cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                                    )

                                    cursor.close()
                                    updater.installUpdate(
                                        context,
                                        localPath,
                                        downloadManager.getMimeTypeForDownloadedFile(downloadID)
                                    )

                                    context.unregisterReceiver(this)
                                }
                                DownloadManager.STATUS_FAILED -> context.unregisterReceiver(this)
                            }
                        }
                        cursor.close()
                    }
                }
            }
            return receiver
        }
    }

}