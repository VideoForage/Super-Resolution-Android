package com.example.myapplication123;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.bytedeco.javacv.FrameFilter;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.text.DecimalFormat;




public class MainActivity extends AppCompatActivity {

    public Bitmap imageBitmap;
    private Button btn;
    private ImageView imageView;
    private TextView txt;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1046;

    static {
        System.loadLibrary("tensorflow_inference");
    }

    private static final String MODEL_FILE = "file:///android_asset/model_2.pb";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = (Button)findViewById(R.id.button);
        imageView = (ImageView)findViewById(R.id.img);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            btn.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA }, 0);
        }
    }

    private class AsyncTaskRunner extends AsyncTask<Bitmap, Integer, Bitmap> {

        // Runs in UI before background thread is called
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            return predict(params[0], MODEL_FILE, getAssets());
//            return results;
        }

        public Bitmap predict(Bitmap bitmap, String modelName, AssetManager assets) {

            // Import the model
            TensorFlowInferenceInterface inferenceInterface = new TensorFlowInferenceInterface(assets, modelName);

//            int INPUT_SIZE = 224;
            int INPUT_SIZE = 192;
//            String INPUT_NAME = "input_1";
//            String OUTPUT = "reshape_2/Reshape";
            String INPUT_NAME = "input_4";
            String OUTPUT = "batch_normalization_17/FusedBatchNorm_1";
            String[] OUTPUT_NAMES = {OUTPUT};
            int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
            float[] floatValues = new float[INPUT_SIZE * INPUT_SIZE * 3];

//            Bitmap bitmap = Bitmap.createScaledBitmap(pic, INPUT_SIZE, INPUT_SIZE, true);

            bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

            for (int j = 0; j < intValues.length; ++j) {
                final int val = intValues[j];

                floatValues[j * 3 + 0] = ((val >> 16) & 0xFF);
                floatValues[j * 3 + 1] = ((val >> 8) & 0xFF);
                floatValues[j * 3 + 2] = (val & 0xFF);

                floatValues[j * 3 + 2] = Color.red(val);
                floatValues[j * 3 + 1] = Color.green(val);
                floatValues[j * 3] = Color.blue(val);

            }

            inferenceInterface.feed(INPUT_NAME, floatValues,  1, INPUT_SIZE, INPUT_SIZE, 3);
            inferenceInterface.run(OUTPUT_NAMES, false);
            float[] outputs = new float[INPUT_SIZE * INPUT_SIZE * 3];
            inferenceInterface.fetch(OUTPUT, outputs);

            for (int i = 0; i < intValues.length; ++i) {
                intValues[i] =
                        0xFF000000
                                | (((int) (floatValues[i * 3] * 255)) << 16)
                                | (((int) (floatValues[i * 3 + 1] * 255)) << 8)
                                | ((int) (floatValues[i * 3 + 2] * 255));
            }
//            bitmap.setPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            bitmap.setPixels(intValues, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);

//            imageView.setImageBitmap(bitmap);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            //used to control the number of decimals places for the output probability
//            DecimalFormat df2 = new DecimalFormat(".##");

            //transfer the neural network output to an array
//            double[] results = {result.getDouble(0,0),result.getDouble(0,1),result.getDouble(0,2),
//                    result.getDouble(0,3),result.getDouble(0,4),result.getDouble(0,5),result.getDouble(0,6),
//                    result.getDouble(0,7),result.getDouble(0,8),result.getDouble(0,9),};
//            float val = arrayMaximum(result);
//            int idx = getIndexOfLargestValue(result);
//            Log.i("result :  ",idx + "  " + val);
            //find the UI tvs to display the prediction and confidence values
//            txt = (TextView)findViewById(R.id.txt);

            //display the values using helper functions defined below
//            txt.setText(idx + "  " + val);
//            out1.setText(String.valueOf(getIndexOfLargestValue(results)));
            imageView.setImageBitmap(result);
        }

        //helper class to return the largest value in the output array
        public float arrayMaximum(float[] arr) {
            float max = Float.NEGATIVE_INFINITY;
            for(float cur: arr)
                max = Math.max(max, cur);
            return max;
        }

        // helper class to find the index (and therefore numerical value) of the largest confidence score
        public int getIndexOfLargestValue( float[] array )
        {
            if ( array == null || array.length == 0 ) return -1;
            int largest = 0;
            for ( int i = 1; i < array.length; i++ )
            {if ( array[i] > array[largest] ) largest = i;            }
            return largest;
        }
    }

    //On clicking button
    public void takePicture(View view) {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                btn.setEnabled(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//        Bundle extras = data.getExtras();
//        imageBitmap = (Bitmap) extras.get("data");
        Uri photoUri = data.getData();
        try {
            // Do something with the photo based on Uri
            Bitmap bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            imageBitmap = Bitmap.createScaledBitmap(bmp, 192, 192, true);
        } catch (Exception ex){
            Log.e("Error", "Exception : ",ex);
        }
        imageView.setImageBitmap(imageBitmap);
        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(imageBitmap);
        Log.i("info", "xys");
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
