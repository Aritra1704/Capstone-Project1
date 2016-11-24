package com.arpaul.geocare;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arpaul.customalertlibrary.popups.statingDialog.CustomPopupType;
import com.arpaul.geocare.common.AppConstant;
import com.arpaul.geocare.common.AppPreference;
import com.arpaul.geocare.common.ApplicationInstance;
import com.arpaul.geocare.dataAccess.GCCPConstants;
import com.arpaul.geocare.dataAccess.InsertDataType;
import com.arpaul.geocare.dataAccess.InsertLoader;
import com.arpaul.geocare.dataObject.PrefLocationDO;
import com.arpaul.geocare.geoFence.GeoFenceNotiService;
import com.arpaul.geocare.geoFence.GeofenceErrorMessages;
import com.arpaul.gpslibrary.fetchAddressGeoCode.AddressConstants;
import com.arpaul.gpslibrary.fetchAddressGeoCode.AddressDO;
import com.arpaul.gpslibrary.fetchAddressGeoCode.FetchAddressLoader;
import com.arpaul.gpslibrary.fetchAddressGeoCode.FetchGeoCodeLoader;
import com.arpaul.gpslibrary.fetchLocation.GPSErrorCode;
import com.arpaul.utilitieslib.LogUtils;
import com.arpaul.utilitieslib.PermissionUtils;
import com.arpaul.utilitieslib.StringUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ARPaul on 29-10-2016.
 */

