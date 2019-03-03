package tech.geekcity.open.geek.tools.system.process;

import com.google.common.collect.Sets;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author ben.wangz
 */
public class RichExecutor extends DefaultExecutor {
    private enum ProcessImplementType {
        UnixProcess("java.lang.UNIXProcess"),
        WindowsProcess("java.lang.Win32Process", "java.lang.ProcessImpl"),
        Others((String[]) null);

        private Set<String> possibleProcessClassNameSet;

        ProcessImplementType(String... possibleProcessClassNameList) {
            this.possibleProcessClassNameSet = null == possibleProcessClassNameList ?
                    Sets.newHashSet() :
                    Sets.newHashSet(possibleProcessClassNameList);
        }

        public static @Nonnull
        ProcessImplementType of(@Nonnull java.lang.Process process) {
            String processClassName = process.getClass().getName();
            Optional<ProcessImplementType> processImplementTypeOption
                    = Arrays.stream(ProcessImplementType.values())
                    .filter(processImplementType ->
                            processImplementType.possibleProcessClassNameSet.contains(processClassName))
                    .findFirst();
            return processImplementTypeOption.orElse(Others);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(RichExecutor.class);
    public static final int EXTRACT_FAILED_PROCESS_ID = -1;
    private java.lang.Process process;

    @Override
    protected java.lang.Process launch(CommandLine command,
                                       Map<String, String> env,
                                       File dir)
            throws IOException {
        process = super.launch(command, env, dir);
        return process;
    }

    public long processId() {
        // TODO upgrade to java 9 and use Process.pid()
        switch (ProcessImplementType.of(process)) {
            case UnixProcess:
                Integer unixLikeProcessId = extractUnixLikeProcessId(process);
                return null == unixLikeProcessId ? EXTRACT_FAILED_PROCESS_ID : unixLikeProcessId;
            case WindowsProcess:
                Long windowsProcessId = extractWindowsProcessId(process);
                return null == windowsProcessId ? EXTRACT_FAILED_PROCESS_ID : windowsProcessId;
            case Others:
                return EXTRACT_FAILED_PROCESS_ID;
            default:
                return EXTRACT_FAILED_PROCESS_ID;
        }
    }

    /**
     * reference: https://github.com/flapdoodle-oss/de.flapdoodle.embed.process/blob/master/src/main/java/de/flapdoodle/embed/process/runtime/Processes.java
     * in unix/Linux system, {@link Integer#MAX_VALUE} is enough for a process,
     * so field {@link UNIXProcess#pid} is defined as int
     *
     * @param process the process to extract pid
     * @return the pid of the {@code process} if extract successful, otherwise null
     */
    private @Nullable
    Integer extractUnixLikeProcessId(@Nullable java.lang.Process process) {
        if (null == process) {
            return null;
        }
        Class<?> clazz = process.getClass();
        if (!clazz.getName().equals("java.lang.UNIXProcess")) {
            return null;
        }
        try {
            Field pidField = clazz.getDeclaredField("pid");
            pidField.setAccessible(true);
            return pidField.getInt(process);
        } catch (SecurityException
                | IllegalAccessException
                | IllegalArgumentException
                | NoSuchFieldException ex) {
            LOGGER.warn(
                    String.format(
                            "extractUnixLikeProcessId failed: %s",
                            ex.getMessage()),
                    ex);
            return null;
        }
    }

    /**
     * do not want to support
     *
     * @param process the process to extract pid
     * @return the pid of the {@code process} if extract successful, otherwise null
     */
    private static Long extractWindowsProcessId(java.lang.Process process) {
        LOGGER.warn("extractWindowsProcessId not supported");
        return null;
    }
}
