package edu.uw.eep523.summer2021.takepictures

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.divyanshu.draw.widget.DrawView
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class MainActivity : AppCompatActivity() {

    private var isLandScape: Boolean = false
    private var imageUri: Uri? = null
    private var imageUri2: Uri? = null
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()


//    private lateinit var result1: List<FirebaseVisionFace>

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

        val previewPane1 = findViewById<DrawView>(R.id.previewPane1)
        val previewPane2 = findViewById<DrawView>(R.id.previewPane2)


//        previewPane1?.setStrokeWidth(10.0f)
//        previewPane1?.setColor(Color.WHITE)
//        previewPane1?.setOnTouchListener { _, event ->
//
//            previewPane1?.onTouchEvent(event)
//            if (event.action == MotionEvent.ACTION_UP) {
//            }
//            true
//        }
//
//        previewPane2?.setStrokeWidth(10.0f)
//        previewPane2?.setColor(Color.WHITE)
//        previewPane2?.setOnTouchListener { _, event ->
//
//            previewPane2?.onTouchEvent(event)
//            if (event.action == MotionEvent.ACTION_UP) {
//                //action goes here
//            }
//            true
//        }
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
            putParcelable(KEY_IMAGE_URI+2, imageUri2)
        }
    }

    fun startCameraIntentForResult(view:View) {
        var previewPane1 = findViewById<DrawView>(R.id.previewPane1)
        previewPane1.clearCanvas()
        // Clean up last time's image
        imageUri = null
        print("hello")
//        previewPane1.set
//        previewPane1?.setImageBitmap(null)

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
        var previewPane2 = findViewById<DrawView>(R.id.previewPane2)
        // Clean up last time's image
        imageUri2 = null
        print("hello")
//        previewPane2?.setImageBitmap(null)
        previewPane2.clearCanvas()
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

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE)
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
        } else if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == Activity.RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            imageUri = data!!.data
            tryReloadAndDetectInImage(1)
        } else if (requestCode == REQUEST_CHOOSE_IMAGE+2 && resultCode == Activity.RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            imageUri2 = data!!.data
            tryReloadAndDetectInImage(2)
        }
    }

    private fun tryReloadAndDetectInImage(buttonNum: Int) {
        lateinit var previewPane: DrawView
        lateinit var localURI: Uri
        when (buttonNum) {
            1 -> {
                previewPane = findViewById<DrawView>(R.id.previewPane1)
                localURI = imageUri!!
            }
            2 -> {
                previewPane = findViewById<DrawView>(R.id.previewPane2)
                localURI = imageUri2!!
            }
        }
        previewPane.clearCanvas()


        try {
            if (buttonNum == 1 && localURI == null) {
                return
            }
            var imageBitmap = if (Build.VERSION.SDK_INT < 29) {
                MediaStore.Images.Media.getBitmap(contentResolver, localURI)
            } else {
                val source = ImageDecoder.createSource(contentResolver, localURI!!)
                ImageDecoder.decodeBitmap(source)
            }
            previewPane.background= BitmapDrawable(getResources(), imageBitmap)
//            previewPane?.setImageBitmap(imageBitmap) //previewPane is the ImageView from the layout

        } catch (e: IOException) {
        }
    }

    fun reloadImage(view: View) {
        tryReloadAndDetectInImage(1)
        tryReloadAndDetectInImage(2)
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


    fun startSwapFace(view: View) {
        CoroutineScope(Default).launch {
            swapFace()
        }
    }

    suspend fun swapFace() {
        val previewPane1 = findViewById<DrawView>(R.id.previewPane1)
        val previewPane2 = findViewById<DrawView>(R.id.previewPane2)


        var imageBitmap1: Bitmap
        var imageBitmap2: Bitmap

        try {
            if (imageUri == null || imageUri2 == null) {
                withContext(Main) {
                    displayText("imageURI: $imageUri, imageURI2: $imageUri2")
                }
                return
            }
            if (Build.VERSION.SDK_INT < 29) {
                imageBitmap1 = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                imageBitmap2 = MediaStore.Images.Media.getBitmap(contentResolver, imageUri2)

            } else {
                var source = ImageDecoder.createSource(contentResolver, imageUri!!)
                imageBitmap1 = ImageDecoder.decodeBitmap(source)
                source = ImageDecoder.createSource(contentResolver, imageUri2!!)
                imageBitmap2 = ImageDecoder.decodeBitmap(source)
            }
        } catch (e: IOException) {
            withContext (Main) {
                displayText("bitmap extraction failed with exception: $e")
            }
            return
        }
        val scaleFactor = 3
        imageBitmap1 = Bitmap.createScaledBitmap( imageBitmap1, (imageBitmap1.width / scaleFactor), (imageBitmap1.height / scaleFactor), true)
        imageBitmap2 = Bitmap.createScaledBitmap( imageBitmap2, (imageBitmap2.width / scaleFactor), (imageBitmap2.height / scaleFactor), true)

        val image1 = FirebaseVisionImage.fromBitmap(imageBitmap1.copy(Bitmap.Config.ARGB_8888, true))
        val image2 = FirebaseVisionImage.fromBitmap(imageBitmap2.copy(Bitmap.Config.ARGB_8888, true))

        val faces1 = detectFace(image1)
        val faces2 = detectFace(image2)

        if (faces1 == null || faces1.isEmpty() || faces2 == null || faces2.isEmpty()) {
            withContext (Main) {
                displayText("faces is null or no face found")
            }
            return
        }
        try {
            val face1 = faces1[0].boundingBox
            val face2 = faces2[0].boundingBox
            val width = min(face1.width(), face2.width())
            val height = min(face1.height(), face2.height())
            val image1_j_offset = if (face1.width()>face2.width()) abs(face1.width()-face2.width())/2 + face1.left else face1.left
            val image2_j_offset = if (face1.width()>face2.width()) face2.left else abs(face1.width()-face2.width())/2 + face2.left
            val image1_i_offset = if (face1.width()>face2.height()) abs(face1.height()-face2.height())/2 + face1.top else face1.top
            val image2_i_offset = if (face1.width()>face2.height()) face2.top else abs(face1.height()-face2.height())/2 + face2.top

            var newImageBitmap1 = imageBitmap1.copy(Bitmap.Config.ARGB_8888, true)
            var newImageBitmap2 = imageBitmap2.copy(Bitmap.Config.ARGB_8888, true)
            for (i in 0..height) {
                for (j in 0..width) {
                    val temp_color = newImageBitmap1.getPixel(image1_j_offset+j, image1_i_offset+i)
                    val cur_x1 = image1_j_offset+j
                    val cur_y1 = image1_i_offset+i
                    val cur_x2 = image2_j_offset+j
                    val cur_y2 = image2_i_offset+i
                    newImageBitmap1.setPixel(cur_x1, cur_y1, newImageBitmap2.getPixel(cur_x2, cur_y2))
                    newImageBitmap2.setPixel(cur_x2, cur_y2, temp_color)
                }
            }
            withContext(Main) {
                previewPane1.background = BitmapDrawable(getResources(), newImageBitmap1); //previewPane is the ImageView from the layout
                previewPane2.background = BitmapDrawable(getResources(), newImageBitmap2); //previewPane is the ImageView from the layout
            }
        } catch (e: IOException) {
        }


    }

    @Synchronized
    suspend fun detectFace(image: FirebaseVisionImage): List<FirebaseVisionFace>? {
        val highAccuracyOpts = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .build()
        val detector = FirebaseVision.getInstance()
            .getVisionFaceDetector(highAccuracyOpts)
//        lock.lock()
        var result: Task<List<FirebaseVisionFace>>? = null


        result = detector.detectInImage(image)
            .addOnSuccessListener { faces ->
                // Task completed successfully
                // ...
                displayText("detect succeeded")

            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...
                displayText("detect failed: ${e.cause}")

            }

        while (!result.isComplete) {
        }
        return result?.result
    }

    fun displayText(text: String) {
        val textBox = findViewById<TextView>(R.id.textView)
        textBox.setTextColor(Color.RED)
        textBox.text = text
    }

    fun startBlurImageForResult1(view:View) {
        CoroutineScope(Default).launch {
            blurFace(1)
        }
    }

    fun startBlurImageForResult2(view:View) {
        CoroutineScope(Default).launch {
            blurFace(2)
        }
    }


    suspend fun blurFace(number: Int) {
        val previewPane1 = if (number == 1) findViewById<DrawView>(R.id.previewPane1) else findViewById<DrawView>(R.id.previewPane2)
        val localURI = if (number == 1) imageUri else imageUri2
        var imageBitmap1: Bitmap

        try {
            if (localURI == null) {
                withContext(Main) {
                    displayText("imageURI: $localURI")
                }
                return
            }
            if (Build.VERSION.SDK_INT < 29) {
                imageBitmap1 = MediaStore.Images.Media.getBitmap(contentResolver, localURI)

            } else {
                var source = ImageDecoder.createSource(contentResolver, localURI!!)
                imageBitmap1 = ImageDecoder.decodeBitmap(source)
            }
        } catch (e: IOException) {
            withContext (Main) {
                displayText("bitmap extraction failed with exception: $e")
            }
            return
        }
        val scaleFactor = 3
        imageBitmap1 = Bitmap.createScaledBitmap( imageBitmap1, (imageBitmap1.width / scaleFactor), (imageBitmap1.height / scaleFactor), true)
        val image1 = FirebaseVisionImage.fromBitmap(imageBitmap1.copy(Bitmap.Config.ARGB_8888, true))

        val faces1 = detectFace(image1)

        if (faces1 == null || faces1.isEmpty()) {
            withContext (Main) {
                displayText("faces is null or no face found")
            }
            return
        }

        var newImageBitmap1 = imageBitmap1.copy(Bitmap.Config.ARGB_8888, true)

        val blurImage1 = bitmapBlur(imageBitmap1, 1.toFloat(), 20)
        if (blurImage1 == null) {
            withContext(Main) {
                displayText("blurImage is null")
            }
            return
        }
        try {
            val face1 = faces1[0].boundingBox
            for (i in face1.top..face1.top+face1.height()) {
                for (j in face1.left..face1.left+face1.width()) {
                    newImageBitmap1.setPixel(j, i, blurImage1.getPixel(j, i))
                }
            }
            withContext(Main) {
                previewPane1.background = BitmapDrawable(getResources(), newImageBitmap1); //previewPane is the ImageView from the layout
//                previewPane1.background = BitmapDrawable(getResources(), blurImage1)
            }
        } catch (e: IOException)  {
            withContext(Main) {
                displayText("Exception during blur face creation: $e")
                displayText("hello world")
            }
            return
        }


    }
    fun bitmapBlur(sentBitmap: Bitmap, scale: Float, radius: Int): Bitmap? { var sentBitmap = sentBitmap
        val width = Math.round(sentBitmap.width * scale)
        val height = Math.round(sentBitmap.height * scale)
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false)
        val bitmap = sentBitmap.copy(Bitmap.Config.ARGB_8888, true)
        if (radius < 1) {
            return null
        }
        val w = bitmap.width
        val h = bitmap.height
        val pix = IntArray(w * h)
        Log.e("pix", w.toString() + " " + h + " " + pix.size)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)
        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1
        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(Math.max(w, h))
        var divsum = div + 1 shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i=0
        while (i < 256 * divsum) {
            dv[i] = i / divsum
            i++ }
        yi = 0
        yw = yi
        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int
        y=0
        while (y < h) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
                    i = -radius
            while (i <= radius) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))]
                sir = stack[i + radius]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - Math.abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++ }
            stackpointer = radius
            x=0
            while (x < w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm)
                }
                p = pix[yw + vmin[x]]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi++
                x++
            }
            yw += w
            y++
        }
        x=0
        while (x < w) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = Math.max(0, yp) + x
                sir = stack[i + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                rbs = r1 - Math.abs(i)
                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                if (i < hm) {
                    yp += w
                }
                i++ }
            yi = x
            stackpointer = radius
            y=0
            while (y < h) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w
                }
                p = x + vmin[y]
                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                yi += w
                y++ }
            x++ }
        Log.e("pix", w.toString() + " " + h + " " + pix.size)
        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
        return bitmap
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
