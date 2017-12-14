package com.rwth.medit.meditapp;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import com.jakewharton.threetenabp.AndroidThreeTen;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.Instant;
import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalField;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

public class MainActivity extends AppCompatActivity {


    private Handler mHandler = new Handler();
    private Runnable mTimer1;
    private LineGraphSeries<DataPoint> mSeries1;
    private AsyncHttpClient client;
    private RequestParams params;
    DataPoint[] dataPoints = null;
    GraphView graph;
    Map<String, String> mapparams;
    TextView tvheartRate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AndroidThreeTen.init(this);
        graph = (GraphView) findViewById(R.id.graph);
        tvheartRate = (TextView) findViewById(R.id.tv_heart_rate);

        params = new RequestParams();
        params.put("db", "medit");
        params.put("q", "select * from ecg_Filtered where time >= now() - 4s");
        params.put("epochs", "ms");


        client = new AsyncHttpClient();

        client.setTimeout(20000);

        mSeries1 = new LineGraphSeries<>();
        graph.addSeries(mSeries1);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(MainActivity.this, simpleDateFormat));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space



        //String URL = "http://localhost:8086/query?db=medit&q=SELECT%20%22value%22%20FROM%20%22ecg_orignal%22%20WHERE%20time%20%3E%3D%20now()%20-%205s&epoch=ms";

        String s = "http://localhost:8086/query?db=medit&q=SELECT%20%22value%22%20FROM%20%22ecg_orignal%22%20WHERE%20time%20%3E%3D%20now()%20-%205s&epoch=ms";
    }

    @Override
    public void onResume() {
        super.onResume();
        mTimer1 = new Runnable() {
            @Override
            public void run() {

                client.get("http://134.61.177.247:8086/query", params, new JsonHttpResponseHandler(){

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        // called when response HTTP status is "200 OK"
                        //Log.d("header", headers[0].toString());
                        //Log.d("JSON Data", response.toString());
                        DataPoint[] dp = processJson(response);
                        if(dp != null) mSeries1.resetData(dp);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        // called when response HTTP status is "200 OK"
                        Log.d("header", headers[0].toString());
                        Log.d("JSON Data", response.toString());
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject response) {
                        // called when response HTTP status is "200 OK"
                        Log.d("Throable", "" + t);
                        Toast.makeText(MainActivity.this, "Error retreiving data. Make sure you are connected to internet.", Toast.LENGTH_SHORT).show();
                    }
                });



                //mSeries1.resetData(generateData());
                mHandler.postDelayed(this, 1000);
            }
        };
        mHandler.postDelayed(mTimer1, 1000);

    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(mTimer1);
        super.onPause();
    }

    private DataPoint[] processJson(JSONObject response) {
        DataPoint[] points = null;
        try {
            JSONObject series = response.getJSONArray("results").getJSONObject(0).getJSONArray("series").getJSONObject(0);
            JSONArray columns = series.getJSONArray("columns");
            JSONArray values = series.getJSONArray("values");

            int len = values.length();
            points = new DataPoint[len];
            Log.d("Columns", columns.toString());

           // Log.d("length", "" + values.length());

            for(int i=0; i < values.length(); i++){
                JSONArray x = values.getJSONArray(i);
                Log.d("values ", "" + i + " : " + x);

                //Date dt  = tryParse(x.getString(0));
                //Log.d("Date", "" + dt);
                //Log.d("milliseconds", "" + dt.getTime());
                //Date dt = new Date(x.getLong(5)*1000L);
                //DataPoint v = new DataPoint(dt.getTime(), x.getDouble(7));
                Instant instant = Instant.parse(x.getString(0));
                //Log.d("Date", "" + instant);
                //Log.d("nano Seconds", "" + instant.getNano());
                //Log.d("EPOCH Seconds", "" + instant.getEpochSecond());
                //Log.d("Converted Seconds", "" + instant.getEpochSecond() + instant.getNano()/1000000);
                if(x.getString(2) != "null") tvheartRate.setText(x.getString(2));
                DataPoint v = new DataPoint(instant.toEpochMilli(), x.getDouble(7));
                points[i] = v;
            }

            String s = "60 ❤ BPM";

            graph.getViewport().setMinX(points[0].getX());
            graph.getViewport().setMaxX(points[len-1].getX());
            graph.getViewport().setXAxisBoundsManual(true);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage() + ". Make sure the device and database is running.", Toast.LENGTH_SHORT).show();
        }
        return points;
    }
}


