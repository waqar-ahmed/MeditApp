package com.rwth.medit.meditapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import heartview.miring.RingView;

public class RingViewActivity extends AppCompatActivity {

    RingView mringView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ring_view);

        mringView =(RingView) findViewById(R.id.ringView);
        findViewById(R.id.startHeartBeatTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mringView.startAnim();
            }
        });
    }
}
