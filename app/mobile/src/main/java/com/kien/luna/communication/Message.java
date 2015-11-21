package com.kien.luna.communication;


import android.os.Parcel;
import android.os.Parcelable;

public class Message implements Parcelable {
    private String remoteDevice;
    private String message;

    public Message() {
    }

    public Message(String remoteDevice, String message) {
        this.remoteDevice = remoteDevice;
        this.message = message;
    }

    public String getRemoteDevice() {
        return remoteDevice;
    }

    public void setRemoteDevice(String remoteDevice) {
        this.remoteDevice = remoteDevice;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Message{" +
                "remoteDevice='" + remoteDevice + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    protected Message(Parcel in) {
        remoteDevice = in.readString();
        message = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(remoteDevice);
        dest.writeString(message);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}
