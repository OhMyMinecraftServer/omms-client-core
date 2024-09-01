package icu.takeneko.omms.client.data.system;


import lombok.Getter;

@Getter
public class MemoryInfo {
    private long memoryTotal;
    private long memoryUsed;
    private long swapTotal;
    private long swapUsed;

}
