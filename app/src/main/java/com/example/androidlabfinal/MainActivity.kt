package com.example.androidlabfinal

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.squareup.picasso.Picasso
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var fusedlocation: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedlocation = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermission()) {
            if (locationEnable()) {
                fusedlocation.lastLocation.addOnCompleteListener { task ->
                    var location: Location? = task.result
                    if (location != null) {
                        val lat = location.latitude.toString()
                        var long = location.longitude.toString()

                        if (lat != null && long != null) {
                            getJsonData(lat, long)
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please Turn on your GPS location", Toast.LENGTH_LONG).show()
            }
        } else {
            requestPermission()
        }
    }

    private fun locationEnable(): Boolean {
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestPermission() {
        try {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), 1001
            )
        } catch (e: Exception) {

        }
    }

    private fun checkPermission(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                requestPermission()
            }
        }
    }

    private fun getJsonData(lat: String, long: String) {
        val API_KEY = "7dca1674a10aeeddb54e09b7e638a0ff" // use your own api_key
        val queue = Volley.newRequestQueue(this)
        val cityName = getCityName(lat.toDouble(), long.toDouble())

        val url =
            "https://api.openweathermap.org/data/2.5/weather?q=${cityName}&appid=${API_KEY}"

        try {
            val jsonRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                Response.Listener { response ->
                    setValues(response)
                },
                Response.ErrorListener {
                    Toast.makeText(
                        this,
                        "Please turn on internet connection",
                        Toast.LENGTH_LONG
                    ).show()
                })


            queue.add(jsonRequest)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "ERROR" + e.message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setValues(response: JSONObject) {
        var weatherIcon = response.getJSONArray("weather").getJSONObject(0).getString("icon")
        runOnUiThread{
            Picasso.get().load("https://openweathermap.org/img/wn/${weatherIcon}@2x.png").into(weather_img)
            weather_img.visibility = View.VISIBLE
        }

        city.text = "City Name: " + response.getString("name")
        country.text = "Country: " + response.getJSONObject("sys").getString("country")
        var tempr = response.getJSONObject("main").getString("temp")
        temp.text = "Temparature: ${(tempr.toFloat() - 273).toInt()}°C"
        humidity.text = "Humidity: " + response.getJSONObject("main").getString("humidity") + "%"
    }

    private fun getCityName(lat: Double, long: Double): String {
        var geoCoder = Geocoder(this, Locale.getDefault())
        var adress = geoCoder.getFromLocation(lat, long, 3)

        return adress.get(0).locality
    }

}