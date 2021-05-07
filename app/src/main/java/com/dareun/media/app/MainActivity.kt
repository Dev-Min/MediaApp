package com.dareun.media.app

import android.content.ContentValues
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import com.bumptech.glide.Glide
import gun0912.tedimagepicker.builder.TedImagePicker
import gun0912.tedimagepicker.builder.type.MediaType
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "Image Tester"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        image_pic_button.setOnClickListener {
            TedImagePicker.with(this)
                    .mediaType(MediaType.IMAGE)
                    .start { uri ->
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                            Glide.with(this).load(uri).into(image_view)
                        } else {
                            Log.e(TAG, "Complete image selection")
                            val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                            val imageDetails = ContentValues().apply {
                                put(MediaStore.Images.Media.DISPLAY_NAME, "Image_${System.currentTimeMillis()}.jpg")
                                put(MediaStore.Images.Media.IS_PENDING, 1)
                            }
                            Log.e(TAG, "Create image meta-data")

                            Log.e(TAG, "Start insert")
                            contentResolver.insert(imageCollection, imageDetails)?.let { imageUri ->
                                Log.e(TAG, "Complete insert")
                                Log.e(TAG, "Start openFileDescriptor")
                                contentResolver.openFileDescriptor(imageUri, "w", null)?.use { pfd ->
                                    Log.e(TAG, "Complete openFileDescriptor")
                                    Log.e(TAG, "Start openInputStream")
                                    contentResolver.openInputStream(uri)?.let { inputStream->
                                        Log.e(TAG, "Complete openInputStream")
                                        Log.e(TAG, "Start FileOutputStream")
                                        val outputStream = FileOutputStream(pfd.fileDescriptor)
                                        outputStream.use {
                                            inputStream.copyTo(it)
                                        }
                                        // Origin Code -> Low speed
//                                        while (true) {
//                                            val data = inputStream.read()
//                                            if (data == -1) {
//                                                break
//                                            }
//                                            outputStream.write(data)
//                                            Log.e(TAG, "Complete FileOutputStream writing")
//                                        }
                                        inputStream.close()
                                        outputStream.close()
                                        Log.e(TAG, "stream close")
                                    }
                                }

                                Log.e(TAG, "Start change image meta-data")
                                imageDetails.clear()
                                imageDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
                                contentResolver.update(imageUri, imageDetails, null, null)
                                Log.e(TAG, "Complete change image meta-data")

                                Glide.with(this).load(imageUri).into(image_view)
                            }
                        }
                    }
        }
    }
}