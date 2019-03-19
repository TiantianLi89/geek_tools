package tech.geekcity.open.geek.tools.system.monitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.geekcity.open.geek.tools.system.monitor.process.ProcessMonitor;

/**
 * @author ben.wangz
 */
public class ProcessMonitorTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessMonitorTest.class);

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void test() {
        // TODO
        ProcessMonitor processMonitor = ProcessMonitor.Builder.of();
        LOGGER.info("name: {}", processMonitor.name());
        LOGGER.info("path: {}", processMonitor.path());
        LOGGER.info("commandLine: {}", processMonitor.commandLine());
        LOGGER.info("workingDirectory: {}", processMonitor.workingDirectory());
        LOGGER.info("processId: {}", processMonitor.processId());
        LOGGER.info("priority: {}", processMonitor.priority());
        LOGGER.info("threadCount: {}", processMonitor.threadCount());
        LOGGER.info("numberOfOpenFiles: {}", processMonitor.numberOfOpenFiles());
        LOGGER.info("startTime: {}", processMonitor.startTime());
        LOGGER.info("user: {}", processMonitor.user());
        LOGGER.info("memory: {}", processMonitor.memory());
        LOGGER.info("cpu: {}", processMonitor.cpu());
        LOGGER.info("disk: {}", processMonitor.disk());
        LOGGER.info("parent: {}", processMonitor.parent());
        LOGGER.info("children: {}", processMonitor.children(10, null));
    }
}
