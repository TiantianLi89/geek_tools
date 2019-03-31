package tech.geekcity.open.geek.tools.system.monitor;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.software.os.OperatingSystem;

import java.util.stream.Stream;

/**
 * @author ben.wangz
 */
public class OperatingSystemMonitorTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(OperatingSystemMonitorTest.class);

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testPlatformEnum() throws Exception {
        OperatingSystemMonitor operatingSystemMonitor = OperatingSystemMonitor.Builder.defaultInstance();
        LOGGER.info("platform: {}", operatingSystemMonitor.platformEnum());
        LOGGER.info("family: {}", operatingSystemMonitor.osFamily());
        LOGGER.info("version: {}", operatingSystemMonitor.versionJson());
        LOGGER.info("manufacturer: {}", operatingSystemMonitor.manufacturer());
        Assert.assertTrue(operatingSystemMonitor.supported());
        Assert.assertTrue(Stream.of(32L, 64L).anyMatch(bits -> bits == operatingSystemMonitor.osBits()));
        LOGGER.info("thread count: {}", operatingSystemMonitor.threadCount());
        Assert.assertTrue(operatingSystemMonitor.threadCount() > 0);
        LOGGER.info("process count: {}", operatingSystemMonitor.processCount());
        Assert.assertTrue(operatingSystemMonitor.processCount() > 0);

        Assert.assertEquals(
                operatingSystemMonitor.processMonitorList(
                        10, null, false)
                        .size(),
                operatingSystemMonitor.processMonitorList(
                        10, null)
                        .size());
        Assert.assertEquals(
                operatingSystemMonitor.processMonitorList(
                        0, OperatingSystem.ProcessSort.NAME, true)
                        .size(),
                operatingSystemMonitor.processMonitorList(
                        0, OperatingSystem.ProcessSort.CPU)
                        .size());
        Assert.assertEquals(
                operatingSystemMonitor.processMonitorList(
                        0, OperatingSystem.ProcessSort.NAME, true)
                        .size(),
                operatingSystemMonitor.processMonitorList(
                        -8, OperatingSystem.ProcessSort.PID)
                        .size());
        Assert.assertEquals(
                operatingSystemMonitor.processMonitorList(
                        -3, OperatingSystem.ProcessSort.MEMORY, false)
                        .size(),
                operatingSystemMonitor.processMonitorList(
                        -5, OperatingSystem.ProcessSort.MEMORY)
                        .size());
    }
}
