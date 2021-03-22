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

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToProcessConvertor.ARTIFACT_NAME;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToProcessConvertor.DESCRIPTION;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToProcessConvertor.NAME;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToProcessConvertor.PROCESS_TYPE;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToProcessConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.types.ElementPropertyName;

/**
 * @author Avrahamg.
 * @since March 23, 2017
 */
public class ProcessDaoZusammenImpl implements ProcessDao {

    private ZusammenAdaptor zusammenAdaptor;

    public ProcessDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
        this.zusammenAdaptor = zusammenAdaptor;
    }

    @Override
    public void registerVersioning(String versionableEntityType) {
        // registerVersioning not implemented for ProcessDaoZusammenImpl
    }

    @Override
    public void create(ProcessEntity processEntity) {
        ZusammenElement processElement = buildProcessElement(processEntity, Action.CREATE);
        ZusammenElement processesElement = buildStructuralElement(ElementType.Processes, Action.IGNORE);
        ZusammenElement aggregatedElement = VspZusammenUtil.aggregateElements(processesElement, processElement);
        ZusammenElement componentElement;
        if (processEntity.getComponentId() != null) {
            componentElement = buildElement(new Id(processEntity.getComponentId()), Action.IGNORE);
            aggregatedElement = VspZusammenUtil.aggregateElements(componentElement, aggregatedElement);
        }
        SessionContext context = createSessionContext();
        ElementContext elementContext = new ElementContext(processEntity.getVspId(), processEntity.getVersion().getId());
        Element savedElement = zusammenAdaptor.saveElement(context, elementContext, aggregatedElement, "Create process");
        processEntity.setId(processEntity.getComponentId() == null ? savedElement.getSubElements().iterator().next().getElementId().getValue()
            : savedElement.getSubElements().iterator().next().getSubElements().iterator().next().getElementId().getValue());
    }

    @Override
    public ProcessEntity get(ProcessEntity processEntity) {
        SessionContext context = createSessionContext();
        ElementContext elementContext = new ElementContext(processEntity.getVspId(), processEntity.getVersion().getId());
        return zusammenAdaptor.getElementInfo(context, elementContext, new Id(processEntity.getId()))
            .map(elementInfo -> convertToProcessEntity(elementInfo, new ElementToProcessConvertor(), processEntity)).orElse(null);
    }

    @Override
    public void update(ProcessEntity processEntity) {
        ProcessEntity retrieved = getArtifact(processEntity);
        if (retrieved != null && retrieved.getArtifact() != null) {
            processEntity.setArtifactName(retrieved.getArtifactName());
            processEntity.setArtifact(retrieved.getArtifact());
        }
        update(processEntity, "Update process");
    }

    @Override
    public void delete(ProcessEntity processEntity) {
        ZusammenElement processElement = buildElement(new Id(processEntity.getId()), Action.DELETE);
        SessionContext context = createSessionContext();
        ElementContext elementContext = new ElementContext(processEntity.getVspId(), processEntity.getVersion().getId());
        zusammenAdaptor.saveElement(context, elementContext, processElement, String.format("Delete process with id %s", processEntity.getId()));
    }

    @Override
    public void deleteAll(ProcessEntity processEntity) {
        SessionContext context = createSessionContext();
        ElementContext elementContext = new ElementContext(processEntity.getVspId(), processEntity.getVersion().getId());
        Optional<Element> optionalElement = zusammenAdaptor
            .getElementByName(context, elementContext, new Id(processEntity.getComponentId()), ElementType.Processes.name());
        if (optionalElement.isPresent()) {
            Element processesElement = optionalElement.get();
            Collection<Element> processes = processesElement.getSubElements();
            processes.forEach(process -> {
                ZusammenElement processZusammenElement = buildElement(process.getElementId(), Action.DELETE);
                zusammenAdaptor.saveElement(context, elementContext, processZusammenElement, "Delete Process with id " + process.getElementId());
            });
        }
    }

    @Override
    public void deleteVspAll(String vspId, Version version) {
        SessionContext context = createSessionContext();
        ElementContext elementContext = new ElementContext(vspId, version.getId());
        Optional<Element> optionalElement = zusammenAdaptor.getElementByName(context, elementContext, null, ElementType.Processes.name());
        if (optionalElement.isPresent()) {
            Element processesElement = optionalElement.get();
            Collection<Element> processes = processesElement.getSubElements();
            processes.forEach(process -> {
                ZusammenElement processZusammenElement = buildElement(process.getElementId(), Action.DELETE);
                zusammenAdaptor.saveElement(context, elementContext, processZusammenElement, "Delete Process with id " + process.getElementId());
            });
        }
    }

    @Override
    public ProcessEntity getArtifact(ProcessEntity processEntity) {
        SessionContext context = createSessionContext();
        ElementContext elementContext = new ElementContext(processEntity.getVspId(), processEntity.getVersion().getId());
        return zusammenAdaptor.getElement(context, elementContext, processEntity.getId()).map(element -> {
            ProcessEntity process = new ElementToProcessConvertor().convert(element);
            process.setVspId(processEntity.getVspId());
            process.setVersion(processEntity.getVersion());
            process.setComponentId(processEntity.getComponentId());
            return process;
        }).orElse(null);
    }

    @Override
    public void uploadArtifact(ProcessEntity processEntity) {
        ProcessEntity retrieved = get(processEntity);
        if (retrieved != null) {
            retrieved.setArtifactName(processEntity.getArtifactName());
            retrieved.setArtifact(processEntity.getArtifact());
            update(retrieved, "Upload process artifact");
        }
    }

    @Override
    public void deleteArtifact(ProcessEntity processEntity) {
        ProcessEntity retrieved = get(processEntity);
        if (retrieved != null) {
            retrieved.setArtifactName(null);
            retrieved.setArtifact(null);
            update(retrieved, "Delete process artifact");
        }
    }

    @Override
    public Collection<ProcessEntity> list(ProcessEntity processEntity) {
        SessionContext context = createSessionContext();
        ElementContext elementContext = new ElementContext(processEntity.getVspId(), processEntity.getVersion().getId());
        Optional<ElementInfo> processesOptional = zusammenAdaptor
            .getElementInfoByName(context, elementContext, extractParentElementId(processEntity), ElementType.Processes.name());
        if (!processesOptional.isPresent()) {
            return new ArrayList<>();
        }
        ElementToProcessConvertor convertor = new ElementToProcessConvertor();
        return zusammenAdaptor.listElements(context, elementContext, processesOptional.get().getId()).stream()
            .map(elementInfo -> convertToProcessEntity(elementInfo, convertor, processEntity)).collect(Collectors.toList());
    }

    private ProcessEntity convertToProcessEntity(ElementInfo elementInfo, ElementToProcessConvertor convertor, ProcessEntity inputProcess) {
        ProcessEntity process = convertor.convert(elementInfo);
        process.setVspId(inputProcess.getVspId());
        process.setVersion(inputProcess.getVersion());
        process.setComponentId(inputProcess.getComponentId());
        return process;
    }

    private void update(ProcessEntity processEntity, String message) {
        ZusammenElement processElement = buildProcessElement(processEntity, Action.UPDATE);
        SessionContext context = createSessionContext();
        ElementContext elementContext = new ElementContext(processEntity.getVspId(), processEntity.getVersion().getId());
        zusammenAdaptor.saveElement(context, elementContext, processElement, message);
    }

    private Id extractParentElementId(ProcessEntity processEntity) {
        return processEntity.getComponentId() == null ? null : new Id(processEntity.getComponentId());
    }

    private ZusammenElement buildProcessElement(ProcessEntity process, Action action) {
        Info info = new Info();
        info.setName(process.getName());
        info.addProperty(NAME, process.getName());
        info.addProperty(ElementPropertyName.elementType.name(), ElementType.Process);
        info.addProperty(ARTIFACT_NAME, process.getArtifactName());
        info.addProperty(DESCRIPTION, process.getDescription());
        info.addProperty(PROCESS_TYPE, process.getType() != null ? process.getType().name() : null);
        ZusammenElement processElement = buildElement(new Id(process.getId()), action);
        processElement.setInfo(info);
        if (Objects.nonNull(process.getArtifact())) {
            processElement.setData(new ByteArrayInputStream(process.getArtifact().array()));
        }
        return processElement;
    }
}
