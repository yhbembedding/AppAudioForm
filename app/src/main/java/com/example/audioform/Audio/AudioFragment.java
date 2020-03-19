package com.example.audioform.Audio;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.audioform.Audio.SQL.RecordingDAO;
import com.example.audioform.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;

public class AudioFragment extends Fragment {
    private CheckBox checkedTest;
    private ScrollView scrollLog;
    private TextView tvLog;
    private FloatingActionButton btnRecord;
    private Chronometer chronometer;
    private MediaRecorder mediaRecorder = null;

    private RecordingDAO recordingDAO;
    boolean recording = false;

    private String fileName, fileNameSensor,fileNameInformation;
    private String pathAudio, pathSensor,pathInfor;
    private String log = "";

    long start, end;

    SensorManager sensorManager;
    Sensor sensor;
    private float[] gravity = new float[]{0, 0, 0};
    private double x, y, z;
    SharedPreferences sharedPreferences = null;
    PrintWriter printWriter,printWriter2;

    Timer timer;

    Runnable loopRunnable;

    EditText eHT,eTuoi,eNghe,eDC,eChuandoan;

    CheckBox c1,c2;

    private boolean state;

    private Thread appThread;

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
        sharedPreferences.edit().putBoolean("quality", false).apply();

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_layout, container, false);
        checkedTest = view.findViewById(R.id.checked_test);
        btnRecord = view.findViewById(R.id.btn_record);
        chronometer = view.findViewById(R.id.chronometer);
        eHT = view.findViewById(R.id.edHovaten);
        eTuoi = view.findViewById(R.id.edTuoi);
        eNghe = view.findViewById(R.id.edNghe);
        eChuandoan = view.findViewById(R.id.edchuandoan);
        eDC = view.findViewById(R.id.edDC);
        c1 = view.findViewById(R.id.cbnam);
        c2 = view.findViewById(R.id.cbNu);
        timer = new Timer();
        checkedTest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                checkedTest.setText(checked ? "Timer to 5 secs" : "Timer to 30 mins");
                Toast.makeText(getContext(), "Stopped! Press Start again", Toast.LENGTH_LONG).show();
                if (recording) {
                    stopRecording();
                }
            }
        });
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.btn_record) {
                    String ten = eHT.getText().toString();
                    String tuoi  = eTuoi.getText().toString();
                    String Nghe = eNghe.getText().toString();
                    String DC = eDC.getText().toString();
                    String CD = eChuandoan.getText().toString();
                    if(TextUtils.isEmpty(ten)){
                        eHT.setError("Chưa nhập Họ và tên");
                    }
                    if(TextUtils.isEmpty(tuoi)){
                        eTuoi.setError("Chưa nhập Tuổi");
                    }
                    if(TextUtils.isEmpty(Nghe)){
                        eNghe.setError("Chưa nhập Nghề");
                    }
                    if(TextUtils.isEmpty(DC)){
                        eDC.setError("Chưa nhập Địa chỉ");
                    }
                    if(TextUtils.isEmpty(CD)){
                        eDC.setError("Chưa nhập chuẩn đoán");
                    }
                    if (!recording && eHT.getText().toString().length()!=0) {
                        startRecording();
                        loopRunnable = new Runnable() {
                            @Override
                            public void run() {
                                Log.d("scheduled", "run");
                                stopRecording();
                                btnRecord.performClick();
                            }
                        };
                        btnRecord.postDelayed(loopRunnable, checkedTest.isChecked() ? 5000 : 30 * 60 * 1000);
                    } else {
                        stopRecording();
                    }
                }
            }
        });
        state = sharedPreferences.getBoolean("quality", false);
        return view;
    }



    @Override
    public void onDestroy() {
        if (recording) {
            stopRecording();
        }
        super.onDestroy();
    }

    private void startRecording() {
        setFileNameAndPath();
        chronometer.post(new Runnable() {
            @Override
            public void run() {
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
            }
        });
        appThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mediaRecorder = new MediaRecorder();
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC_ELD);
                    mediaRecorder.setAudioSamplingRate(48000);

                    if (state) {
                        mediaRecorder.setAudioEncodingBitRate(192000);
                    }
                    mediaRecorder.setOutputFile(pathAudio);
                    mediaRecorder.prepare();
                    mediaRecorder.start();

                    if (sensor != null) {
                        printWriter = new PrintWriter(pathSensor);
                        printWriter2 = new PrintWriter(pathInfor);
                        printWriter2.write(eHT.getText().toString()+"\n");
                        printWriter2.write(eTuoi.getText().toString()+"\n");
                        if(c1.isChecked()) {
                            printWriter2.write(c1.getText().toString() + "\n");
                        }
                        else {
                            printWriter2.write(c2.getText().toString()+"\n");
                        }
                        printWriter2.write(eNghe.getText().toString()+"\n");
                        printWriter2.write(eDC.getText().toString()+"\n");
                        printWriter2.write(eChuandoan.getText().toString()+"\n");
                        printWriter2.close();
                        start = System.currentTimeMillis();
                        sensorManager.registerListener(sensorEventListener, sensor, 10000);

                    }
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
        });
        appThread.start();

        recording = true;
        btnRecord.setImageResource(R.drawable.ic_stop);
    }

    private void stopRecording() {
        try {
            btnRecord.getHandler().removeCallbacks(loopRunnable);
            chronometer.stop();
            mediaRecorder.stop();
            mediaRecorder.release();
            printWriter.close();
            sensorManager.unregisterListener(sensorEventListener);
            appThread.interrupt();

            gravity = new float[]{0, 0, 0};
            String date;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            date = sdf.format(new Date());
            long length = SystemClock.elapsedRealtime() - chronometer.getBase();
            recordingDAO.addRecording(fileName, pathAudio, length, date);

            recording = false;
            btnRecord.setImageResource(R.drawable.ic_mic);

            Toast.makeText(getContext(), "Saved to: " + pathAudio, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getContext(), "Can not stop, please restart!", Toast.LENGTH_LONG).show();
        }
    }
    private void createFolderRecorder() {
        String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recorder";
        File fileFolder = new File(folder);
        if (!fileFolder.isDirectory() || !fileFolder.exists()) {
            boolean folderCreated = fileFolder.mkdirs();
            if (!folderCreated) {
                Toast.makeText(getContext(), "Can not create folder recorded", Toast.LENGTH_LONG).show();

            }
        }
    }
    private void createFolderIfNeeded() {
        String date;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-YYYY", Locale.US);
        date = sdf.format(new Date());
        String ten = eHT.getText().toString();
        String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recorder/"+ ten+date;
        File fileFolder = new File(folder);
        if (!fileFolder.isDirectory() || !fileFolder.exists()) {
            boolean folderCreated = fileFolder.mkdirs();
            if (!folderCreated) {
                Toast.makeText(getContext(), "Can not create folder recorded", Toast.LENGTH_LONG).show();

            }
        }
    }

    /**
     * create audio file and sensor file to storage
     */
    private void setFileNameAndPath() {
        createFolderRecorder();
        createFolderIfNeeded();
        File fileAudio, fileSensor,fileInformation;
        String date;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-YYYY", Locale.US);
        date = sdf.format(new Date());
        String ten = eHT.getText().toString();
        String dt = ten+date;
        fileName = "rc_" +ten+ (recordingDAO.getCount() + 1) + "_t" + date + ".mp4";
        fileNameSensor = "rc_" +ten+ (recordingDAO.getCount() + 1) + "_t" + date + ".txt";
        fileNameInformation = "Information_"+ten + "_t" + date + ".txt";
        pathAudio = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recorder/"+"/" +dt+ "/"+ fileName;
        pathSensor = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recorder/"+"/" +dt+ "/" + fileNameSensor;
        pathInfor = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recorder/"+"/" +dt+ "/" +fileNameInformation;
        fileAudio = new File(pathAudio);
        fileSensor = new File(pathSensor);
        fileInformation = new File(pathInfor);
        try {
            if (fileAudio.exists()) {
                boolean deleted = fileAudio.delete();

            }
            boolean createdAudioFile = fileAudio.createNewFile();

            if (fileSensor.exists()) {
                boolean deleted = fileSensor.delete();

            }
            boolean createdSensorFile = fileSensor.createNewFile();
            if (fileInformation.exists()) {
                boolean deleted = fileInformation.delete();

            }
            boolean createdInfor= fileInformation.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            final float alpha = 0.8f;

            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            x = event.values[0] - gravity[0];
            y = event.values[1] - gravity[1];
            z = event.values[2] - gravity[2];
            end = System.currentTimeMillis();
            start = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
            String date = sdf.format(new Date());
            try {
                String toado = date + " " + x + " " + y + " " + z + "\n";
                printWriter.write(toado);
//                AudioFragment.this.setLog(toado);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

}
