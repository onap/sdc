/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.types;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Relation;
import com.amdocs.zusammen.utils.fileutils.FileUtils;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class AsdcElement implements Element {

    private String type;
    private String name;
    private String description;
    private Map<String, Object> properties;
    private byte[] data;
    private Collection<Relation> relations;
    private Collection<Element> subElements = new ArrayList<>();
    private Action action;
    private Id elementId;

    @Override
    public Action getAction() {
        return this.action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public Id getElementId() {
        return this.elementId;
    }

    public void setElementId(Id elementId) {
        this.elementId = elementId;
    }

    @Override
    public Info getInfo() {
        Info info = new Info();
        info.setProperties(this.properties);
        info.addProperty(ElementPropertyName.elementType.name(), this.type != null ? this.type : this.name);
        info.setName(this.name);
        info.setDescription(this.description);
        return info;
    }

    @Override
    public Collection<Relation> getRelations() {
        return this.relations;
    }

    public void setRelations(Collection<Relation> relations) {
        this.relations = relations;
    }

    @Override
    public InputStream getData() {
        return FileUtils.toInputStream(this.data);
    }

    public void setData(InputStream data) {
        this.data = FileUtils.toByteArray(data);
    }

    @Override
    public InputStream getSearchableData() {
        return null;
    }

    @Override
    public InputStream getVisualization() {
        return null;
    }

    @Override
    public Collection<Element> getSubElements() {
        return this.subElements;
    }

    public void setSubElements(Collection<Element> subElements) {
        this.subElements = subElements;
    }

    public AsdcElement addSubElement(Element element) {
        this.subElements.add(element);
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
