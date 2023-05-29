package org.dromara.dynamictp.core.thread;

import org.dromara.dynamictp.common.queue.VariableLinkedBlockingQueue;
import org.dromara.dynamictp.core.support.selector.ExecutorSelector;
import org.dromara.dynamictp.core.support.selector.HashedExecutorSelector;
import org.dromara.dynamictp.core.support.task.Ordered;
import org.dromara.dynamictp.core.support.task.runnable.DtpRunnable;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * {@link OrderedDtpExecutor}可以保证交付的任务按照key和任务提交顺序执行。
 * 它适用于通过并行处理提高吞吐量并且任务按一定顺序运行的场景。
 *
 * @author yanhom
 * @since 1.1.3
 */
@Slf4j
public class OrderedDtpExecutor extends DtpExecutor {

    private final ExecutorSelector selector = new HashedExecutorSelector();

    private final List<Executor> childExecutors = Lists.newArrayList();

    public OrderedDtpExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), new AbortPolicy());
    }
    public OrderedDtpExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                threadFactory, new AbortPolicy());
    }
    public OrderedDtpExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                Executors.defaultThreadFactory(), handler);
    }
    public OrderedDtpExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        for (int i = 0; i < corePoolSize; i++) {
            ChildExecutor childExecutor = new ChildExecutor(workQueue.size() + workQueue.remainingCapacity());
            childExecutors.add(childExecutor);
        }
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException();
        }
        if (command instanceof Ordered) {
            doOrderedExecute(command, ((Ordered) command).hashKey());
        } else {
            doUnorderedExecute(command);
        }
    }

    public void execute(Runnable command, Object hashKey) {
        if (Objects.nonNull(hashKey)) {
            doOrderedExecute(command, hashKey);
        } else {
            doUnorderedExecute(command);
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        Object hashKey = task instanceof Ordered ? ((Ordered) task).hashKey() : null;
        RunnableFuture<T> futureTask = newTaskFor(task);
        execute(futureTask, hashKey);
        return futureTask;
    }

    public <T> Future<T> submit(Callable<T> task, Object hashKey) {
        if (task == null) {
            throw new NullPointerException();
        }
        RunnableFuture<T> futureTask = newTaskFor(task);
        execute(futureTask, hashKey);
        return futureTask;
    }

    private void doOrderedExecute(Runnable command, Object hashKey) {
        Executor executor = selector.select(childExecutors, hashKey);
        executor.execute(command);
    }

    private void doUnorderedExecute(Runnable command) {
        super.execute(command);
    }

    @Override
    public long getCompletedTaskCount() {
        long count = 0;
        for (Executor executor : childExecutors) {
            count += ((ChildExecutor) executor).getCompletedTaskCount();
        }
        return super.getCompletedTaskCount() + count;
    }

    @Override
    public long getTaskCount() {
        long count = 0;
        for (Executor executor : childExecutors) {
            count += ((ChildExecutor) executor).getTaskCount();
        }
        return super.getTaskCount() + count;
    }

    @Override
    public long getRejectedTaskCount() {
        long count = 0;
        for (Executor executor : childExecutors) {
            count += ((ChildExecutor) executor).getRejectedTaskCount();
        }
        return super.getRejectedTaskCount() + count;
    }

    @Override
    public void onRefreshQueueCapacity(int capacity) {
        for (Executor executor : childExecutors) {
            ChildExecutor childExecutor = (ChildExecutor) executor;
            if (childExecutor.getTaskQueue() instanceof VariableLinkedBlockingQueue) {
                ((VariableLinkedBlockingQueue<Runnable>) childExecutor.getTaskQueue()).setCapacity(capacity);
            }
        }
    }

    protected DtpRunnable getEnhancedTask(Runnable command) {
        DtpRunnable dtpRunnable = (DtpRunnable) wrapTasks(command);
        dtpRunnable.startQueueTimeoutTask(this);
        return dtpRunnable;
    }

    /**
     * 任务执行前被调用
     *
     * @param t
     * @param r
     */
    void onBeforeExecute(Thread t, Runnable r) {
        beforeExecute(t, r);
    }

    /**
     * 任务执行完成后被调用
     *
     * @param r
     * @param t
     */
    void onAfterExecute(Runnable r, Throwable t) {
        afterExecute(r, t);
    }

    private final class ChildExecutor implements Executor, Runnable {

        private final BlockingQueue<Runnable> taskQueue;

        private final LongAdder completedTaskCount = new LongAdder();

        private final LongAdder rejectedTaskCount = new LongAdder();

        private boolean running;

        ChildExecutor(int queueSize) {
            if (queueSize <= 0) {
                taskQueue = new SynchronousQueue<>();
                return;
            }
            taskQueue = new VariableLinkedBlockingQueue<>(queueSize);
        }

        @Override
        public void execute(Runnable command) {
            boolean start = false;
            synchronized (this) {
                try {
                    if (!taskQueue.add(getEnhancedTask(command))) {
                        rejectedTaskCount.increment();
                        throw new RejectedExecutionException("Task " + command.toString() + " rejected from " + this);
                    }
                } catch (IllegalStateException ex) {
                    rejectedTaskCount.increment();
                    throw ex;
                }

                if (!running) {
                    running = true;
                    start = true;
                }
            }
            if (start) {
                doUnorderedExecute(this);
            }
        }

        @Override
        public void run() {
            Thread thread = Thread.currentThread();
            Runnable task;
            while ((task = getTask()) != null) {
                onBeforeExecute(thread, task);
                Throwable thrown = null;
                try {
                    task.run();
                } catch (RuntimeException x) {
                    thrown = x;
                    throw x;
                } finally {
                    onAfterExecute(task, thrown);
                    completedTaskCount.increment();
                }
            }
        }

        /**
         * 从任务队列中poll一个任务
         *
         * @return
         */
        private synchronized Runnable getTask() {
            Runnable task = taskQueue.poll();
            if (task == null) {
                running = false;
            }
            return task;
        }

        /**
         * 获取任务队列
         *
         * @return
         */
        public BlockingQueue<Runnable> getTaskQueue() {
            return taskQueue;
        }

        /**
         * 获取任务数量：包括已执行完成的任务和当前队列中的任务
         *
         * @return
         */
        public long getTaskCount() {
            return completedTaskCount.sum() + taskQueue.size();
        }

        /**
         * 获取指定完成的任务数量
         *
         * @return
         */
        public long getCompletedTaskCount() {
            return completedTaskCount.sum();
        }

        /**
         * 获取被拒绝的任务数量
         *
         * @return
         */
        public long getRejectedTaskCount() {
            return rejectedTaskCount.sum();
        }

        @Override
        public String toString() {
            return super.toString() +
                    "[queue size = " + taskQueue.size() +
                    ", completed tasks = " + completedTaskCount +
                    ", rejected tasks = " + rejectedTaskCount +
                    ", running = " + running +
                    "]";
        }
    }
}

