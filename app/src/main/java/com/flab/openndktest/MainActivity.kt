package com.flab.openndktest

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.TextView
import com.flab.openndktest.databinding.ActivityMainBinding
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import java.util.*
import java.util.logging.Logger

class MainActivity : AppCompatActivity() , CameraBridgeViewBase.CvCameraViewListener2 {

  private lateinit var binding: ActivityMainBinding

  private lateinit var matInput: Mat
  private lateinit var matResult: Mat
  external fun ConvertRGBtoGray(matAddrInput: Long, matAddrResult: Long)

  init {
    System.loadLibrary("opencv_java4")
    System.loadLibrary("native-lib")
  }


  private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
    override fun onManagerConnected(status: Int) {
      when (status) {
        SUCCESS -> {
          Log.d("test", "enable")
          binding.surfaceView.enableView()
        }
        else -> {
          super.onManagerConnected(status)
        }
      }
    }
  }
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.setFlags(
      WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
      WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    binding = ActivityMainBinding.inflate(layoutInflater)

    setContentView(binding.root)
    with(binding.surfaceView){
      visibility = SurfaceView.VISIBLE;
      setCvCameraViewListener(this@MainActivity);
      setCameraIndex(0);
    }
  }

  override fun onPause() {
    super.onPause()
    binding.surfaceView.disableView()
  }


  override fun onResume() {
    super.onResume()
    if (!OpenCVLoader.initDebug()) {
      OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback)
    } else {
      mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    binding.surfaceView.disableView()
  }


  override fun onCameraViewStarted(width: Int, height: Int) {
  }

  override fun onCameraViewStopped() {
  }

  override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {

    matInput = inputFrame!!.rgba()
    matResult = Mat(matInput.rows(), matInput.cols(), matInput.type())
    ConvertRGBtoGray(matInput.nativeObjAddr, matResult.nativeObjAddr)
    return matResult
  }


  protected fun getCameraViewList(): List<CameraBridgeViewBase?>? {
    return Collections.singletonList(binding.surfaceView)
  }

  private val CAMERA_PERMISSION_REQUEST_CODE = 200


  protected fun onCameraPermissionGranted() {
    Log.d("test","${getCameraViewList()}")
    val cameraViews = getCameraViewList() ?: return
    for (cameraBridgeViewBase in cameraViews) {
      Log.d("test","$cameraBridgeViewBase")
      cameraBridgeViewBase?.setCameraPermissionGranted()
    }
  }
  override fun onStart() {
    super.onStart()
    var havePermission = true
    if (checkSelfPermission(Manifest.permission.CAMERA) !== PackageManager.PERMISSION_GRANTED) {
      requestPermissions(arrayOf<String>(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
      havePermission = false
    }
    if (havePermission) {
      Log.d("test","have permission")
      onCameraPermissionGranted()
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {

    if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      onCameraPermissionGranted()
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
  }


}