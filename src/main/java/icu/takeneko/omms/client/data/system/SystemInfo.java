package icu.takeneko.omms.client.data.system;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class SystemInfo {

    String osName;
    String osVersion;
    String osArch;
    FileSystemInfo fileSystemInfo;
    MemoryInfo memoryInfo;
    NetworkInfo networkInfo;
    ProcessorInfo processorInfo;
    StorageInfo storageInfo;
}
