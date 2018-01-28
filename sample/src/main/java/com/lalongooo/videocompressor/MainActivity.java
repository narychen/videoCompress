package com.lalongooo.videocompressor;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.yovenny.videocompress.MediaController;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class RealPathUtils {

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri uri, String type){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { type.equals("image") ? MediaStore.Images.Media.DATA : MediaStore.Video.Media.DATA };

        // where id is equal to
        String sel = type.equals("image") ? MediaStore.Images.Media._ID : MediaStore.Video.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(
            type.equals("image") ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI : MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            column, sel, new String[]{ id }, null
        );

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri, String type) {
        String[] proj = { type.equals("image") ? MediaStore.Images.Media.DATA : MediaStore.Video.Media.DATA};
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if(cursor != null){
            int column_index =
                    cursor.getColumnIndexOrThrow(type.equals("image") ? MediaStore.Images.Media.DATA : MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri, String type){
        String[] proj = { type.equals("image") ? MediaStore.Images.Media.DATA : MediaStore.Video.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index
                = cursor.getColumnIndexOrThrow(type.equals("image") ? MediaStore.Images.Media.DATA : MediaStore.Video.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}

public class MainActivity extends Activity {
    private static final int RESULT_CODE_COMPRESS_VIDEO = 3;
    private static final String TAG = "MainActivity";
    private EditText editText;
    private ProgressBar progressBar;

    public static final String APP_DIR = "VideoCompressor";

    public static final String COMPRESSED_VIDEOS_DIR = "/Compressed Videos/";

    public static final String TEMP_DIR = "/Temp/";


    public static void try2CreateCompressDir() {
        File f = new File(Environment.getExternalStorageDirectory(), File.separator + APP_DIR);
        f.mkdirs();
        f = new File(Environment.getExternalStorageDirectory(), File.separator + APP_DIR + COMPRESSED_VIDEOS_DIR);
        f.mkdirs();
        f = new File(Environment.getExternalStorageDirectory(), File.separator + APP_DIR + TEMP_DIR);
        f.mkdirs();
    }

    public static String getMimeType(Context context, Uri uri) {
        String mime;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
//            final MimeTypeMap mime = MimeTypeMap.getSingleton();
//            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
            mime = context.getContentResolver().getType(uri);
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
//            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
            mime = Uri.fromFile(new File(uri.getPath())).toString();
        }

        String type = mime.split("/")[0];
        return type;

//        return extension;
    }


    String getPath(Uri uri, String type) {
        String path;
        if (Build.VERSION.SDK_INT < 11)
            path = RealPathUtils.getRealPathFromURI_BelowAPI11(MainActivity.this, uri, type);

            // SDK >= 11 && SDK < 19
        else if (Build.VERSION.SDK_INT < 19)
            path = RealPathUtils.getRealPathFromURI_API11to18(MainActivity.this, uri, type);

            // SDK > 19 (Android 4.4)
        else
            path = RealPathUtils.getRealPathFromURI_API19(MainActivity.this, uri, type);
        return path;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        editText = (EditText) findViewById(R.id.editText);

        findViewById(R.id.btnSelectVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                startActivityForResult(intent, RESULT_CODE_COMPRESS_VIDEO);
            }
        });


        String filepath = "/storage/emulated/0/DCIM/Camera/VID_20180127_175817.mp4";
        File file = new File(filepath);
        boolean b = file.exists();
        if (b) editText.setText(filepath);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if (resCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (reqCode == RESULT_CODE_COMPRESS_VIDEO) {
                if (uri != null) {
//                    String[] proj = { MediaStore.Images.Media.DATA };
//                    Cursor cursor = getContentResolver().query(uri,  proj, null, null, null);
//                    cursor.moveToFirst();
//                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//
//                    String path = cursor.getString(column_index);
                    String type = getMimeType(MainActivity.this, uri);
                    String path = getPath(uri, type);
                    editText.setText(path);
//                    Cursor cursor = getContentResolver().query(uri, null, null, null, null, null);
//                    try {
//                        if (cursor != null && cursor.moveToFirst()) {
//                            String path=cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
//                            editText.setText(path);
//                        }else {
//                            editText.setText(uri.getPath());
//                        }
//                    } finally {
//                        if (cursor != null) {
//                            cursor.close();
//                        }
//                    }
                }
            }
        }
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void compress(View v) {
        try2CreateCompressDir();
        String outPath=Environment.getExternalStorageDirectory()
                        + File.separator
                        + APP_DIR
                        + COMPRESSED_VIDEOS_DIR
                        +"VIDEO_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".mp4";
        new VideoCompressor().execute(editText.getText().toString(),outPath);
    }

    class VideoCompressor extends AsyncTask<String, Void, Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            Log.d(TAG,"Start video compression");
        }

        @Override
        protected Boolean doInBackground(String... params) {
            return MediaController.getInstance().convertVideo(params[0],params[1]);
        }

        @Override
        protected void onPostExecute(Boolean compressed) {
            super.onPostExecute(compressed);
            progressBar.setVisibility(View.GONE);
            if(compressed){
                Log.d(TAG,"Compression successfully!");
            }
        }
    }

}