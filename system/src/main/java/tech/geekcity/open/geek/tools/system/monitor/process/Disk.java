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
@JsonDeserialize(builder = Disk.Builder.class)
public interface Disk {
    /**
     * Returns a new {@link Builder} with the same property values as this Disk
     */
    Builder toBuilder();

    /**
     * Builder of {@link Disk} instances
     * auto generated builder className which cannot be modified
     */
    class Builder extends Disk_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public String toJson() throws JsonProcessingException {
            return objectMapper.writeValueAsString(build());
        }

        public Disk parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, Disk.class);
        }
    }

    /**
     * @return bytes read
     * @see OSProcess#getBytesRead()
     */
    long bytesRead();

    /**
     * @return bytes written
     * @see OSProcess#getBytesWritten()
     */
    long bytesWritten();
}
