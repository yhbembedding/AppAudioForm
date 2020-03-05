package com.example.audioform.Audio;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;


import com.example.audioform.R;
import com.example.audioform.Audio.SQL.RecordingDAO;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AudioFragment extends Fragment {
    private FloatingActionButton btnRecord;
    private Chronometer chronometer;
    private MediaRecorder mediaRecorder = null;

    private RecordingDAO recordingDAO;
    boolean recording = true;
    Context context;

    private String fileName, fileNameSensor;
    private String path, pathSensor;

    long start,end;

    SensorManager sensorManager;
    Sensor sensor;
    private              float[]  gravity    = new float[]{ 0, 0, 0 };
    double x,y,z;
    SharedPreferences sharedPreferences;
    PrintWriter printWriter;

    private  boolean state;


    public static AudioFragment newInstance() {
        
        Bundle args = new Bundle();

        AudioFragment fragment = new AudioFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordingDAO = new RecordingDAO(getContext());
        sharedPreferences = getContext().getSharedPreferences("pref_high_quality", Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("quality", false).commit();

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_layout, container, false);
        btnRecord = view.findViewById(R.id.btn_record);
        chronometer = view.findViewById(R.id.chronometer);

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.btn_record:
                        if(recording){
                            startRecording();
                            recording = false;
                            btnRecord.setImageResource(R.drawable.ic_stop);

                        }
                        else{
                            stopRecording();
                            recording = true;
                            btnRecord.setImageResource(R.drawable.ic_mic);
                        }
                        break;


                }
            }
        });
        state = sharedPreferences.getBoolean("quality", false);
        return view;
    }



    public void startRecording(){
        try {
            setFileNameAndPath();
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC_ELD);
            mediaRecorder.setAudioSamplingRate(48000);

            if(state == true){
                mediaRecorder.setAudioEncodingBitRate(192000);
            }
            mediaRecorder.setOutputFile(path);
            mediaRecorder.prepare();
            mediaRecorder.start();

            if(sensor != null){
                printWriter = new PrintWriter(pathSensor);
                start = System.currentTimeMillis();
                sensorManager.registerListener(sensorEventListener,sensor,10000);

               // printWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopRecording(){
        try {
            chronometer.stop();
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;

            printWriter.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        gravity    = new float[]{ 0, 0, 0 };
        String date;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        date = sdf.format(new Date());
        long length = SystemClock.elapsedRealtime() - chronometer.getBase();
        recordingDAO.addRecording(fileName, path, length, date);

    }

    public void setFileNameAndPath(){
        int count = 0;
        File file, file2;

        do{
            count++;
            String date;
            SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yy");
            date = sdf.format(new Date());
            fileName = "my_recording_" + (recordingDAO.getCount() + count)+ "in_"+ date + ".mp4";
            fileNameSensor = "my_recording_" + (recordingDAO.getCount() + count) + "in_"+ date + ".txt";
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recorder/" + fileName;
            pathSensor = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recorder/" + fileNameSensor;
            file = new File(path);
            file2 = new File(pathSensor);

        }
        while(file.exists() && !file.isDirectory() && file2.exists() && !file2.isDirectory());
    }
    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            final float alpha = 0.8f;

            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha)* event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            x = event.values[0] - gravity[0];
            y = event.values[1] - gravity[1];
            z = event.values[2] - gravity[2];
            end = System.currentTimeMillis();
            long t = end - start;
            start = System.currentTimeMillis();
            try {
                String toado =String.valueOf(t)+" "+ String.valueOf(x) + " " + String.valueOf(y) + " " + String.valueOf(z) +  "\n";
                //printWriter = new PrintWriter(pathSensor);
                printWriter.write(toado);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

}
