package com.example.mcnewz.icareservice.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mcnewz.icareservice.R;
import com.example.mcnewz.icareservice.dao.DepartmentsItemCollectionDao;
import com.example.mcnewz.icareservice.dao.DepartmentsItemDao;
import com.example.mcnewz.icareservice.dao.NearRegencyListItemDao;
import com.example.mcnewz.icareservice.dao.RegencyInfoItemDao;
import com.example.mcnewz.icareservice.dao.TypeAcidentsAlertListItemCollectionDao;
import com.example.mcnewz.icareservice.dao.TypeAcidentsAlertListItemDao;
import com.example.mcnewz.icareservice.manager.HttpManager;
import com.example.mcnewz.icareservice.util.LogoClickShow;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.inthecheesefactory.thecheeselibrary.manager.Contextor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.mcnewz.icareservice.R.id.editDetail;
import static com.example.mcnewz.icareservice.R.id.editSubject;


/**
 * Created by nuuneoi on 11/16/2014.
 */
public class AlertFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener {

    /************
     * Variables
     *************/
    //Mapready
    private MapView mMapView;
    private GoogleMap mMap;
    private Marker mCurrLocationMarker;

    // Location
    private final String LOG = "LaurenceTestApp";
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private LatLng latLng;

    //Marker
    private MarkerOptions options;
    private LatLng dragMarkerLatLng;
    // Circle
    private Circle circle;
    private Marker marker2;

    //View Object
    private View vInfo;
    private TextView tvLocality;
    private TextView tvLat;
    private TextView tvLng;
    private TextView tvSnippet;
    private ImageView ivLogoDepartment;
    private ProgressDialog pDialog;
    private FloatingActionButton fabLocation;

    private ImageButton btnShowMark1;
    private ImageButton btnShowMark2;
    private ImageButton btnShowMark3;
    private ImageButton btnShowMark4;

    // variable Acidents
    private int type1 = 1;
    private int type2 = 1;
    private int type3 = 1;
    private int type4 = 1;
    private int typeDe;
    private int dontMoveMarkerWhichOutClick = 1;
    private int showTypeLogo ;

    // Intent Send
    private double lt, lg;
    private String typeAc = "1";

    // typeNameSelected Acidents
    String nameAcident1;
    String nameAcident2;
    String nameAcident3;
    String nameAcident4;
    String typeNameSubject;

    // Index Location
    ArrayList<Integer> departmentType = new ArrayList<Integer>(); ;
    ArrayList<Integer> localClickDepartment = new ArrayList<Integer>();

    // Button Sheet
    DepartmentsBottomSheetDialog departmentsBottomSheetDialog;



    /************
     * Functions
     *************/

    public AlertFragment() {
        super();
    }

    public static AlertFragment newInstance() {
        AlertFragment fragment = new AlertFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        buildGoogleApiClient();
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_alert, container, false);
        vInfo = inflater.inflate(R.layout.info_window, container, false);
        initvMark();

        // InitMap Here
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(this);

