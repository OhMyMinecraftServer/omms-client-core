package icu.takeneko.omms.client.data.system;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemoryInfo {
    private long memoryTotal;
    private long memoryUsed;
    private long swapTotal;
    private long swapUsed;

}
