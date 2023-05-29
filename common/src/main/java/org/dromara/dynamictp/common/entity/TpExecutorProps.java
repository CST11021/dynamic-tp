package org.dromara.dynamictp.common.entity;

import org.dromara.dynamictp.common.constant.DynamicTpConst;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import lombok.Data;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 线程池的基础配置
 *
 * @author yanhom
 * @since 1.0.6
 **/
@Data
public class TpExecutorProps {

    /** 线程池名称 */
    private String threadPoolName;
    /** 给线程池定义的别名，用于消息通知 */
    private String threadPoolAliasName;
    /** 核心线程数 */
    private int corePoolSize = 1;
    /** 最大线程数 */
    private int maximumPoolSize = DynamicTpConst.AVAILABLE_PROCESSORS;
    /** 线程存活时间 */
    private long keepAliveTime = 60;
    /** Timeout unit */
    private TimeUnit unit = TimeUnit.SECONDS;


    /** 是否启用消息通知 */
    private boolean notifyEnabled = true;
    /** 消息通知平台标识 */
    private List<String> platformIds;
    /** 消息通知平台配置，参见：{@link NotifyItemEnum} */
    private List<NotifyItem> notifyItems;

}
