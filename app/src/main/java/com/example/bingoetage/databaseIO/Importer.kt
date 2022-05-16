package com.example.bingoetage.databaseIO

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

open class Importer {

    companion object {

        fun readFile(
            ARL: ActivityResultLauncher<Intent>,
            mimeType: String,
        )
        {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = mimeType
            }
            ARL.launch(intent)
        }
    }
}