package net.zhuruoling.omms.client.request;

import com.google.gson.annotations.SerializedName;

public class Request {
    public Request(String cmd, String[] load){
        this.cmd = cmd;
        this.load = load;
    }
    @SerializedName("cmd")
    String cmd = "";
    @SerializedName("load")
    String[] load;

    public String getCmd() {
        return cmd;
    }

    public String[] getLoad() {
        return load;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public void setLoad(String[] load) {
        this.load = load;
    }

}
