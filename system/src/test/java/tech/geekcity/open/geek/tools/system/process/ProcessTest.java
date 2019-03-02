package tech.geekcity.open.geek.tools.system.process;

import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author ben.wangz
 */
public class ProcessTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessTest.class);
    private Map<String, String> currentProcessEnvironment;

    @Before
    public void before() throws Exception {
        currentProcessEnvironment = Process.currentProcessEnvironment();
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testEnvironment() throws Exception {
        //TODO: test not designed, mock the behavior of dependencies?
    }

    @Test
    public void testTimeoutToKillInMs() throws Exception {
        long sleepMilliseconds = 6 * 1000;
        long timeoutMilliseconds = 3 * 1000;
        /**
         * test default timeout = {@link ExecuteWatchdog#INFINITE_TIMEOUT}
         */
        Process defaultTimeoutProcess = new Process.Builder()
                .putAllEnvironment(currentProcessEnvironment)
                .executable("sh")
                .addArgumentList(Argument.Builder.of("-c"))
                .addArgumentList(Argument.Builder.of(
                        String.format("sleep %s", sleepMilliseconds / 1000),
                        false))
                .build();
        defaultTimeoutProcess.run();
        Assert.assertFalse(defaultTimeoutProcess.hasResult());
        defaultTimeoutProcess.waitFor();
        Assert.assertTrue(defaultTimeoutProcess.hasResult());
        Assert.assertEquals(0, defaultTimeoutProcess.exitValue());
        Assert.assertFalse(defaultTimeoutProcess.killed());

        /**
         * test negative value to {@link ExecuteWatchdog#INFINITE_TIMEOUT}
         */
        Process negativeTimeoutProcess = defaultTimeoutProcess.toBuilder()
                .timeoutToKillInMs(-10)
                .build();
        negativeTimeoutProcess.run();
        Assert.assertFalse(negativeTimeoutProcess.hasResult());
        negativeTimeoutProcess.waitFor();
        Assert.assertTrue(negativeTimeoutProcess.hasResult());
        Assert.assertEquals(0, negativeTimeoutProcess.exitValue());
        Assert.assertFalse(negativeTimeoutProcess.killed());

        // test normal timeout process
        Process timeoutProcess = defaultTimeoutProcess.toBuilder()
                .timeoutToKillInMs(timeoutMilliseconds)
                .build();
        long timestampBeforeRun = System.currentTimeMillis();
        timeoutProcess.run();
        timeoutProcess.waitFor();
        long runningMilliseconds = System.currentTimeMillis() - timestampBeforeRun;
        Assert.assertTrue(timeoutProcess.hasResult());
        Assert.assertNotEquals(0, timeoutProcess.exitValue());
        Assert.assertTrue(timeoutProcess.killed());
        Assert.assertTrue(
                runningMilliseconds >= timeoutMilliseconds
                        && runningMilliseconds <= sleepMilliseconds);
        LOGGER.info("running timeoutProcess({}) timeout and killed: {}",
                timeoutProcess,
                ExceptionUtils.getStackTrace(timeoutProcess.exception()));
    }

    @Test
    public void testStandardOutputStream() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String signature = RandomStringUtils.randomAlphanumeric(32);
        Process process = new Process.Builder()
                .putAllEnvironment(currentProcessEnvironment)
                .executable("sh")
                .addArgumentList(Argument.Builder.of("-c"))
                .addArgumentList(Argument.Builder.of(String.format("echo %s", signature), false))
                .standardOutputStream(outputStream)
                .build();
        process.run();
        process.waitFor();
        Assert.assertEquals(0, process.exitValue());
        // echo may append '\n'
        Assert.assertEquals(signature, outputStream.toString().trim());
    }

    @Test
    public void testErrorOutputStream() throws Exception {
        ByteArrayOutputStream standardOutputStream = new ByteArrayOutputStream();

        // test use standard output stream by default
        String signature = RandomStringUtils.randomAlphanumeric(32);
        Process useStandardOutputByDefaultProcess = new Process.Builder()
                .putAllEnvironment(currentProcessEnvironment)
                .executable("sh")
                .addArgumentList(Argument.Builder.of("-c"))
                .addArgumentList(Argument.Builder.of(String.format("(>&2 echo %s)", signature), false))
                .standardOutputStream(standardOutputStream)
                .build();
        useStandardOutputByDefaultProcess.run();
        useStandardOutputByDefaultProcess.waitFor();
        Assert.assertEquals(0, useStandardOutputByDefaultProcess.exitValue());
        // echo may append '\n'
        Assert.assertEquals(signature, standardOutputStream.toString().trim());

        // test normal error output stream
        standardOutputStream.reset();
        ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();
        Process errorOutputStreamSetProcess = useStandardOutputByDefaultProcess.toBuilder()
                .errorOutputStream(errorOutputStream)
                .build();
        errorOutputStreamSetProcess.run();
        errorOutputStreamSetProcess.waitFor();
        Assert.assertEquals(0, errorOutputStreamSetProcess.exitValue());
        // echo may append '\n'
        Assert.assertEquals("", standardOutputStream.toString());
        Assert.assertEquals(signature, errorOutputStream.toString().trim());
    }

    @Test
    public void testKill() throws Exception {
        // not killed process
        Process process = new Process.Builder()
                .putAllEnvironment(currentProcessEnvironment)
                .executable("sh")
                .addArgumentList(Argument.Builder.of("-c"))
                .addArgumentList(Argument.Builder.of("sleep 6", false))
                .build();
        process.run();
        Assert.assertFalse(process.hasResult());
        process.waitFor();
        Assert.assertTrue(process.hasResult());
        Assert.assertEquals(0, process.exitValue());
        Assert.assertFalse(process.killed());

        // killed process
        Process processToKill = process.toBuilder().build();
        processToKill.run();
        Assert.assertFalse(processToKill.hasResult());
        processToKill.kill();
        // sleep one seconds to let process change it's state
        // TODO more choices?
        TimeUnit.MILLISECONDS.sleep(100);
        Assert.assertTrue(processToKill.hasResult());
        Assert.assertNotEquals(0, processToKill.exitValue());
        Assert.assertTrue(processToKill.killed());
    }

    @Test
    public void testProcessId() throws Exception {
        Process process = new Process.Builder()
                .putAllEnvironment(currentProcessEnvironment)
                .executable("sh")
                .addArgumentList(Argument.Builder.of("-c"))
                .addArgumentList(Argument.Builder.of("sleep 6", false))
                .build();
        process.run();
        Assert.assertFalse(process.hasResult());
        if (SystemUtils.IS_OS_LINUX
                || SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
            long processId = process.processId();
            LOGGER.info("the processId of process({}) is {}", process, processId);
            Assert.assertTrue(processId != RichExecutor.EXTRACT_FAILED_PROCESS_ID);
        }
        // other system may not support
        process.waitFor();
        Assert.assertTrue(process.hasResult());
        Assert.assertEquals(0, process.exitValue());
    }

    @Test
    public void testClose() throws Exception {
        Process process = new Process.Builder()
                .putAllEnvironment(currentProcessEnvironment)
                .executable("sh")
                .addArgumentList(Argument.Builder.of("-c"))
                .addArgumentList(Argument.Builder.of("sleep 6", false))
                .build();
        process.run();
        Assert.assertFalse(process.hasResult());
        process.waitFor();
        Assert.assertTrue(process.hasResult());
        Assert.assertEquals(0, process.exitValue());
        Assert.assertFalse(process.killed());
        // run the process again
        process.close();
        process.run();
        Assert.assertFalse(process.hasResult());
        process.waitFor();
        Assert.assertTrue(process.hasResult());
        Assert.assertEquals(0, process.exitValue());
        Assert.assertFalse(process.killed());
    }
}
