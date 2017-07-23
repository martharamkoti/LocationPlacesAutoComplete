package com.example.locationplacesautocomplete;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends Activity {

    AutoCompleteTextView sourceAddress;
    AutoCompleteTextView destinationAddress;
    Button getRouteButton;
    Button getDistanceButton;
    PlacesTask placesTask;
    ParserTask parserTask;
    String parsedDistance;
    String response;
    LatLng sourceLatLng,destinationLatLng;
    TextView distanceTextView;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getRouteButton =(Button) findViewById(R.id.get_route);
        distanceTextView = (TextView) findViewById(R.id.distanceTextView);
        getDistanceButton = (Button) findViewById(R.id.get_distance);
        sourceAddress = (AutoCompleteTextView) findViewById(R.id.source_address);
        destinationAddress = (AutoCompleteTextView) findViewById(R.id.destination_address);
        destinationAddress.setThreshold(1);
        sourceAddress.setThreshold(1);

        sourceAddress.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesTask = new PlacesTask();
                placesTask.execute(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        destinationAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                placesTask = new PlacesTask();
                placesTask.execute(s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        getRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sourceAddress.setError(null);
                destinationAddress.setError(null);
                System.out.println("User clicked on the get route button!!!!!");
                if (sourceAddress.getText().toString().length() == 0){
                    sourceAddress.setError("Required");
                    sourceAddress.requestFocus();
                } else if (destinationAddress.getText().toString().length() == 0) {
                    destinationAddress.setError("Required");
                    destinationAddress.requestFocus();
                } else {
                    sourceLatLng = getLocationFromAddress(MainActivity.this,sourceAddress.getText().toString());
                    destinationLatLng = getLocationFromAddress(MainActivity.this,destinationAddress.getText().toString());
                    //System.out.println("Source  "+sourceAddress.getText().toString()+" and "+destinationAddress.getText().toString());
                    //Toast.makeText(MainActivity.this,"source and destination : "+sourceLatLng+" and "+destinationLatLng,Toast.LENGTH_LONG).show();Toast

                    if (sourceLatLng != null && destinationLatLng != null) {
                        Intent intent = new Intent(MainActivity.this,GoogleMapActivity.class);

                        Bundle bundle = new Bundle();
                        bundle.putString("sourceLat", String.valueOf(sourceLatLng.latitude));
                        bundle.putString("sourceLng", String.valueOf(sourceLatLng.longitude));
                        bundle.putString("destinationLat", String.valueOf(destinationLatLng.latitude));
                        bundle.putString("destinationLng", String.valueOf(destinationLatLng.longitude));
                        intent.putExtras(bundle);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this,"Something went wrong! Try again!",Toast.LENGTH_LONG).show();
                    }

                }
            }
        });

        getDistanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                distanceTextView.setText("loading...");
                sourceAddress.setError(null);
                destinationAddress.setError(null);
                System.out.println("User clicked on the get route button!!!!!");
                if (sourceAddress.getText().toString().length() == 0){
                    sourceAddress.setError("Required");
                    sourceAddress.requestFocus();
                } else if (destinationAddress.getText().toString().length() == 0) {
                    destinationAddress.setError("Required");
                    destinationAddress.requestFocus();
                } else {
                    try {
                        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    sourceLatLng = getLocationFromAddress(MainActivity.this,sourceAddress.getText().toString());
                    destinationLatLng = getLocationFromAddress(MainActivity.this,destinationAddress.getText().toString());
                    if (sourceLatLng != null && destinationLatLng != null) {
                        System.out.println("SENDING  : "+sourceLatLng.latitude+" "+sourceLatLng.longitude+" and "+destinationLatLng.latitude+" "+destinationLatLng.longitude);
                        String distance = getDistance(sourceLatLng.latitude,sourceLatLng.longitude,destinationLatLng.latitude,destinationLatLng.longitude);
                        Toast.makeText(MainActivity.this,"Distance is : "+distance,Toast.LENGTH_LONG).show();
                        distanceTextView.setText("Distance is : "+distance);
                    } else {
                        Toast.makeText(MainActivity.this,"Something went wrong! Try again!",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    public String getDistance(final double lat1, final double lon1, final double lat2, final double lon2){

        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    URL url = new URL("http://maps.googleapis.com/maps/api/directions/json?origin=" + lat1 + "," + lon1 + "&destination=" + lat2 + "," + lon2 + "&sensor=false&units=metric&mode=driving");
                    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    response = org.apache.commons.io.IOUtils.toString(in, "UTF-8");

                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray array = jsonObject.getJSONArray("routes");
                    JSONObject routes = array.getJSONObject(0);
                    JSONArray legs = routes.getJSONArray("legs");
                    JSONObject steps = legs.getJSONObject(0);
                    JSONObject distance = steps.getJSONObject("distance");
                    parsedDistance=distance.getString("text");

                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return parsedDistance;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            System.out.println("Exception while downloading url   "+e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches all places from GooglePlaces AutoComplete Web Service
    private class PlacesTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... place) {
            // For storing data from web service
            String data = "";

            // Obtain browser key from https://code.google.com/apis/console
            String key = "key=AIzaSyDxqQEvtdEtl6dDIvG7vcm6QTO45Si0FZs";

            String input="";

            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // place type to be searched
            String types = "types=geocode";

            // Sensor enabled
            String sensor = "sensor=false";

            // Building the parameters to the web service
            String parameters = input+"&"+types+"&"+sensor+"&"+key;

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;

            try{
                // Fetching the data from we service
                data = downloadUrl(url);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Creating ParserTask
            parserTask = new ParserTask();

            // Starting Parsing the JSON string returned by Web Service
            parserTask.execute(result);
        }
    }
    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;

            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try{
                jObject = new JSONObject(jsonData[0]);

                // Getting the parsed data as a List construct
                places = placeJsonParser.parse(jObject);

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            String[] from = new String[] { "description"};
            int[] to = new int[] { android.R.id.text1 };

            // Creating a SimpleAdapter for the AutoCompleteTextView
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);

            // Setting the adapter

            if (sourceAddress.isFocused()){
                System.out.println("source address text box is focused!!!!!!!!!!");
                sourceAddress.setAdapter(adapter);
            }
            if (destinationAddress.isFocused()) {
                System.out.println("destination address text box is focused!!!!!!!!!!");
                destinationAddress.setAdapter(adapter);
            }
        }
    }
}
