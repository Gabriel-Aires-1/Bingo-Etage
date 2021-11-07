package com.example.bingoetage.updater

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import com.example.bingoetage.BuildConfig
import kotlin.math.min

/**
 * Helper class for the Updater class
 */
class UpdaterHelper
{
    companion object
    {
        /**
         * Check if the version in the UpdateSummaryContainer object is different than the current version
         * @param update    UpdateSummaryContainer containing the update information
         * @return Boolean: True if update is different than current version, false otherwise
         */
        fun isNewVersionAvailable(update: UpdateSummaryContainer): Boolean
        {
            val currentVersion = BuildConfig.VERSION_NAME.lowercase()
            val newVersion = update.versionNumber.lowercase().removePrefix("v")

            val currentVersionList = currentVersion.substringBefore("-").split(".").map { it.toInt() }
            val newVersionList = newVersion.substringBefore("-").split(".").map { it.toInt() }

            val currentVersionSuffix = currentVersion.substringAfter("-", "")
            val newVersionSuffix = newVersion.substringAfter("-", "")

            var newerVersionAvailable = false

            for ( i in 0 until min(currentVersionList.size, newVersionList.size) )
            {
                if ( newVersionList[i] > currentVersionList[i] )
                {
                    newerVersionAvailable = true
                    break
                }
            }
            if ( !newerVersionAvailable && currentVersionSuffix != newVersionSuffix )
                newerVersionAvailable = true

            return newerVersionAvailable
        }


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
                                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)
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