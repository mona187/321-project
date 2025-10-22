package com.example.cpen_321.utils

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Utility class for JSON operations.
 * Combines Gson-based object serialization and org.json safe parsing helpers.
 */
object JsonUtils {

    // Must be public because inline functions below reference it
    val gson = Gson()

    /**
     * Convert object to JSON string
     */
    fun <T> toJson(obj: T): String = gson.toJson(obj)

    /**
     * Convert JSON string to object (supports generics)
     */
    inline fun <reified T> fromJson(json: String): T? {
        return try {
            val type = object : TypeToken<T>() {}.type
            gson.fromJson<T>(json, type)
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    /**
     * Parse JSONObject safely
     */
    fun parseJsonObject(jsonString: String): JSONObject? {
        return try {
            JSONObject(jsonString)
        } catch (e: JSONException) {
            null
        }
    }

    /**
     * Parse JSONArray safely
     */
    fun parseJsonArray(jsonString: String): JSONArray? {
        return try {
            JSONArray(jsonString)
        } catch (e: JSONException) {
            null
        }
    }

    // ------------------------------------------------------------------------
    // JSONObject safe accessors
    // ------------------------------------------------------------------------

    fun JSONObject.getStringSafe(key: String, default: String = ""): String =
        try {
            if (has(key) && !isNull(key)) getString(key) else default
        } catch (e: JSONException) {
            default
        }

    fun JSONObject.getIntSafe(key: String, default: Int = 0): Int =
        try {
            if (has(key) && !isNull(key)) getInt(key) else default
        } catch (e: JSONException) {
            default
        }

    fun JSONObject.getLongSafe(key: String, default: Long = 0L): Long =
        try {
            if (has(key) && !isNull(key)) getLong(key) else default
        } catch (e: JSONException) {
            default
        }

    fun JSONObject.getDoubleSafe(key: String, default: Double = 0.0): Double =
        try {
            if (has(key) && !isNull(key)) getDouble(key) else default
        } catch (e: JSONException) {
            default
        }

    fun JSONObject.getBooleanSafe(key: String, default: Boolean = false): Boolean =
        try {
            if (has(key) && !isNull(key)) getBoolean(key) else default
        } catch (e: JSONException) {
            default
        }

    fun JSONObject.getJSONObjectSafe(key: String): JSONObject? =
        try {
            if (has(key) && !isNull(key)) getJSONObject(key) else null
        } catch (e: JSONException) {
            null
        }

    fun JSONObject.getJSONArraySafe(key: String): JSONArray? =
        try {
            if (has(key) && !isNull(key)) getJSONArray(key) else null
        } catch (e: JSONException) {
            null
        }

    // ------------------------------------------------------------------------
    // JSONArray utilities
    // ------------------------------------------------------------------------

    fun JSONArray.toStringList(): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until length()) {
            try {
                list.add(getString(i))
            } catch (_: JSONException) { /* skip invalid entries */ }
        }
        return list
    }

    fun JSONArray.toIntList(): List<Int> {
        val list = mutableListOf<Int>()
        for (i in 0 until length()) {
            try {
                list.add(getInt(i))
            } catch (_: JSONException) { /* skip invalid entries */ }
        }
        return list
    }

    // ------------------------------------------------------------------------
    // Pretty print JSON for debugging
    // ------------------------------------------------------------------------

    fun prettyPrint(json: String): String {
        return try {
            val trimmed = json.trim()
            if (trimmed.startsWith("[")) {
                JSONArray(trimmed).toString(2)
            } else {
                JSONObject(trimmed).toString(2)
            }
        } catch (e: JSONException) {
            json
        }
    }
}
