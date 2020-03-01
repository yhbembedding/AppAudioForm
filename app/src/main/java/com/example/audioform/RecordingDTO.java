package com.example.audioform;

public class RecordingDTO {
    private int _id;
    private String name;
    private String path;
    private long length;
    private String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public RecordingDTO() {

    }

    public RecordingDTO(String name, String path, long length, String date) {
        this.name = name;
        this.path = path;
        this.length = length;
        this.date = date;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
}
