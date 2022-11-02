package com.yendoplan.openappfile

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import org.json.JSONObject
import java.io.File
import java.io.IOException

fun Map<String, Any?>.toJsonString(): String {
    return JSONObject().also {
        for ((key, value) in this) {
            it.put(key, value)
        }
    }.toString()
}

class OpenAppFilePlugin : MethodCallHandler, FlutterPlugin, ActivityAware {
    private lateinit var channel: MethodChannel

    private var activity: Activity? = null

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if (call.method == "open_app_file") {
            val filePath = call.argument<String>("file_path")
                ?: throw RuntimeException("file_path cannot be null")
            val file = File(filePath)
            if (pathRequiresPermission(file) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                !isMediaStorePath(filePath) && !Environment.isExternalStorageManager()
            ) {
                result.success(
                    makeResult(
                        -3,
                        "Permission denied: android.Manifest.permission.MANAGE_EXTERNAL_STORAGE"
                    )
                )
                return
            }
            val mimeType = call.argument<String?>("mime_type") ?: detectMimeType(filePath)
            result.success(startActivity(file, mimeType))
        } else {
            result.notImplemented()
        }
    }

    private fun startActivity(file: File, mimeType: String?): String {
        val context =
            activity?.applicationContext ?: throw RuntimeException("Not attached to context")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val packageName = context.packageName
            val uri = FileProvider.getUriForFile(
                context,
                "$packageName.fileProvider.com.yendoplan.openappfile",
                file
            )
            intent.setDataAndType(uri, mimeType)
        } else {
            intent.setDataAndType(Uri.fromFile(file), mimeType)
        }

        try {
            activity?.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            return makeResult(-1, "No APP found to open this file.")
        } catch (e: Exception) {
            return makeResult(-4, "File opened incorrectly.")
        }
        return makeResult(0, "done")
    }

    private fun makeResult(type: Int, message: String): String {
        return mapOf(
            "type" to type,
            "message" to message
        ).toJsonString()
    }

    private fun detectMimeType(filePath: String): String {
        return MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(filePath.substringAfterLast('.', "").lowercase()) ?: "*/*"
    }

    private fun isMediaStorePath(filePath: String): Boolean {
        return listOf(
            "/DCIM/",
            "/Pictures/",
            "/Movies/",
            "/Alarms/",
            "/Audiobooks/",
            "/Music/",
            "/Notifications/",
            "/Podcasts/",
            "/Ringtones/",
            "/Download/"
        ).any {
            filePath.contains(it)
        }
    }

    private fun pathRequiresPermission(file: File): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            false
        } else try {
            val context =
                activity?.applicationContext ?: throw RuntimeException("Not attached to context")
            val fileCanonicalPath = file.canonicalPath
            return !listOfNotNull(
                File(context.applicationInfo.dataDir).canonicalPath,
                context.externalCacheDir?.canonicalPath,
                context.getExternalFilesDir(null)?.canonicalPath
            ).any {
                fileCanonicalPath.startsWith(it)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            true
        }
    }

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "open_app_file")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }
}