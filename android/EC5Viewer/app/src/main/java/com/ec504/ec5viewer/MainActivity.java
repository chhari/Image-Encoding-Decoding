package com.ec504.ec5viewer;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import static android.R.id.message;
import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 0;
    public static final String FILE_PATH = "com.ec504.ec5viewer.FILE_PATH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void showFileChooser(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");

        try {
            startActivityForResult(Intent.createChooser(intent, "Select File to View"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startPlaybackActivity(String filepath) {
        Intent intent = new Intent(this, PlaybackActivity.class);
        intent.putExtra(FILE_PATH, filepath);
        startActivity(intent);
    }

    // Get the Uri of the selected file and convert it into the Path
    // Then
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    try {

                        Uri uri = data.getData();
                        String path = FilePathFinder.getPath(this, uri);
                        startPlaybackActivity(path);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Bad Result Code: " + resultCode);
                }
                break;
            default:
                System.out.println("Bad Request Code: " + requestCode);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}