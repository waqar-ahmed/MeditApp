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
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
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

public class MISingleActivity extends AppCompatActivity {


    private Handler mHandler = new Handler();
    private Runnable mTimer1;
    private LineGraphSeries<DataPoint> mSeries1;
    private AsyncHttpClient client;
    private RequestParams params;
    DataPoint[] dataPoints = null;
    GraphView graph;
    Map<String, String> mapparams;
    TextView tvheartRate;
    SharedPreferences prefs;
    private String serverIP = "";
    private int channel = 1;
    GridLabelRenderer glr;
    String title = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ppgsingle);
        AndroidThreeTen.init(this);
        graph = (GraphView) findViewById(R.id.graph);

        //Get the bundle
        Bundle bundle = getIntent().getExtras();
        channel = bundle.getInt("channel", 1);
        title = bundle.getString("title", "MI");

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        params = new RequestParams();
        params.put("db", "medit");
        params.put("q", "select MI, MI_RAW, IR_AVG, RED_AVG from mi where time >= now() - 20s");
        params.put("epochs", "ms");


        client = new AsyncHttpClient();

        client.setTimeout(20000);

        mSeries1 = new LineGraphSeries<>();

        graph.addSeries(mSeries1);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(MISingleActivity.this, simpleDateFormat));
        graph.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space
        //graph.getGridLabelRenderer().setLabelVerticalWidth(7);

        glr = graph.getGridLabelRenderer();
        glr.setPadding(80); // should allow for 3 digits to fit on screen

        mSeries1.setTitle(title);

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

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
                        Log.d("header", headers[0].toString());
                        Log.d("JSON Data", response.toString());
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject response) {
                        // called when response HTTP status is "200 OK"
                        Log.d("Throable", "" + t);
                        Toast.makeText(MISingleActivity.this, "Error retreiving data. Make sure you are connected to internet.", Toast.LENGTH_SHORT).show();
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

    private void processJson(JSONObject response) {
        DataPoint[] points = null;
        int index = 0;

        try {
            JSONObject series = response.getJSONArray("results").getJSONObject(0).getJSONArray("series").getJSONObject(0);
            JSONArray columns = series.getJSONArray("columns");
            JSONArray values = series.getJSONArray("values");

            //int len = countChannelValues(values);
            int len = values.length();
            points = new DataPoint[len];

            for(int i=0; i < values.length(); i++){
                JSONArray x = values.getJSONArray(i);

                Instant instant = Instant.parse(x.getString(0));
                DataPoint v = new DataPoint(instant.toEpochMilli(), x.isNull(channel) ? 0.0 : x.getDouble(channel));
                points[index] = v;
                index++;
            }

            if(points.length == 0) return;
            graph.getViewport().setMinX(points[0].getX());
            graph.getViewport().setMaxX(points[len-1].getX());
            graph.getViewport().setXAxisBoundsManual(true);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MISingleActivity.this, e.getMessage() + ". Make sure the device and database is running.", Toast.LENGTH_SHORT).show();
        }
        if(points != null){
            mSeries1.resetData(points);
        }
    }

    private int countChannelValues(JSONArray values) {
        int count = 0;
        for(int i=0; i < values.length(); i++) {
            try {
                if (values.getJSONArray(i).getString(channel) != "null") count++;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return count;
    }
}


