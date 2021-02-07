package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback, LocationListener {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()

    private lateinit var binding: FragmentSelectLocationBinding

    private var googleMap: GoogleMap? = null

    private val zoomLevel = 15f

    private var selectedPOI: PointOfInterest? = null
    private var latitude: Double? = null
    private var longitude: Double? = null

    private val locationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    companion object {
        const val REQUEST_LOCATION_PERMISSION = 102
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        (childFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment)?.apply {
            getMapAsync(this@SelectLocationFragment)
        }

        binding.saveButton.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    override fun onMapReady(gMap: GoogleMap?) {
        googleMap = gMap
        shouldEnableLocation()
        onMapClick()
        setPoiClick()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationManager.removeUpdates(this)
    }

    private fun getCurrentLocation() {
        val isNetworkProviderEnabled =
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val isGPSProviderEnabled =
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        when {
            isNetworkProviderEnabled -> {
                moveToCurrentLocation(LocationManager.NETWORK_PROVIDER)
            }
            isGPSProviderEnabled -> {
                moveToCurrentLocation(LocationManager.GPS_PROVIDER)
            }
            else -> {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.location_required_error)
                    .setPositiveButton(R.string.okay) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun moveToCurrentLocation(provider: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (checkLocationPermission()) {
                locationManager.getCurrentLocation(provider, null, requireContext().mainExecutor, {
                    addMarker(LatLng(it.latitude, it.longitude))
                })
            }
        } else {
            locationManager.requestSingleUpdate(provider, this, Looper.getMainLooper())
        }
    }

    private fun addMarker(latLng: LatLng) {
        latitude = latLng.latitude
        longitude = latLng.longitude

        val snippet = getString(R.string.lat_long_snippet, latLng.latitude, latLng.longitude)

        googleMap?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel)
        )

        googleMap?.addMarker(
            MarkerOptions().position(latLng)
                .title(getString(R.string.dropped_pin))
                .snippet(snippet)
        )
    }

    private fun setPoiClick() {
        googleMap?.setOnPoiClickListener { poi ->
            selectedPOI = poi
            latitude = poi.latLng.latitude
            longitude = poi.latLng.longitude

            googleMap?.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )?.apply {
                showInfoWindow()
            }
        }
    }

    private fun onMapClick() {
        googleMap?.setOnMapClickListener { latLng ->
            selectedPOI = null
            addMarker(latLng)
        }
    }

    private fun shouldEnableLocation() {
        if (checkLocationPermission()) {
            googleMap?.isMyLocationEnabled = true
            getCurrentLocation()
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getCurrentLocation()
                shouldEnableLocation()
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return context?.let {
            return@let ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } ?: false
    }

    private fun onLocationSelected() {
        _viewModel.latitude.value = latitude
        _viewModel.longitude.value = longitude

        selectedPOI?.let {
            _viewModel.selectedPOI.value = it
            _viewModel.reminderSelectedLocationStr.value = it.name
        } ?: run {
            if (latitude != null && longitude != null) {
                _viewModel.reminderSelectedLocationStr.value =
                    getString(R.string.reminder_lat_long_text, latitude, longitude)
            }
        }

        _viewModel.navigationCommand.postValue(
            NavigationCommand.Back
        )
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            googleMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            googleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            googleMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onLocationChanged(location: Location) {
        addMarker(LatLng(location.latitude, location.longitude))
    }
}
