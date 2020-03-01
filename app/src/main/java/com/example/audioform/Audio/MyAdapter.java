package com.example.audioform.Audio;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audioform.R;
import com.example.audioform.RecordingDTO;
import com.example.audioform.SQL.RecordingDAO;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private Context context;
    private RecordingDAO recordingDAO;
    private RecordingDTO item;
    public MyAdapter(Context context){
        this.context = context;
        recordingDAO = new RecordingDAO(context);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        View cardView;
        TextView txtDate;
        TextView txtLength;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img);
            textView = itemView.findViewById(R.id.txtName);
            cardView = itemView.findViewById(R.id.card_view);
            txtDate = itemView.findViewById(R.id.txt_date);
            txtLength = itemView.findViewById(R.id.length);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.row_item, parent, false);

        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        item = recordingDAO.getItemAt(position);
        holder.textView.setText(item.getName());
        holder.txtDate.setText(item.getDate());
        holder.imageView.setImageResource(R.drawable.ic_item_list);

        DateFormat dateFormat = new SimpleDateFormat("mm:ss");
        Date date = new Date(item.getLength());
        holder.txtLength.setText(dateFormat.format(date));


        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String item_name[] = {"Rename", "Delete", "Delete All"};
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Options");
                builder.setItems(item_name, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:

                                break;
                            case 1:
                                deleteFile(holder.getAdapterPosition());
                                break;
                            case 2:
                                deleteAll();
                                break;
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return recordingDAO.getCount();
    }

    public void deleteFile(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete File?");
        builder.setMessage("Are you sure you want to delete this file?");
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RecordingDTO item1 = recordingDAO.getItemAt(position);
                File file = new File(item1.getPath());
                file.delete();
                recordingDAO.deleteItem(item1.get_id());
                notifyItemRemoved(position);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void deleteAll(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete All?");
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                recordingDAO.deleteAllItem();
                deleteAllFile();
                notifyDataSetChanged();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void deleteAllFile(){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recorder/";
        File file = new File(path);
        File files[] = file.listFiles();
        for(File file1: files){
            file1.delete();
        }
    }

    public void rename(int position, String name){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recorder/" + name;
        File file = new File(path);
        if (file.exists() && !file.isDirectory()){
            Toast.makeText(context, "File exists", Toast.LENGTH_SHORT).show();
        }
        else{

            RecordingDTO recordingDTO = recordingDAO.getItemAt(position);
            File oldFile = new File(recordingDTO.getPath());
            oldFile.renameTo(file);
            recordingDAO.renameItem(recordingDTO.get_id(), name, path);
            notifyItemChanged(position);
        }
    }
}
