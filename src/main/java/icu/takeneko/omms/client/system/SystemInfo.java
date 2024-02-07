package icu.takeneko.omms.client.system;

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

    public FileSystemInfo getFileSystemInfo() {
        return fileSystemInfo;
    }

    public void setFileSystemInfo(FileSystemInfo fileSystemInfo) {
        this.fileSystemInfo = fileSystemInfo;
    }

    public MemoryInfo getMemoryInfo() {
        return memoryInfo;
    }

    public void setMemoryInfo(MemoryInfo memoryInfo) {
        this.memoryInfo = memoryInfo;
    }

    public NetworkInfo getNetworkInfo() {
        return networkInfo;
    }

    public void setNetworkInfo(NetworkInfo networkInfo) {
        this.networkInfo = networkInfo;
    }

    public ProcessorInfo getProcessorInfo() {
        return processorInfo;
    }

    public void setProcessorInfo(ProcessorInfo processorInfo) {
        this.processorInfo = processorInfo;
    }

    public StorageInfo getStorageInfo() {
        return storageInfo;
    }

    public void setStorageInfo(StorageInfo storageInfo) {
        this.storageInfo = storageInfo;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getOsArch() {
        return osArch;
    }

    public void setOsArch(String osArch) {
        this.osArch = osArch;
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
