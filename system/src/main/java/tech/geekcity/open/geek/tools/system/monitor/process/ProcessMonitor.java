package tech.geekcity.open.geek.tools.system.monitor.process;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.inferred.freebuilder.FreeBuilder;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import tech.geekcity.open.geek.tools.system.monitor.OperatingSystemMonitor;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ben.wangz
 */
@FreeBuilder
@JsonDeserialize(builder = ProcessMonitor.Builder.class)
public abstract class ProcessMonitor {
    /**
     * Returns a new {@link Builder} with the same property values as this ProcessMonitor
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link ProcessMonitor} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends ProcessMonitor_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public Builder() {
            operatingSystem(OperatingSystemMonitor.Builder
                    .defaultInstance()
                    .operatingSystem());
        }

        public String toJson() throws JsonProcessingException {
            return objectMapper.writeValueAsString(build());
        }

        public ProcessMonitor parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, ProcessMonitor.class);
        }

        public static ProcessMonitor of() {
            return of(ProcessMonitor.currentProcessId());
        }

        public static ProcessMonitor of(int pid) {
            return new ProcessMonitor.Builder()
                    .process(OperatingSystemMonitor.Builder
                            .defaultInstance()
                            .operatingSystem()
                            .getProcess(pid))
                    .build();
        }
    }

    /**
     * <p>
     * has a default value constructed from
     * {@link OperatingSystemMonitor.Builder#defaultInstance()}
     * .{@link OperatingSystemMonitor#operatingSystem()}
     * </p>
     *
     * @return an instance of {@link OperatingSystem}
     */
    public abstract OperatingSystem operatingSystem();

    public abstract OSProcess process();

    /**
     * @return name of the process
     * @see OSProcess#getName()
     */
    public String name() {
        return process().getName();
    }

    /**
     * @return full path of the executing process
     * @see OSProcess#getPath()
     */
    public String path() {
        return process().getPath();
    }

    /**
     * @return the process command line
     * @see OSProcess#getCommandLine()
     */
    public String commandLine() {
        return process().getCommandLine();
    }

    /**
     * @return current working directory
     * @see OSProcess#getCurrentWorkingDirectory()
     */
    public String workingDirectory() {
        return process().getCurrentWorkingDirectory();
    }

    /**
     * @return pid
     * @see OSProcess#getProcessID()
     */
    public int processId() {
        return process().getProcessID();
    }

    /**
     * @return priority of the process
     * @see OSProcess#getPriority()
     */
    public int priority() {
        return process().getPriority();
    }

    /**
     * @return number of threads in this process
     * @see OSProcess#getThreadCount()
     */
    public int threadCount() {
        return process().getThreadCount();
    }

    /**
     * @return number of open files(include network connections) or -1 (not supported)
     * @see OSProcess#getOpenFiles()
     */
    public long numberOfOpenFiles() {
        return process().getOpenFiles();
    }

    /**
     * @return timestamp of the process start time
     */
    public long startTime() {
        return process().getStartTime();
    }

    public User user() {
        OSProcess process = process();
        return new User.Builder()
                .username(process.getUser())
                .userId(process.getUserID())
                .groupName(process.getGroup())
                .groupId(process.getGroupID())
                .build();
    }

    public Memory memory() {
        OSProcess process = process();
        return new Memory.Builder()
                .virtualSize(process.getVirtualSize())
                .residentSetSize(process.getResidentSetSize())
                .build();
    }

    public CPU cpu() {
        OSProcess process = process();
        return new CPU.Builder()
                .kernelTime(process.getKernelTime())
                .userTime(process.getUserTime())
                .upTime(process.getUpTime())
                .cpuPercent(process.calculateCpuPercent())
                .build();
    }

    public Disk disk() {
        OSProcess process = process();
        return new Disk.Builder()
                .bytesRead(process.getBytesRead())
                .bytesWritten(process.getBytesWritten())
                .build();
    }

    /**
     * @return parent ProcessMonitor
     */
    public ProcessMonitor parent() {
        return ProcessMonitor.Builder
                .of(process().getParentProcessID());
    }

    /**
     * @param limit a number to limit return list size
     * @param sort  specify the sort behavio
     * @return a list of {@link ProcessMonitor} which are the children of {@code this} process
     */
    public List<ProcessMonitor> children(int limit, @Nullable OperatingSystem.ProcessSort sort) {
        OSProcess[] childProcesses = operatingSystem().getChildProcesses(processId(), limit, sort);
        return Arrays
                .stream(childProcesses)
                .map(child -> ProcessMonitor.Builder.of(child.getProcessID()))
                .collect(Collectors.toList());
    }

    protected static int currentProcessId() {
        return OperatingSystemMonitor.Builder
                .defaultInstance()
                .operatingSystem()
                .getProcessId();
    }
}
