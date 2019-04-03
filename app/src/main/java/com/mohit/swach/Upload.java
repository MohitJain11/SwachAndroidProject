package com.mohit.swach;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

public class Upload extends AppCompatActivity {

    ImageView image_capture;
    int CAMERA_PIC_REQUEST = 1;
    public Bitmap image = null;
    String encodedImage;
    Button button_upload, button_submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        button_submit = findViewById(R.id.button_submit);
        image_capture = findViewById(R.id.image_capture);
        button_upload = findViewById(R.id.button_upload);
        button_upload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
            }
        });

        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("resultImage",encodedImage);
                setResult(RESULT_OK,returnIntent);
                finish();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_PIC_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                image = (Bitmap) data.getExtras().get("data");
                image_capture.setImageBitmap(image);
                try {
                    encodedImage = encodeImage(image);
                } catch (Exception e) {
                    Log.e("Exception Image", e.getMessage());
                }
            }
        }
    }

    //encode image to base64 string
    private String encodeImage(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encImage;
    }

}
