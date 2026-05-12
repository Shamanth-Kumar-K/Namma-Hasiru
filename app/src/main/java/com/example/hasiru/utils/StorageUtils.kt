package com.example.hasiru.utils

import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

object StorageUtils {
    private val storage = Firebase.storage.reference

    suspend fun uploadImage(uri: Uri, folder: String): String? {
        return try {
            val fileName = "img_${System.currentTimeMillis()}.jpg"
            val fileRef = storage.child("$folder/$fileName")
            
            // Upload file
            fileRef.putFile(uri).await()
            
            // Get download URL
            val downloadUrl = fileRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
