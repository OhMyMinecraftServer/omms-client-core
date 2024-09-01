package icu.takeneko.omms.client.data.system;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class StorageInfo {
    @SerializedName("storages")
    List<Storage> storageList = new ArrayList<>();

    public static class Storage {
        final String name;
        final String model;
        final long size;

        public Storage(String name, String model, long size) {
            this.name = name;
            this.model = model;
            this.size = size;
        }
    }

    public static String asJsonString(StorageInfo storageInfo) {
        return new GsonBuilder().serializeNulls().create().toJson(storageInfo);
    }
}
