package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val TAG = "SaveReminderFragment"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    private val runningQOrLater=android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.Q
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private var requestIsApproved=false

    private val pendingIntent :PendingIntent by lazy {
        val intent=Intent(requireContext(),GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )}

    private lateinit var geofencingClient: GeofencingClient


        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

            setDisplayHomeAsUpEnabled(true)

            binding.viewModel = _viewModel

            geofencingClient = LocationServices.getGeofencingClient(requireActivity())

            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            binding.lifecycleOwner = this
            binding.selectLocation.setOnClickListener {
                //            Navigate to another fragment to get the user location
                _viewModel.navigationCommand.value =
                    NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
            }
            requestForegroundAndBackgroundLocationPermissions()




            binding.saveReminder.setOnClickListener {
                val title = _viewModel.reminderTitle.value
                val description = _viewModel.reminderDescription.value
                val location = _viewModel.reminderSelectedLocationStr.value
                val latitude = _viewModel.latitude.value
                val longitude = _viewModel.longitude.value

                val reminderDataItem = ReminderDataItem(title,description,location,latitude,longitude)
                val checker = _viewModel.validateAndSaveReminder(reminderDataItem)
                if(requestIsApproved && checker){
                    Log.i(TAG,"iam before starting adding the reminder")
                    addGeofence(reminderDataItem)
                    _viewModel.saveReminder(reminderDataItem)
                }

            }
        }

    @SuppressLint("MissingPermission")
    private fun addGeofence(reminderDataItem: ReminderDataItem) {

        val geofence = Geofence.Builder()
            .setRequestId(reminderDataItem.id)
            .setCircularRegion(
                reminderDataItem.latitude!!,
                reminderDataItem.longitude!!,
                _viewModel.reminderRadius
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)?.run {
            addOnSuccessListener {
                _viewModel.showToast.value = "Done added the geofence"
            }
            addOnFailureListener {
                _viewModel.showToast.value = "Error happening while adding the geofance"
            }
        }


    }


    @TargetApi(30)
        private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
            val forgroundLocationApproved =
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    context!!, android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            val backgroundLocationApproved =
                if (runningQOrLater) {
                    PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                        context!!, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                } else {
                    true
                }
            //  permissions were approved
            if(forgroundLocationApproved && backgroundLocationApproved)
                requestIsApproved=true
            return forgroundLocationApproved && backgroundLocationApproved
        }

        @TargetApi(30)
        private fun requestForegroundAndBackgroundLocationPermissions() {
            if (foregroundAndBackgroundLocationPermissionApproved()) {
                requestIsApproved=true
                Log.i(TAG,"my permission is approved")
                return
            }

            var permissionArray = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
            val resultCode = when {
                runningQOrLater -> {
                    permissionArray += android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
                }
                else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            }
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionArray,
                resultCode
            )
        }


        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            Log.d(TAG, "onRequestPermisionResult")
            if (
                grantResults.isEmpty() ||
                grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
                (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                        grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                        PackageManager.PERMISSION_DENIED)
            ) {
                Snackbar.make(
                    binding.root,
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()
            } else {
                requestIsApproved=true
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            //make sure to clear the view model after destroy, as it's a single view model.
            _viewModel.onClear()
        }
    companion object {
        internal const val ACTION_GEOFENCE_EVENT = "SaveReminder.reminder.action.ACTION_GEOFENCE_EVENT"
    }
    }

