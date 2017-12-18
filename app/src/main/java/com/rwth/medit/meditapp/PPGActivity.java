package com.rwth.medit.meditapp;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.threetenabp.AndroidThreeTen;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.Instant;

import java.text.SimpleDateFormat;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import helper.CustomDataPoint;
import helper.CustomDataPointInterface;
import helper.CustomPointsGraphSeries;

public class PPGActivity extends AppCompatActivity {


    private Handler mHandler = new Handler();
    private Runnable mTimer1;
    private LineGraphSeries<DataPoint> mSeries1;
    private CustomPointsGraphSeries<CustomDataPoint> annotations;
    private AsyncHttpClient client;
    private RequestParams params;
    DataPoint[] dataPoints = null;
    GraphView graph;
    Map<String, String> mapparams;
    TextView tvheartRate;
    SharedPreferences prefs;
    private String serverIP = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ppg);
        AndroidThreeTen.init(this);
        graph = (GraphView) findViewById(R.id.ppg_graph_1);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        params = new RequestParams();
        params.put("db", "medit");
        params.put("q", "select * from ppg where time >= now() - 5s");
        params.put("epochs", "ms");


        client = new AsyncHttpClient();

        client.setTimeout(20000);

        mSeries1 = new LineGraphSeries<>();

        graph.addSeries(mSeries1);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(PPGActivity.this, simpleDateFormat));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
    }

    @Override
    public void onResume() {
        super.onResume();

        serverIP = prefs.getString("server_ip", "");

        mTimer1 = new Runnable() {
            @Override
            public void run() {

                client.get("http://" + serverIP + ":8086/query", params, new JsonHttpResponseHandler(){

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        // called when response HTTP status is "200 OK"
                        processJson(response);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        // called when response HTTP status is "200 OK"
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject response) {
                        // called when response HTTP status is "200 OK"
                        Log.d("Throable", "" + t);
                        Toast.makeText( PPGActivity.this, "Error retreiving data. Make sure you are connected to internet.", Toast.LENGTH_SHORT).show();
                    }
                });

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

    private void processJson(JSONObject response) {
        DataPoint[] points = null;
        int index = 0;
        try {
            JSONObject series = response.getJSONArray("results").getJSONObject(0).getJSONArray("series").getJSONObject(0);
            JSONArray columns = series.getJSONArray("columns");
            JSONArray values = series.getJSONArray("values");

            //Columns: ["time","channel_1","channel_2","channel_3","channel_4","host","temperature"]

            int len = countChannelValues(values);
            points = new DataPoint[len];

            boolean isNull = false;

            for(int i=0; i < values.length(); i++){
                JSONArray x = values.getJSONArray(i);
                Instant instant = Instant.parse(x.getString(0));
                if(x.getString(1) == "null") {
                    continue;
                }

                DataPoint v = new DataPoint(instant.toEpochMilli(), x.getInt(1));
                points[index] = v;
                index++;
            }

            graph.getViewport().setMinX(points[0].getX());
            graph.getViewport().setMaxX(points[len-1].getX());
            graph.getViewport().setXAxisBoundsManual(true);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(PPGActivity.this, e.getMessage() + ". Make sure the device and database is running.", Toast.LENGTH_SHORT).show();
        }
        if(points != null){
            mSeries1.resetData(points);
        }
    }

    private int countChannelValues(JSONArray values) {
        int count = 0;
        for(int i=0; i < values.length(); i++) {
            try {
                if (values.getJSONArray(i).getString(1) != "null") count++;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

}


