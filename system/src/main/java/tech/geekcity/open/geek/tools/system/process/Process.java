package tech.geekcity.open.geek.tools.system.process;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.exec.*;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.inferred.freebuilder.FreeBuilder;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author ben.wangz
 */
@FreeBuilder
@JsonDeserialize(builder = Process.Builder.class)
public abstract class Process implements Runnable, Closeable {
    /**
     * Returns a new {@link Builder} with the same property values as this Process
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link Process} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends Process_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public Builder() {
            timeoutToKillInMs(ExecuteWatchdog.INFINITE_TIMEOUT);
        }

        public String toJson() throws JsonProcessingException {
            return objectMapper.writeValueAsString(build());
        }

        public Process parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, Process.class);
        }

        @Override
        public Builder timeoutToKillInMs(long timeoutToKillInMs) {
            return super.timeoutToKillInMs(
                    timeoutToKillInMs < 0 ?
                            ExecuteWatchdog.INFINITE_TIMEOUT :
                            timeoutToKillInMs);
        }
    }

    private transient boolean executed = false;
    private transient RichExecutor executor;
    private transient DefaultExecuteResultHandler resultHandler;
    private transient ExecuteWatchdog executeWatchdog;

    public abstract String executable();

    public abstract List<Argument> argumentList();

    /**
     * <p>
     * If null, the environment of the current process is used
     * </p>
     * <p>
     * to get the environment of current process, use {@link #currentProcessEnvironment()}
     * NOTE: it's recommend to add {@link #currentProcessEnvironment()} as the base environment at the beginning
     * </p>
     *
     * @return the environment for the process
     * @see Executor#execute(CommandLine, Map, ExecuteResultHandler)
     */
    public abstract Map<String, String> environment();

    /**
     * has a default value({@link ExecuteWatchdog#INFINITE_TIMEOUT}) which means no timeout at all
     * will be {@link ExecuteWatchdog#INFINITE_TIMEOUT} if set negative value to it
     *
     * @return timeout in milliseconds to kill the process if it is not terminated itself
     * @see ExecuteWatchdog#ExecuteWatchdog(long)
     */
    public abstract long timeoutToKillInMs();

    /**
     * @return the standard output stream of the process
     */
    @Nullable
    public abstract OutputStream standardOutputStream();

    /**
     * the process will use {@link #standardOutputStream()} as {@link #errorOutputStream()} if not set
     *
     * @return the error output stream of the process
     */
    @Nullable
    public abstract OutputStream errorOutputStream();

    /**
     * NOTE: a closed {@link Process} can be used again
     */
    @Override
    public void close() {
        executed = false;
        // set null for jvm to release memory
        executeWatchdog = null;
        resultHandler = null;
    }

    /**
     * start asynchronous execution
     */
    @Override
    public void run() {
        try {
            execute();
        } catch (IOException e) {
            throw new RuntimeException(
                    String.format(
                            "exception caught while running process(%s): %s",
                            this,
                            e.getMessage()),
                    e);
        }
    }


    /**
     * Causes the current thread to wait, if necessary, until the
     * process has terminated. This method returns immediately if
     * the process has already terminated. If the process has
     * not yet terminated, the calling thread will be blocked until the
     * process exits.
     *
     * @throws InterruptedException if the current thread is
     *                              {@linkplain Thread#interrupt() interrupted} by another
     *                              thread while it is waiting, then the wait is ended and
     *                              an {@link InterruptedException} is thrown.
     * @see DefaultExecuteResultHandler#waitFor()
     */
    public void waitFor() throws InterruptedException {
        checkExecuted();
        resultHandler.waitFor();
    }

    /**
     * Destroys the running process manually
     */
    public void kill() {
        checkExecuted();
        executeWatchdog.destroyProcess();
    }

    /**
     * @return {@code true} if the process was killed, otherwise {@code false}
     * @see ExecuteWatchdog#killedProcess()
     */
    public boolean killed() {
        checkExecuted();
        return executeWatchdog.killedProcess();
    }

    /**
     * @return true if a result of the execution is available
     * @see DefaultExecuteResultHandler#hasResult()
     */
    public boolean hasResult() {
        checkExecuted();
        return resultHandler.hasResult();
    }

    /**
     * @return Returns the exitValue
     * @throws IllegalStateException if the process has not exited yet
     * @see DefaultExecuteResultHandler#getExitValue()
     */
    public int exitValue() {
        checkExecuted();
        return resultHandler.getExitValue();
    }

    /**
     * @return Returns the exception.
     * @throws IllegalStateException if the process has not exited yet
     * @see DefaultExecuteResultHandler#getException()
     */
    public ExecuteException exception() {
        checkExecuted();
        return resultHandler.getException();
    }

    /**
     * @return <p>
     * the process id of this process if extracting from process succeed,
     * otherwise{@link RichExecutor#EXTRACT_FAILED_PROCESS_ID}
     * </p>
     */
    public long processId() {
        checkExecuted();
        return executor.processId();
    }

    /**
     * @return the environment map of current process(not {@code this}
     * @throws IOException the exception from {@link EnvironmentUtils#getProcEnvironment()}
     * @see EnvironmentUtils#getProcEnvironment()
     */
    public static Map<String, String> currentProcessEnvironment() throws IOException {
        return EnvironmentUtils.getProcEnvironment();
    }

    private void execute() throws IOException {
        CommandLine commandLine = new CommandLine(executable());
        argumentList().forEach(argument ->
                commandLine.addArgument(
                        argument.argument(),
                        argument.handleQuoting()));
        executor = new RichExecutor();
        OutputStream standardOutputStream = standardOutputStream();
        OutputStream errorOutputStream = errorOutputStream();
        if (null != standardOutputStream) {
            executor.setStreamHandler(
                    null == errorOutputStream ?
                            new PumpStreamHandler(standardOutputStream) :
                            new PumpStreamHandler(standardOutputStream, errorOutputStream));
        }
        executeWatchdog = new ExecuteWatchdog(timeoutToKillInMs());
        executor.setWatchdog(executeWatchdog);
        resultHandler = new DefaultExecuteResultHandler();
        executor.execute(commandLine, environment(), resultHandler);
        executed = true;
    }

    private void checkExecuted() {
        if (!executed) {
            throw new RuntimeException(
                    String.format("this process(%s) is not executed", this));
        }
    }
}
