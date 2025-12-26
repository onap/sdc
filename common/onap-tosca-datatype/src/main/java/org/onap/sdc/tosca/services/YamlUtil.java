/*
 * Copyright © 2016-2017 European Support Limited
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
package org.onap.sdc.tosca.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * The type Yaml util.
 */
@SuppressWarnings("unchecked")
public class YamlUtil {

    static final String DEFAULT = "default";
    static final String DEFAULT_STR = "_default";
    private static final Logger LOGGER = LoggerFactory.getLogger(YamlUtil.class.getName());

    /**
     * Parse a YAML file to List
     *
     * @param yamlFileInputStream the YAML file input stream
     * @return The YAML casted as a list
     */
    public static Optional<List<Object>> yamlToList(final InputStream yamlFileInputStream) {
        List<Object> yamlList = null;
        try {
            yamlList = (List<Object>) read(yamlFileInputStream);
        } catch (final ClassCastException ex) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not parse YAML to List.", ex);
            }
        }
        return Optional.ofNullable(yamlList);
    }

    /**
     * Parse a YAML file to Object
     *
     * @param yamlFileInputStream the YAML file input stream
     * @return The YAML Object
     */
    public static Object read(final InputStream yamlFileInputStream) {
         return new Yaml(new SafeConstructor(new LoaderOptions())).load(yamlFileInputStream);
    }

    /**
     * Yaml to object t.
     *
     * @param <T>         the type parameter
     * @param yamlContent the yaml content
     * @param typClass    the t class
     * @return the t
     */
    public <T> T yamlToObject(String yamlContent, Class<T> typClass) {
        Constructor constructor = getConstructor(typClass);
        constructor.setPropertyUtils(getPropertyUtils());
        constructor.addTypeDescription(new TypeDescription(typClass));

        DumperOptions dumper = new DumperOptions();
        LoaderOptions loader = getLoaderOptions();
        Representer rep = new Representer(dumper);

        return new Yaml(constructor, rep, dumper, loader).load(yamlContent);
    }

    public InputStream loadYamlFileIs(String yamlFullFileName) {
        return YamlUtil.class.getResourceAsStream(yamlFullFileName);
    }

    /**
     * Yaml to object t.
     *
     * @param <T>         the type parameter
     * @param yamlContent the yaml content
     * @param typClass    the t class
     * @return the t
     */
    public <T> T yamlToObject(InputStream yamlContent, Class<T> typClass) {
        try {
            Constructor constructor = getConstructor(typClass);
            constructor.setAllowDuplicateKeys(false);
            constructor.setPropertyUtils(getPropertyUtils());
            constructor.addTypeDescription(new TypeDescription(typClass));

            DumperOptions dumper = new DumperOptions();
            LoaderOptions loader = getLoaderOptions();
            Representer rep = new Representer(dumper);

            T yamlObj = new Yaml(constructor, rep, dumper, loader).load(yamlContent);
            if (yamlObj != null) {
                //noinspection ResultOfMethodCallIgnored
                yamlObj.toString();
                return yamlObj;
            } else {
                throw new RuntimeException("YAML parsed as null");
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        } finally {
            try {
                if (yamlContent != null) {
                    yamlContent.close();
                }
            } catch (IOException ignore) {
                //do nothing
            }
        }
    }

    private LoaderOptions getLoaderOptions() {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        options.setMaxAliasesForCollections(9999);
        return options;
    }

    /**
     * Gets constructor.
     *
     * @param <T>      the type parameter
     * @param typClass the t class
     * @return the constructor
     */
    public <T> Constructor getConstructor(Class<T> typClass) {
        return new StrictMapAppenderConstructor(typClass, getLoaderOptions());
    }

    /**
     * Gets property utils.
     *
     * @return the property utils
     */
    protected PropertyUtils getPropertyUtils() {
        return new MyPropertyUtils();
    }

    /**
     * Yaml to map map.
     *
     * @param yamlContent the yaml content
     * @return the map
     */
    public Map<String, LinkedHashMap<String, Object>> yamlToMap(InputStream yamlContent) {
        return new Yaml(new SafeConstructor(new LoaderOptions())).load(yamlContent);
    }

    /**
     * Object to yaml string.
     *
     * @param obj the obj
     * @return the string
     */
    public String objectToYaml(Object obj) {
        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        CustomRepresenter rep = new CustomRepresenter(options);
        rep.addClassTag(obj.getClass(), Tag.MAP);
        rep.setPropertyUtils(new MyPropertyUtils());
        return new Yaml(rep, options).dump(obj);
    }

    /**
     * Is yaml file content valid boolean.
     *
     * @param yamlFullFileName the yaml full file name
     * @return the boolean
     */
    public boolean isYamlFileContentValid(String yamlFullFileName) {
        try {
            return new Yaml(new SafeConstructor(new LoaderOptions())).load(yamlFullFileName) != null;
        } catch (Exception exception) {
            return false;
        }
    }

    private class CustomRepresenter extends Representer {

        public CustomRepresenter(DumperOptions options) {
            super(options);
        }

        @Override
        protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
            //remove the bean type from the output yaml (!! ...)
            if (!classTags.containsKey(javaBean.getClass())) {
                addClassTag(javaBean.getClass(), Tag.MAP);
            }
            return super.representJavaBean(properties, javaBean);
        }

        @Override
        protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
            if (propertyValue == null) {
                return null;
            } else {
                NodeTuple defaultNode = super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
                return DEFAULT_STR.equals(property.getName()) ? new NodeTuple(representData(DEFAULT), defaultNode.getValueNode()) : defaultNode;
            }
        }
    }
}
