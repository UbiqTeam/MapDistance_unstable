package com.example.spencer.mapdistance;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    TextView areaText;              //display area
    TextView markerCountText;       //display marker count
    TextView lengthsText;           //displays lengths between markers
    TextView coordinatesText;       //displays coordinates of markers
    Polyline mPolyline;
    Polygon area;
    Button clear;                   //clears map
    Button drop;                    //drops GPS marker
    Button setMarker;               //sets marker position
    Button setMarkerGPS;            //sets GPS marker
    Switch mode;                    //switch between GPS and manual mode
    private GoogleMap mMap;

    LocationManager locationManager;
    Location location;

    final List<Marker> markers = new ArrayList<>();                     //list of markers on the map
    final List<MarkerOptions> markerOptionsList = new ArrayList<>();    //list of options for each marker
    final List<Double> distances = new ArrayList<>();                   //list of distances between markers in meters
    final List<LatLng> coorList = new ArrayList<>();                    //list of marker coordinates

    double totalArea = 1.0;
    Marker marker = null;

    public void handleMarkers(MarkerOptions markerOptions, Marker marker) {             //handles distance between markers, line drawing, and adds marker to the map

        markers.add(marker);
        mPolyline = mMap.addPolyline(new PolylineOptions().geodesic(true));
        //markerCountText.setText("Marker Count: " + markers.size());


        if (markers.size() == 1) {                                  //no information with just one marker
            mMap.clear();
        }

        if (markers.size() == 4) {
            for(int i = 0; i < markers.size()-1; i++ ){
                double distance = SphericalUtil.computeDistanceBetween(markers.get(i).getPosition(), markers.get(i+1).getPosition());  //for markers 1-4 find distance & area
                distances.add(distance);
            }
            double distance = SphericalUtil.computeDistanceBetween(markers.get(3).getPosition(), markers.get(0).getPosition());     //distance between 1 and 4
            distances.add(distance);
            area = mMap.addPolygon(new PolygonOptions().add(markers.get(0).getPosition(), markers.get(1).getPosition(), markers.get(2).getPosition(),
                    markers.get(3).getPosition()).strokeColor(Color.BLACK));

            //lengthsText.setText("lengths: " + distances.get(0) + ", " + distances.get(1) + ", " + distances.get(2) + ", " + distances.get(3));
            markers.clear();                                                                //clear markers from list
            totalArea = computeArea(distances);                                             //computer area based on distances list
            String areaSTR = String.format("%.2f", totalArea);
            coordinatesText.setText(coorList.toString());
            distances.clear();                                                              //clear distances list
            coorList.clear();                                                               //clear coordinates list
            areaText.setText("Area (sq m): " + areaSTR);   //using a string to trunc to 2 decs
        } else {
            //lengthsText.setText("");
        }

        mMap.addMarker(markerOptions);                                                      //add marker through markerOptions to map
    }

    public double computeArea(List<Double> distances) {
        BigDecimal tmp = new BigDecimal(0);
        BigDecimal TWO = new BigDecimal(2);

        double a = distances.get(0);
        double b = distances.get(1);
        double c = distances.get(2);
        double d = distances.get(3);

        for (Double val : distances) {
            tmp = tmp.add(new BigDecimal(val.intValue()));
        }

        double s = tmp.divide(TWO).doubleValue();
        return Math.sqrt((s - a) * (s - b) * (s - c) * (s - d));
    }

    void setupMap(){
        areaText = findViewById(R.id.areaText);
        markerCountText = findViewById(R.id.markerCountText);
        coordinatesText = findViewById(R.id.coordinatesText);
        lengthsText = findViewById(R.id.lengthsText);
        clear = findViewById(R.id.clearButton);
        drop = findViewById(R.id.drop);
        setMarker = findViewById(R.id.setMarkerButton);
        setMarkerGPS = findViewById(R.id.setMarkerGPS);
        mode = findViewById(R.id.modeSwitch);
        //markerCountText.setText("Marker Count: " + markers.size());
        //areaText.setText("Area (sq m): ");
        mMap.setMapType(mMap.MAP_TYPE_SATELLITE);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            mMap.setMyLocationEnabled(true);
        } else {
            markerCountText.setText("no GPS permission");
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18.0f));  //move camera to current GPS location

        areaText.setTextColor(Color.RED);
        markerCountText.setTextColor(Color.RED);
        lengthsText.setTextColor(Color.RED);
        coordinatesText.setTextColor(Color.RED);
        mode.setTextColor(Color.BLACK);
        mode.setBackgroundColor(Color.LTGRAY);

        mode.setChecked(true);
        setMarker.setEnabled(false);
        clear.setEnabled(false);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final MarkerOptions markerOptions = new MarkerOptions();

        setupMap();

        mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                                //mode switch to change drop/lock marker button
                if (!mode.isChecked()) {
                    Log.d("MODE","GPS");
                    drop.setVisibility(View.VISIBLE);
                    setMarker.setVisibility(View.GONE);
                    setMarkerGPS.setVisibility(View.GONE);
                    clear.setEnabled(true);

                    //////////////////////// GPS Mode ///////////////////////////////////

                    drop.setOnClickListener(new View.OnClickListener() {                    //drop GPS marker

                        public void onClick(View view) {
                            if (!mode.isChecked()) {
                                try {
                                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                } catch (SecurityException e) {
                                    markerCountText.setText("no GPS permission");
                                }
                                Log.d("GPS","drop");
                                LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());         //get GPS lat/long
                                markerOptions.position(newLatLng);
                                marker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).draggable(true));  //create marker

                                drop.setVisibility(View.GONE);          //switches drop marker to lock marker
                                setMarkerGPS.setVisibility(View.VISIBLE);

                                mode.setEnabled(false);                 //disable mode switch after gps marker drop, but before locking it
                            }                                           //(causes some issues if user changes mode with un-set marker)

                        }
                    });

                    mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {     //drag


                        @Override
                        public void onMarkerDragStart(Marker marker) {
                        }

                        @SuppressWarnings("unchecked")
                        @Override
                        public void onMarkerDragEnd(Marker arg0) {      //set new position when done dragging
                            Log.d("GPS","drag");
                            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude)).draggable(true));
                            LatLng newLatLng = new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude);
                            markerOptions.position(newLatLng);
                        }

                        @Override
                        public void onMarkerDrag(Marker arg0) {
                        }
                    });

                    setMarkerGPS.setOnClickListener(new View.OnClickListener() {  //lock GPS marker
                        public void onClick(View view) {
                            if (!mode.isChecked()) {
                                Log.d("GPS", "setMarker");
                                mode.setEnabled(true);
                                markerOptionsList.add(markerOptions);
                                handleMarkers(markerOptions, marker);

                                LatLng tmp = markerOptions.getPosition();
                                coorList.add(tmp);
                                drop.setVisibility(View.VISIBLE);
                                setMarkerGPS.setVisibility(View.GONE);
                            }

                        }
                    });
                } else {
                    Log.d("MODE","manual");
                    drop.setVisibility(View.GONE);
                    setMarker.setVisibility(View.VISIBLE);
                    setMarkerGPS.setVisibility(View.GONE);
                }
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {                                  //clears the map & initializes vals

                mMap.clear();
                markers.clear();
                clear.setEnabled(false);
                //markerCountText.setText("Marker Count: " + markers.size());
                //areaText.setText("Area (sq m): ");
                areaText.setText("");
                lengthsText.setText(" ");
                coordinatesText.setText(" ");
                distances.clear();
                coorList.clear();
                totalArea = 1;
                if (!mode.isChecked()) {
                    Log.d("GPS","clear");
                    drop.setVisibility(View.VISIBLE);          //clear in GPS mode reverts lock to drop
                    setMarkerGPS.setVisibility(View.GONE);
                    setMarker.setVisibility(View.GONE);
                    clear.setEnabled(true);
                } else {
                    Log.d("manual","clear");
                    drop.setVisibility(View.GONE);
                    setMarker.setVisibility(View.VISIBLE);
                    setMarker.setEnabled(false);            //disables setMarker when no marker is on screen
                    setMarkerGPS.setVisibility(View.GONE);
                }
                mode.setEnabled(true);
            }
        });

        //////////////////////////////// Manual Mode ////////////////////////////////////////

        mMap.setOnMapClickListener(new OnMapClickListener() {
            Boolean button = false;         //disables onClick to set new latlng after a marker has been dropped, but before it's locked (causes crash otherwise see issue #1)
            Marker marker = null;

            @Override
            public void onMapClick(final LatLng latLng) {

                if(!button) {                                                //prevent issue #1
                    if (mode.isChecked()) {                                  //if in manual mode
                        Log.d("manual","mapClick");
                        final List<Marker> tmpMarkers = new ArrayList<>();
                        final MarkerOptions markerOptions = new MarkerOptions();
                        mode.setEnabled(false);
                        setMarker.setEnabled(true);                          //enables setMarker when marker is on screen
                        clear.setEnabled(false);                             //prevents deleting marker before setting causing bug
                        if (!button) {                                       //prevents onClick input while manipulating marker (issue #1)
                            markerOptions.position(latLng);
                            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).draggable(true));  //create marker
                            button = true;                                      //since marker is created onClick is re-enabled
                        }

                        if (mode.isChecked()) {
                            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {     //drag
                                @Override
                                public void onMarkerDragStart(Marker marker) {
                                    button = false;
                                }

                                @SuppressWarnings("unchecked")
                                @Override
                                public void onMarkerDragEnd(Marker arg0) {                  //set new position when done dragging
                                    if (mode.isChecked()) {
                                        Log.d("manual", "drag");
                                        marker = mMap.addMarker(new MarkerOptions().position(new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude)).draggable(true));
                                        LatLng newLatLng = new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude);
                                        markerOptions.position(newLatLng);
                                        button = true;
                                    }

                                }

                                @Override
                                public void onMarkerDrag(Marker arg0) {
                                }

                            });
                        }

                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {           //remove a marker
                                Log.d("manual","remove");  //GPS remove only removes marker not entry
                                button = false;
                                marker.remove();
                                return true;
                            }
                        });


                        setMarker.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View view) {                //lock a marker in place
                                if (button) {
                                    Log.d("manual","setMarker");
                                    mode.setEnabled(true);
                                    tmpMarkers.add(marker);
                                    markerOptionsList.add(markerOptions);
                                    LatLng tmp = markerOptions.getPosition();
                                    coorList.add(tmp);
                                    setMarker.setEnabled(false);                          //diables setMarker when no marker is on screen
                                    clear.setEnabled(true);
                                    handleMarkers(markerOptions, marker);
                                    button = false;
                                }

                            }
                        });
                    }
                }
            }
        });
    }
}
