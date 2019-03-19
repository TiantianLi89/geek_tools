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
@JsonDeserialize(builder = Memory.Builder.class)
public interface Memory {
    /**
     * Returns a new {@link Builder} with the same property values as this Memory
     */
    Builder toBuilder();

    /**
     * Builder of {@link Memory} instances
     * auto generated builder className which cannot be modified
     */
    class Builder extends Memory_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public String toJson() throws JsonProcessingException {
            return objectMapper.writeValueAsString(build());
        }

        public Memory parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, Memory.class);
        }
    }

    /**
     * @return VSZ
     * @see OSProcess#getVirtualSize()
     */
    long virtualSize();

    /**
     * @return RSS
     * @see OSProcess#getResidentSetSize()
     */
    long residentSetSize();
}
