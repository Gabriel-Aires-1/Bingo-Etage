package com.example.bingoetage.updater

import java.io.IOError

interface UpdateListener {
    /**
     * onSuccess method called after it is successful
     * onFailed method called if it can't retrieve the latest version
     *
     * @param update            object with the latest update information: version, patchNote and url to download
     */
    fun onSuccess(update: UpdateSummaryContainer)

    fun onFailed(error: IOError)
}