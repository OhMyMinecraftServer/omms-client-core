package icu.takeneko.omms.client.data.controller;


import lombok.Getter;

@Getter
public class Controller {
    private String name;
    private String type;

    private String displayName;
    private boolean statusQueryable;

    public Controller(String name, String type, String displayName, boolean statusQueryable) {
        this.name = name;
        this.type = type;
        this.displayName = displayName;
        this.statusQueryable = statusQueryable;
    }

    public String getId() {
        return name;
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
