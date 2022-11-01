package com.yendoplan.openappfile.utils

import org.json.JSONException
import org.json.JSONObject

/**
 * Note of this class.
 *
 * @author crazecoder
 * @since 2018/12/28
 */
object JsonUtil {
    fun toJson(map: Map<String, Any?>): String {
        val jsonObject = JSONObject()
        try {
            for ((key, value) in map) {
                jsonObject.put(key, value)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject.toString()
    }
}