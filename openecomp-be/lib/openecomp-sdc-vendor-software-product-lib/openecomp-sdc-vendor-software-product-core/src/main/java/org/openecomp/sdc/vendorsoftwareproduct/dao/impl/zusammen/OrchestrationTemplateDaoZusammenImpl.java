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

package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.utils.fileutils.FileUtils;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Optional;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

public class OrchestrationTemplateDaoZusammenImpl implements OrchestrationTemplateDao {

  private static final Logger LOGGER = LoggerFactory.getLogger
      (OrchestrationTemplateDaoZusammenImpl.class);
  private ZusammenAdaptor zusammenAdaptor;

  public OrchestrationTemplateDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {
    // registerVersioning not implemented for OrchestrationTemplateDaoZusammenImpl
  }

  @Override
  public OrchestrationTemplateEntity getInfo(String vspId, Version version) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    Optional<ElementInfo> vspModel = zusammenAdaptor
        .getElementInfoByName(context, elementContext, null, ElementType.VspModel.name());
    if (!vspModel.isPresent()) {
      return null;
    }

    Optional<ElementInfo> elementInfo = zusammenAdaptor
        .getElementInfoByName(context, elementContext, vspModel.get().getId(),
            ElementType.OrchestrationTemplate.name());
    if (!elementInfo.isPresent()) {
      return null;
    }

    Optional<Element> element =
        zusammenAdaptor.getElementByName(context, elementContext, elementInfo.get().getId(),
            ElementType.OrchestrationTemplateValidationData.name());

    OrchestrationTemplateEntity orchestrationTemplate = new OrchestrationTemplateEntity();
    if (!element.isPresent()) {
      return orchestrationTemplate;
    }
    orchestrationTemplate
        .setFileSuffix(element.get().getInfo().getProperty(InfoPropertyName.FILE_SUFFIX.getVal()));
    orchestrationTemplate
        .setFileName(element.get().getInfo().getProperty(InfoPropertyName.FILE_NAME.getVal()));
    if (!VspZusammenUtil.hasEmptyData(element.get().getData())) {
      orchestrationTemplate
          .setValidationData(new String(FileUtils.toByteArray(element.get().getData())));
    }
    return orchestrationTemplate;
  }

  @Override
  public OrchestrationTemplateEntity get(String vspId, Version version) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    OrchestrationTemplateEntity orchestrationTemplate = new OrchestrationTemplateEntity();

    Optional<ElementInfo> vspModel = zusammenAdaptor
        .getElementInfoByName(context, elementContext, null, ElementType.VspModel.name());
    if (!vspModel.isPresent()) {
      return orchestrationTemplate;
    }

    Optional<Element> orchestrationTemplateElement = zusammenAdaptor
        .getElementByName(context, elementContext, vspModel.get().getId(),
            ElementType.OrchestrationTemplate.name());
    if (!orchestrationTemplateElement.isPresent()) {
      return orchestrationTemplate;
    }

    if (!VspZusammenUtil.hasEmptyData(orchestrationTemplateElement.get().getData())) {
      orchestrationTemplate.setContentData(
          ByteBuffer.wrap(FileUtils.toByteArray(orchestrationTemplateElement.get().getData())));
    }

    Optional<Element> validationDataElement =
        zusammenAdaptor.getElementByName(context, elementContext,
            orchestrationTemplateElement.get().getElementId(),
            ElementType.OrchestrationTemplateValidationData.name());
    if (validationDataElement.isPresent()) {
      orchestrationTemplate.setFileSuffix(validationDataElement.get().getInfo()
          .getProperty(InfoPropertyName.FILE_SUFFIX.getVal()));
      orchestrationTemplate.setFileName(validationDataElement.get().getInfo()
          .getProperty(InfoPropertyName.FILE_NAME.getVal()));
      if (!VspZusammenUtil.hasEmptyData(validationDataElement.get().getData())) {
        orchestrationTemplate.setValidationData(
            new String(FileUtils.toByteArray(validationDataElement.get().getData())));
      }
    }
    return orchestrationTemplate;
  }

  @Override
  public void update(String vspId, Version version,
                     OrchestrationTemplateEntity orchestrationTemplate) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    ZusammenElement validationData =
        buildStructuralElement(ElementType.OrchestrationTemplateValidationData, Action.UPDATE);
    validationData
        .setData(new ByteArrayInputStream(orchestrationTemplate.getValidationData().getBytes()));
    validationData.getInfo()
        .addProperty(InfoPropertyName.FILE_SUFFIX.getVal(), orchestrationTemplate.getFileSuffix());
    validationData.getInfo()
        .addProperty(InfoPropertyName.FILE_NAME.getVal(), orchestrationTemplate.getFileName());
    ZusammenElement orchestrationTemplateStructure = buildStructuralElement(ElementType
        .OrchestrationTemplateStructure, Action.UPDATE);
    orchestrationTemplateStructure
        .setData(new ByteArrayInputStream(orchestrationTemplate.getFilesDataStructure()
            .getBytes()));
    ZusammenElement orchestrationTemplateElement =
        buildStructuralElement(ElementType.OrchestrationTemplate, Action.UPDATE);
    orchestrationTemplateElement
        .setData(new ByteArrayInputStream(orchestrationTemplate.getContentData().array()));
    orchestrationTemplateElement.addSubElement(validationData);
    orchestrationTemplateElement.addSubElement(orchestrationTemplateStructure);
    ZusammenElement vspModel = buildStructuralElement(ElementType.VspModel, Action.IGNORE);
    vspModel.addSubElement(orchestrationTemplateElement);

    zusammenAdaptor.saveElement(context, elementContext, vspModel, "Update Orchestration Template");
  }

  @Override
  public Optional<String> getOrchestrationTemplateStructure(String vspId, Version version) {
    LOGGER.info("Getting orchestration template structure for VendorSoftwareProduct id" +
        " " + "-> " + vspId);

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    Optional<ElementInfo> vspModel = zusammenAdaptor.getElementInfoByName(context, elementContext,
        null, ElementType.VspModel.name());
    if (!vspModel.isPresent()) {
      return Optional.empty();
    }
    Optional<Element> orchestrationTemplateElement = zusammenAdaptor.getElementByName(context,
        elementContext, vspModel.get().getId(), ElementType.OrchestrationTemplate.name());
    if (!orchestrationTemplateElement.isPresent()) {
      return Optional.empty();
    }

    Optional<Element> orchestrationTemplateStructureElement = zusammenAdaptor
        .getElementByName(context, elementContext,
            orchestrationTemplateElement.get().getElementId(),
            ElementType.OrchestrationTemplateStructure.name());
    if (orchestrationTemplateStructureElement.isPresent() &&
        !VspZusammenUtil.isEmpty(orchestrationTemplateStructureElement.get().getData())) {
      return Optional.of(new String(
          FileUtils.toByteArray(orchestrationTemplateStructureElement.get().getData())));
    }
    LOGGER.info("Finished getting orchestration template structure for VendorSoftwareProduct " +
        "id -> " + vspId);

    return Optional.empty();
  }

  private enum InfoPropertyName {
    FILE_SUFFIX("fileSuffix"),
    FILE_NAME("fileName");

    private String val;

    InfoPropertyName(String val){
      this.val=val;
    }

    public String getVal() {
      return val;
    }
  }
}
