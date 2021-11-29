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
package org.openecomp.sdc.be.resources.data.category;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.category.CategoryDataDefinition;
import org.openecomp.sdc.be.datatypes.category.MetadataKeyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class CategoryData extends GraphNode {

    private CategoryDataDefinition categoryDataDefinition;

    public CategoryData(NodeTypeEnum label) {
        super(label);
        categoryDataDefinition = new CategoryDataDefinition();
    }

    public CategoryData(NodeTypeEnum label, CategoryDataDefinition categoryDataDefinition) {
        super(label);
        this.categoryDataDefinition = categoryDataDefinition;
    }

    public CategoryData(Map<String, Object> properties) {
        this(NodeTypeEnum.getByName((String) properties.get(GraphPropertiesDictionary.LABEL.getProperty())));
        categoryDataDefinition.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));
        categoryDataDefinition.setNormalizedName((String) properties.get(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty()));
        categoryDataDefinition.setDisplayName((String) properties.get(GraphPropertiesDictionary.DISPLAY_NAME.getProperty()));
        categoryDataDefinition.setName((String) properties.get(GraphPropertiesDictionary.NAME.getProperty()));
        final Object useServiceSubstitutionForNestedServicesProperty = properties
            .get(GraphPropertiesDictionary.USE_SERVICE_SUBSTITUTION_FOR_NESTED_SERVICES.getProperty());
        final boolean useServiceSubstitutionForNestedServices =
            useServiceSubstitutionForNestedServicesProperty != null && (boolean) useServiceSubstitutionForNestedServicesProperty;
        categoryDataDefinition.setUseServiceSubstitutionForNestedServices(useServiceSubstitutionForNestedServices);
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        List<String> iconsfromJson = getGson().fromJson((String) properties.get(GraphPropertiesDictionary.ICONS.getProperty()), listType);
        categoryDataDefinition.setIcons(iconsfromJson);
        categoryDataDefinition.setModels(getGson().fromJson((String) properties.get(GraphPropertiesDictionary.MODEL.getProperty()), listType));
        final Type metadataKeylistType = new TypeToken<List<MetadataKeyDataDefinition>>() {
        }.getType();
        final List<MetadataKeyDataDefinition> metadataKeysfromJson = getGson()
            .fromJson((String) properties.get(GraphPropertiesDictionary.METADATA_KEYS.getProperty()), metadataKeylistType);
        categoryDataDefinition.setMetadataKeys(metadataKeysfromJson);
    }

    @Override
    public String getUniqueId() {
        return categoryDataDefinition.getUniqueId();
    }

    public CategoryDataDefinition getCategoryDataDefinition() {
        return categoryDataDefinition;
    }

    @Override
    public Map<String, Object> toGraphMap() {
        Map<String, Object> map = new HashMap<>();
        addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, categoryDataDefinition.getUniqueId());
        addIfExists(map, GraphPropertiesDictionary.NAME, categoryDataDefinition.getName());
        addIfExists(map, GraphPropertiesDictionary.DISPLAY_NAME, categoryDataDefinition.getDisplayName());
        addIfExists(map, GraphPropertiesDictionary.NORMALIZED_NAME, categoryDataDefinition.getNormalizedName());
        addIfExists(map, GraphPropertiesDictionary.MODEL, categoryDataDefinition.getModels());
        addIfExists(map, GraphPropertiesDictionary.ICONS, categoryDataDefinition.getIcons());
        addIfExists(map, GraphPropertiesDictionary.USE_SERVICE_SUBSTITUTION_FOR_NESTED_SERVICES,
            categoryDataDefinition.isUseServiceSubstitutionForNestedServices());
        addIfExists(map, GraphPropertiesDictionary.METADATA_KEYS, categoryDataDefinition.getMetadataKeys());
        return map;
    }

    @Override
    public String toString() {
        return "CategoryData [categoryDataDefinition=" + categoryDataDefinition + "]";
    }
}
