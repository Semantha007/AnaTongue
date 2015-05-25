package com.maxx.anatounge;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by maxx on 5/21/15.
 */
public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private LinearLayout captureLL;
    private LinearLayout settingsLL;
    private LinearLayout helpLL;
    private LinearLayout aboutLL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialiseAllViews();

        captureLL.setOnClickListener(this);
        settingsLL.setOnClickListener(this);
        helpLL.setOnClickListener(this);
        aboutLL.setOnClickListener(this);
    }


    private void initialiseAllViews() {
        captureLL = (LinearLayout) findViewById(R.id.mACaptureLL);
        settingsLL = (LinearLayout) findViewById(R.id.mASettingsLL);
        helpLL = (LinearLayout) findViewById(R.id.mAHelpLL);
        aboutLL = (LinearLayout) findViewById(R.id.mAAboutLL);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mACaptureLL:
                Intent captureIntent = new Intent(getApplicationContext(), CaptureActivity.class);
                startActivity(captureIntent);
                break;
            case R.id.mASettingsLL:
                break;
            case R.id.mAHelpLL:
                Intent helpIntent = new Intent(getApplicationContext(), HelpActivity.class);
                startActivity(helpIntent);
                break;
            case R.id.mAAboutLL:
                Intent aboutIntent = new Intent(getApplicationContext(), AboutUsActivity.class);
                startActivity(aboutIntent);
                break;
        }
    }

}
