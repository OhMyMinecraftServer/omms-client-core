package net.zhuruoling.omms.client.system;

public class SystemInfo {
    FileSystemInfo fileSystemInfo;
    MemoryInfo memoryInfo;
    NetworkInfo networkInfo;
    ProcessorInfo processorInfo;
    StorageInfo storageInfo;

    public SystemInfo(FileSystemInfo fileSystemInfo, MemoryInfo memoryInfo, NetworkInfo networkInfo, ProcessorInfo processorInfo, StorageInfo storageInfo) {
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
}
