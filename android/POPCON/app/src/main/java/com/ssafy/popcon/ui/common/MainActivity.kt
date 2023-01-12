package com.ssafy.popcon.ui.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.ssafy.popcon.R
import com.ssafy.popcon.databinding.ActivityMainBinding
import com.ssafy.popcon.util.CheckPermission
import com.ssafy.popcon.util.ShakeDetector
import com.ssafy.popcon.util.Utils.navigationHeight
import com.ssafy.popcon.util.Utils.setStatusBarTransparent

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var shakeDetector: ShakeDetector
    private lateinit var checkPermission: CheckPermission

    val PERMISSION_REQUEST_CODE = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setNavBar()
        checkPermissions()
    }

    private fun setNavBar() {
        this.setStatusBarTransparent()
        binding.innerContainer.setPadding(
            0,
            0,
            0,
            this.navigationHeight()
        )

        val navHosFragment =
            supportFragmentManager.findFragmentById(R.id.frame_layout_main) as NavHostFragment
        val navController = navHosFragment.navController

        NavigationUI.setupWithNavController(binding.tabLayoutBottomNavigation, navController)
        binding.tabLayoutBottomNavigation.setOnNavigationItemReselectedListener { item ->
            // 재선택시 다시 랜더링 하지 않기 위해 수정
            if (binding.tabLayoutBottomNavigation.selectedItemId != item.itemId) {
                binding.tabLayoutBottomNavigation.selectedItemId = item.itemId
            }
        }
    }

    private val runtimePermissions = arrayOf(
        Manifest.permission.CALL_PHONE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    // 위치, 갤러리, 전화 권한
    private fun checkPermissions() {
        checkPermission = CheckPermission(this)

        if (!checkPermission.runtimeCheckPermission(this, *runtimePermissions)) {
            ActivityCompat.requestPermissions(this, runtimePermissions, PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED
                    && grantResults[3] == PackageManager.PERMISSION_GRANTED
                    && grantResults[4] == PackageManager.PERMISSION_GRANTED
                ) {
                    //권한 승인
                } else {
                    checkPermission.requestPermission()
                }
            }
        }
    }

    fun hideBottomNav(state: Boolean) {
        if (state) binding.tabLayoutBottomNavigation.visibility = View.GONE
        else binding.tabLayoutBottomNavigation.visibility = View.VISIBLE
    }

    override fun onPause() {
        removeShakeSensor(this, ShakeDetector())
        super.onPause()
    }
    fun setShakeSensor(context: Context, shakeDetector: ShakeDetector) {//센서
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun removeShakeSensor(context: Context, shakeDetector: ShakeDetector) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(shakeDetector)
    }
}