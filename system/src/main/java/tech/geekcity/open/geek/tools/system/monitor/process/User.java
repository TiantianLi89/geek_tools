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
@JsonDeserialize(builder = User.Builder.class)
public interface User {
    /**
     * Returns a new {@link Builder} with the same property values as this User
     */
    Builder toBuilder();

    /**
     * Builder of {@link User} instances
     * auto generated builder className which cannot be modified
     */
    class Builder extends User_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public String toJson() throws JsonProcessingException {
            return objectMapper.writeValueAsString(build());
        }

        public User parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, User.class);
        }
    }

    /**
     * @return username
     * @see OSProcess#getUser()
     */
    String username();

    /**
     * @return user id
     * @see OSProcess#getUserID()
     */
    String userId();

    /**
     * @return group name
     * @see OSProcess#getGroup()
     */
    String groupName();

    /**
     * @return group id
     * @see OSProcess#getGroupID()
     */
    String groupId();
}
