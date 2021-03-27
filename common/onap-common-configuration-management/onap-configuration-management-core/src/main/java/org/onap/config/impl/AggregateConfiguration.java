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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.MergeCombiner;
import org.apache.commons.configuration2.tree.OverrideCombiner;
import org.apache.commons.configuration2.tree.UnionCombiner;
import org.onap.config.ConfigurationUtils;
import org.onap.config.type.ConfigurationMode;

public final class AggregateConfiguration {

    private final Map<String, Configuration> rootConfig = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Configuration> unionConfig = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Configuration> mergeConfig = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Configuration> overrideConfig = Collections.synchronizedMap(new LinkedHashMap<>());

    public void addConfig(File file) throws ConfigurationException {
        addConfig(fileToUrl(file), ConfigurationUtils.getMergeStrategy(file), ConfigurationUtils.getConfigurationBuilder(file).getConfiguration());
    }

    public void addConfig(URL url) throws ConfigurationException {
        addConfig(url, ConfigurationUtils.getMergeStrategy(url), ConfigurationUtils.getConfigurationBuilder(url).getConfiguration());
    }

    private void addConfig(URL url, ConfigurationMode configMode, Configuration config) {
        String normalizedUrl = normalize(url);
        if (configMode != null) {
            switch (configMode) {
                case MERGE:
                    mergeConfig.put(normalizedUrl, config);
                    break;
                case OVERRIDE:
                    overrideConfig.put(normalizedUrl, config);
                    break;
                case UNION:
                    unionConfig.put(normalizedUrl, config);
                    break;
                default:
            }
        } else {
            rootConfig.put(normalizedUrl, config);
        }
    }

    private String normalize(URL url) {
        // what about Linux where paths are case sensitive?
        return url.toString().toUpperCase();
    }

    private URL fileToUrl(File file) {
        try {
            return file.getAbsoluteFile().toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("URL produced by JDK and is not expected to be malformed. File: " + file.getAbsoluteFile());
        }
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
        return sort(conf1, conf2, (o1, o2) -> o2 - o1);
    }

    private int sortForMerge(Configuration conf1, Configuration conf2) {
        return sort(conf1, conf2, (o1, o2) -> o1 - o2);
    }

    private int sort(Configuration conf1, Configuration conf2, Comparator<Integer> comparator) {
        int order1 = readLoadOrder(conf1);
        int order2 = readLoadOrder(conf2);
        return comparator.compare(order1, order2);
    }

    private int readLoadOrder(Configuration conf) {
        String order = conf.getString(LOAD_ORDER_KEY);
        if (ConfigurationUtils.isBlank(order) || !order.trim().matches("\\d+")) {
            return 0;
        }
        return Integer.parseInt(order.trim());
    }
}
