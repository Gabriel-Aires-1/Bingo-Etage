package com.example.bingoetage.databaseIO.csv

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.example.bingoetage.databaseIO.Importer

class CSVImporter: Importer() {

    companion object {

        fun readFile(ARL: ActivityResultLauncher<Intent>)
        = readFile(ARL, "text/plain")
    }
}