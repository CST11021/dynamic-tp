package org.dromara.dynamictp.common.entity;

import lombok.Data;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * TpMainFields related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Data
public class TpMainFields {

    private static final List<Field> FIELD_NAMES;

    static {
        FIELD_NAMES = Arrays.asList(TpMainFields.class.getDeclaredFields());
    }

    /** 线程池名称 */
    private String threadPoolName;
    /** 核心线程数 */
    private int corePoolSize;
    /** 最大线程数 */
    private int maxPoolSize;
    /** 核心线程的存活时间 */
    private long keepAliveTime;
    /** 队列类型 */
    private String queueType;
    /** 队列大小 */
    private int queueCapacity;
    /** 拒绝策略 */
    private String rejectType;
    /** 当设置allowCoreThreadTimeOut(true)时，线程池中corePoolSize线程空闲时间达到keepAliveTime也将关闭 */
    private boolean allowCoreThreadTimeOut;

    public static List<Field> getMainFields() {
        return FIELD_NAMES;
    }
}
