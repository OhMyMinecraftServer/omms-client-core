package icu.takeneko.omms.client.data.system;

import lombok.Getter;

@Getter
public class SystemInfo {

    String osName;
    String osVersion;
    String osArch;
    FileSystemInfo fileSystemInfo;
    MemoryInfo memoryInfo;
    NetworkInfo networkInfo;
    ProcessorInfo processorInfo;
    StorageInfo storageInfo;

    public SystemInfo(String osName, String osVersion, String osArch, FileSystemInfo fileSystemInfo, MemoryInfo memoryInfo, NetworkInfo networkInfo, ProcessorInfo processorInfo, StorageInfo storageInfo) {
        this.osName = osName;
        this.osVersion = osVersion;
        this.osArch = osArch;
        this.fileSystemInfo = fileSystemInfo;
        this.memoryInfo = memoryInfo;
        this.networkInfo = networkInfo;
        this.processorInfo = processorInfo;
        this.storageInfo = storageInfo;
    }


    @Override
    public String toString() {
        return "SystemInfo{" +
                "osName='" + osName + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", osArch='" + osArch + '\'' +
                ", fileSystemInfo=" + fileSystemInfo +
                ", memoryInfo=" + memoryInfo +
                ", networkInfo=" + networkInfo +
                ", processorInfo=" + processorInfo +
                ", storageInfo=" + storageInfo +
                '}';
    }
}
