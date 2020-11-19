package fr.eseo.arnou.test_position;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private int start;
    private double initialVal;

    private SensorManager mSensorManager;
    private Sensor mAccelerator;

    private SensorManager mSensorManager2;
    private Sensor mAccelerator2;

    private Thread thread;

    private String sensorGravity;

    private TextView hautBas;
    private TextView gaucheDroite;

    private JSONArray data = new JSONArray();

    private Button btnSubmit;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resetSensors();

        addListenerOnButton();

        startPlot();

    }

    private void resetSensors(){
        start = 0;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerator = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

        if(mAccelerator != null){
            mSensorManager.registerListener(this,mAccelerator,SensorManager.SENSOR_DELAY_GAME);
        }

        mSensorManager2 = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerator2 = mSensorManager2.getDefaultSensor(Sensor.TYPE_GRAVITY);

        sensorGravity = mAccelerator2.getName();

        if(mAccelerator2 != null){
            mSensorManager2.registerListener(this,mAccelerator2,SensorManager.SENSOR_DELAY_GAME);
        }

    }


    private void startPlot(){
        if(thread != null){
            thread.interrupt();
        }

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(10);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    private void addEntry(SensorEvent event){
        if(event.sensor.getName() == sensorGravity){
            float valueSensor = event.values[2];
            int finalValue = (int) (127 + (valueSensor * 127 / (-9.81)));
            hautBas = (TextView) findViewById(R.id.textView);
            hautBas.setText("bas/haut: " + String.valueOf(finalValue));
            data.put("x"+ finalValue);
        }else{
            if(start == 0){
                float[] mRotationMatrixIni = new float[9];
                SensorManager.getRotationMatrixFromVector(mRotationMatrixIni, event.values);
                final float[] orientationAnglesIni = new float[3];
                mSensorManager.getOrientation(mRotationMatrixIni, orientationAnglesIni);
                initialVal = orientationAnglesIni[0]*180/Math.PI;
                start = 1;
            }

            float[] mRotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
            final float[] orientationAngles = new float[3];
            mSensorManager.getOrientation(mRotationMatrix, orientationAngles);

            int finalValue = (int) (127 - (((orientationAngles[0]*180/Math.PI)-initialVal)*127/(-55)) );
            if(finalValue>255){
                finalValue = 255;
            }else if(finalValue<0){
                finalValue = 0;
            }

            gaucheDroite = (TextView) findViewById(R.id.textView2);
            gaucheDroite.setText("gauche/droite: " + String.valueOf(finalValue));
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        addEntry(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onPostResume() {
        super.onPostResume();
    }

    @Override
    public void onPause(){
        super.onPause();

        if(thread != null){
            thread.interrupt();
        }
        mSensorManager.unregisterListener(this);
        mSensorManager2.unregisterListener(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        mSensorManager.registerListener(this,mAccelerator,SensorManager.SENSOR_DELAY_GAME);
        mSensorManager2.registerListener(this,mAccelerator2,SensorManager.SENSOR_DELAY_GAME);
    }

    public void addListenerOnButton() {

        btnSubmit = (Button) findViewById(R.id.button);

        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                    //writeToFile();
                    resetSensors();
            }

        });
    }

    /*private void writeToFile() throws IOException {
        File path = this.getApplicationContext().getExternalFilesDir(null);
        File file = new File(path, "my-file-name.txt");
        FileOutputStream stream = new FileOutputStream(file);
        try {
            stream.write(String.valueOf(data).getBytes());
        } finally {
            stream.close();
        }
    }*/
}