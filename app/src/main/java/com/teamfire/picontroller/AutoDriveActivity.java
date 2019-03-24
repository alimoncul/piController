package com.teamfire.picontroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AutoDriveActivity extends AppCompatActivity {

    Button btn_manualDrive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autodrive);

        btn_manualDrive = findViewById(R.id.btn_manualDrive);

        btn_manualDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_OK, new Intent());
                finish();
            }
        });
    }
}
