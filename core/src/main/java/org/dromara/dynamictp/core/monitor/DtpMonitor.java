package org.dromara.dynamictp.core.monitor;

import org.dromara.dynamictp.common.ApplicationContextHolder;
import org.dromara.dynamictp.common.entity.ThreadPoolStats;
import org.dromara.dynamictp.common.event.AlarmCheckEvent;
import org.dromara.dynamictp.common.event.CollectEvent;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.converter.ExecutorConverter;
import org.dromara.dynamictp.core.handler.CollectorHandler;
import org.dromara.dynamictp.core.notifier.manager.AlarmManager;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.thread.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.SCHEDULE_NOTIFY_ITEMS;

/**
 * DtpMonitor related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpMonitor implements ApplicationRunner {

    /** 采集线程池状态的线程 */
    private static final ScheduledExecutorService MONITOR_EXECUTOR = new ScheduledThreadPoolExecutor(
            1, new NamedThreadFactory("dtp-monitor", true));

    private final DtpProperties dtpProperties;

    public DtpMonitor(DtpProperties dtpProperties) {
        this.dtpProperties = dtpProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        MONITOR_EXECUTOR.scheduleWithFixedDelay(this::run, 0, dtpProperties.getMonitorInterval(), TimeUnit.SECONDS);
    }

    private void run() {
        Set<String> executorNames = DtpRegistry.listAllExecutorNames();
        checkAlarm(executorNames);
        collect(executorNames);
    }

    /**
     * 采集线程池状态
     *
     * @param executorNames
     */
    private void collect(Set<String> executorNames) {
        if (!dtpProperties.isEnabledCollect()) {
            return;
        }
        executorNames.forEach(x -> {
            ExecutorWrapper wrapper = DtpRegistry.getExecutorWrapper(x);
            doCollect(ExecutorConverter.toMetrics(wrapper));
        });
        // 发布采集事件
        publishCollectEvent();
    }

    /**
     * 检查是否需要发送消息通知
     *
     * @param executorNames
     */
    private void checkAlarm(Set<String> executorNames) {
        executorNames.forEach(x -> {
            ExecutorWrapper wrapper = DtpRegistry.getExecutorWrapper(x);
            AlarmManager.doAlarmAsync(wrapper, SCHEDULE_NOTIFY_ITEMS);
        });

        // 发布消息通知检查事件
        publishAlarmCheckEvent();
    }

    /**
     * 发布事件
     */
    private void publishAlarmCheckEvent() {
        AlarmCheckEvent event = new AlarmCheckEvent(this, dtpProperties);
        ApplicationContextHolder.publishEvent(event);
    }

    /**
     * 采集线程池状态
     *
     * @param threadPoolStats
     */
    private void doCollect(ThreadPoolStats threadPoolStats) {
        try {
            CollectorHandler.getInstance().collect(threadPoolStats, dtpProperties.getCollectorTypes());
        } catch (Exception e) {
            log.error("DynamicTp monitor, metrics collect error.", e);
        }
    }

    /**
     * 发布采集事件
     */
    private void publishCollectEvent() {
        CollectEvent event = new CollectEvent(this, dtpProperties);
        ApplicationContextHolder.publishEvent(event);
    }



    public static void destroy() {
        MONITOR_EXECUTOR.shutdownNow();
    }
}
