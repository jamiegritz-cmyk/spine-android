package com.spine.musicplayer.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.DocumentsContract

object MusicFolderPreferences {
    private const val PREFS_NAME = "graiz_folder_prefs"
    private const val KEY_FOLDER_URI = "selected_folder_uri"
    private const val KEY_FOLDER_NAME = "selected_folder_name"
    private const val KEY_FOLDER_PATH = "selected_folder_path"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSelectedFolder(context: Context, uri: Uri) {
        val uriString = uri.toString()
        val docId = DocumentsContract.getTreeDocumentId(uri) ?: ""
        // docId is typically like "primary:Music" or "primary:Music/My Music" or "0123-4567:Audio"
        val pathPart = if (docId.contains(":")) {
            docId.substringAfter(":")
        } else {
            docId
        }.trim('/')

        val displayName = if (pathPart.isNotEmpty()) {
            pathPart
        } else {
            uri.lastPathSegment ?: "Selected Folder"
        }

        getPrefs(context).edit()
            .putString(KEY_FOLDER_URI, uriString)
            .putString(KEY_FOLDER_NAME, displayName)
            .putString(KEY_FOLDER_PATH, pathPart)
            .apply()
    }

    fun getSelectedFolderUri(context: Context): String? {
        return getPrefs(context).getString(KEY_FOLDER_URI, null)
    }

    fun getSelectedFolderName(context: Context): String? {
        return getPrefs(context).getString(KEY_FOLDER_NAME, null)
    }

    fun getSelectedFolderPath(context: Context): String? {
        return getPrefs(context).getString(KEY_FOLDER_PATH, null)
    }

    fun clearSelectedFolder(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
