/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.config.impl;

import static org.onap.config.Constants.LOAD_ORDER_KEY;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.tree.MergeCombiner;
import org.apache.commons.configuration2.tree.OverrideCombiner;
import org.apache.commons.configuration2.tree.UnionCombiner;
import org.onap.config.ConfigurationUtils;
import org.onap.config.type.ConfigurationMode;

public final class AggregateConfiguration {

    private final Map<String, Configuration> rootConfig = new HashMap<>();
    private final Map<String, Configuration> unionConfig = new HashMap<>();
    private final Map<String, Configuration> mergeConfig = new HashMap<>();
    private final Map<String, Configuration> overrideConfig = new LinkedHashMap<>();

    public void addConfig(File file) throws Exception {
        addConfig(file.getAbsolutePath().toUpperCase(), ConfigurationUtils.getMergeStrategy(file),
                ConfigurationUtils.getConfigurationBuilder(file, false).getConfiguration());
    }

    private void addConfig(String path, ConfigurationMode configMode, Configuration config) {
        if (configMode != null) {
            switch (configMode) {
                case MERGE:
                    mergeConfig.put(path, config);
                    break;
                case OVERRIDE:
                    overrideConfig.put(path, config);
                    break;
                case UNION:
                    unionConfig.put(path, config);
                    break;
                default:
            }
        } else {
            rootConfig.put(path, config);
        }
    }

    public void addConfig(URL url) throws Exception {
        addConfig(url.getFile().toUpperCase(), ConfigurationUtils.getMergeStrategy(url),
                ConfigurationUtils.getConfigurationBuilder(url).getConfiguration());
    }

    public void removeConfig(File file) {
        String key = file.getAbsolutePath().toUpperCase();
        if (rootConfig.containsKey(key)) {
            rootConfig.remove(key);
        } else if (mergeConfig.containsKey(key)) {
            mergeConfig.remove(key);
        } else if (unionConfig.containsKey(key)) {
            unionConfig.remove(key);
        } else if (overrideConfig.containsKey(key)) {
            overrideConfig.remove(key);
        }
    }

    public boolean containsConfig(File file) {
        String key = file.getAbsolutePath().toUpperCase();
        return rootConfig.containsKey(key) || mergeConfig.containsKey(key) || unionConfig.containsKey(key)
                       || overrideConfig.containsKey(key);
    }

    public Configuration getFinalConfiguration() {
        CombinedConfiguration ccRoot = new CombinedConfiguration(new MergeCombiner());
        ArrayList<Configuration> tempList = new ArrayList<>(rootConfig.values());
        tempList.sort(this::sortForMerge);
        for (Configuration conf : tempList) {
            ccRoot.addConfiguration(conf);
        }
        CombinedConfiguration ccMergeRoot = new CombinedConfiguration(new MergeCombiner());
        ccMergeRoot.addConfiguration(ccRoot);
        tempList = new ArrayList<>(mergeConfig.values());
        tempList.sort(this::sortForMerge);
        for (Configuration conf : tempList) {
            ccMergeRoot.addConfiguration(conf);
        }
        CombinedConfiguration ccUnionRoot = new CombinedConfiguration(new UnionCombiner());
        ccUnionRoot.addConfiguration(ccMergeRoot);
        for (Configuration conf : unionConfig.values()) {
            ccUnionRoot.addConfiguration(conf);
        }
        ArrayList<Configuration> tempOverrideConfigs = new ArrayList<>(overrideConfig.values());
        Collections.reverse(tempOverrideConfigs);
        tempOverrideConfigs.sort(this::sortForOverride);
        CombinedConfiguration ccOverrideRoot = new CombinedConfiguration(new OverrideCombiner());
        for (Configuration conf : tempOverrideConfigs) {
            ccOverrideRoot.addConfiguration(conf);
        }
        ccOverrideRoot.addConfiguration(ccUnionRoot);
        return ccOverrideRoot;
    }

    private int sortForOverride(Configuration conf1, Configuration conf2) {
        String order1 = conf1.getString(LOAD_ORDER_KEY);
        String order2 = conf2.getString(LOAD_ORDER_KEY);
        if (ConfigurationUtils.isBlank(order1) || !order1.trim().matches("\\d+")) {
            order1 = "0";
        }
        if (ConfigurationUtils.isBlank(order2) || !order2.trim().matches("\\d+")) {
            order2 = "0";
        }
        return Integer.parseInt(order2.trim()) - Integer.parseInt(order1.trim());
    }

    private int sortForMerge(Configuration conf1, Configuration conf2) {
        String order1 = conf1.getString(LOAD_ORDER_KEY);
        String order2 = conf2.getString(LOAD_ORDER_KEY);
        if (ConfigurationUtils.isBlank(order1) || !order1.trim().matches("\\d+")) {
            order1 = "0";
        }
        if (ConfigurationUtils.isBlank(order2) || !order2.trim().matches("\\d+")) {
            order2 = "0";
        }
        return Integer.parseInt(order1.trim()) - Integer.parseInt(order2.trim());
    }
}
