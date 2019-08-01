package com.jhfactory.aospimagepick.sample;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SampleFragmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_fragment);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.activity_name_fragment);
        setSupportActionBar(toolbar);
    }
}
