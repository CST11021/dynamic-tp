package org.dromara.dynamictp.common.entity;

import org.dromara.dynamictp.common.em.QueueTypeEnum;
import org.dromara.dynamictp.common.em.RejectedTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;
import java.util.concurrent.RejectedExecutionHandler;

/**
 * Dynamic ThreadPool main properties.
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DtpExecutorProps extends TpExecutorProps {

    /** 线程池的类型, 线程创建的时候生效，参见: com.dtp.core.support.ExecutorType */
    private String executorType;
    /** 任务队列的类型, 参见：{@link QueueTypeEnum} */
    private String queueType = QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE.getName();
    /** 任务队列大小 */
    private int queueCapacity = 1024;
    /** 是否启用公平策略, for SynchronousQueue */
    private boolean fair = false;
    /** MemorySafeLBQ的最大可用内存, 单位：M */
    private int maxFreeMemory = 16;
    /** 拒绝处理策略, see {@link RejectedTypeEnum} */
    private String rejectedHandlerType = RejectedTypeEnum.ABORT_POLICY.getName();
    /** 当设置allowCoreThreadTimeOut(true)时，线程池中corePoolSize线程空闲时间达到keepAliveTime也将关闭 */
    private boolean allowCoreThreadTimeOut = false;
    /** 线程池中的线程名前缀 */
    private String threadNamePrefix = "dtp";
    /** 线程池关闭时，是否等待所有任务都执行完后在关闭，包括正常运行的任务和队列中等待的任务 */
    private boolean waitForTasksToCompleteOnShutdown = false;
    /** 线程池关闭时，阻塞的最大秒数，以便在容器的其余部分继续关闭之前等待剩余任务完成其执行 */
    private int awaitTerminationSeconds = 0;
    /** 线程池在完成初始化之后,默认情况下,线程池中不会有任何线程,线程池会等有任务来的时候再去创建线程，当preStartAllCoreThreads为true时，线程池启动的时候就创建相应的线程数量，例如：Tomcat在启动线程池的时候就已经初始化了所有核心线程，线程池启动 */
    private boolean preStartAllCoreThreads = false;

    /** 可以使用{@link RejectedExecutionHandler}队列拒绝处理实现进行增强处理 */
    private boolean rejectEnhanced = true;

    /** 任务执行的超时时间, 单位: ms*/
    private long runTimeout = 0;
    /** 任务在队里中等待指定的超时时间, 单位: ms */
    private long queueTimeout = 0;

    /** Task wrapper names. */
    private Set<String> taskWrapperNames;

    /**
     * 检查核心线程池配置参数是否有效
     *
     * @return boolean return true means params is inValid
     */
    public boolean coreParamIsInValid() {
        return this.getCorePoolSize() < 0
                || this.getMaximumPoolSize() <= 0
                || this.getMaximumPoolSize() < this.getCorePoolSize()
                || this.getKeepAliveTime() < 0;
    }

}
