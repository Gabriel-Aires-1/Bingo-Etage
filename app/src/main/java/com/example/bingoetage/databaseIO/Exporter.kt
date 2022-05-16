package com.example.bingoetage.databaseIO

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat.startActivityForResult

open class Exporter {

    companion object {

        fun createFile(
            ARL: ActivityResultLauncher<Intent>,
            fileName: String,
            mimeType: String,
        )
        {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = mimeType
                putExtra(Intent.EXTRA_TITLE, fileName)
            }
            ARL.launch(intent)
        }
    }
}