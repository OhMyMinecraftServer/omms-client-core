package net.zhuruoling.omms.client.controller;


public class Controller {
    private Controller(){}
    private String name;
    private String type;
    private boolean statusQueryable;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isStatusQueryable() {
        return statusQueryable;
    }

}