public class LocationSearchActivity extends BaseActivity implements
        OnMapReadyCallback,
        LoaderManager.LoaderCallbacks,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private View llLocSearchActivity;
    private final String LOG_TAG ="FenceLocator";

    private ImageView ivLocation, ivCross;
    private GoogleMap mMap;
    private EditText edtAddress;
    private Button btnSave;
    private SupportMapFragment mapFragment;
    private LatLng currentLatLng = null;
    private float mZoom = 0.0f;
    private boolean isGpsEnabled;
    private boolean ispermissionGranted = false;
    private MaterialDialog mdFilter;
    private static int HANDLER_TIME_OUT = 2500;
    private int locationId = 1;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    //https://classroom.udacity.com/courses/ud0352/lessons/daa58d76-0146-4c52-b5d8-45e32a3dfb08/concepts/ef189a80-7f09-47cc-87c8-dc101ead7a1e

    @Override
    public void initialize() {
        llLocSearchActivity = baseInflater.inflate(R.layout.activity_location_search,null);
        llBody.addView(llLocSearchActivity, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        initialiseControls();

        bindControls();
    }

    private void bindControls(){
        if(new PermissionUtils().checkPermission(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}) != 0){
            new PermissionUtils().verifyLocation(this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        } else{
            buildGoogleApiClient();
        }

        ivCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtAddress.setText("");
            }
        });

        edtAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performLocationSearch();

                    hideKeyBoard();
                    return true;
                }
                return false;
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!TextUtils.isEmpty(edtAddress.getText().toString()))
                    saveLocation();
                else
                    showCustomDialog(getString(R.string.alert),getString(R.string.location_address_empty),getString(R.string.ok),null,getString(R.string.location_address_empty), CustomPopupType.DIALOG_ALERT,false);
            }
        });
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if(isGpsEnabled()) {
            isGpsEnabled = true;
        } else {
            isGpsEnabled = false;
            showCustomDialog(getString(R.string.gpssettings),getString(R.string.gps_not_enabled),getString(R.string.settings),getString(R.string.cancel),getString(R.string.settings), CustomPopupType.DIALOG_ALERT,false);
        }

        if(mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    private void saveLocation(){

        boolean wrapInScrollView = true;
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title(R.string.location)
                .cancelable(false)
                .customView(R.layout.dialog_savelocation, wrapInScrollView);

        if(mdFilter == null)
            mdFilter =  builder.build();
        final LatLng saveLatLng = currentLatLng;

        View view = mdFilter.getCustomView();

        final EditText edtLocationName        = (EditText) view.findViewById(R.id.edtLocationName);
        final EditText edtFenceRadius         = (EditText) view.findViewById(R.id.edtFenceRadius);
        TextView tvAddress                    = (TextView) view.findViewById(R.id.tvAddress);

//        edtFenceRadius.setText(String.valueOf(AppConstant.GEOFENCE_RADIUS_IN_METERS));

        final String address = edtAddress.getText().toString();
        tvAddress.setText(address);

        String radiusParam = edtFenceRadius.getText().toString();
        if(TextUtils.isEmpty(radiusParam))
            radiusParam = AppConstant.GEOFENCE_RADIUS_IN_METERS + "";
        final int radius = (int) StringUtils.getFloat(radiusParam);

        edtLocationName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyBoard();
                    handled = true;
                }
                return handled;
            }
        });

        builder.positiveText(R.string.accept)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(final @NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        if(!TextUtils.isEmpty(edtLocationName.getText().toString())){
                            String locationName = edtLocationName.getText().toString();

                            Uri CONTENT_URI = GCCPConstants.CONTENT_URI_SAVED_LOC;
                            locationName = StringUtils.wordFirstCap(locationName).trim();
                            Cursor cursor = getContentResolver().query(CONTENT_URI,
                                    new String[] {PrefLocationDO.LOCATIONID},
                                    PrefLocationDO.LOCATIONNAME + GCCPConstants.TABLE_QUES,
                                    new String[]{locationName},
                                    null);

                            if(cursor != null && cursor.moveToFirst()){
                                cursor.close();
                                showCustomDialog(getString(R.string.alert),getString(R.string.same_location_exists),getString(R.string.ok),null,getString(R.string.same_location_exists), CustomPopupType.DIALOG_ALERT,true);
                            } else {
                                cursor.close();

                                cursor = getContentResolver().query(CONTENT_URI,
                                        new String[]{"MAX(" + PrefLocationDO.LOCATIONID + ") AS " + PrefLocationDO.MAXLOCATIONID},
                                        null,
                                        null,
                                        null);

                                if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0)
                                    locationId = cursor.getInt(cursor.getColumnIndex(PrefLocationDO.MAXLOCATIONID)) + 1;

                                PrefLocationDO objPrefLocationDO = new PrefLocationDO();
                                objPrefLocationDO.LocationId = locationId;
                                objPrefLocationDO.LocationName = locationName;
                                objPrefLocationDO.Address = address;
                                objPrefLocationDO.Latitude = saveLatLng.latitude;
                                objPrefLocationDO.Longitude = saveLatLng.longitude;
                                objPrefLocationDO.Radius = radius;

                                Bundle bundle = new Bundle();
                                bundle.putSerializable(InsertLoader.BUNDLE_INSERTLOADER,objPrefLocationDO);

                                getSupportLoaderManager().initLoader(ApplicationInstance.LOADER_SAVE_LOCATION, bundle, LocationSearchActivity.this).forceLoad();
                            }
                            cursor.close();

                        }
                    }
                });

        builder.negativeText(R.string.discard)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        mdFilter = null;
                    }
                });
        try{
            if (mdFilter == null || !mdFilter.isShowing()){
                mdFilter = builder.build();
                mdFilter.show();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private void performLocationSearch(){
        String address = edtAddress.getText().toString();
        if(TextUtils.isEmpty(address))
            showCustomDialog(getString(R.string.alert),getString(R.string.please_enter_proper_address),getString(R.string.ok),null,getString(R.string.please_enter_proper_address), CustomPopupType.DIALOG_ALERT,false);
        else {
            if(getSupportLoaderManager().getLoader(ApplicationInstance.LOADER_FETCH_LOCATION) != null )
                getSupportLoaderManager().restartLoader(ApplicationInstance.LOADER_FETCH_LOCATION, null, this).forceLoad();
            else
                getSupportLoaderManager().initLoader(ApplicationInstance.LOADER_FETCH_LOCATION, null, this).forceLoad();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap = googleMap;

        if(isGpsEnabled) {
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    showCurrentLocation();
                    mMap.setOnCameraMoveStartedListener(LocationSearchActivity.this);
                    mMap.setOnCameraIdleListener(LocationSearchActivity.this);
                }
            }, 1000);
        }
        else if(ispermissionGranted) {
            showSettingsAlert();
        }

//        if(gpsUtills != null && ispermissionGranted && !isGpsEnabled){
//            gpsUtills.isGpsProviderEnabled();
//        }
//        if(isGpsEnabled) {
//            new Handler().postDelayed(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    gpsUtills.getCurrentLatLng();
//                    showCurrentLocation();
////                    mMap.setOnCameraChangeListener(LocationSearchActivity.this);
////                    mMap.setOnCameraMoveListener(LocationSearchActivity.this);
//                    mMap.setOnCameraMoveStartedListener(LocationSearchActivity.this);
//                    mMap.setOnCameraIdleListener(LocationSearchActivity.this);
//                }
//            }, 1000);
//        }
//        else if(ispermissionGranted) {
//            showSettingsAlert();
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {

            int location = 0;
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                        grantResult == PackageManager.PERMISSION_GRANTED) {
                    location++;
                } else if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                        grantResult == PackageManager.PERMISSION_GRANTED) {
                    location++;
                }
            }

            if(location == 2) {
                ispermissionGranted = true;
                buildGoogleApiClient();

//                getCurrentLocation();
            } else {
                showCustomDialog(getString(R.string.gpssettings),getString(R.string.allow_app_access_location),getString(R.string.ok),null,getString(R.string.allow_app_access_location), CustomPopupType.DIALOG_ALERT,false);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null && (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected())) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LogUtils.infoLog(LOG_TAG, "Connected to GoogleApiClient");

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(location != null){
            currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            if(getSupportLoaderManager().getLoader(ApplicationInstance.LOADER_FETCH_ALL_LOCATION) != null)
                getSupportLoaderManager().restartLoader(ApplicationInstance.LOADER_FETCH_ALL_LOCATION, null, this);
            else
                getSupportLoaderManager().initLoader(ApplicationInstance.LOADER_FETCH_ALL_LOCATION, null, this);

        } //else {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(AppConstant.LOCATION_UPDATES_IN_SECONDS * 1000); // Update location every second

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ){
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        } else
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        //}
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LogUtils.infoLog(LOG_TAG, "GoogleApiClient connection has failed");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionSuspended(int i) {
        LogUtils.infoLog(LOG_TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onLocationChanged(Location location) {
        LogUtils.infoLog(LOG_TAG, location.toString());

        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        Toast.makeText(LocationSearchActivity.this, "Lat: "+currentLatLng.latitude+" Lon: "+currentLatLng.longitude, Toast.LENGTH_SHORT).show();
        showCurrentLocation();
        startIntentService();
    }

    private boolean isGpsEnabled(){
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isGpsProviderEnabled;
    }

//    @Override
//    public void gotGpsValidationResponse(Object response, GPSErrorCode code)
//    {
//        if(code == GPSErrorCode.EC_GPS_PROVIDER_NOT_ENABLED) {
//            isGpsEnabled = false;
//            showCustomDialog(getString(R.string.gpssettings),getString(R.string.gps_not_enabled),getString(R.string.settings),getString(R.string.cancel),getString(R.string.settings), CustomPopupType.DIALOG_ALERT,false);
//        }
//        else if(code == GPSErrorCode.EC_GPS_PROVIDER_ENABLED) {
//            isGpsEnabled = true;
//            gpsUtills.getCurrentLatLng();
//        }
//        else if(code == GPSErrorCode.EC_UNABLE_TO_FIND_LOCATION) {
//            currentLatLng = (LatLng) response;
//        }
//        else if(code == GPSErrorCode.EC_LOCATION_FOUND) {
//            currentLatLng = (LatLng) response;
//            LogUtils.debugLog("GPSTrack", "Currrent latLng :"+currentLatLng.latitude+" \n"+currentLatLng.longitude);
//
//            //loader.hideLoader();
//            showCurrentLocation();
//            gpsUtills.stopLocationUpdates();
//        }
//        else if(code == GPSErrorCode.EC_CUSTOMER_LOCATION_IS_VALID) {
//        }
//        else if(code == GPSErrorCode.EC_CUSTOMER_lOCATION_IS_INVAILD) {
//        }
//        else if(code == GPSErrorCode.EC_DEVICE_CONFIGURED_PROPERLY) {
//            startIntentService();
//        }
//    }

    protected void startIntentService() {
        if(getSupportLoaderManager().getLoader(ApplicationInstance.LOADER_FETCH_ADDRESS) == null)
            getSupportLoaderManager().initLoader(ApplicationInstance.LOADER_FETCH_ADDRESS, null, this).forceLoad();
        else
            getSupportLoaderManager().restartLoader(ApplicationInstance.LOADER_FETCH_ADDRESS, null, this).forceLoad();
    }

//    private void getCurrentLocation(){
//        new Handler().postDelayed(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                gpsUtills.getCurrentLatLng();
//                showCurrentLocation();
//            }
//        }, 2 * 1000);
//    }

    private void showCurrentLocation(){
        if(currentLatLng != null)
        {
            if(mMap!=null)
            {
                mMap.clear();
                /*final MarkerOptions markerOptions = new MarkerOptions().position(currentLatLng);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                markerOptions.title("Your Location");
                mMap.addMarker(markerOptions);*/
                mZoom = 18.0f;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,mZoom));
            }
        }
        else {
            Toast.makeText(this, "Unable to fetch your current location please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    public void setFailureAddress(String message){
        showCustomDialog(getString(R.string.failure),message,getString(R.string.ok),null,getString(R.string.failure), CustomPopupType.DIALOG_FAILURE,false);
        edtAddress.setText("");
    }

    private void setAddress(String message){
        if(message.contains("\n"))
            message = message.replace("\n", " ");
        edtAddress.setText(message);
    }

//    @Override
//    public void onCameraChange(CameraPosition cameraPosition) {
//
//        currentLatLng = cameraPosition.target;
//        gpsUtills.isDeviceConfiguredProperly();
//    }

//    @Override
//    public void onCameraMove() {
//    }

    @Override
    public void onCameraMoveStarted(int i) {
    }

    @Override
    public void onCameraIdle() {
        // Cleaning all the markers.
        if (mMap != null) {
            mMap.clear();
        }

        currentLatLng = mMap.getCameraPosition().target;
        mZoom = mMap.getCameraPosition().zoom;
        startIntentService();
//        gpsUtills.isDeviceConfiguredProperly();

    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch(id){
            case ApplicationInstance.LOADER_FETCH_ADDRESS:
                return new FetchAddressLoader(this, currentLatLng);

            case ApplicationInstance.LOADER_FETCH_LOCATION:
                return new FetchGeoCodeLoader(this, edtAddress.getText().toString());

            case ApplicationInstance.LOADER_SAVE_LOCATION:
                return new InsertLoader(this, InsertDataType.INSERT_PREF_LOC, args);

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader loader, final Object data) {
        switch(loader.getId()){
            case ApplicationInstance.LOADER_FETCH_ADDRESS:
                if(data instanceof AddressDO){
                    AddressDO objAddressDO = (AddressDO) data;
                    if(objAddressDO.code == AddressConstants.SUCCESS_RESULT)
                        setAddress(objAddressDO.message);
                    else if(objAddressDO.code == AddressConstants.FAILURE_RESULT)
                        setFailureAddress(objAddressDO.message);
                }
                break;

            case ApplicationInstance.LOADER_FETCH_LOCATION:
                if(data instanceof AddressDO) {
                    AddressDO objAddressDO = (AddressDO) data;
                    if(objAddressDO.code == AddressConstants.SUCCESS_RESULT){
                        LatLng latlng = new LatLng(objAddressDO.location.getLatitude(),objAddressDO.location.getLongitude());

                        currentLatLng = latlng;
                        showCurrentLocation();
                    } else if(objAddressDO.code == AddressConstants.FAILURE_RESULT)
                        setFailureAddress(objAddressDO.message);
                }
                break;

            case ApplicationInstance.LOADER_SAVE_LOCATION:
                if(data instanceof String){
                    if(data != null && !TextUtils.isEmpty((String) data)){
                        showCustomDialog(getString(R.string.success),getString(R.string.location_successfuly_added),null,null,getString(R.string.location_successfuly_added), CustomPopupType.DIALOG_SUCCESS,false);
                        startService(new Intent(LocationSearchActivity.this, GeoFenceNotiService.class));
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                hideCustomDialog();
//                            dialog.dismiss();//Need to think

                                preference.saveStringInPreference(AppPreference.PREF_LOC, ""+locationId);
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("address",(String) data);
                                setResult(1001);

                                finish();
                            }
                        }, HANDLER_TIME_OUT);
                    }
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    private void initialiseControls(){
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ivLocation          = (ImageView) llLocSearchActivity.findViewById(R.id.ivLocation);
        ivCross             = (ImageView) llLocSearchActivity.findViewById(R.id.ivCross);
        edtAddress          = (EditText) llLocSearchActivity.findViewById(R.id.edtAddress);
        btnSave             = (Button) llLocSearchActivity.findViewById(R.id.btnSave);

        toolbarBase.setVisibility(View.GONE);
    }
}
