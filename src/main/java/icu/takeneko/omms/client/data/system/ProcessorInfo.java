package icu.takeneko.omms.client.data.system;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProcessorInfo {
    private int physicalCPUCount;
    private int logicalProcessorCount;
    private String processorName;
    private double cpuLoadAvg;
    private String processorId;
    private double cpuTemp;

}
