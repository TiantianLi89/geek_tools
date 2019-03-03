package tech.geekcity.open.geek.tools.system.process;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.inferred.freebuilder.FreeBuilder;

import java.io.IOException;

/**
 * @author ben.wangz
 */
@FreeBuilder
@JsonDeserialize(builder = Argument.Builder.class)
public abstract class Argument {
    /**
     * Returns a new {@link Builder} with the same property values as this Argument
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link Argument} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends Argument_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public Builder() {
            handleQuoting(true);
        }

        public String toJson() throws JsonProcessingException {
            return objectMapper.writeValueAsString(build());
        }

        public Argument parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, Argument.class);
        }

        public static Argument of(String argument) {
            return new Builder()
                    .argument(argument)
                    .build();
        }

        public static Argument of(String argument, boolean handleQuoting) {
            return new Builder()
                    .argument(argument)
                    .handleQuoting(handleQuoting)
                    .build();
        }
    }

    /**
     * @return {@code true} the argument string
     * @see org.apache.commons.exec.CommandLine#addArgument(String, boolean)
     */
    public abstract String argument();

    /**
     * has a default value {@code true}
     *
     * @return {@code true} if handling quoting, otherwise false
     * @see org.apache.commons.exec.CommandLine#addArgument(String, boolean)
     */
    public abstract boolean handleQuoting();
}
