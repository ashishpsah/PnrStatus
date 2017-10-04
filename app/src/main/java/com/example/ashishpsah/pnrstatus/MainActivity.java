package com.example.ashishpsah.pnrstatus;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    EditText editTextPnr;
    TextView textViewTainName,textViewDoj,textViewSrc,textViewDes,textViewClass,textViewPlist,textViewRefresh;
    ImageView fetch_image_refresh;
    ProgressBar progressBar;
    String pnrNumber,API_URL,passangerList="ASHISH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewTainName = (TextView) findViewById(R.id.textViewTrainName);
        textViewDoj = (TextView) findViewById(R.id.textViewDoj);
        textViewSrc = (TextView) findViewById(R.id.textViewSrc);
        textViewDes = (TextView) findViewById(R.id.textViewDes);
        textViewClass = (TextView) findViewById(R.id.textViewClass);
        textViewPlist = (TextView) findViewById(R.id.textViewPlist);
        textViewRefresh = (TextView) findViewById(R.id.textViewRefresh);
        editTextPnr = (EditText) findViewById(R.id.editTextPnr);
        fetch_image_refresh = (ImageView) findViewById(R.id.fetch_image_refresh);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        Button queryButton = (Button) findViewById(R.id.queryButton);
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pnrNumber = editTextPnr.getText().toString();
                if(!pnrNumber.isEmpty()) {
                    API_URL = "http://api.railwayapi.com/v2/pnr-status/pnr/" + pnrNumber + "/apikey/dj88mek5ca";
                    new RetrieveFeedTask().execute();
                }
                else {
                    Toast.makeText(MainActivity.this,"Enter Pnr number", Toast.LENGTH_LONG).show();
                }
            }
        });
        fetch_image_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                API_URL = "http://api.railwayapi.com/v2/pnr-status/pnr/"+pnrNumber+"/apikey/dj88mek5ca";
                new RetrieveFeedTask().execute();
            }
        });
    }

    @Override
    public void onBackPressed() {
        //Do Nothing
    }

    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);

        }

        protected String doInBackground(Void... urls) {

            try {
                URL url = new URL(API_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if(response == null) {
                response = "THERE WAS AN ERROR";
            }
            progressBar.setVisibility(View.GONE);
            Log.i("INFO", response);
            try {
                JSONObject jsonObj = new JSONObject(response);
                String responsecode = jsonObj.getString("response_code");

                if(responsecode.equals("200")){

                //JSONArray jsonArray = new JSONArray(response);
                JSONObject trainquery = jsonObj.getJSONObject("train");
                JSONObject srcquery = jsonObj.getJSONObject("from_station");
                JSONObject desquery = jsonObj.getJSONObject("to_station");
                JSONObject journeyClassquery = jsonObj.getJSONObject("journey_class");
                JSONArray plistquery =jsonObj.getJSONArray("passengers");
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < plistquery.length(); i++) {
                JSONObject c = plistquery.getJSONObject(i);
                stringBuilder.append("\nPassenger: " + (i+1) + " "+c.getString("current_status") );
                }
                passangerList = stringBuilder.toString();
                String trainName = trainquery.getString("name");
                String dateOfJourney = jsonObj.getString("doj");
                String src = srcquery.getString("name");
                String des = desquery.getString("name");
                String journeyClass = journeyClassquery.getString("name");
                Date currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String refreshedOn = timeformat.format(currentTime);
                textViewTainName.setText("Train Name: "+trainName);
                textViewDoj.setText("Date of Journey: "+dateOfJourney);
                textViewSrc.setText("Source: "+src);
                textViewDes.setText("Destination: "+des);
                textViewClass.setText("Journey Class: "+journeyClass);
                textViewPlist.setText(passangerList);
                textViewRefresh.setText("Refreshed on:\n"+refreshedOn);}
                else{
                    startActivity(new Intent(MainActivity.this,InvalidPnr.class));

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
