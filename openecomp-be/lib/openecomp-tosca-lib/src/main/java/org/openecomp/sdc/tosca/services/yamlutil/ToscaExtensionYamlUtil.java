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

package org.openecomp.sdc.tosca.services.yamlutil;

import org.openecomp.core.utilities.yaml.YamlUtil;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeId;

import java.beans.IntrospectionException;

public class ToscaExtensionYamlUtil extends YamlUtil {

  @Override
  public <T> Constructor getConstructor(Class<T> typClass) {
    return new ToscaWithHeatExtensionConstructor(typClass);
  }

  @Override
  protected PropertyUtils getPropertyUtils() {
    return new ToscaPropertyUtilsWithHeatExtension();
  }

  public class ToscaPropertyUtilsWithHeatExtension extends MyPropertyUtils {
    @Override
    public Property getProperty(Class<? extends Object> type, String name)
        throws IntrospectionException {
      try {
        if (type.equals(
            Class.forName("org.openecomp.sdc.tosca.datatypes.model.ParameterDefinition"))) {
          type = Class.forName(
              "org.openecomp.sdc.tosca.datatypes.model.heatextend.ParameterDefinitionExt");
        }
      } catch (ClassNotFoundException exception) {
        throw new RuntimeException(exception);
      }
      return super.getProperty(type, name);
    }
  }

  protected class ToscaWithHeatExtensionConstructor extends StrictMapAppenderConstructor {
    public ToscaWithHeatExtensionConstructor(Class<?> theRoot) {
      super(theRoot);
      yamlClassConstructors.put(NodeId.mapping, new MyPersistentObjectConstruct());
    }

    class MyPersistentObjectConstruct extends ConstructMapping {
      @Override
      protected Object constructJavaBean2ndStep(MappingNode node, Object object) {
        Class type = node.getType();
        try {
          if (type.equals(
              Class.forName("org.openecomp.sdc.tosca.datatypes.model.ParameterDefinition"))) {
            Class extendHeatClass = Class.forName(
                "org.openecomp.sdc.tosca.datatypes.model.heatextend.ParameterDefinitionExt");
            Object extendHeatObject = extendHeatClass.newInstance();
            // create JavaBean
            return super.constructJavaBean2ndStep(node, extendHeatObject);
          } else {
            // create JavaBean
            return super.constructJavaBean2ndStep(node, object);
          }
        } catch (ClassNotFoundException | InstantiationException
            | IllegalAccessException exception) {
          throw new RuntimeException(exception);
        }
      }
    }
  }
}



