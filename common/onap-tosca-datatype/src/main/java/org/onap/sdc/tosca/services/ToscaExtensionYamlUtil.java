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

package org.onap.sdc.tosca.services;

import org.onap.sdc.tosca.error.ToscaRuntimeException;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeId;

import java.beans.IntrospectionException;


public class ToscaExtensionYamlUtil extends YamlUtil {

    public static final String TOSCA_MODEL_PARAMETER_DEFINITION =
            "org.onap.sdc.tosca.datatypes.model.ParameterDefinition";
    public static final String TOSCA_MODEL_EXT_PARAMETER_DEFINITION =
            "org.onap.sdc.tosca.datatypes.model.heatextend.ParameterDefinitionExt";
    public static final String TOSCA_MODEL_REQUIREMENT_ASSIGNMENT =
            "org.onap.sdc.tosca.datatypes.model.RequirementAssignment";
    public static final String TOSCA_MODEL_EXT_REQUIREMENT_ASSIGNMENT =
            "org.onap.sdc.tosca.datatypes.model.extension.RequirementAssignment";

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
        public Property getProperty(Class<? extends Object> type, String name) throws IntrospectionException {
            Class<? extends Object> classType = type;
            try {
                if (type.equals(Class.forName(TOSCA_MODEL_PARAMETER_DEFINITION))) {
                    classType = Class.forName(TOSCA_MODEL_EXT_PARAMETER_DEFINITION);
                }
                if (type.equals(Class.forName(TOSCA_MODEL_REQUIREMENT_ASSIGNMENT))) {
                    classType = Class.forName(TOSCA_MODEL_EXT_REQUIREMENT_ASSIGNMENT);
                }
            } catch (ClassNotFoundException ex) {
                throw new ToscaRuntimeException(ex);
            }
            return super.getProperty(classType, name);
        }
    }

    protected class ToscaWithHeatExtensionConstructor extends StrictMapAppenderConstructor {

        public ToscaWithHeatExtensionConstructor(Class<?> theRoot) {
            super(theRoot);
            yamlClassConstructors.put(NodeId.mapping, new MyPersistentObjectConstruct());
        }

        class MyPersistentObjectConstruct extends Constructor.ConstructMapping {

            @Override
            protected Object constructJavaBean2ndStep(MappingNode node, Object object) {
                Class type = node.getType();
                try {
                    if (type.equals(Class.forName(TOSCA_MODEL_PARAMETER_DEFINITION))) {
                        Class extendHeatClass = Class.forName(TOSCA_MODEL_EXT_PARAMETER_DEFINITION);
                        Object extendHeatObject = extendHeatClass.newInstance();
                        // create JavaBean
                        return super.constructJavaBean2ndStep(node, extendHeatObject);
                    } else if (type.equals(Class.forName(TOSCA_MODEL_REQUIREMENT_ASSIGNMENT))) {
                        Class extendHeatClass = Class.forName(TOSCA_MODEL_EXT_REQUIREMENT_ASSIGNMENT);
                        Object extendHeatObject = extendHeatClass.newInstance();
                        // create JavaBean
                        return super.constructJavaBean2ndStep(node, extendHeatObject);
                    } else {
                        // create JavaBean
                        return super.constructJavaBean2ndStep(node, object);
                    }
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                    throw new ToscaRuntimeException(ex);
                }
            }
        }
    }
}
