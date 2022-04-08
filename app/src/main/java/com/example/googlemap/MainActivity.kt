package com.example.googlemap

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.googlemap.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task

class MainActivity : AppCompatActivity() {
    private lateinit var supportMapFragment: SupportMapFragment
    private lateinit var client: FusedLocationProviderClient
    private lateinit var task: Task<Location>
    private var context: Context = this@MainActivity
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportMapFragment = supportFragmentManager.findFragmentById(R.id.fragment) as SupportMapFragment
        client = LocationServices.getFusedLocationProviderClient(this)

        /**
         *
         * Floating button listner
         */
        binding.fab.setOnClickListener {
            checkLocationAndNetwork()
        }
    }

    /**
     * check system Location is on and off
     */
    private fun checkLocationAndNetwork() {
        val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        var gpsEnabled = false
        var networkEnabled = false
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }
        //                check isNetworkConnected
        networkEnabled = isNetworkConnected()
        if (!gpsEnabled) {
            alertDialog("Location error", "Location is no enabled", 2)
        } else if (!networkEnabled) {
            alertDialog("Network error", "Network is no enabled", 1)
        } else {
            checkLocationPermission()
        }
    }

    /**
     * permission AlertDialog
     */
    fun alertDialog(title: String?, msg: String?, i: Int) {
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(
                "open settings"
            ) { paramDialogInterface, paramInt ->
                if (i == 1) context.startActivity(Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS))
                else if (i == 2) context.startActivity(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                )
            }
            .setNegativeButton(
                "Cancel", null
            )
            .setCancelable(false)
            .show()
    }

    /**
     * check system Network is Connected or not
     */
    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnected
    }

    /**
     * check location permission method
     */
    fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            task = client.lastLocation
            getLiveLocation()
        } else {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                1
            )
        }
    }

    /**
     * when user give the location permission this method call
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission()
        } else {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * get live location method
     */
    fun getLiveLocation() {
        task.addOnSuccessListener { location ->
            supportMapFragment.getMapAsync { googleMap ->
                val latLng = LatLng(location.latitude, location.longitude)
                val markerOptions = MarkerOptions().position(latLng)
                    .title(getString(R.string.map_marker_title))
                googleMap.addMarker(markerOptions)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
        }
    }
}