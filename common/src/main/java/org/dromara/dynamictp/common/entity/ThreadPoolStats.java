package org.dromara.dynamictp.common.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ThreadPoolStats related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class ThreadPoolStats extends Metrics {

    /** 是否DtpExecutor线程池 */
    private boolean dynamic;

    /** 线程池名称 */
    private String poolName;
    /** 核心池大小 */
    private int corePoolSize;
    /** 最大线程池大小 */
    private int maximumPoolSize;
    /** 当前池中存在的线程总数 */
    private int poolSize;
    /** 等待执行的任务数量 */
    private int waitTaskCount;
    /** 正在执行任务的活跃线程大致总数 */
    private int activeCount;
    /** 大致任务总数 */
    private long taskCount;
    /** 已执行完成的大致任务总数 */
    private long completedTaskCount;
    /** 池中曾经同时存在的最大线程数量 */
    private int largestPoolSize;


    // 队列

    /** 队列的类型 */
    private String queueType;
    /** SynchronousQueue队列模式 */
    private boolean fair;
    /** 当前队列容量大小 */
    private int queueCapacity;
    /** 当前队列的任务数量 */
    private int queueSize;
    /** 队列剩余容量 */
    private int queueRemainingCapacity;

    // 拒绝策略

    /** 拒绝策略名称 */
    private String rejectHandlerName;
    /** 拒绝的任务数量 */
    private long rejectCount;


    /** 执行超时任务数量 */
    private long runTimeoutCount;
    /** 在队列等待超时任务数量 */
    private long queueTimeoutCount;

}
