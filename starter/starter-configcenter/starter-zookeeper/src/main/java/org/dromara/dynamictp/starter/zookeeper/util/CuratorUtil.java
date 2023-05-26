package org.dromara.dynamictp.starter.zookeeper.util;

import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.core.handler.ConfigHandler;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.dromara.dynamictp.common.em.ConfigFileTypeEnum.JSON;
import static org.dromara.dynamictp.common.em.ConfigFileTypeEnum.PROPERTIES;

/**
 * CuratorUtil related
 *
 * @author yanhom
 * @since 1.0.4
 **/
@Slf4j
public class CuratorUtil {

    private static CuratorFramework curatorFramework;

    private static final CountDownLatch COUNT_DOWN_LATCH = new CountDownLatch(1);

    private CuratorUtil() {

    }

    /**
     * 创建zk客户端
     *
     * @param dtpProperties
     * @return
     */
    public static CuratorFramework getCuratorFramework(DtpProperties dtpProperties) {
        if (curatorFramework == null) {
            // 获取zk配置，创建zk客户端
            DtpProperties.Zookeeper zookeeper = dtpProperties.getZookeeper();
            curatorFramework = CuratorFrameworkFactory.newClient(zookeeper.getZkConnectStr(),
                    new ExponentialBackoffRetry(1000, 3));

            // 创建zk监听
            final ConnectionStateListener connectionStateListener = (client, newState) -> {
                if (newState == ConnectionState.CONNECTED) {
                    COUNT_DOWN_LATCH.countDown();
                }
            };
            curatorFramework.getConnectionStateListenable().addListener(connectionStateListener);
            curatorFramework.start();

            try {
                COUNT_DOWN_LATCH.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
        return curatorFramework;
    }

    /**
     * 创建zk的配置节点：/${rootNode}/${node}
     *
     * @param dtpProperties
     * @return
     */
    public static String nodePath(DtpProperties dtpProperties) {
        DtpProperties.Zookeeper zookeeper = dtpProperties.getZookeeper();
        return ZKPaths.makePath(
                ZKPaths.makePath(zookeeper.getRootNode(), zookeeper.getConfigVersion()),
                zookeeper.getNode()
        );
    }

    /**
     * 获取配置信息
     *
     * @param dtpProperties
     * @return
     */
    @SneakyThrows
    public static Map<Object, Object> genPropertiesMap(DtpProperties dtpProperties) {

        val curatorFramework = getCuratorFramework(dtpProperties);
        String nodePath = nodePath(dtpProperties);

        Map<Object, Object> result = Maps.newHashMap();
        // properties、json类型的配置
        if (PROPERTIES.getValue().equalsIgnoreCase(dtpProperties.getConfigType().trim())) {
            result = genPropertiesTypeMap(nodePath, curatorFramework);
        } else if (JSON.getValue().equalsIgnoreCase(dtpProperties.getConfigType().trim())) {
            nodePath = ZKPaths.makePath(nodePath, dtpProperties.getZookeeper().getConfigKey());
            String value = getVal(nodePath, curatorFramework);
            result = ConfigHandler.getInstance().parseConfig(value, JSON);
        }

        return result;
    }

    /**
     * 获取zk节点的配置信息
     *
     * @param nodePath
     * @param curatorFramework
     * @return
     */
    private static Map<Object, Object> genPropertiesTypeMap(String nodePath, CuratorFramework curatorFramework) {
        try {
            final GetChildrenBuilder childrenBuilder = curatorFramework.getChildren();
            final List<String> children = childrenBuilder.watched().forPath(nodePath);
            Map<Object, Object> properties = Maps.newHashMap();
            children.forEach(c -> {
                String path = ZKPaths.makePath(nodePath, c);
                final String nodeName = ZKPaths.getNodeFromPath(path);
                String value = getVal(path, curatorFramework);
                properties.put(nodeName, value);
            });
            return properties;
        } catch (Exception e) {
            log.error("get zk configs error, nodePath is {}", nodePath, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 获取zk节点的配置信息
     *
     * @param path
     * @param curatorFramework
     * @return
     */
    private static String getVal(String path, CuratorFramework curatorFramework) {
        final GetDataBuilder data = curatorFramework.getData();
        String value = "";
        try {
            value = new String(data.watched().forPath(path), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("get zk config value failed, path: {}", path, e);
        }
        return value;
    }
}
