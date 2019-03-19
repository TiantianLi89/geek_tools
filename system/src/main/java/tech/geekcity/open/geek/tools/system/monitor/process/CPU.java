package tech.geekcity.open.geek.tools.system.monitor.process;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.inferred.freebuilder.FreeBuilder;
import oshi.software.os.OSProcess;

import java.io.IOException;

/**
 * @author ben.wangz
 */
@FreeBuilder
@JsonDeserialize(builder = CPU.Builder.class)
public interface CPU {
    /**
     * Returns a new {@link Builder} with the same property values as this CPU
     */
    Builder toBuilder();

    /**
     * Builder of {@link CPU} instances
     * auto generated builder className which cannot be modified
     */
    class Builder extends CPU_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public String toJson() throws JsonProcessingException {
            return objectMapper.writeValueAsString(build());
        }

        public CPU parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, CPU.class);
        }
    }

    /**
     * @return the number of milliseconds the process has executed in kernel/system mode
     * @see OSProcess#getKernelTime()
     */
    long kernelTime();

    /**
     * @return the number of milliseconds the process has executed in user mode
     * @see OSProcess#getUserTime()
     */
    long userTime();

    /**
     * @return the number of milliseconds since the process started
     * @see OSProcess#getUpTime()
     */
    long upTime();

    /**
     * @return the proportion of up time that the process was executing in kernel or user mode
     * @see OSProcess#calculateCpuPercent()
     */
    double cpuPercent();
}
