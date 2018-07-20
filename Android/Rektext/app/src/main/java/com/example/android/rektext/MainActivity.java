package com.example.android.rektext;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    Bitmap takenImage;
    TextView resultText;
    private List<Classifier> mClassifiers = new ArrayList<>();
    private static final int PIXEL_WIDTH = 32;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCamera = findViewById(R.id.btnCamera);
        imageView = findViewById(R.id.imageView);
        resultText = findViewById(R.id.result);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,0);
            }
        });

        loadModel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            takenImage = Bitmap.createScaledBitmap(bitmap,32, 32, true);
            takenImage = toGrayscale(takenImage);
            imageView.setImageBitmap(bitmap);

            int x = takenImage.getWidth();
            int y = takenImage.getHeight();
            int[] pixels = new int[x * y];
            takenImage.getPixels(pixels, 0, x, 0, 0, x, y);

            float[] retPixels = new float[pixels.length];
            for (int i = 0; i < pixels.length; ++i) {
                // Set 0 for white and 255 for black pixel
                int pix = pixels[i];
                int b = pix & 0xff;
                retPixels[i] = (float)((0xff - b)/255.0);
            }
            String text = "";
            //for each classifier in our array
            for (Classifier classifier : mClassifiers) {
                //perform classification on the image
                final Classification res = classifier.recognize(retPixels);
                //if it can't classify, output a question mark
                if (res.getLabel() == null) {
                    text.concat(classifier.name() + ": ?\n");
                } else {
                    //else output its name
                    text = (classifier.name() +  " classifies this as " + res.getLabel() + " with probability " + res.getConf());
                }
            }
            resultText.setText(text);

        }catch (Exception e){


        }
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    private void loadModel(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mClassifiers.add(
                            TensorFlowClassifier.create(getAssets(), "TensorFlow",
                                    "opt_svhn_single_greyscale.pb", "labels.txt", PIXEL_WIDTH,
                                    "x", "out", true));
                } catch (final Exception e) {
                    //if they aren't found, throw an error!
                    throw new RuntimeException("Error initializing classifiers!", e);
                }
            }
        }).start();
    }

}

