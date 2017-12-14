package com.rwth.medit.meditapp;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

import java.text.SimpleDateFormat;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import helper.CustomDataPoint;
import helper.CustomDataPointInterface;
import helper.CustomPointsGraphSeries;

public class MainActivity extends AppCompatActivity {


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
        annotations = new CustomPointsGraphSeries<>();

        graph.addSeries(mSeries1);
        graph.addSeries(annotations);

        annotations.setColor(Color.RED);
        annotations.setCustomShape(new CustomPointsGraphSeries.CustomShape() {
            @Override
            public void draw(Canvas canvas, Paint paint, float x, float y, CustomDataPointInterface dataPoint) {
                paint.setStrokeWidth(15);
                paint.setTextSize(20);
                canvas.drawText(dataPoint.getLabel(), x, y + 0.3f, paint);
            }
        });

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

    private void processJson(JSONObject response) {
        DataPoint[] points = null;
        CustomDataPoint[] ann = null;
        int annIndex = 0;
        try {
            JSONObject series = response.getJSONArray("results").getJSONObject(0).getJSONArray("series").getJSONObject(0);
            JSONArray columns = series.getJSONArray("columns");
            JSONArray values = series.getJSONArray("values");

            int len = values.length();
            points = new DataPoint[len];
            ann = new CustomDataPoint[countAnnotations(values)];

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
                if(x.getString(1) != "null"){
                    CustomDataPoint cp = new CustomDataPoint(instant.toEpochMilli(), x.getDouble(7), x.getString(1));
                    ann[annIndex] = cp;
                    annIndex++;
                }
                if(x.getString(2) != "null") tvheartRate.setText(x.getString(2));
                DataPoint v = new DataPoint(instant.toEpochMilli(), x.getDouble(7));
                points[i] = v;
            }

            //String s = "60 ❤ BPM";

            graph.getViewport().setMinX(points[0].getX());
            graph.getViewport().setMaxX(points[len-1].getX());
            graph.getViewport().setXAxisBoundsManual(true);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage() + ". Make sure the device and database is running.", Toast.LENGTH_SHORT).show();
        }
        if(points != null){
            mSeries1.resetData(points);
            annotations.resetData(ann);
        }
    }

    private int countAnnotations(JSONArray values) {
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


