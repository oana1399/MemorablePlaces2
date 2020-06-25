package com.example.memorableplaces;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String> places = new ArrayList<String>();
    static ArrayList<LatLng> locations = new ArrayList<LatLng>();
    static ArrayAdapter arrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*DownloadTask task = new DownloadTask();

        String result=null;
        try {
            result=task.execute("https://zappycode.com/").get();
        }catch(Exception e) {
            e.printStackTrace();
        }
        Log.i("Result",result);*/

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.memorableplaces", Context.MODE_PRIVATE);

        ArrayList<String> latitudes = new ArrayList<>();
        ArrayList<String> longitudes = new ArrayList<>();

        places.clear();
        latitudes.clear();
        longitudes.clear();
        locations.clear();

        try {

            places = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("places",ObjectSerializer.serialize(new ArrayList<String>()))); //convertim din bytes inapoi in objects si pune in array places
            latitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("lats",ObjectSerializer.serialize(new ArrayList<String>())));
            longitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("longs",ObjectSerializer.serialize(new ArrayList<String>())));

        } catch(Exception e) {
            e.printStackTrace();
        }

        if (places.size() > 0 && latitudes.size() > 0 && longitudes.size() > 0) {  //verificam daca s-a decodat cu succes si s-a adaugat in arrays
            if (places.size() == latitudes.size() && places.size() == longitudes.size()) {
                for (int i=0; i < latitudes.size(); i++) {
                    locations.add(new LatLng(Double.parseDouble(latitudes.get(i)), Double.parseDouble(longitudes.get(i))));  //adaugam lat si long din decodare la array de lat si long
                }
            }
        } else {
            places.add("Add a new place..."); //pentru prima utilizare a aplicatiei
            locations.add(new LatLng(0,0));
        }

        ListView listView = findViewById(R.id.listView);



        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, places);

        listView.setAdapter(arrayAdapter);  //adauga array de places la list view

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("placeNumber",position); //transmitem date prin intent (poz din array)

                startActivity(intent); //deschidem mapsactivity prin intent
            }
        });
    }
}