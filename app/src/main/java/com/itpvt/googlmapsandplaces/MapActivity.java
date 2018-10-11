package com.itpvt.googlmapsandplaces;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.itpvt.googlmapsandplaces.Models.PlaceInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS= new LatLngBounds(
            new LatLng(-40,-168),new LatLng(71,136)
    );

    private GoogleMap mMap;
    private FusedLocationProviderClient mfusedLocationProviderClient;
    private Boolean mLoactionPermissionGranted = false;

    private AutoCompleteTextView mSearch;
    private ImageView mGps;

    private PlaceAutoCompleteAdapter mplaceAutoCompleteAdapter;
    private PlaceAutocompleteAdapter1 mplaceAutoCompleteAdapter1;
    private GoogleApiClient mGoogleApiClient;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private PlaceInfo mPlace;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(MapActivity.this, "Map is Ready", Toast.LENGTH_LONG).show();
        Log.d(TAG, "Map is Ready");
        mMap = googleMap;
        if (mLoactionPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            init();

        }


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mSearch=(AutoCompleteTextView)findViewById(R.id.input_search);
        mGps=(ImageView)findViewById(R.id.gps);
        getLocationPermission();
        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
            }
        });
    }
    private void init()
    {
        Log.d(TAG,"init:initializing");
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
//        mplaceAutoCompleteAdapter=new PlaceAutoCompleteAdapter(this,mGoogleApiClient,LAT_LNG_BOUNDS,null);
//        mSearch.setAdapter(mplaceAutoCompleteAdapter);
        mplaceAutoCompleteAdapter1=new PlaceAutocompleteAdapter1(this,mGeoDataClient,LAT_LNG_BOUNDS,null);
        mSearch.setAdapter(mplaceAutoCompleteAdapter1);
        mSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId== EditorInfo.IME_ACTION_SEARCH
                        || actionId==EditorInfo.IME_ACTION_DONE
                        ||keyEvent.getAction()==KeyEvent.ACTION_DOWN
                        || keyEvent.getAction()==keyEvent.KEYCODE_ENTER)
                {
                    geoLocate();


                }


                return false;
            }

        });
        hidesoftinputkeyboard();
    }

    private void geoLocate()
    {
        Log.d(TAG,"Geoloacte:geolocate");
        String searchString=mSearch.getText().toString();
        Geocoder geocoder=new Geocoder(MapActivity.this);
        List<Address> list=new ArrayList<>();
        try
        {
            list=geocoder.getFromLocationName(searchString,1);

        }catch (IOException e)
        {
            Log.d(TAG,"Geoloacte:geolocate"+e.getMessage());
        }
        if (list.size()>0)
        {
            Address address=list.get(0);
            Log.d(TAG,"Geoloacte:location found:"+address.toString());
//            Toast.makeText(MapActivity.this,address.toString(),Toast.LENGTH_LONG).show();
            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM
                    ,address.getAddressLine(0));
        }
    }

    private void getDeviceLocation()
    {
        Log.d(TAG,"getDeviceLocation:getting the Device current Location");
        mfusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        try
        {
            if (mLoactionPermissionGranted)
            {
                Task loaction=mfusedLocationProviderClient.getLastLocation();
                loaction.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful())
                        {
                            Log.d(TAG,"onComplete:found Location!");
                            Location currentLocation=(Location)task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude())
                                    ,DEFAULT_ZOOM,"My Location");


                        }else
                            {
                                Log.d(TAG,"onComplete:current location in null!");
                                Toast.makeText(MapActivity.this,"Unable to get current location",Toast.LENGTH_LONG).show();
                            }
                    }
                });
            }

        }catch (SecurityException e)
        {
            Log.d(TAG,"getDeviceLocation:SecurityException:"+e.getMessage());
        }
    }
private void moveCamera(LatLng latLng,float zoom,String title)
{
    Log.d(TAG,"moveCamera:moving the camera to:lat"+latLng.latitude+",lng:"+latLng.longitude);
    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
    if (!title.equals("My Location")){
    MarkerOptions options= new MarkerOptions()
            .position(latLng)
            .title(title);
    mMap.addMarker(options);
    }
    hidesoftinputkeyboard();
}
    private void initmap()
    {
        Log.d(TAG,"initialize map");
        SupportMapFragment mapFragment=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission()
    {
        Log.d(TAG,"getting location permissions");
        String[] permission={Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION)==PackageManager.PERMISSION_GRANTED)
            {
                mLoactionPermissionGranted=true;
                initmap();

            }else
                {
                    ActivityCompat.requestPermissions(this,permission,
                            LOCATION_PERMISION_REQUEST_CODE);
                }
        }else
        {
            ActivityCompat.requestPermissions(this,permission,
                    LOCATION_PERMISION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG,"permission called");
        mLoactionPermissionGranted=false;
        switch (requestCode)
        {
            case LOCATION_PERMISION_REQUEST_CODE:
                {
                    if (grantResults.length>0)

                    {
                        for (int i=0;i<grantResults.length;i++)
                        {
                            if (grantResults[i]!=PackageManager.PERMISSION_GRANTED)
                            {
                                Log.d(TAG,"permission failed");
                                mLoactionPermissionGranted=false;
                            }
                        }
                        Log.d(TAG,"permission granted");
                        mLoactionPermissionGranted=true;
                        initmap();
                    }
                }
        }
    }
    private void hidesoftinputkeyboard()
    {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
/////////////////Google Places Api autocomplete Suggestions/////////////////////////
    private AdapterView.OnItemClickListener mAutoComplteClickListner=new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        hidesoftinputkeyboard();
        final AutocompletePrediction item=mplaceAutoCompleteAdapter1.getItem(i);
        final String placeId=item.getPlaceId();
        PendingResult<PlaceBuffer> placeResult=Places.GeoDataApi
                .getPlaceById(mGoogleApiClient,placeId);
        placeResult.setResultCallback(mUpdatePlaceDetailCallback);
    }
};
private ResultCallback<PlaceBuffer> mUpdatePlaceDetailCallback=new ResultCallback<PlaceBuffer>() {
    @Override
    public void onResult(@NonNull PlaceBuffer places) {
        if (!places.getStatus().isSuccess())
        {
            Toast.makeText(MapActivity.this, "Failed Place Query"+places.getStatus().toString(), Toast.LENGTH_LONG).show();
            places.release();
            return;
        }
        final Place place=places.get(0);

    }
};
}
