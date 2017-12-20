package com.rwth.medit.meditapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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

public class PPGActivity extends AppCompatActivity {


    private Handler mHandler = new Handler();
    private Runnable mTimer1;
    private LineGraphSeries<DataPoint> mSeries1, mSeries2, mSeries3, mSeries4;
    private AsyncHttpClient client;
    private RequestParams params;
    GraphView ppgGraph1, ppgGraph2, ppgGraph3, ppgGraph4;
    Map<String, String> mapparams;
    TextView tvheartRate;
    SharedPreferences prefs;
    private String serverIP = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ppg);
        AndroidThreeTen.init(this);

        ppgGraph1 = (GraphView) findViewById(R.id.ppg_graph_1);
        ppgGraph2 = (GraphView) findViewById(R.id.ppg_graph_2);
        ppgGraph3 = (GraphView) findViewById(R.id.ppg_graph_3);
        ppgGraph4 = (GraphView) findViewById(R.id.ppg_graph_4);



        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        params = new RequestParams();
        params.put("db", "medit");
        params.put("q", "select * from ppg where time >= now() - 5s");
        params.put("epochs", "ms");


        client = new AsyncHttpClient();

        client.setTimeout(20000);

        mSeries1 = new LineGraphSeries<>();
        mSeries2 = new LineGraphSeries<>();
        mSeries3 = new LineGraphSeries<>();
        mSeries4 = new LineGraphSeries<>();

        mSeries1.setTitle("Channel 1");
        mSeries2.setTitle("Channel 2");
        mSeries3.setTitle("Channel 3");
        mSeries4.setTitle("Channel 4");

        ppgGraph1.getLegendRenderer().setVisible(true);
        ppgGraph1.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        ppgGraph2.getLegendRenderer().setVisible(true);
        ppgGraph2.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        ppgGraph3.getLegendRenderer().setVisible(true);
        ppgGraph3.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        ppgGraph4.getLegendRenderer().setVisible(true);
        ppgGraph4.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        ppgGraph1.addSeries(mSeries1);
        ppgGraph2.addSeries(mSeries2);
        ppgGraph3.addSeries(mSeries3);
        ppgGraph4.addSeries(mSeries4);

        int padding = 60;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

        // set date label formatter
        ppgGraph1.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(PPGActivity.this, simpleDateFormat));
        ppgGraph1.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space

        GridLabelRenderer glr1 = ppgGraph1.getGridLabelRenderer();
        glr1.setPadding(padding); // should allow for 3 digits to fit on screen

        ppgGraph2.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(PPGActivity.this, simpleDateFormat));
        ppgGraph2.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space

        GridLabelRenderer glr2 = ppgGraph2.getGridLabelRenderer();
        glr2.setPadding(padding); // should allow for 3 digits to fit on screen

        ppgGraph3.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(PPGActivity.this, simpleDateFormat));
        ppgGraph3.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space

        GridLabelRenderer glr3 = ppgGraph3.getGridLabelRenderer();
        glr3.setPadding(padding); // should allow for 3 digits to fit on screen

        ppgGraph4.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(PPGActivity.this, simpleDateFormat));
        ppgGraph4.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space

        GridLabelRenderer glr4 = ppgGraph4.getGridLabelRenderer();
        glr4.setPadding(padding); // should allow for 3 digits to fit on screen


        ppgGraph1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // it was the 1st button
                Intent i = new Intent(PPGActivity.this, PPGSingleActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("channel", 1);
                i.putExtras(bundle);
                startActivity(i);
            }
        });
        ppgGraph2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // it was the 1st button
                Intent i = new Intent(PPGActivity.this, PPGSingleActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("channel", 2);
                i.putExtras(bundle);
                startActivity(i);
            }
        });

        ppgGraph3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // it was the 1st button
                Intent i = new Intent(PPGActivity.this, PPGSingleActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("channel", 3);
                i.putExtras(bundle);
                startActivity(i);
            }
        });

        ppgGraph4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // it was the 1st button
                Intent i = new Intent(PPGActivity.this, PPGSingleActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("channel", 4);
                i.putExtras(bundle);
                startActivity(i);
            }
        });


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
        DataPoint[] points1 = null;
        DataPoint[] points2 = null;
        DataPoint[] points3 = null;
        DataPoint[] points4 = null;
        int index = 0;
        try {
            JSONObject series = response.getJSONArray("results").getJSONObject(0).getJSONArray("series").getJSONObject(0);
            JSONArray columns = series.getJSONArray("columns");
            JSONArray values = series.getJSONArray("values");

            //Columns: ["time","channel_1","channel_2","channel_3","channel_4","host","temperature"]

            int len = countChannelValues(values);

            points1 = new DataPoint[len];
            points2 = new DataPoint[len];
            points3 = new DataPoint[len];
            points4 = new DataPoint[len];

            for(int i=0; i < values.length(); i++){
                JSONArray x = values.getJSONArray(i);
                Instant instant = Instant.parse(x.getString(0));

                if(x.getString(1) == "null") {
                    continue;
                }

                DataPoint v = new DataPoint(instant.toEpochMilli(), x.getLong(1));
                points1[index] = v;

                v = new DataPoint(instant.toEpochMilli(), x.getLong(2)/1000000000);
                points2[index] = v;

                v = new DataPoint(instant.toEpochMilli(), x.getLong(3)/1000);
                points3[index] = v;

                v = new DataPoint(instant.toEpochMilli(), x.getLong(4));
                points4[index] = v;
                index++;
            }

            if(points1.length == 0) return;

            ppgGraph1.getViewport().setMinX(points1[0].getX());
            ppgGraph1.getViewport().setMaxX(points1[len-1].getX());
            ppgGraph1.getViewport().setXAxisBoundsManual(true);


            ppgGraph2.getViewport().setMinX(points2[0].getX());
            ppgGraph2.getViewport().setMaxX(points2[len-1].getX());
            ppgGraph2.getViewport().setXAxisBoundsManual(true);

            ppgGraph3.getViewport().setMinX(points3[0].getX());
            ppgGraph3.getViewport().setMaxX(points3[len-1].getX());
            ppgGraph3.getViewport().setXAxisBoundsManual(true);

            ppgGraph4.getViewport().setMinX(points4[0].getX());
            ppgGraph4.getViewport().setMaxX(points4[len-1].getX());
            ppgGraph4.getViewport().setXAxisBoundsManual(true);


        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(PPGActivity.this, e.getMessage() + ". Make sure the device and database is running.", Toast.LENGTH_SHORT).show();
        }
        if(points1 != null){
            mSeries1.resetData(points1);
            mSeries2.resetData(points2);
            mSeries3.resetData(points3);
            mSeries4.resetData(points4);
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


