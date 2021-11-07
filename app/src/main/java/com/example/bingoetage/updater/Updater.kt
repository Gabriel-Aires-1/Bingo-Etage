package com.example.bingoetage.updater

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bingoetage.R
import org.json.JSONObject
import java.io.IOError


/**
 * General updater class, retrieves update information with checkUpdate method,
 * download update with the downloadUpdate method and install the update with the installUpdate method
 *
 * checkUpdate method retrieves update information in background and calls updateListener methods on success or failure
 * downloadUpdate method download the update described in the updateSummary object in background
 * installUpdate method installs the update
 */
abstract class Updater {

    /**
     * Abstract function to be overridden by children Updater classes with the corresponding logic
     * @param context           The application context
     * @param updateListener    The listener called when a response is received from GitHub servers
     */
    abstract fun checkUpdate(context: Context, updateListener: UpdateListener)

    /**
     * Start the download from the address contained in the UpdateSummaryContainer object
     * @param context           The application context
     * @param update            The update summary object retrieved from the checkUpdate method
     */
    fun downloadUpdate(context: Context, update: UpdateSummaryContainer): Long
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

        return downloadManager.enqueue(downloadRequest)
    }

    /**
     * Start the installation of the update
     * @param context           The application context
     * @param localPath         Local path of the update file
     * @param MimeType          MimeType of the update
     */
    fun installUpdate(context: Context, localPath: String, MimeType: String){
        val installIntent = Intent(Intent.ACTION_VIEW)

        installIntent.setDataAndType(
            Uri.parse(localPath),
            MimeType
        )
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(installIntent)
    }
}

/**
 * GitHub updater class, retrieves update information from the GitHub repo given in constructor
 *
 * checkUpdate method retrieves update information and calls updateListener methods on success or failure
 *
 * @param user            name of the owner of the repository
 * @param repo            name of the repository
 */
class GitHubUpdater(user: String, repo: String): Updater() {

    private var releaseURL = "${GITHUB_URL}${user}/${repo}/releases/latest"

    /**
     * Retrieves update information and calls updateListener methods on success or failure
     * @param context           The application context
     * @param updateListener    The listener called when a response is received from GitHub servers
     */
    override fun checkUpdate(context: Context, updateListener: UpdateListener)
    {

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

    /**
     * Parse the GitHub JSON to retrieve the relevant information in an UpdateSummaryContainer
     * @param response      The JSONObject response from GitHub
     * @return              An UpdateSummaryContainer containing the relevant information from the JSONObject
     */
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