package org.dromara.dynamictp.core.spring;

import org.dromara.dynamictp.common.properties.DtpProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;

import java.util.Map;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.MAIN_PROPERTIES_PREFIX;

/**
 * PropertiesBinder related
 *
 * @author yanhom
 * @since 1.0.3
 **/
public class PropertiesBinder {

    private PropertiesBinder() { }

    public static void bindDtpProperties(Map<?, Object> properties, DtpProperties dtpProperties) {
        ConfigurationPropertySource sources = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(sources);
        ResolvableType type = ResolvableType.forClass(DtpProperties.class);
        // withExistingValue: 如果变更配置时，把配置的key移除了，则不会生效，只会更新有值的配置项
        Bindable<?> target = Bindable.of(type).withExistingValue(dtpProperties);
        binder.bind(MAIN_PROPERTIES_PREFIX, target);
    }

    public static void bindDtpProperties(Environment environment, DtpProperties dtpProperties) {
        Binder binder = Binder.get(environment);
        ResolvableType type = ResolvableType.forClass(DtpProperties.class);
        // withExistingValue: 如果变更配置时，把配置的key移除了，则不会生效，只会更新有值的配置项
        Bindable<?> target = Bindable.of(type).withExistingValue(dtpProperties);
        binder.bind(MAIN_PROPERTIES_PREFIX, target);
    }
}
