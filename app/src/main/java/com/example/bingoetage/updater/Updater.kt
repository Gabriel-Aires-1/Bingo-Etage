package com.example.bingoetage.updater

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
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

    suspend fun downloadUpdate(updateSummary: UpdateSummaryContainer){
        TODO("Not yet implemented")
    }

    suspend fun installUpdate(){
        TODO("Not yet implemented")
    }
}

class GitHubUpdater(user: String, repo: String): Updater() {
    /**
     * GitHub updater class, retrieves update information from the GitHub repo given in constructor
     *
     * checkUpdate method retrieves update information and calls updateListener methods on success or failure
     *
     * @param user            name of the owner of the repository
     * @param repo            name of the repository
     */

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