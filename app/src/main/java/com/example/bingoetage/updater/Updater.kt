package com.example.bingoetage.updater

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bingoetage.R
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.IOError


abstract class Updater {
    /**
     * General updater class, retrieves update information with checkUpdate method,
     * download update with the downloadUpdate method and install the update with the installUpdate method
     *
     * checkUpdate method retrieves update information in background and calls updateListener methods on success or failure
     * downloadUpdate method download the update described in the updateSummary object in background
     * installUpdate method installs the update
     */
    abstract suspend fun checkUpdate(context: Context, updateListener: UpdateListener)

    suspend fun downloadUpdate(activity: FragmentActivity, context: Context, updateSummary: UpdateSummaryContainer){
        showVersionDialog(activity, context, updateSummary)
    }

    suspend fun installUpdate(context: Context, localPath: String, downloadID: Long){
        val installIntent = Intent(Intent.ACTION_VIEW)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        installIntent.setDataAndType(
            Uri.parse(localPath),
            downloadManager.getMimeTypeForDownloadedFile(downloadID)
        )
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(installIntent)
    }

    private fun showVersionDialog(activity: FragmentActivity, context: Context, update: UpdateSummaryContainer)
    {
        val versionDialog = VersionDialog(
            update,
            object: VersionDialogListener
            {
                override fun onClickPositiveButton()
                {
                    val downloadRequest = DownloadManager.Request(Uri.parse(update.downloadURL))

                    downloadRequest.setTitle(context.resources.getString(R.string.main_toolbar_title))
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                        .setAllowedOverRoaming(false)
                        .setAllowedNetworkTypes(
                            DownloadManager.Request.NETWORK_WIFI
                                    or DownloadManager.Request.NETWORK_MOBILE
                        )

                    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                    val downloadID = downloadManager.enqueue(downloadRequest)

                    context.registerReceiver(
                        setupBroadcastReceiver(
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
        )
        versionDialog.show(activity.supportFragmentManager, "VersionDialog")
    }

    private fun setupBroadcastReceiver(downloadID: Long, downloadManager: DownloadManager): BroadcastReceiver
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
                                runBlocking{
                                    installUpdate(context, localPath, downloadID)
                                }

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

class GitHubUpdater(user: String, repo: String): Updater() {

    private var releaseURL = "${GITHUB_URL}${user}/${repo}/releases/latest"

    override suspend fun checkUpdate(context: Context, updateListener: UpdateListener) {
        /**
         * Retrieves update information and calls updateListener methods on success or failure
         */

        val queue = Volley.newRequestQueue(context)

        // Request a JSONObject response from the URL.
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, releaseURL, null,
            {response ->
                updateListener.onSuccess(
                    parseJSON(response)
                )
            },
            {error -> updateListener.onFailed(IOError(error))})

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest)
    }

    private fun parseJSON(response: JSONObject): UpdateSummaryContainer{

        val versionNumber: String = response.getString("tag_name")
        val versionTitle: String = response.getString("name")
        val patchNote: String = response.getString("body")

        // The file data are stored in the first asset
        val assetSummary: JSONObject = response.getJSONArray("assets").getJSONObject(0)

        val downloadSizeInBytes: Long = assetSummary.getString("size").toLong()
        val downloadURL: String = assetSummary.getString("browser_download_url")

        return UpdateSummaryContainer(
            versionNumber.removePrefix("v"),
            versionTitle,
            patchNote,
            downloadURL,
            downloadSizeInBytes,
        )
    }

    companion object {
        private const val GITHUB_URL = "https://api.github.com/repos/"
    }

}