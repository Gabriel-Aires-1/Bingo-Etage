package com.example.bingoetage.databaseIO.csv

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.example.bingoetage.databaseIO.Exporter

class CSVExporter: Exporter() {

    companion object {

        fun createFile(
            ARL: ActivityResultLauncher<Intent>,
            fileName: String,
            ) = createFile(ARL, fileName, "text/plain")
    }
}