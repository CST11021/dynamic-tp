package org.dromara.dynamictp.core.monitor.collector;

import org.dromara.dynamictp.common.entity.ThreadPoolStats;

/**
 * MetricsCollector related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public interface MetricsCollector {

    /**
     * 采集线程池指标
     *
     * @param poolStats ThreadPoolStats instance
     */
    void collect(ThreadPoolStats poolStats);

    /**
     * 采集类型
     *
     * @return collector type
     */
    String type();

    /**
     * 是否支持该采集类型
     *
     * @param type collector type
     * @return true if the collector supports this type, else false
     */
    boolean support(String type);
}
