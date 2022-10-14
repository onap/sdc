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
package org.openecomp.sdc.model.impl.zusammen;

import static org.openecomp.core.model.types.ToscaServiceModelProperty.BASE;
import static org.openecomp.core.model.types.ToscaServiceModelProperty.MODELS;
import static org.openecomp.core.zusammen.api.ZusammenUtil.buildElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.errors.RetrieveServiceTemplateFromDbErrorBuilder;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.types.ElementPropertyName;

public class ServiceModelDaoZusammenImpl implements ServiceModelDao<ToscaServiceModel> {

    private static final Logger logger = LoggerFactory.getLogger(ServiceModelDaoZusammenImpl.class);
    protected ZusammenAdaptor zusammenAdaptor;
    protected ElementType elementType;

    public ServiceModelDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
        this.zusammenAdaptor = zusammenAdaptor;
        this.elementType = ElementType.ServiceModel;
    }

    @Override
    public void registerVersioning(String versionableEntityType) {
    }

    @Override
    public ToscaServiceModel getServiceModel(final String vspId, final Version version) {
        final var context = ZusammenUtil.createSessionContext();
        final var elementContext = new ElementContext(vspId, version.getId());
        final Optional<ElementInfo> serviceModelOpt = getServiceModelElementInfo(context, elementContext);
        if (serviceModelOpt.isEmpty()) {
            return null;
        }
        final var serviceModelElementInfo = serviceModelOpt.get();
        final var serviceModelElementId = serviceModelElementInfo.getId();
        final Map<String, ServiceTemplate> serviceTemplates = getTemplates(context, elementContext, serviceModelElementId);
        if (serviceTemplates == null) {
            return null;
        }
        final FileContentHandler artifacts = getArtifacts(context, elementContext, serviceModelElementId);
        final String entryDefinitionServiceTemplate = serviceModelElementInfo.getInfo().getProperty(BASE.getName());
        final List<String> modelList = serviceModelElementInfo.getInfo().getProperty(MODELS.getName());
        return new ToscaServiceModel(modelList, artifacts, serviceTemplates, entryDefinitionServiceTemplate);
    }

    @Override
    public void storeServiceModel(final String vspId, final Version version, final ToscaServiceModel serviceModel) {
        logger.info("Storing service model for VendorSoftwareProduct id '{}', version '{}', models '{}'", vspId, version,
            String.join(",", serviceModel.getModelList() == null ? Collections.emptyList() : serviceModel.getModelList()));
        final ZusammenElement templatesElement = buildStructuralElement(ElementType.Templates, Action.UPDATE);
        serviceModel.getServiceTemplates().forEach((key, value) -> templatesElement
            .addSubElement(buildServiceTemplateElement(key, value, serviceModel.getEntryDefinitionServiceTemplate(), Action.CREATE)));
        final ZusammenElement artifactsElement = buildStructuralElement(ElementType.Artifacts, Action.UPDATE);
        if (Objects.nonNull(serviceModel.getArtifactFiles())) {
            serviceModel.getArtifactFiles().getFiles()
                .forEach((key, value) -> artifactsElement.addSubElement(buildArtifactElement(key, value, Action.CREATE)));
        }
        final ZusammenElement serviceModelElement = buildServiceModelElement(serviceModel.getEntryDefinitionServiceTemplate());
        serviceModelElement.getInfo().addProperty(MODELS.getName(), serviceModel.getModelList());
        serviceModelElement.addSubElement(templatesElement);
        serviceModelElement.addSubElement(artifactsElement);
        final ZusammenElement vspModel = buildStructuralElement(ElementType.VspModel, Action.IGNORE);
        vspModel.getInfo().addProperty(MODELS.getName(), serviceModel.getModelList());
        vspModel.addSubElement(serviceModelElement);
        final var context = ZusammenUtil.createSessionContext();
        final var elementContext = new ElementContext(vspId, version.getId());
        zusammenAdaptor.saveElement(context, elementContext, vspModel, "Store service model");
        logger.info("Finished storing {} for VendorSoftwareProduct id -> {}", elementType.name(), vspId);
    }

    @Override
    public void deleteAll(String vspId, Version version) {
        logger.info("Started deleting content of Templates and Artifacts of {} of vsp {} version {}", elementType.name(), vspId, version.getId());
        SessionContext context = ZusammenUtil.createSessionContext();
        ElementContext elementContext = new ElementContext(vspId, version.getId());
        Optional<ElementInfo> serviceModel = getServiceModelElementInfo(context, elementContext);
        if (serviceModel.isEmpty()) {
            logger.info("{} of vsp {} version {} does not exist - nothing to delete", elementType.name(), vspId, version.getId());
            return;
        }
        ZusammenElement serviceModelElement = buildElement(serviceModel.get().getId(), Action.IGNORE);
        for (Id serviceModelSubElementId : serviceModel.get().getSubElements().stream().map(ElementInfo::getId).collect(Collectors.toSet())) {
            ElementInfo serviceModelSubElementInfo = zusammenAdaptor.getElementInfo(context, elementContext, serviceModelSubElementId).orElseThrow(
                () -> new IllegalStateException(String
                    .format("Element %s declared as sub element of element %s (%s) does not exist", serviceModelSubElementId.getValue(),
                        serviceModel.get().getId().getValue(), elementType.name())));
            if (ElementType.Templates.name().equals(serviceModelSubElementInfo.getInfo().getName()) || ElementType.Artifacts.name()
                .equals(serviceModelSubElementInfo.getInfo().getName())) {
                ZusammenElement serviceModelSubElement = buildElement(serviceModelSubElementId, Action.IGNORE);
                serviceModelSubElement.setSubElements(
                    serviceModelSubElementInfo.getSubElements().stream().map(elementInfo -> buildElement(elementInfo.getId(), Action.DELETE))
                        .collect(Collectors.toSet()));
                serviceModelElement.addSubElement(serviceModelSubElement);
            }
        }
        zusammenAdaptor.saveElement(context, elementContext, serviceModelElement,
            String.format("Delete content of Templates and Artifacts of %s", elementType.name()));
        logger.info("Finished deleting content of Templates and Artifacts of {} of vsp {} version {}", elementType.name(), vspId, version.getId());
    }

    @Override
    public void overrideServiceModel(String vspId, Version version, ToscaServiceModel serviceModel) {
        SessionContext context = ZusammenUtil.createSessionContext();
        ElementContext elementContext = new ElementContext(vspId, version.getId());
        Optional<ElementInfo> origServiceModel = getServiceModelElementInfo(context, elementContext);
        if (!origServiceModel.isPresent()) {
            return;
        }
        Id serviceModelElementId = origServiceModel.get().getId();
        ZusammenElement serviceModelElement = buildServiceModelElement(serviceModel.getEntryDefinitionServiceTemplate());
        serviceModelElement.setElementId(serviceModelElementId);
        overrideServiceTemplates(serviceModelElementId, serviceModel, context, elementContext, serviceModelElement);
        zusammenAdaptor.saveElement(context, elementContext, serviceModelElement, "Override service model");
    }

    private void overrideServiceTemplates(Id serviceModelElementId, ToscaServiceModel serviceModel, SessionContext context,
                                          ElementContext elementContext, ZusammenElement serviceModelElement) {
        Optional<ElementInfo> elementInfo = zusammenAdaptor
            .getElementInfoByName(context, elementContext, serviceModelElementId, ElementType.Templates.name());
        if (elementInfo.isEmpty()) {
            return;
        }
        ZusammenElement templateElement = buildStructuralElement(ElementType.Templates, Action.UPDATE);
        templateElement.setElementId(elementInfo.get().getId());
        serviceModel.getServiceTemplates().forEach((templateName, serviceTemplate) -> templateElement.addSubElement(
            buildServiceTemplateElement(templateName, serviceTemplate, serviceModel.getEntryDefinitionServiceTemplate(), Action.UPDATE)));
        serviceModelElement.addSubElement(templateElement);
    }

    private Optional<ElementInfo> getServiceModelElementInfo(SessionContext context, ElementContext elementContext) {
        Collection<ElementInfo> vspModelSubs = zusammenAdaptor.listElementsByName(context, elementContext, null, ElementType.VspModel.name());
        return vspModelSubs.stream()
            .filter(elementInfo -> elementInfo.getInfo() != null && elementType.name().equals(elementInfo.getInfo().getName())).findFirst();
    }

    private Map<String, ServiceTemplate> getTemplates(SessionContext context, ElementContext elementContext, Id serviceModelElementId) {
        Optional<ElementInfo> templatesElementInfo = zusammenAdaptor
            .getElementInfoByName(context, elementContext, serviceModelElementId, ElementType.Templates.name());
        if (templatesElementInfo.isPresent()) {
            Collection<Element> elements = zusammenAdaptor.listElementData(context, elementContext, templatesElementInfo.get().getId());
            return elements.stream().collect(Collectors.toMap(element -> element.getInfo().getName(), this::elementToServiceTemplate));
        }
        return null;
    }

    private FileContentHandler getArtifacts(SessionContext context, ElementContext elementContext, Id serviceModelElementId) {
        Optional<ElementInfo> artifactsElement = zusammenAdaptor
            .getElementInfoByName(context, elementContext, serviceModelElementId, ElementType.Artifacts.name());
        if (artifactsElement.isPresent()) {
            Collection<Element> elements = zusammenAdaptor.listElementData(context, elementContext, artifactsElement.get().getId());
            FileContentHandler fileContentHandler = new FileContentHandler();
            elements.forEach(element -> fileContentHandler.addFile(element.getInfo().getName(), element.getData()));
            return fileContentHandler;
        }
        return null;
    }

    private ZusammenElement buildServiceModelElement(String entryDefinitionServiceTemplate) {
        ZusammenElement serviceModelElement = buildStructuralElement(elementType, Action.UPDATE);
        serviceModelElement.getInfo().addProperty(BASE.getName(), entryDefinitionServiceTemplate);
        return serviceModelElement;
    }

    private Element buildServiceTemplateElement(String name, ServiceTemplate serviceTemplate, String entryDefinitionServiceTemplate, Action action) {
        ZusammenElement zusammenElement = buildElement(null, action);
        Info info = new Info();
        info.setName(name);
        info.setDescription(serviceTemplate.getDescription());
        info.addProperty(ElementPropertyName.elementType.name(), ElementType.ServiceTemplate.name());
        info.addProperty(BASE.getName(), entryDefinitionServiceTemplate);
        String yaml = new ToscaExtensionYamlUtil().objectToYaml(serviceTemplate);
        zusammenElement.setData(new ByteArrayInputStream(yaml.getBytes()));
        zusammenElement.setInfo(info);
        return zusammenElement;
    }

    protected ZusammenElement buildArtifactElement(String name, byte[] artifact, Action action) {
        ZusammenElement artifactElement = buildElement(null, action);
        Info info = new Info();
        info.setName(name);
        info.addProperty(ElementPropertyName.elementType.name(), ElementType.Artifact.name());
        artifactElement.setInfo(info);
        artifactElement.setData(new ByteArrayInputStream(artifact));
        return artifactElement;
    }

    private ServiceTemplate elementToServiceTemplate(Element element) {
        try {
            String yamlContent = IOUtils.toString(element.getData());
            return new ToscaExtensionYamlUtil().yamlToObject(yamlContent, ServiceTemplate.class);
        } catch (Exception e) {
            throw new CoreException(new RetrieveServiceTemplateFromDbErrorBuilder(element.getInfo().getName(), e.getMessage()).build());
        }
    }
}
