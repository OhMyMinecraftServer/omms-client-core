package net.zhuruoling.omms.client.system;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class FileSystemInfo { // TODO: 2022/9/10

    @SerializedName("filesystems")
    final
    List<FileSystem> fileSystemList = new ArrayList<>();

    public static String asJsonString(FileSystemInfo fileSystemInfo) {
        return new GsonBuilder().serializeNulls().create().toJson(fileSystemInfo);
    }

    public List<FileSystem> getFileSystemList() {
        return fileSystemList;
    }

    static class FileSystem {
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

        public long getFree() {
            return free;
        }

        public void setFree(long free) {
            this.free = free;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public String getVolume() {
            return volume;
        }

        public void setVolume(String volume) {
            this.volume = volume;
        }

        public String getMountPoint() {
            return mountPoint;
        }

        public void setMountPoint(String mountPoint) {
            this.mountPoint = mountPoint;
        }

        public String getFileSystemType() {
            return fileSystemType;
        }

        public void setFileSystemType(String fileSystemType) {
            this.fileSystemType = fileSystemType;
        }
    }
}
