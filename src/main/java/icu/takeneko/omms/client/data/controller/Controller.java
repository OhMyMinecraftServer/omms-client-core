package icu.takeneko.omms.client.data.controller;


public class Controller {
    private Controller() {
    }

    private String name;
    private String type;

    private String displayName;
    private boolean statusQueryable;

    public String getName() {
        return name;
    }
    public String getId() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isStatusQueryable() {
        return statusQueryable;
    }

    public String getDisplayName() {
        return displayName == null ? name : displayName;
    }

    @Override
    public String toString() {
        return "Controller{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", displayName='" + displayName + '\'' +
                ", statusQueryable=" + statusQueryable +
                '}';
    }
}
