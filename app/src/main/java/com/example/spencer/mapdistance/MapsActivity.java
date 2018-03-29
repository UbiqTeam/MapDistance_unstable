package com.example.spencer.mapdistance;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.location.Location;
import android.support.v4.content.ContextCompat;
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
    Switch mode;                    //switch between GPS and manual mode
    private GoogleMap mMap;

    LocationManager locationManager;
    Location location;

    final List<Marker> markers = new ArrayList<>();                     //list of markers on the map
    final List<MarkerOptions> markerOptionsList = new ArrayList<>();    //list of options for each marker
    final List<Double> distances = new ArrayList<>();                   //list of distances between markers in meters
    final List<LatLng> coorList = new ArrayList<>();                    //list of marker coordinates

    int count = -1;
    double totalArea = 1.0;
    Boolean switchState = true;
    Marker marker = null;

    public void handleMarkers(MarkerOptions markerOptions, Marker marker) {             //handles distance between markers, line drawing, and adds marker to the map

        markers.add(marker);
        mPolyline = mMap.addPolyline(new PolylineOptions().geodesic(true));
        markerCountText.setText("Marker Count: " + markers.size());
        coordinatesText.setText(coorList.toString());

        if (markers.size() == 1) {                                  //no information with just one marker
            mMap.clear();
        }

        if (markers.size() > 1) {
            double distance = SphericalUtil.computeDistanceBetween(markers.get(count).getPosition(), markers.get(count + 1).getPosition());  //for markers 1-4 find distance & area
            distances.add(distance);
            areaText.setText("Area (sq m): " + totalArea);
        }

        if (markers.size() == 4) {
            double distance = SphericalUtil.computeDistanceBetween(markers.get(3).getPosition(), markers.get(0).getPosition());               //find distance between 4th and 1st marker
            distances.add(distance);
            area = mMap.addPolygon(new PolygonOptions().add(markers.get(0).getPosition(), markers.get(1).getPosition(), markers.get(2).getPosition(),       //draw square
                    markers.get(3).getPosition()).strokeColor(Color.BLACK));
            int i = 0;
            lengthsText.setText("lengths: " + distances.get(i) + ", " + distances.get(i + 1) + ", " + distances.get(i + 2) + ", " + distances.get(i + 3));
            markers.clear();                                                                //clear markers from list
            totalArea = computeArea(distances);                                             //computer area based on distances list
            distances.clear();                                                              //clear distances list
            coorList.clear();                                                               //clear coordinates list
            count = -2;                                                                     //initialize count
            areaText.setText("Area (sq m): " + totalArea);
        } else {
            lengthsText.setText("");
        }

        mMap.addMarker(markerOptions);                                                      //add marker through markerOptions to map
        count++;
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
        mode = findViewById(R.id.modeSwitch);
        markerCountText.setText("Marker Count: " + markers.size());
        areaText.setText("Area (sq m): ");
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
            public void onClick(View view) {                        //mode switch to change drop/lock marker button
                if (!mode.isChecked()) {
                    drop.setVisibility(View.VISIBLE);
                    setMarker.setVisibility(View.GONE);
                } else {
                    drop.setVisibility(View.GONE);
                    setMarker.setVisibility(View.VISIBLE);
                }
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {                                   //clears the map & initializes vals
                mMap.clear();
                markers.clear();
                markerCountText.setText("Marker Count: " + markers.size());
                areaText.setText("Area (sq m): ");
                lengthsText.setText(" ");
                coordinatesText.setText(" ");
                distances.clear();
                coorList.clear();
                count = -1;
                totalArea = 1;
                switchState = mode.isChecked();
            }
        });

        
        //////////////////////// GPS Mode ///////////////////////////////////

        drop.setOnClickListener(new View.OnClickListener() {                    //drop GPS marker

            public void onClick(View view) {
                if (!mode.isChecked()) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    try {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    } catch (SecurityException e) {
                        markerCountText.setText("no GPS permission");
                    }
                    LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());         //get GPS lat/long
                    markerOptions.position(newLatLng);
                    coorList.add(newLatLng);
                    marker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).draggable(true));  //create marker

                    drop.setVisibility(View.GONE);          //switches drop marker to lock marker
                    setMarker.setVisibility(View.VISIBLE);
                    //handleMarkers(markerOptions, marker);
                }

            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {     //drag
            MarkerOptions markerOptions = new MarkerOptions();

            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onMarkerDragEnd(Marker arg0) {      //set new position when done dragging

                marker = mMap.addMarker(new MarkerOptions().position(new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude)).draggable(true));
                LatLng newLatLng = new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude);
                markerOptions.position(newLatLng);
            }

            @Override
            public void onMarkerDrag(Marker arg0) {
            }
        });

        setMarker.setOnClickListener(new View.OnClickListener() {  //lock GPS marker
            public void onClick(View view) {
                markers.add(marker);
                markerOptionsList.add(markerOptions);
                LatLng tmp = markerOptions.getPosition();
                coorList.add(tmp);
                //handleMarkers(markerOptions, marker);    //This causes app to crash
                drop.setVisibility(View.VISIBLE);
                setMarker.setVisibility(View.GONE);

            }
        });

        //////////////////////////////// Manual Mode ////////////////////////////////////////
            mMap.setOnMapClickListener(new OnMapClickListener() {
                Boolean button = false;         //disables onClick to set new latlng after a marker has been dropped, but before it's locked (causes crash otherwise see issue #1)
                Marker marker = null;

                @Override
                public void onMapClick(final LatLng latLng) {
                    if(!button) {           //prevent issue #1
                        if (mode.isChecked()) {         //if in manual mode
                            final List<Marker> tmpMarkers = new ArrayList<>();
                            final MarkerOptions markerOptions = new MarkerOptions();

                            if (!button) {   //prevents onClick input while manipulating marker (issue #1)
                                markerOptions.position(latLng);
                                marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).draggable(true));  //create marker
                                button = true;  //since marker is created onClick is re-enabled
                            }


                            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                                @Override
                                public void onMarkerDragStart(Marker marker) {
                                    button = false;
                                }

                                @SuppressWarnings("unchecked")
                                @Override
                                public void onMarkerDragEnd(Marker arg0) {

                                    marker = mMap.addMarker(new MarkerOptions().position(new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude)).draggable(true));
                                    LatLng newLatLng = new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude);
                                    markerOptions.position(newLatLng);
                                    button = true;

                                }

                                @Override
                                public void onMarkerDrag(Marker arg0) {
                                }
                            });

                            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {  //remove a marker
                                @Override
                                public boolean onMarkerClick(Marker marker) {
                                    button = false;
                                    marker.remove();
                                    return true;
                                }
                            });


                            setMarker.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {                    //lock a marker in place
                                    tmpMarkers.add(marker);
                                    markerOptionsList.add(markerOptions);
                                    LatLng tmp = markerOptions.getPosition();
                                    coorList.add(tmp);
                                    if (button) {
                                        handleMarkers(markerOptions, marker);
                                    }
                                    button = false;
                                }
                            });
                        }
                    }
                }
            });
    }
}
