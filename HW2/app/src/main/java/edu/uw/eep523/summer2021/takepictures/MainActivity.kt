package edu.uw.eep523.summer2021.takepictures

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    private var isLandScape: Boolean = false
    private var imageUri: Uri? = null
    private var imageUri2: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        getRuntimePermissions()
        if (!allPermissionsGranted()) {
            getRuntimePermissions()
        }
        isLandScape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        savedInstanceState?.let {
            imageUri = it.getParcelable(KEY_IMAGE_URI+1)
        }

        savedInstanceState?.let {
            imageUri2 = it.getParcelable(KEY_IMAGE_URI+2)
        }
    }

    private fun getRequiredPermissions(): Array<String?> {
        return try {
            val info = this.packageManager
                .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.isNotEmpty()) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: Exception) {
            arrayOfNulls(0)
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in getRequiredPermissions()) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    return false
                }
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions = ArrayList<String>()
        for (permission in getRequiredPermissions()) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    allNeededPermissions.add(permission)
                }
            }
        }

        if (allNeededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this, allNeededPermissions.toTypedArray(), PERMISSION_REQUESTS)
        }
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }


    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        with(outState) {
            putParcelable(KEY_IMAGE_URI+1, imageUri)
            putParcelable(KEY_IMAGE_URI+2, imageUri)
        }
    }

    fun startCameraIntentForResult(view:View) {
        var previewPane1 = findViewById<ImageView>(R.id.previewPane1)
        // Clean up last time's image
        imageUri = null
        print("hello")
        previewPane1?.setImageBitmap(null)

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.resolveActivity(packageManager)?.let {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }


    fun startCameraIntentForResult2(view:View) {
        var previewPane2 = findViewById<ImageView>(R.id.previewPane2)
        // Clean up last time's image
        imageUri2 = null
        print("hello")
        previewPane2?.setImageBitmap(null)

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.resolveActivity(packageManager)?.let {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE+2)
        }
    }

    fun startChooseImageIntentForResult1(view:View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE+1)
    }

    fun startChooseImageIntentForResult2(view:View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE+2)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            tryReloadAndDetectInImage(1)
        } else if (requestCode == REQUEST_IMAGE_CAPTURE+2 && resultCode == Activity.RESULT_OK) {
            tryReloadAndDetectInImage(2)
        } else if (requestCode == REQUEST_CHOOSE_IMAGE+1 && resultCode == Activity.RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            imageUri = data!!.data
            tryReloadAndDetectInImage(1)
        } else if (requestCode == REQUEST_CHOOSE_IMAGE+2 && resultCode == Activity.RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            imageUri = data!!.data
            tryReloadAndDetectInImage(2)
        }
    }

    private fun tryReloadAndDetectInImage(buttonNum: Int) {
        lateinit var previewPane: ImageView
        when (buttonNum) {
            1 -> previewPane = findViewById<ImageView>(R.id.previewPane1)
            2 -> previewPane = findViewById<ImageView>(R.id.previewPane2)
        }

        try {
            if (imageUri == null) {
                return
            }
            var imageBitmap = if (Build.VERSION.SDK_INT < 29) {
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            } else {
                val source = ImageDecoder.createSource(contentResolver, imageUri!!)
                ImageDecoder.decodeBitmap(source)
            }


            previewPane?.setImageBitmap(imageBitmap) //previewPane is the ImageView from the layout

        } catch (e: IOException) {
        }
    }

    fun startEditIntentForResult(view: View) {
        val previewPane2 = findViewById<ImageView>(R.id.previewPane2)

        try {
            if (imageUri == null) {
                print("hello world")
                return
            }
            var imageBitmap = if (Build.VERSION.SDK_INT < 29) {
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            } else {
                val source = ImageDecoder.createSource(contentResolver, imageUri!!)
                ImageDecoder.decodeBitmap(source)
            }

            val flippedBitmap = createFlippedBitmap(imageBitmap)
            previewPane2?.setImageBitmap(flippedBitmap) //previewPane is the ImageView from the layout

        } catch (e: IOException) {
        }
    }
    fun createFlippedBitmap(source: Bitmap): Bitmap? {
        val matrix = Matrix()
        matrix.postScale(
            -1F,
            1F,
            source.width / 2f,
            source.height / 2f
        )
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }


    private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
        outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }
    companion object {
        private const val KEY_IMAGE_URI = "edu.uw.eep523.takepicture.KEY_IMAGE_URI"
        private const val REQUEST_IMAGE_CAPTURE = 1001
        private const val REQUEST_CHOOSE_IMAGE = 1002
        private const val PERMISSION_REQUESTS = 1
    }
}
