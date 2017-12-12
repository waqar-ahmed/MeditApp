package com.rwth.medit.meditapp;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

public class MainActivity extends AppCompatActivity {


    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private LineGraphSeries<DataPoint> mSeries1;
    private AsyncHttpClient client;
    private RequestParams params;
    DataPoint[] dataPoints = null;
    GraphView graph;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        graph = (GraphView) findViewById(R.id.graph);

        params = new RequestParams();
        params.put("db", "medit");
        params.put("q", "select * from ecg_Filtered where time >= now() - 5s");
        params.put("epochs", "ms");

        client = new AsyncHttpClient();

        mSeries1 = new LineGraphSeries<>();
        graph.addSeries(mSeries1);

        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(MainActivity.this));
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


                client.get("http://134.61.74.152:8086/query", params, new JsonHttpResponseHandler(){

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        // called when response HTTP status is "200 OK"
                        Log.d("header", headers[0].toString());
                        Log.d("JSON Data", response.toString());
                        mSeries1.resetData(processJson(response));
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
                        Log.d("header", headers[0].toString());
                        Log.d("JSON Data", response.toString());
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

    Random mRand = new Random();

    private DataPoint[] generateData() {

        dataPoints = null;

        client.get("http://134.61.74.152:8086/query", params, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // called when response HTTP status is "200 OK"
                Log.d("header", headers[0].toString());
                Log.d("JSON Data", response.toString());
                dataPoints = processJson(response);
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
                Log.d("header", headers[0].toString());
                Log.d("JSON Data", response.toString());
            }
        });
        return dataPoints;
    }

    private DataPoint[] processJson(JSONObject response) {
        DataPoint[] points = null;
        try {
            JSONObject series = response.getJSONArray("results").getJSONObject(0).getJSONArray("series").getJSONObject(0);
            JSONArray columns = series.getJSONArray("columns");
            JSONArray values = series.getJSONArray("values");

            int len = values.length();
            points = new DataPoint[len];

            Log.d("length", "" + values.length());

            for(int i=0; i < values.length(); i++){
                JSONArray x = values.getJSONArray(i);
                Log.d("values ", "" + i + " : " + x);

                Date date = tryParse(x.getString(0));
                long millis = date.getTime();
                Log.d("millis ", "" + millis);

                DataPoint v = new DataPoint(millis, x.getDouble(6));
                points[i] = v;
            }


            graph.getViewport().setMinX(points[0].getX());
            graph.getViewport().setMaxX(points[len-1].getX());
            graph.getViewport().setXAxisBoundsManual(true);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return points;
    }


    List<String> formatStrings = Arrays.asList("yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ss.S'Z'", "yyyy-MM-dd'T'HH:mm:ss.SS'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSSSS'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'");

    Date tryParse(String dateString)
    {
        /*
        int i = 0;
        String[] arr = dateString.split(".");
        int count = arr[1].length()-1;
        String zeroStr = "";
        for(int j = 0; j < count; j++)
            zeroStr += "0";
        dateString = dateString.split("Z")[0] + zeroStr + "Z";
        */
        for (String formatString : formatStrings)
        {
            try
            {
                SimpleDateFormat formatter = new SimpleDateFormat(formatString);
                formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                return formatter.parse(dateString);
            }
            catch (ParseException e) {}
        }

        return null;
    }

}


