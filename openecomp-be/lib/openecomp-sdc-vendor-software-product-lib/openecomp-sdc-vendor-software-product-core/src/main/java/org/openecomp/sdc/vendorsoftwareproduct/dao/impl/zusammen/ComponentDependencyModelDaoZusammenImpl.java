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
package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDependencyModelDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToComponentDependencyModelConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.types.ElementPropertyName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.openecomp.core.zusammen.api.ZusammenUtil.*;

/**
 * Created by ayalaben on 5/16/2017.
 */
public class ComponentDependencyModelDaoZusammenImpl implements ComponentDependencyModelDao {

    private ZusammenAdaptor zusammenAdaptor;

    public ComponentDependencyModelDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
        this.zusammenAdaptor = zusammenAdaptor;
    }

    @Override
    public ComponentDependencyModelEntity get(ComponentDependencyModelEntity dependency) {
        SessionContext context = createSessionContext();
        ElementContext elementContext = new ElementContext(dependency.getVspId(), dependency.getVersion().getId());
        Optional<ElementInfo> componentDependencyElement = zusammenAdaptor.getElementInfo(context, elementContext, new Id(dependency.getId()));
        if (componentDependencyElement.isPresent()) {
            ElementToComponentDependencyModelConvertor convertor = new ElementToComponentDependencyModelConvertor();
            ComponentDependencyModelEntity entity = convertor.convert(componentDependencyElement.get());
            entity.setVspId(dependency.getVspId());
            entity.setVersion(dependency.getVersion());
            return entity;
        }
        return null;
    }

    @Override
    public void create(ComponentDependencyModelEntity dependency) {
        ZusammenElement componentDependency = buildComponentDependencyElement(dependency, Action.CREATE);
        ZusammenElement componentDependencies = buildStructuralElement(ElementType.ComponentDependencies, Action.IGNORE);
        componentDependencies.addSubElement(componentDependency);
        ZusammenElement vspModel = buildStructuralElement(ElementType.VspModel, Action.IGNORE);
        vspModel.addSubElement(componentDependencies);
        SessionContext context = createSessionContext();
        ElementContext elementContext = new ElementContext(dependency.getVspId(), dependency.getVersion().getId());
        Element compDepsSavedElement = zusammenAdaptor.saveElement(context, elementContext, vspModel, "Create component dependency model");
        dependency.setId(compDepsSavedElement.getSubElements().iterator().next().getSubElements().iterator().next().getElementId().getValue());
    }

    @Override
    public void update(ComponentDependencyModelEntity dependency) {
        ZusammenElement componentDependencyElement = buildComponentDependencyElement(dependency, Action.UPDATE);
        SessionContext context = createSessionContext();
        ElementContext elementContext = new ElementContext(dependency.getVspId(), dependency.getVersion().getId());
        zusammenAdaptor.saveElement(context, elementContext, componentDependencyElement,
            String.format("Update component dependency model with id %s", dependency.getId()));
    }

    @Override
    public void delete(ComponentDependencyModelEntity dependency) {
        ZusammenElement componentDependencyElement = buildElement(new Id(dependency.getId()), Action.DELETE);
        SessionContext context = createSessionContext();
        ElementContext elementContext = new ElementContext(dependency.getVspId(), dependency.getVersion().getId());
        zusammenAdaptor.saveElement(context, elementContext, componentDependencyElement,
            String.format("Delete component dependency model with id %s", dependency.getId()));
    }

    @Override
    public void registerVersioning(String versionableEntityType) {
        //not implemented?
    }

    @Override
    public Collection<ComponentDependencyModelEntity> list(ComponentDependencyModelEntity dependency) {
        SessionContext context = createSessionContext();
        ElementContext elementContext = new ElementContext(dependency.getVspId(), dependency.getVersion().getId());
        Optional<ElementInfo> vspModel = zusammenAdaptor.getElementInfoByName(context, elementContext, null, ElementType.VspModel.name());
        if (!vspModel.isPresent()) {
            return new ArrayList<>();
        }
        ElementToComponentDependencyModelConvertor convertor = new ElementToComponentDependencyModelConvertor();
        return zusammenAdaptor.listElementsByName(context, elementContext, vspModel.get().getId(), ElementType.ComponentDependencies.name()).stream()
            .map(elementInfo -> {
                ComponentDependencyModelEntity entity = convertor.convert(elementInfo);
                entity.setVspId(dependency.getVspId());
                entity.setVersion(dependency.getVersion());
                entity.setId(elementInfo.getId().getValue());
                return entity;
            }).collect(Collectors.toList());
    }

    private ZusammenElement buildComponentDependencyElement(ComponentDependencyModelEntity compDep, Action action) {
        ZusammenElement componentDependencyElement = buildElement(compDep.getId() == null ? null : new Id(compDep.getId()), action);
        Info info = new Info();
        info.addProperty(ElementPropertyName.elementType.name(), ElementType.ComponentDependency);
        info.addProperty(ComponentDependencyModelPropertyName.RELATION.getVal(), compDep.getRelation());
        info.addProperty(ComponentDependencyModelPropertyName.SOURCE_COMPONENT_ID.getVal(), compDep.getSourceComponentId());
        info.addProperty(ComponentDependencyModelPropertyName.TARGET_COMPONENT_ID.getVal(), compDep.getTargetComponentId());
        componentDependencyElement.setInfo(info);
        return componentDependencyElement;
    }

    private enum ComponentDependencyModelPropertyName {
        ID("id"), RELATION("relation"), SOURCE_COMPONENT_ID("sourcecomponent_id"), TARGET_COMPONENT_ID("targetcomponent_id");
        private String val;

        ComponentDependencyModelPropertyName(String val) {
            this.val = val;
        }

        public String getVal() {
            return val;
        }
    }
}
