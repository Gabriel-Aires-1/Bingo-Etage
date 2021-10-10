package com.example.bingoetage.updater

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.bingoetage.BuildConfig
import kotlinx.coroutines.runBlocking
import java.io.IOError

class UpdaterHelper {
    fun startUpdate(activity: FragmentActivity, context: Context, updater: Updater){

        if (!checkPermissions(activity, context, Manifest.permission.INTERNET)) return

        runBlocking{
            updater.checkUpdate(context, object: UpdateListener {
                override fun onSuccess(update: UpdateSummaryContainer) {
                    Log.d("BELTA", update.downloadURL)
                    Log.d("BELTA", "new version: ${isNewVersionAvailable(update)}")
                }

                override fun onFailed(error: IOError) {
                    Log.d("BELTA", error.toString())
                }
            }
            )
        }
    }

    private fun isNewVersionAvailable(update: UpdateSummaryContainer) =
        BuildConfig.VERSION_NAME.lowercase() != update.versionNumber.lowercase()

    private fun showVersionDialog(update: UpdateSummaryContainer): Boolean{

        return false
    }

    private fun checkPermissions(activity: FragmentActivity, context: Context, permission: String): Boolean{
        return when {
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

}