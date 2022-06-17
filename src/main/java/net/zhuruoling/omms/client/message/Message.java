package net.zhuruoling.omms.client.message;

import com.google.gson.annotations.SerializedName;

import java.awt.image.TileObserver;
import java.util.Arrays;

public class Message {
    public Message(String msg, String[] load){
        this.msg = msg;
        this.load = load;
    }
    @SerializedName("msg")
    String msg;
    @SerializedName("load")
    String[] load;

    public String getMsg() {
        return msg;
    }

    public String[] getLoad() {
        return load;
    }

    public void setLoad(String[] load) {
        this.load = load;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "Message{" +
                "msg='" + msg + '\'' +
                ", load=" + Arrays.toString(load) +
                '}';
    }
}
