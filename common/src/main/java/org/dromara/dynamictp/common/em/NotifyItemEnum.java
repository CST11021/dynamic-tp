package org.dromara.dynamictp.common.em;

import lombok.Getter;

/**
 * NotifyItemEnum related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Getter
public enum NotifyItemEnum {

    /** 配置变更通知 */
    CHANGE("change"),
    /** 活跃性通知：liveness = activeCount / maximumPoolSize */
    LIVENESS("liveness"),
    /** 队列容量阈值通知 */
    CAPACITY("capacity"),
    /** 拒绝任务触发通知 */
    REJECT("reject"),
    /** 任务执行超时触发通知 */
    RUN_TIMEOUT("run_timeout"),
    /** 任务在队列中等待超时触发通知 */
    QUEUE_TIMEOUT("queue_timeout");

    private final String value;

    NotifyItemEnum(String value) {
        this.value = value;
    }

    public static NotifyItemEnum of(String value) {
        for (NotifyItemEnum notifyItem : NotifyItemEnum.values()) {
            if (notifyItem.value.equals(value)) {
                return notifyItem;
            }
        }
        return null;
    }
}
