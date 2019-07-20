package com.example.bthvi.mycloassloaderapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.bthvi.mycloassloaderapplication.xxx.BugClass;

public class MainActivity extends AppCompatActivity {


    static {
        System.loadLibrary("native-lib");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BugClass bug = new BugClass();
                bug.test();
            }
        });
        findViewById(R.id.fix).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FixDexManager dexManager = new FixDexManager(MainActivity.this);
                dexManager.isGoingToFix();
            }
        });
    }
}
