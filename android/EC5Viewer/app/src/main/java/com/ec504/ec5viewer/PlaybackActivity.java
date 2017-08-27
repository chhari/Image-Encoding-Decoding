package com.ec504.ec5viewer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlaybackActivity extends AppCompatActivity {

    private List<Bitmap> imgBitmaps;
    private final Handler handler = new Handler();
    private int imgCounter = 0;

    private ImageView imgView;

    private Paint colorTransform;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String filePath = getIntent().getStringExtra(MainActivity.FILE_PATH);

        ImageProcessor processor = new ImageProcessor();
        processor.execute(filePath);

        imgView = (ImageView) findViewById(R.id.imgView);

        colorTransform = setupColorTransform();
    }

    protected void finishedProcessing(List<Bitmap> imgBitmaps) {
        this.imgBitmaps = imgBitmaps;
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void beginPlayback(View v) {
        String imsString = ((EditText) findViewById(R.id.ims)).getText().toString();
        int ims = Integer.valueOf(imsString);

        if (ims < 10) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please enter an Images per Second rate of at least 10.");
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();;
                }
            });
            builder.show();
            return;
        }

        closeKeyboard();

        Bitmap bm = imgBitmaps.get(0);
        imgView.setImageBitmap(bm);

        imgCounter++;
        nextImage(ims);
    }

    private Paint setupColorTransform() {
        float[] colorTransform = {
                2f, 0, 0, 0, 0,
                0, 2f, 0, 0, 0,
                0, 0, 2f, 0, 0,
                0, 0, 0, 2f, 0};
        ColorMatrix cMatrix = new ColorMatrix();
        cMatrix.setSaturation(0);
        cMatrix.set(colorTransform);

        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(cMatrix);
        Paint paint = new Paint();
        paint.setColorFilter(colorFilter);

        return paint;
    }

    public void nextImage(final int ims) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (imgCounter < imgBitmaps.size()) {
                    Bitmap bm = imgBitmaps.get(imgCounter++);
                    imgView.setImageBitmap(bm);

                    nextImage(ims);
                } else {
                    imgCounter = 0;
                }

            }
        }, 1000/ims);
    }

    private class ImageProcessor extends AsyncTask<String, Void, List<Bitmap>> {

        ProgressDialog processingDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            processingDialog = new ProgressDialog(PlaybackActivity.this);
            processingDialog.setIndeterminate(true);
            processingDialog.setMessage("Loading File...");
            processingDialog.show();
        }

        @Override
        protected List<Bitmap> doInBackground(String... paths) {
            File file = new File(paths[0]);
            int size = (int) file.length();
            byte[] bytes = new byte[size];



            try {
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                buf.read(bytes, 0, bytes.length);
                buf.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            int startingIndex = 0;
            int endingIndex = 0;
            List<Bitmap> imgBitmaps = new ArrayList<>();
            int count = 0;

            // Iterate through bytes to separate images
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] == ((byte) 0xff)) {
                    // Start of image (could be thumbnail within image)
                    if (bytes[i+1] == (byte) 0xd8) {
                        count++;
                        if (count == 1) startingIndex = i;

                    } else if (bytes[i+1] == (byte) 0xd9) {
                        count--;    // End of image/thumbnail

                        // If you've reached the end of the image, decode it and store the bitmap
                        if (count == 0) {
                            endingIndex = i+2;
                            Bitmap bm = BitmapFactory.decodeByteArray(bytes, startingIndex, endingIndex-startingIndex);
                            bm = bm.copy(bm.getConfig(), true);

                            Canvas canvas = new Canvas(bm);
                            canvas.drawBitmap(bm, 0, 0, colorTransform);
                            imgBitmaps.add(bm);
                        }
                    }
                }
            }

            return imgBitmaps;
        }

        @Override
        protected void onPostExecute(List<Bitmap> bitmaps) {
            super.onPostExecute(bitmaps);
            processingDialog.dismiss();
            finishedProcessing(bitmaps);
        }

    }

}
