package com.dynamsoft.mrz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        String message = intent.getStringExtra("path");
        String result = intent.getStringExtra("result");

        Bitmap bitmap = BitmapFactory.decodeFile(message);
        ImageView iv = new ImageView(this);
        iv.setImageBitmap(bitmap);
//        TextView tv = new TextView(this);
//        tv.setText(message);
        setContentView(iv);

        Toast.makeText(this, result, Toast.LENGTH_LONG)
                .show();
    }
}