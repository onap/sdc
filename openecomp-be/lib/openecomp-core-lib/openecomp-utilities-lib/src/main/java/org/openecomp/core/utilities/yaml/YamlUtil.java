/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.core.utilities.yaml;

import org.openecomp.core.utilities.CommonMethods;
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
public class YamlUtil {

  private static Logger logger = LoggerFactory.getLogger(YamlUtil.class);

  /**
   * Yaml to object t.
   *
   * @param <T>         the type parameter
   * @param yamlContent the yaml content
   * @param typClass    the typ class
   * @return the t
   */
  public <T> T yamlToObject(String yamlContent, Class<T> typClass) {
    Constructor constructor = getConstructor(typClass);
    constructor.setPropertyUtils(getPropertyUtils());
    TypeDescription yamlFileDescription = new TypeDescription(typClass);
    constructor.addTypeDescription(yamlFileDescription);
    Yaml yaml = new Yaml(constructor);
    T yamlObj = (T) yaml.load(yamlContent);
    yamlObj.toString();
    return yamlObj;
  }

  /**
   * Yaml to object t.
   *
   * @param <T>         the type parameter
   * @param yamlContent the yaml content
   * @param typClass    the typ class
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
        yamlObj.toString();
        return yamlObj;
      } else {
        throw new RuntimeException();
      }
    } catch (Exception exception) {
      logger.error("Error will trying to convert yaml to object:" + exception.getMessage());
      throw new RuntimeException(exception);
    } finally {
      try {
        if (yamlContent != null) {
          yamlContent.close();
        }
      } catch (IOException ignore) {
        //nothing to dd
      }
    }
  }


  /**
   * Gets constructor.
   *
   * @param <T>      the type parameter
   * @param typClass the typ class
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
    Map<String, LinkedHashMap<String, Object>> yamlData =
        (Map<String, LinkedHashMap<String, Object>>) yaml.load(yamlContent);
    return yamlData;
  }

  /**
   * Object to yaml string.
   *
   * @param <T> the type parameter
   * @param obj the obj
   * @return the string
   */
  public <T> String objectToYaml(Object obj) {
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
   * Load yaml file is input stream.
   *
   * @param yamlFullFileName the yaml full file name
   * @return the input stream
   */
  public InputStream loadYamlFileIs(String yamlFullFileName) {
    return CommonMethods.class.getResourceAsStream(yamlFullFileName);
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
      if (loadResult == null) {
        return false;
      }
      return true;
    } catch (Exception exception) {
      return false;
    }
  }


  private class CustomRepresenter extends Representer {
    @Override
    protected NodeTuple representJavaBeanProperty(Object javaBean, Property property,
                                                  Object propertyValue, Tag customTag) {
      if (propertyValue == null) {
        return null;
      } else {
        NodeTuple defaultNode =
            super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);

        return property.getName().equals("_default")
            ? new NodeTuple(representData("default"), defaultNode.getValueNode())
            : defaultNode;
      }
    }

    @Override
    protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
      //remove the bean type from the output yaml (!! ...)
      if (!classTags.containsKey(javaBean.getClass())) {
        addClassTag(javaBean.getClass(), Tag.MAP);
      }

      return super.representJavaBean(properties, javaBean);
    }
  }


  /**
   * The type My property utils.
   */
  public class MyPropertyUtils extends PropertyUtils {
    @Override
    public Property getProperty(Class<?> type, String name) throws IntrospectionException {
      if (name.equals("default")) {
        name = "_default";
      }
      return super.getProperty(type, name);
    }

    //Unsorted properties
    @Override
    protected Set<Property> createPropertySet(Class<? extends Object> type, BeanAccess beanAccess)
        throws IntrospectionException {
      return new LinkedHashSet<Property>(getPropertiesMap(type,
          BeanAccess.FIELD).values());
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
    protected Map<Object, Object> constructMapping(MappingNode node) {
      try {
        return super.constructMapping(node);
      } catch (IllegalStateException exception) {
        throw new ParserException("while parsing MappingNode", node.getStartMark(),
            exception.getMessage(), node.getEndMark());
      }
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
  }
}



