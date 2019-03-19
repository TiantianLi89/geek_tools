package tech.geekcity.open.geek.tools.system.monitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.inferred.freebuilder.FreeBuilder;
import oshi.PlatformEnum;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import tech.geekcity.open.geek.tools.system.monitor.process.ProcessMonitor;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ben.wangz
 */
@FreeBuilder
@JsonDeserialize(builder = OperatingSystemMonitor.Builder.class)
public abstract class OperatingSystemMonitor {
    /**
     * Returns a new {@link Builder} with the same property values as this OperatingSystemMonitor
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link OperatingSystemMonitor} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends OperatingSystemMonitor_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();
        private static final OperatingSystemMonitor SINGLETON_INSTANCE = defaultBuilder().build();

        public String toJson() throws JsonProcessingException {
            return objectMapper.writeValueAsString(build());
        }

        public OperatingSystemMonitor parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, OperatingSystemMonitor.class);
        }

        public static OperatingSystemMonitor defaultInstance() {
            return SINGLETON_INSTANCE;
        }

        private static Builder defaultBuilder() {
            SystemInfo systemInfo = new SystemInfo();
            return new Builder()
                    .operatingSystem(systemInfo.getOperatingSystem())
                    .hardware(systemInfo.getHardware());
        }
    }

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public abstract OperatingSystem operatingSystem();

    public abstract HardwareAbstractionLayer hardware();

    public PlatformEnum platformEnum() {
        return SystemInfo.getCurrentPlatformEnum();
    }

    public int osBits() {
        return operatingSystem().getBitness();
    }

    public String osFamily() {
        return operatingSystem().getFamily();
    }

    public String versionJson() throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(operatingSystem().getVersion());
    }

    public String manufacturer() {
        return operatingSystem().getManufacturer();
    }

    /**
     * @return count of running threads
     */
    public int threadCount() {
        return operatingSystem().getThreadCount();
    }

    /**
     * @return count of running processes
     */
    public int processCount() {
        return operatingSystem().getProcessCount();
    }


    public List<ProcessMonitor> processMonitorList(int limit, OperatingSystem.ProcessSort sort) {
        return processMonitorList(limit, sort, false);
    }

    /**
     * @param limit      a number to limit the result size, 0 to set no limitation and negative values is equal to 0
     * @param sort       specify the sort behavior
     * @param slowFields skip OSProcess fields that are slow to retrieve if false, else include all fields
     * @return a list of OSProcess
     * @see OperatingSystem#getProcesses(int, OperatingSystem.ProcessSort, boolean)
     */
    public List<ProcessMonitor> processMonitorList(
            int limit,
            @Nullable OperatingSystem.ProcessSort sort,
            boolean slowFields) {
        return Arrays.stream(
                operatingSystem().getProcesses(limit, sort, slowFields))
                .map(process
                        -> new ProcessMonitor.Builder()
                        .process(process)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * we only support mac osx and linux
     *
     * @return {@code true} if supported by the monitor, otherwise false
     */
    public boolean supported() {
        switch (platformEnum()) {
            case MACOSX:
            case LINUX:
                return true;
            default:
                return false;
        }
    }
}
