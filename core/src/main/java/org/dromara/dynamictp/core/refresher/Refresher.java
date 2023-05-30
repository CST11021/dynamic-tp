package org.dromara.dynamictp.core.refresher;

import org.dromara.dynamictp.common.em.ConfigFileTypeEnum;

/**
 * Refresher related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public interface Refresher {

    /**
     * 当配置变更时，调用该方法刷新本地的线程池配置
     *
     * @param content content
     * @param fileType file type
     */
    void refresh(String content, ConfigFileTypeEnum fileType);
}
