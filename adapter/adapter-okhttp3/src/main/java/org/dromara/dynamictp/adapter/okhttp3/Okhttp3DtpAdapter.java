/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.adapter.okhttp3;

import org.dromara.dynamictp.adapter.common.AbstractDtpAdapter;
import org.dromara.dynamictp.common.ApplicationContextHolder;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.common.properties.DtpProperties;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.OkHttpClient;
import org.apache.commons.collections4.MapUtils;

/**
 * Okhttp3DtpAdapter related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Slf4j
public class Okhttp3DtpAdapter extends AbstractDtpAdapter {

    private static final String NAME = "okhttp3Tp";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getOkhttp3Tp(), dtpProperties.getPlatforms());
    }

    @Override
    protected void initialize() {
        super.initialize();
        val beans = ApplicationContextHolder.getBeansOfType(OkHttpClient.class);
        if (MapUtils.isEmpty(beans)) {
            log.warn("Cannot find beans of type OkHttpClient.");
            return;
        }
        beans.forEach((k, v) -> {
            val executor = v.dispatcher().executorService();
            String key = genTpName(k);
            val executorWrapper = new ExecutorWrapper(key, executor);
            initNotifyItems(key, executorWrapper);
            executors.put(key, executorWrapper);
        });
        log.info("DynamicTp adapter, okhttp3 executors init end, executors: {}", executors);
    }

    private String genTpName(String clientName) {
        return clientName + "Tp";
    }
}
