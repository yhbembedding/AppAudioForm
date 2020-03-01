package com.example.audioform.Audio;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.example.audioform.R;
import com.example.audioform.SQL.RecordingDAO;
import com.example.audioform.databinding.AudioFragmentBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AudioFragment extends Fragment {
    private FloatingActionButton btnRecord;
    private Chronometer chronometer;
    private MediaRecorder mediaRecorder = null;

    private RecordingDAO recordingDAO;
    boolean recording = true;
    Context context;

    private String fileName;
    private String path;
    SharedPreferences sharedPreferences;

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
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            if(state == true){
                mediaRecorder.setAudioEncodingBitRate(192000);
                mediaRecorder.setAudioSamplingRate(44100);
            }
            mediaRecorder.setOutputFile(path);
            mediaRecorder.prepare();
            mediaRecorder.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopRecording(){
        chronometer.stop();
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;

        String date;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        date = sdf.format(new Date());
        long length = SystemClock.elapsedRealtime() - chronometer.getBase();
        recordingDAO.addRecording(fileName, path, length, date);
    }

    public void setFileNameAndPath(){
        int count = 0;
        File file;
        do{
            count++;
            fileName = "my_recording_" + (recordingDAO.getCount() + count) + ".mp4";
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recorder/" + fileName;
            file = new File(path);
        }
        while(file.exists() && !file.isDirectory());
    }

}
