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

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.representer.Representer;


import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * The type Yaml util.
 */
@SuppressWarnings("unchecked")
public class YamlUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(YamlUtil.class.getName());

  private static final String DEFAULT = "default";
  private static final String DEFAULT_STR = "_default";

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
    TypeDescription yamlFileDescription = new TypeDescription(typClass);
    constructor.addTypeDescription(yamlFileDescription);
    Yaml yaml = new Yaml(constructor);
    T yamlObj = (T) yaml.load(yamlContent);
    //noinspection ResultOfMethodCallIgnored
    yamlObj.toString();
    return yamlObj;
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
      constructor.setPropertyUtils(getPropertyUtils());
      TypeDescription yamlFileDescription = new TypeDescription(typClass);
      constructor.addTypeDescription(yamlFileDescription);
      Yaml yaml = new Yaml(constructor);
      T yamlObj = (T) yaml.load(yamlContent);
      if (yamlObj != null) {
        //noinspection ResultOfMethodCallIgnored
        yamlObj.toString();
        return yamlObj;
      } else {
        throw new RuntimeException();
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


  /**
   * Gets constructor.
   *
   * @param <T>      the type parameter
   * @param typClass the t class
   * @return the constructor
   */
  public <T> Constructor getConstructor(Class<T> typClass) {
    return new StrictMapAppenderConstructor(typClass);
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
    Yaml yaml = new Yaml();
    return (Map<String, LinkedHashMap<String, Object>>) yaml.load(yamlContent);
  }


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
        final Yaml yaml = new Yaml();
        return yaml.load(yamlFileInputStream);
    }

  /**
   * Object to yaml string.
   * @param obj the obj
   * @return the string
   */
  public String objectToYaml(Object obj) {
    DumperOptions options = new DumperOptions();
    options.setPrettyFlow(true);
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    Representer representer = new CustomRepresenter();
    representer.addClassTag(obj.getClass(), Tag.MAP);
    representer.setPropertyUtils(new MyPropertyUtils());

    Yaml yaml = new Yaml(representer, options);
    return yaml.dump(obj);
  }

  /**
   * Is yaml file content valid boolean.
   *
   * @param yamlFullFileName the yaml full file name
   * @return the boolean
   */
  public boolean isYamlFileContentValid(String yamlFullFileName) {
    Yaml yaml = new Yaml();
    try {
      Object loadResult = yaml.load(yamlFullFileName);
      return loadResult != null;
    } catch (Exception exception) {
      return false;
    }
  }


  private class CustomRepresenter extends Representer {
    @Override
    protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
      //remove the bean type from the output yaml (!! ...)
      if (!classTags.containsKey(javaBean.getClass())) {
        addClassTag(javaBean.getClass(), Tag.MAP);
      }

      return super.representJavaBean(properties, javaBean);
    }

    @Override
    protected NodeTuple representJavaBeanProperty(Object javaBean, Property property,
                                                  Object propertyValue, Tag customTag) {
      if (propertyValue == null) {
        return null;
      } else {
        NodeTuple defaultNode =
            super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);

        return DEFAULT_STR.equals(property.getName())
            ? new NodeTuple(representData(DEFAULT), defaultNode.getValueNode())
            : defaultNode;
      }
    }
  }


  /**
   * The type My property utils.
   */
  public class MyPropertyUtils extends PropertyUtils {
    //Unsorted properties
    @Override
    protected Set<Property> createPropertySet(Class<? extends Object> type, BeanAccess bnAccess)
        throws IntrospectionException {
      return new LinkedHashSet<>(getPropertiesMap(type,
          BeanAccess.FIELD).values());
    }

    @Override
    public Property getProperty(Class<?> type, String name) throws IntrospectionException {
      String updatedName = name;
      if (DEFAULT.equals(updatedName)) {
        updatedName = DEFAULT_STR;
      }
      return super.getProperty(type, updatedName);
    }

  }

  /**
   * The type Strict map appender constructor.
   */
  protected class StrictMapAppenderConstructor extends Constructor {

    /**
     * Instantiates a new Strict map appender constructor.
     *
     * @param theRoot the the root
     */
    public StrictMapAppenderConstructor(Class<?> theRoot) {
      super(theRoot);
    }

    @Override
    protected Map<Object, Object> createDefaultMap() {
      final Map<Object, Object> delegate = super.createDefaultMap();
      return new AbstractMap<Object, Object>() {
        @Override
        public Object put(Object key, Object value) {
          if (delegate.containsKey(key)) {
            throw new IllegalStateException("duplicate key: " + key);
          }
          return delegate.put(key, value);
        }

        @Override
        public Set<Entry<Object, Object>> entrySet() {
          return delegate.entrySet();
        }
      };
    }

    @Override
    protected Map<Object, Object> constructMapping(MappingNode node) {
      try {
        return super.constructMapping(node);
      } catch (IllegalStateException exception) {
        throw new ParserException("while parsing MappingNode",
            node.getStartMark(), exception.getMessage(),
            node.getEndMark());
      }
    }
  }
}
