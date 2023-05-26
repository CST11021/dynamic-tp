package org.dromara.dynamictp.starter.zookeeper.refresh;

import org.dromara.dynamictp.core.refresher.AbstractRefresher;
import org.dromara.dynamictp.starter.zookeeper.util.CuratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.WatchedEvent;
import org.dromara.dynamictp.starter.zookeeper.autoconfigure.ZkConfigEnvironmentProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * @author Redick01
 */
@Slf4j
public class ZookeeperRefresher extends AbstractRefresher implements EnvironmentAware, InitializingBean {

    @Override
    public void setEnvironment(Environment environment) {
        ConfigurableEnvironment env = ((ConfigurableEnvironment) environment);
        env.getPropertySources().remove(ZkConfigEnvironmentProcessor.ZK_PROPERTY_SOURCE_NAME);
    }

    @Override
    public void afterPropertiesSet() {

        final ConnectionStateListener connectionStateListener = (client, newState) -> {
            if (newState == ConnectionState.RECONNECTED) {
                loadAndRefresh();
            }
        };

        final CuratorListener curatorListener = (client, curatorEvent) -> {
            final WatchedEvent watchedEvent = curatorEvent.getWatchedEvent();
            if (null != watchedEvent) {
                switch (watchedEvent.getType()) {
                    case NodeChildrenChanged:
                    case NodeDataChanged:
                        loadAndRefresh();
                        break;
                    default:
                        break;
                }
            }
        };

        CuratorFramework curatorFramework = CuratorUtil.getCuratorFramework(dtpProperties);
        String nodePath = CuratorUtil.nodePath(dtpProperties);

        curatorFramework.getConnectionStateListenable().addListener(connectionStateListener);
        curatorFramework.getCuratorListenable().addListener(curatorListener);

        log.info("DynamicTp refresher, add listener success, nodePath: {}", nodePath);
    }

    /**
     * zk重连时，或者节点配置变更新时，调用该方法
     */
    private void loadAndRefresh() {
        doRefresh(CuratorUtil.genPropertiesMap(dtpProperties));
    }

}
