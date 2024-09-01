package icu.takeneko.omms.client.data.system;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class FileSystemInfo {

    @SerializedName("filesystems")
    final
    List<FileSystem> fileSystemList = new ArrayList<>();

    public static String asJsonString(FileSystemInfo fileSystemInfo) {
        return new GsonBuilder().serializeNulls().create().toJson(fileSystemInfo);
    }

    @Setter
    public static class FileSystem {
        long free;
        long total;
        String volume;
        String mountPoint;
        String fileSystemType;

        public FileSystem(long free, long total, String volume, String mountPoint, String fileSystemType) {
            this.free = free;
            this.total = total;
            this.volume = volume;
            this.mountPoint = mountPoint;
            this.fileSystemType = fileSystemType;
        }

    }
}
