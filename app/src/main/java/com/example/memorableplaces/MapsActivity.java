package com.example.memorableplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    LocationManager locationManager;

    LocationListener locationListener;

    private GoogleMap mMap;

    public void centerMapOnLocation(Location location, String title) {
        if (location!=null) {
            //luam locatia actuala a userului si o salvam in userLocation
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(userLocation).title(title)); //punem marker
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12)); //centram camera
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //cerem permisiune pt locatie
        if(grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0,locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); //luam locatie user
                centerMapOnLocation(lastKnownLocation, "Your Location");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();
       if(intent.getIntExtra("placeNumber", 0)==0) { //luam datele din intent si daca e 0 inseamna ca s-a selectat add location
           //centram pe user location
           locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
           locationListener = new LocationListener() {
               @Override
               public void onLocationChanged(Location location) {
                   centerMapOnLocation(location,"Your Location");
               }

               @Override
               public void onStatusChanged(String provider, int status, Bundle extras) {

               }

               @Override
               public void onProviderEnabled(String provider) {

               }

               @Override
               public void onProviderDisabled(String provider) {

               }
           };

           if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
               locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0,locationListener);
               Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
               centerMapOnLocation(lastKnownLocation, "Your Location");
           }
           else {
               ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
           }
       }
       else { //click pe o locatie din lista
           Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
           placeLocation.setLatitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).latitude); //setam lat si long locatiei alese
           placeLocation.setLongitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).longitude);

           centerMapOnLocation(placeLocation, MainActivity.places.get(intent.getIntExtra("placeNumber",0))); //centram pe locatia aleasa
       }

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault()); //pentru a putea lua adresa

        String address = "";

        try {

            List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude,1); //ia adresa de la long si lat si pune in array

            if (listAddresses != null && listAddresses.size() > 0) {
                if (listAddresses.get(0).getThoroughfare() != null) { //numele strazii
                    if (listAddresses.get(0).getSubThoroughfare() != null) { //nr strazii
                        address += listAddresses.get(0).getSubThoroughfare() + " ";
                    }
                    address += listAddresses.get(0).getThoroughfare();
                }
            }

        }catch(Exception e) {
            e.printStackTrace();
        }

        if (address.equals("")) { //daca locul ales nu are o adresa
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy");
            address += sdf.format(new Date()); //setam timpu si data
        }

        mMap.addMarker(new MarkerOptions().position(latLng).title(address)); //punem markeru

        MainActivity.places.add(address); //adaugam adresa in array places ca sa apara in listview
        MainActivity.locations.add(latLng); //adaugam si lat si long la array de locations

        MainActivity.arrayAdapter.notifyDataSetChanged(); //refresh la listview

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.memorableplaces", Context.MODE_PRIVATE);

        try {

            ArrayList<String> latitudes = new ArrayList<>();
            ArrayList<String> longitudes = new ArrayList<>();

            for(LatLng coord : MainActivity.locations) { //salvam lat si long din locations in 2 arrays separate sa le putem codifica pt salvare
                latitudes.add(Double.toString(coord.latitude));
                longitudes.add(Double.toString(coord.longitude));
            }

            sharedPreferences.edit().putString("places", ObjectSerializer.serialize(MainActivity.places)).apply();
            sharedPreferences.edit().putString("lats", ObjectSerializer.serialize(latitudes)).apply();
            sharedPreferences.edit().putString("longs", ObjectSerializer.serialize(longitudes)).apply(); //salvam adresele si locatiile(lat si longs) in telefon



        } catch(Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "Location saved!",Toast.LENGTH_SHORT).show();
    }
}