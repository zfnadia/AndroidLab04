package com.nadiaferdoush.lab4cse491;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


public class MainActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void manageEmployeeData(View v) {
        Intent intent = new Intent(this, JsonEmployeeActivity.class);
        startActivity(intent);
    }

    public void goToShowJsonData(View view) {
        Intent intent = new Intent(this, ShowJsonDataActivity.class);
        startActivity(intent);
    }

}
