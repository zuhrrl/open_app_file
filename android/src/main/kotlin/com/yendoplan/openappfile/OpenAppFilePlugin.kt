package com.yendoplan.openappfile

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import java.io.FileNotFoundException

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
            try {
                // This seems the most straightforward solution to checking if we have
                // read access to the file: just try to peek in it (opposed to convoluted
                // solution of the original `open_file` library where they maintain
                // a whole list of accessible locations for different permission conditions).
                // use() call in the end makes sure that the buffer is closed nicely
                // if no exception has been thrown.
                file.bufferedReader(bufferSize = 1).use { }
            } catch (e: FileNotFoundException) {
                result.success(makeResult(-3, "No file access permission."))
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
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            addCategory(Intent.CATEGORY_DEFAULT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileProvider.com.yendoplan.openappfile",
                    file
                )
                setDataAndType(uri, mimeType)
            } else {
                setDataAndType(Uri.fromFile(file), mimeType)
            }
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