        //setRegencyInfo();
        callBackItemDepartments();
        initInstances(rootView);
        return rootView;
    }

    private void initvMark() {
        ivLogoDepartment  = (ImageView)vInfo.findViewById(R.id.ivLogoDepartment);
        tvLocality = (TextView) vInfo.findViewById(R.id.tv_locality);
        tvLat = (TextView) vInfo.findViewById(R.id.tv_lat);
        tvLng = (TextView) vInfo.findViewById(R.id.tv_lng);
        tvSnippet = (TextView) vInfo.findViewById(R.id.tv_snippet);
    }

    private void initInstances(View rootView) {
        // init instance with rootView.findViewById here
//        pDialog = new ProgressDialog(getContext());
//        pDialog.setMessage("Please wait...");
//        pDialog.setCancelable(false);

        fabLocation = (FloatingActionButton)  rootView.findViewById(R.id.fabLocation);

        btnShowMark1 = (ImageButton) rootView.findViewById(R.id.btnShowMark1);
        btnShowMark2 = (ImageButton) rootView.findViewById(R.id.btnShowMark2);
        btnShowMark3 = (ImageButton) rootView.findViewById(R.id.btnShowMark3);
        btnShowMark4 = (ImageButton) rootView.findViewById(R.id.btnShowMark4);

        setListenerAllView();
        callTypeAcidentsAlert();

    }

    private void setListenerAllView() {
        fabLocation.setOnClickListener(fabLocationListener);
        btnShowMark1.setOnClickListener(showMarker1Listener);
        btnShowMark2.setOnClickListener(showMarker2Listener);
        btnShowMark3.setOnClickListener(showMarker3Listener);
        btnShowMark4.setOnClickListener(showMarker4Listener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);

        if(mMap != null){
            infoWindowMark(); // Info Custom Show
            myLocationHere(); // Set MyLocation
        }


    }
    private void infoWindowMark() {
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                MarkerClickShow(marker);
                return null;
            }

            @SuppressLint("SetTextI18n")
            @Override
            public View getInfoContents(Marker marker) {
                LatLng ll = marker.getPosition();

                ivLogoDepartment.setImageResource(showTypeLogo);
                tvLocality.setText(marker.getTitle());
                tvSnippet.setText(marker.getSnippet());

                if(ll != null){
                    tvLat.setText("Latitude: " +  ll.latitude);
                    tvLng.setText("longitude : " + ll.longitude);
                }
                return vInfo;
            }
        });

        // 3.9 setOnMarkerDragListener Update Location End
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                if(circle != null){
                    //4.4 remove Here
                    removeEverything();
                }
                Toast.makeText(getContext(), "Move to Acidents Point", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                //Toast.makeText(getContext(), "Please put down somewhere", Toast.LENGTH_SHORT).show();

            }

            // 3.10 getFromLocation end Drag
            @Override
            public void onMarkerDragEnd(Marker marker) {
                Geocoder gc = new Geocoder(getContext());
                LatLng ll = marker.getPosition();

                double lat = ll.latitude;
                double lng = ll.longitude;
                List<Address> list = null;

                try {
                    list = gc.getFromLocation(lat, lng, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //3.11 add End drag
                if(list != null){
                    Address add = list.get(0);
                    marker.setTitle(add.getLocality());
                    marker.showInfoWindow();
                }

                lt = lat;
                lg = lng;
                // Circle Here
                circle = drawCircle(new LatLng(lat, lng));
            }
        });
    }

    // department
    private void callBackItemDepartments() {
        Call<DepartmentsItemCollectionDao> call = HttpManager.getInstance().getService().loadItemListDepartment();
        call.enqueue(new Callback<DepartmentsItemCollectionDao>() {
            @Override
            public void onResponse(Call<DepartmentsItemCollectionDao> call, Response<DepartmentsItemCollectionDao> response) {
                DepartmentsItemDao dao ;
                DepartmentsItemCollectionDao collectionDao = response.body();
                int sizeDao = collectionDao.getData().size();

                for (int i = 0; i < sizeDao; i++){

                    dao = collectionDao.getData().get(i);

                    int id = dao.getId();
                    String latDao = dao.getLatitude();
                    String lngDao = dao.getLongtitude();
                    String detailDao = dao.getDescription();
                    String name = dao.getName();
                    int type = dao.getTypeId();
                    departmentType.add(type); //this adds an element to the list.

                    LatLng latLng = new LatLng(Double.parseDouble(latDao), Double.parseDouble(lngDao));
                    //Toast.makeText(Contextor.getInstance().getContext(), "123456789"+latLng + type, Toast.LENGTH_SHORT).show();

                    if(type == 1){
                        typeDe = R.drawable.ic_depart_police;

                    } else if (type == 2){
                        typeDe = R.drawable.ic_depart_hospital;


                    }else if (type == 3){
                        typeDe = R.drawable.ic_depart_fire;

                    }else {
                        typeDe = R.drawable.ic_depart_wor;
                    }


                    marker2 = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(name)
                            .icon(BitmapDescriptorFactory.fromResource(typeDe)));
                    marker2.setSnippet(String.valueOf(type));
                    marker2.setTag(id);

//                    mMarkerArray.add(marker2);
                    localClickDepartment.add(id);

                    // show marker
                }
            }

            @Override
            public void onFailure(Call<DepartmentsItemCollectionDao> call, Throwable t) {
                Toast.makeText(Contextor.getInstance().getContext(), t.toString()+"DepartmentsItemCollectionDao Erorr", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Retrieve the data from the marker.
        MarkerClickShow(marker);
        return false;
    }

    private void MarkerClickShow(Marker marker) {
        Integer clickCount = (Integer) marker.getTag();
        String snippet = marker.getSnippet();

        // Check if a click count was set, then display the click count.
        if (clickCount != null) {

            int indexArray = localClickDepartment.indexOf(clickCount);

            showDepartmentBottomSheetDialog(clickCount , indexArray); // Show ButtonSheet Department

            Toast.makeText(Contextor.getInstance().getContext(), "Departments", Toast.LENGTH_SHORT).show();

            if(snippet.equals("1")){
                showTypeLogo = R.drawable.ic_depart_police;

            }
            if(snippet.equals("2")){
                showTypeLogo = R.drawable.ic_depart_hospital;

            }
            if(snippet.equals("3")){
                showTypeLogo = R.drawable.ic_depart_fire;

            }
            if(snippet.equals("4")){
                showTypeLogo = R.drawable.ic_depart_wor;
            }

        } else {
            showTypeLogo = R.drawable.ic_launcher;
            Toast.makeText(Contextor.getInstance().getContext(), "Not found Tag", Toast.LENGTH_SHORT).show();
        }
    }

    // ButtonSheet Department
    public void showDepartmentBottomSheetDialog(int clickCount ,int indexArray) {
        departmentsBottomSheetDialog = DepartmentsBottomSheetDialog.newInstance(clickCount, indexArray);
        departmentsBottomSheetDialog.show(getFragmentManager(), "bottomsheetDepartments");
    }

    private void callTypeAcidentsAlert() {
        Call<TypeAcidentsAlertListItemCollectionDao> call = HttpManager.getInstance().getService().loadTypeAcidentsAlert();
        call.enqueue(new Callback<TypeAcidentsAlertListItemCollectionDao>() {
            @Override
            public void onResponse(Call<TypeAcidentsAlertListItemCollectionDao> call, Response<TypeAcidentsAlertListItemCollectionDao> response) {

                TypeAcidentsAlertListItemDao dao ;
                TypeAcidentsAlertListItemCollectionDao collectionDao = response.body();
                int sizeDao = collectionDao.getData().size();

                for (int i = 0; i < sizeDao; i++){

                    dao = collectionDao.getData().get(i);
                    int id = dao.getId();
                    String typeName = dao.getName();

                    if(id == 1){
                        nameAcident1 = typeName;
                    }
                    if (id == 2){
                        nameAcident2 = typeName;
                    }
                    if (id == 3){
                        nameAcident3 = typeName;
                    }
                    if (id == 4) {
                        nameAcident4 = typeName;
                    }
                }
            }

            @Override
            public void onFailure(Call<TypeAcidentsAlertListItemCollectionDao> call, Throwable t) {
                Toast.makeText(Contextor.getInstance().getContext(), t.toString()+"DepartmentsItemCollectionDao Erorr", Toast.LENGTH_LONG).show();
            }
        });

    }


    private void myLocationHere() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        updateLocation(); // Update Onlocation Change
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG, "Coinnect suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(LOG, "Coinnect failed"+ result.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(LOG, location.toString());
        mLastLocation = location;

        if (dontMoveMarkerWhichOutClick != 0){

            if (mCurrLocationMarker != null) {
                mCurrLocationMarker.remove();
                circle.remove();
            }

            //Place current location marker
            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            options = new MarkerOptions()
                    .title("Current Position")
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                    .position(latLng)
                    .snippet("I am Here");

            mCurrLocationMarker = mMap.addMarker(options);
            //mCurrLocationMarker.setTag(1);
            circle = drawCircle(latLng);

            // Move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

            // latlnt update incident send
            lt = mLastLocation.getLatitude();
            lg = mLastLocation.getLongitude();
        }

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }



    private void updateLocation() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    // Circle Here
    private Circle drawCircle(LatLng latLng) {
        CircleOptions options = new CircleOptions()
                .center(latLng)
                .radius(1000)
                .fillColor(0x33ff0000)
                .strokeColor(Color.BLUE)
                .strokeWidth(3);
        return mMap.addCircle(options);
    }

    private void removeEverything() {
        //4.7 remove marker1 and marker2
//        mCurrLocationMarker.remove();
//        mCurrLocationMarker = null;

        circle.remove();
        circle = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        dontMoveMarkerWhichOutClick = 0;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_send, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }
    public interface FragmentListener{
        void onSendClickAlertFrament(String tab,
                         String lat,
                         String lng,
                         String typeAc,
                         String typeName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_settings:
                //Toast.makeText(getContext(), "ไปหน้าต่อไปเลยจ้า " + String.valueOf(lt) +" , "+ String.valueOf(lg ), Toast.LENGTH_SHORT).show();
                if(typeNameSubject == null){
                    typeNameSubject = nameAcident1;
                }
                FragmentListener listener =  (FragmentListener) getActivity();
                listener.onSendClickAlertFrament(
                        "a",
                        String.valueOf(lt),
                        String.valueOf(lg),
                        typeAc,
                        typeNameSubject);
                break;
        }
        return true;
    }

    private void checkColor() {
        btnShowMark1.setBackgroundResource(R.color.gray_active_icon);
        btnShowMark2.setBackgroundResource(R.color.gray_active_icon);
        btnShowMark3.setBackgroundResource(R.color.gray_active_icon);
        btnShowMark4.setBackgroundResource(R.color.gray_active_icon);
    }


    /*
     * Save Instance State Here
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save Instance State here
    }

    /*
     * Restore Instance State Here
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore Instance State here
        }
    }
    View.OnClickListener fabLocationListener = new View.OnClickListener() {
        public void onClick(View v) {
            dontMoveMarkerWhichOutClick = 1;
            updateLocation();
            Toast.makeText(Contextor.getInstance().getContext(), "" + latLng, Toast.LENGTH_SHORT).show();
        }
    };


    View.OnClickListener showMarker1Listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (type1 == 1) {
                Toast.makeText(getContext(), "อุบัติเหตุ", Toast.LENGTH_SHORT).show();
                typeNameSubject = nameAcident1;
                typeAc = "1";
                checkColor();
                btnShowMark1.setBackgroundResource(R.color.bottom_item_type_1);

            }
        }
    };

    View.OnClickListener showMarker2Listener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            if (type2 == 1) {
                Toast.makeText(getContext(), "ไฟไหม้", Toast.LENGTH_SHORT).show();
                typeNameSubject = nameAcident2;
                typeAc = "2";
                checkColor();
                btnShowMark2.setBackgroundResource(R.color.bottom_item_type_2);
            }
        }
    };

    View.OnClickListener showMarker3Listener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            if (type3 == 1) {
                Toast.makeText(getContext(), "จีปล้น", Toast.LENGTH_SHORT).show();
                typeNameSubject = nameAcident3;
                typeAc = "3";
                checkColor();
                btnShowMark3.setBackgroundResource(R.color.bottom_item_type_3);
            }
        }
    };


    View.OnClickListener showMarker4Listener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            if (type4 == 1) {
                Toast.makeText(getContext(), "ทำร้ายร่างกาย", Toast.LENGTH_SHORT).show();
                typeNameSubject = nameAcident4;
                typeAc = "4";
                checkColor();
                btnShowMark4.setBackgroundResource(R.color.bottom_item_type_4);
            }

        }
    };




}
