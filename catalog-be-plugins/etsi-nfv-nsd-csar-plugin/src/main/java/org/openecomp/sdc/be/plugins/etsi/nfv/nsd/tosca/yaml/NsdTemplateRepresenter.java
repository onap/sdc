 
/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.plugins.etsi.nfv.nsd.tosca.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyConstraintValidValues;
import org.openecomp.sdc.be.tosca.model.ToscaTemplate;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

/**
 * A NSD YAML Representer
 */
public class NsdTemplateRepresenter extends Representer {

    private final Set<String> ignoredPropertySet = Stream.of("dependencies").collect(Collectors.toSet());

    public NsdTemplateRepresenter() {
        super();
        this.nullRepresenter = new RepresentNull();
    }

    @Override
    protected NodeTuple representJavaBeanProperty(final Object javaBean, final Property property, final Object propertyValue, final Tag customTag) {
        if (propertyValue == null) {
            return null;
        }
        if (ignoredPropertySet.contains(property.getName())) {
            return null;
        }
        if (javaBean instanceof ToscaTemplate) {
            return handleToscaTemplate(javaBean, property, propertyValue, customTag);
        }
        if (javaBean instanceof ToscaPropertyConstraintValidValues) {
            return handleToscaPropertyConstraintValidValues((ToscaPropertyConstraintValidValues)javaBean, property, propertyValue, customTag);
        }
        if (javaBean instanceof ToscaProperty) {
            return handleToscaProperty(javaBean, property, propertyValue, customTag);
        }
        return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
    }

    private NodeTuple handleToscaProperty(final Object javaBean, final Property property, final Object propertyValue, final Tag customTag) {
        final NodeTuple nodeTuple = super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
        if ("_defaultp_".equals(property.getName())) {
            return new NodeTuple(representData("default"), nodeTuple.getValueNode());
        }
        return nodeTuple;
    }

    private NodeTuple handleToscaPropertyConstraintValidValues(final ToscaPropertyConstraintValidValues javaBean, final Property property, final Object propertyValue,
                                                               final Tag customTag) {
        final NodeTuple nodeTuple = super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
        if ("validValues".equals(property.getName())) {
            final String validValuesEntryName = javaBean.getEntryToscaName("validValues");
            return new NodeTuple(representData(validValuesEntryName), nodeTuple.getValueNode());
        }
        return nodeTuple;
    }

    private NodeTuple handleToscaTemplate(final Object javaBean, final Property property, final Object propertyValueObj, final Tag customTag) {
        if ("imports".equals(property.getName())) {
            final List<Map<String, Map<String, String>>> importsList = (List<Map<String, Map<String, String>>>) propertyValueObj;
            final List<Map<String, String>> newImportList = new ArrayList<>();
            importsList.forEach(importMap -> importMap.forEach((key, value) -> newImportList.add(value)));
            return super.representJavaBeanProperty(javaBean, property, newImportList, customTag);
        }
        return super.representJavaBeanProperty(javaBean, property, propertyValueObj, customTag);
    }

    @Override
    protected MappingNode representJavaBean(final Set<Property> properties, final Object javaBean) {
        if (!classTags.containsKey(javaBean.getClass())) {
            addClassTag(javaBean.getClass(), Tag.MAP);
        }
        return super.representJavaBean(properties, javaBean);
    }

    private class RepresentNull implements Represent {

        @Override
        public Node representData(final Object data) {
            return representScalar(Tag.NULL, "");
        }
    }
}
