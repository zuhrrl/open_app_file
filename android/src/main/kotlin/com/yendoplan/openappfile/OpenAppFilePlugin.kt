package com.yendoplan.openappfile

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.yendoplan.openappfile.utils.JsonUtil.toJson
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import java.io.File
import java.io.IOException

class OpenAppFilePlugin : MethodCallHandler, FlutterPlugin, ActivityAware {
    private lateinit var channel: MethodChannel

    private var activity: Activity? = null

    private var result: MethodChannel.Result? = null
    private var isResultSubmitted = false

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        isResultSubmitted = false
        if (call.method == "open_app_file") {
            this.result = result
            val filePath = call.argument<String?>("file_path")
            if (filePath == null) {
                // this check should be on the flutter side
                setResult(-4, "file path cannot be null")
                return
            }
            val typeString = if (call.hasArgument("type") && call.argument<Any?>("type") != null) {
                call.argument("type")
            } else {
                getFileType(filePath)
            }
            if (pathRequiresPermission(filePath)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!isFileAvailable(filePath)) {
                        return
                    }
                    if (!isMediaStorePath(filePath) && !Environment.isExternalStorageManager()) {
                        setResult(
                            -3,
                            "Permission denied: android.Manifest.permission.MANAGE_EXTERNAL_STORAGE"
                        )
                        return
                    }
                }
            } else {
                startActivity(filePath, typeString)
            }
        } else {
            result.notImplemented()
            isResultSubmitted = true
        }
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

    private fun pathRequiresPermission(filePath: String): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            false
        } else try {
            val context =
                activity?.applicationContext ?: throw RuntimeException("Not attached to context")
            val fileCanonicalPath = File(filePath).canonicalPath
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

    private fun isFileAvailable(filePath: String): Boolean {
        if (!File(filePath).exists()) {
            setResult(-2, "the $filePath file does not exists")
            return false
        }
        return true
    }

    private fun startActivity(filePath: String, typeString: String?) {
        val context = activity?.applicationContext
        if (!isFileAvailable(filePath) || context == null) {
            return
        }
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val packageName = context.packageName
            val uri = FileProvider.getUriForFile(
                context,
                "$packageName.fileProvider.com.yendoplan.openappfile",
                File(filePath)
            )
            intent.setDataAndType(uri, typeString)
        } else {
            intent.setDataAndType(Uri.fromFile(File(filePath)), typeString)
        }
        var type = 0
        var message = "done"
        try {
            activity?.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            type = -1
            message = "No APP found to open this file."
        } catch (e: Exception) {
            type = -4
            message = "File opened incorrectly."
        }
        setResult(type, message)
    }

    private fun getFileType(filePath: String): String {
        // maybe, it's better to use getMimeTypeFromExtension() instead of this?
        // ideally, extension to mime type conversion should be on the flutter side,
        // although current iOS implementation does not need mime type at all
        return when (filePath.substringAfterLast('.', "").lowercase()) {
            "3gp" -> "video/3gpp"
            "torrent" -> "application/x-bittorrent"
            "kml" -> "application/vnd.google-earth.kml+xml"
            "gpx" -> "application/gpx+xml"
            "asf" -> "video/x-ms-asf"
            "avi" -> "video/x-msvideo"
            "bin", "class", "exe" -> "application/octet-stream"
            "bmp" -> "image/bmp"
            "c" -> "text/plain"
            "conf" -> "text/plain"
            "cpp" -> "text/plain"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls", "csv" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "gif" -> "image/gif"
            "gtar" -> "application/x-gtar"
            "gz" -> "application/x-gzip"
            "h" -> "text/plain"
            "htm" -> "text/html"
            "html" -> "text/html"
            "ics" -> "text/calendar"
            "jar" -> "application/java-archive"
            "java" -> "text/plain"
            "jpeg" -> "image/jpeg"
            "jpg" -> "image/jpeg"
            "js" -> "application/x-javascript"
            "log" -> "text/plain"
            "m3u" -> "audio/x-mpegurl"
            "m4a" -> "audio/mp4a-latm"
            "m4b" -> "audio/mp4a-latm"
            "m4p" -> "audio/mp4a-latm"
            "m4u" -> "video/vnd.mpegurl"
            "m4v" -> "video/x-m4v"
            "mov" -> "video/quicktime"
            "mp2" -> "audio/x-mpeg"
            "mp3" -> "audio/x-mpeg"
            "mp4" -> "video/mp4"
            "mpc" -> "application/vnd.mpohun.certificate"
            "mpe" -> "video/mpeg"
            "mpeg" -> "video/mpeg"
            "mpg" -> "video/mpeg"
            "mpg4" -> "video/mp4"
            "mpga" -> "audio/mpeg"
            "msg" -> "application/vnd.ms-outlook"
            "ogg" -> "audio/ogg"
            "pdf" -> "application/pdf"
            "png" -> "image/png"
            "pps" -> "application/vnd.ms-powerpoint"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "prop" -> "text/plain"
            "rc" -> "text/plain"
            "rmvb" -> "audio/x-pn-realaudio"
            "rtf" -> "application/rtf"
            "sh" -> "text/plain"
            "tar" -> "application/x-tar"
            "tgz" -> "application/x-compressed"
            "txt" -> "text/plain"
            "wav" -> "audio/x-wav"
            "wma" -> "audio/x-ms-wma"
            "wmv" -> "audio/x-ms-wmv"
            "wps" -> "application/vnd.ms-works"
            "xml" -> "text/plain"
            "z" -> "application/x-compress"
            "zip" -> "application/x-zip-compressed"
            else -> "*/*"
        }
    }

    private fun setResult(type: Int, message: String) {
        // this is terrible "global variable" code, needs refactoring
        if (result != null && !isResultSubmitted) {
            result?.success(
                toJson(
                    mapOf(
                        "type" to type,
                        "message" to message
                    )
                )
            )
            isResultSubmitted = true
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