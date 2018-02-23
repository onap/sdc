/*
 * Copyright © 2018 European Support Limited
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

package org.openecomp.sdc.healing.healers;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.utils.fileutils.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenAdaptorFactory;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OnboardingMethod;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CandidateServiceFactory;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.services.utils.CandidateEntityBuilder;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

public class NetworkPackageHealer implements Healer {

  private VendorSoftwareProductInfoDao vspInfoDao;
  private ZusammenAdaptor zusammenAdaptor;
  private CandidateService candidateService;
  private static final String EMPTY_DATA = "{}";

  public NetworkPackageHealer() {
    this.vspInfoDao = VendorSoftwareProductInfoDaoFactory.getInstance().createInterface();
    this.zusammenAdaptor = ZusammenAdaptorFactory.getInstance().createInterface();
    this.candidateService = CandidateServiceFactory.getInstance().createInterface();
  }

  @Override
  public Object heal(String itemId, Version version) throws Exception {

    VspDetails vspDetails = vspInfoDao.get(new VspDetails(itemId, version));

    if (vspDetails.getOnboardingMethod().equals(OnboardingMethod.NetworkPackage.name())) {

      SessionContext context = createSessionContext();
      ElementContext elementContext = new ElementContext(vspDetails.getId(), version.getId());

      Optional<Element> orchestrationTemplateCandidateElement =
          zusammenAdaptor.getElementByName(context, elementContext, null,
              ElementType.OrchestrationTemplateCandidate.name());
      if (!isValidationElementAvailable(orchestrationTemplateCandidateElement, context,
          elementContext)) {
        updateOrchestrationTemplateCandidateStructure(context, elementContext);
      }

      Optional<ElementInfo> vspModel =
          zusammenAdaptor.getElementInfoByName(context, elementContext, null,
              ElementType.VspModel.name());
      Optional<Element> orchestrationTemplateElement = null;
      if (vspModel.isPresent()) {
        orchestrationTemplateElement =
            zusammenAdaptor.getElementByName(context, elementContext, vspModel.get().getId(),
                ElementType.OrchestrationTemplate.name());
      }
      if (Objects.isNull(orchestrationTemplateCandidateElement) || Objects.isNull
          (orchestrationTemplateElement)) {
        return null;
      }

      if (!orchestrationTemplateCandidateElement.isPresent() ||
          !orchestrationTemplateElement.isPresent()) {
        return null;
      }

      if (!isStructureElementAvailable(orchestrationTemplateElement, context, elementContext)) {
        updateVspModel(context, elementContext);
      }

      OrchestrationTemplateEntity orchestrationTemplateEntity =
          getOrchestrationTemplateEntity(orchestrationTemplateElement, context, elementContext);

      if (!StringUtils.isNotEmpty(orchestrationTemplateEntity.getFileSuffix())) {
        return null;
      }

      OrchestrationTemplateCandidateData orchestrationTemplateCandidateData =
          getOrchestrationTemplateCandidateData(orchestrationTemplateCandidateElement, context,
              elementContext);
      if (isEqual(orchestrationTemplateEntity, orchestrationTemplateCandidateData)) {
        heal(context, elementContext, orchestrationTemplateEntity,
            orchestrationTemplateElement, vspDetails, orchestrationTemplateCandidateElement);
      } else {
        healOrchestrationTemplateStructure(orchestrationTemplateElement, vspDetails, context,
            elementContext);
      }
    }

    return null;
  }

  private void heal(SessionContext context, ElementContext elementContext,
                    OrchestrationTemplateEntity orchestrationTemplateEntity,
                    Optional<Element> orchestrationTemplateElement, VspDetails vspDetails,
                    Optional<Element> orchestrationTemplateCandidateElement) throws Exception {

    if (isProcessedEntityValid(orchestrationTemplateEntity)) {

      Optional<Element> element = zusammenAdaptor.getElementByName(context, elementContext,
          orchestrationTemplateCandidateElement.get().getElementId(), ElementType
              .OrchestrationTemplateCandidateContent.name());

      ZusammenElement orchestrationTemplateCandidateContent = buildStructuralElement(ElementType
          .OrchestrationTemplateCandidateContent, Action.UPDATE);
      orchestrationTemplateCandidateContent
          .setData(new ByteArrayInputStream(EMPTY_DATA.getBytes()));
      if (element.isPresent()) {
        orchestrationTemplateCandidateContent.setElementId(element.get().getElementId());
      }

      ZusammenElement orchestrationTemplateCandidate = buildStructuralElement(ElementType
          .OrchestrationTemplateCandidate, Action.UPDATE);
      orchestrationTemplateCandidate.setData(new ByteArrayInputStream(EMPTY_DATA.getBytes()));
      orchestrationTemplateCandidate.setElementId(orchestrationTemplateCandidateElement.get()
          .getElementId());
      orchestrationTemplateCandidate.addSubElement(orchestrationTemplateCandidateContent);

      zusammenAdaptor.saveElement(context, elementContext, orchestrationTemplateCandidate, " "
          + "Healed Orchestration Template Candidate and Orchestration Template Candidate "
          + "Content");

      healOrchestrationTemplateStructure(orchestrationTemplateElement, vspDetails, context,
          elementContext);
    } else {
      ZusammenElement orchestrationTemplate =
          buildStructuralElement(ElementType.OrchestrationTemplate, Action.UPDATE);
      orchestrationTemplate.setData(new ByteArrayInputStream(EMPTY_DATA.getBytes()));
      orchestrationTemplate.setElementId(orchestrationTemplateElement.get().getElementId());

      Optional<Element> validationElement = zusammenAdaptor.getElementByName(context,
          elementContext, orchestrationTemplateElement.get().getElementId(), ElementType
              .OrchestrationTemplateValidationData.name());

      ZusammenElement orchestrationTemplateValidationData = buildStructuralElement(ElementType
          .OrchestrationTemplateValidationData, Action.UPDATE);
      if (validationElement.isPresent()) {
        orchestrationTemplateValidationData.setElementId(validationElement.get().getElementId());
      }
      orchestrationTemplateValidationData.setData(new ByteArrayInputStream(EMPTY_DATA.getBytes()));

      orchestrationTemplate.addSubElement(orchestrationTemplateValidationData);
      zusammenAdaptor.saveElement(context, elementContext, orchestrationTemplate, " "
          + "Healed Orchestration Template and Orchestration Template Validation data");

      Optional<Element> element = zusammenAdaptor.getElementByName(context, elementContext,
          orchestrationTemplateCandidateElement.get().getElementId(), ElementType
              .OrchestrationTemplateCandidateValidationData.name());

      ZusammenElement orchestrationTemplateCandidateValidationData = buildStructuralElement
          (ElementType.OrchestrationTemplateCandidateValidationData, Action.UPDATE);
      orchestrationTemplateCandidateValidationData
          .setData(new ByteArrayInputStream(orchestrationTemplateEntity
              .getValidationData().getBytes()));
      if (element.isPresent()) {
        orchestrationTemplateCandidateValidationData.setElementId(element.get().getElementId());
      }

      zusammenAdaptor
          .saveElement(context, elementContext, orchestrationTemplateCandidateValidationData,
              "Healed Orchestration Template Candidate Validation data");
    }
  }

  private boolean isProcessedEntityValid(OrchestrationTemplateEntity orchestrationTemplateEntity) {
    String validationData = orchestrationTemplateEntity.getValidationData();
    return !validationData.contains(ErrorLevel.ERROR.name());
  }

  private void healOrchestrationTemplateStructure(Optional<Element> orchestrationTemplateElement,
                                                  VspDetails vspDetails, SessionContext context,
                                                  ElementContext elementContext) throws Exception {
    byte[] byteData = FileUtils.toByteArray(orchestrationTemplateElement.get().getData());
    FileContentHandler contentMap = CommonUtil.validateAndUploadFileContent(
        OnboardingTypesEnum.ZIP, byteData);
    OrchestrationTemplateCandidateData orchestrationTemplateEntityData = new
        CandidateEntityBuilder(candidateService)
        .buildCandidateEntityFromZip(vspDetails, byteData, contentMap,
            null);
    String fileDataStructure = orchestrationTemplateEntityData.getFilesDataStructure();

    Optional<Element> element = zusammenAdaptor
        .getElementByName(context, elementContext,
            orchestrationTemplateElement.get().getElementId(),
            ElementType.OrchestrationTemplateStructure.name());

    ZusammenElement orchestrationTemplateStructure = buildStructuralElement(ElementType
        .OrchestrationTemplateStructure, Action.UPDATE);
    orchestrationTemplateStructure.setData(new ByteArrayInputStream(fileDataStructure.getBytes()));
    if (element.isPresent()) {
      orchestrationTemplateStructure.setElementId(element.get().getElementId());
    }

    zusammenAdaptor.saveElement(context, elementContext, orchestrationTemplateStructure, "Healed "
        + "Orchestration Template Structure");
  }

  private boolean isEqual(OrchestrationTemplateEntity orchestrationTemplateEntity,
                          OrchestrationTemplateCandidateData orchestrationTemplateCandidateData) {
    return orchestrationTemplateEntity.getFileName().equals(orchestrationTemplateCandidateData
        .getFileName()) && orchestrationTemplateEntity.getFileSuffix().equals
        (orchestrationTemplateCandidateData.getFileSuffix()) ? true : false;
  }

  private OrchestrationTemplateEntity getOrchestrationTemplateEntity(
      Optional<Element> orchestrationTemplateElement,
      SessionContext context,
      ElementContext elementContext) {
    OrchestrationTemplateEntity orchestrationTemplateEntity = new OrchestrationTemplateEntity();

    if (!orchestrationTemplateElement.isPresent()) {
      return orchestrationTemplateEntity;
    }

    if (!isEmpty(orchestrationTemplateElement.get().getData())) {
      orchestrationTemplateEntity.setContentData(
          ByteBuffer.wrap(FileUtils.toByteArray(orchestrationTemplateElement.get().getData())));
    }

    Optional<Element> validationDataElement =
        zusammenAdaptor.getElementByName(context, elementContext,
            orchestrationTemplateElement.get().getElementId(),
            ElementType.OrchestrationTemplateValidationData.name());
    if (validationDataElement.isPresent()) {
      orchestrationTemplateEntity.setFileSuffix(validationDataElement.get().getInfo()
          .getProperty(InfoPropertyName.FILE_SUFFIX.getVal()));
      orchestrationTemplateEntity.setFileName(validationDataElement.get().getInfo()
          .getProperty(InfoPropertyName.FILE_NAME.getVal()));
      if (!isEmpty(validationDataElement.get().getData())) {
        orchestrationTemplateEntity.setValidationData(
            new String(FileUtils.toByteArray(validationDataElement.get().getData())));
      }
    }
    return orchestrationTemplateEntity;
  }


  private OrchestrationTemplateCandidateData getOrchestrationTemplateCandidateData(
      Optional<Element> orchestrationTemplateCandidate, SessionContext context, ElementContext
      elementContext) {
    OrchestrationTemplateCandidateData candidateData = new
        OrchestrationTemplateCandidateData();
    if (!orchestrationTemplateCandidate.isPresent()) {
      return candidateData;
    }

    candidateData.setFilesDataStructure(
        new String(FileUtils.toByteArray(orchestrationTemplateCandidate.get().getData())));

    Optional<Element> candidateContentElement = zusammenAdaptor
        .getElementByName(context, elementContext,
            orchestrationTemplateCandidate.get().getElementId(),
            ElementType.OrchestrationTemplateCandidateContent.name());

    if (candidateContentElement.isPresent()) {
      candidateData.setContentData(
          ByteBuffer.wrap(FileUtils.toByteArray(candidateContentElement.get().getData())));
      candidateData.setFileSuffix(candidateContentElement.get().getInfo()
          .getProperty(InfoPropertyName.FILE_SUFFIX.getVal()));
      candidateData.setFileName(candidateContentElement.get().getInfo()
          .getProperty(InfoPropertyName.FILE_NAME.getVal()));
    }
    return candidateData;
  }

  private void updateOrchestrationTemplateCandidateStructure(SessionContext context, ElementContext
      elementContext) {

    ZusammenElement validationElement =
        buildStructuralElement(ElementType.OrchestrationTemplateCandidateValidationData,
            Action.CREATE);
    ZusammenElement candidateElement = buildStructuralElement(ElementType
        .OrchestrationTemplateCandidate, Action.IGNORE);
    candidateElement.addSubElement(validationElement);

    zusammenAdaptor.saveElement(context, elementContext, candidateElement, "Update Orchestration"
        + " Template Candidate Elements");
  }

  private boolean isValidationElementAvailable(Optional<Element> orchestrationTemplateCandidate,
                                               SessionContext context, ElementContext
                                                   elementContext) {
    if (orchestrationTemplateCandidate.isPresent()) {
      Optional<Element> validationElement = zusammenAdaptor
          .getElementByName(context, elementContext,
              orchestrationTemplateCandidate.get().getElementId(),
              ElementType.OrchestrationTemplateCandidateValidationData.name());
      if (validationElement.isPresent()) {
        return true;
      }
    }
    return false;
  }

  private void updateVspModel(SessionContext context, ElementContext elementContext) {

    ZusammenElement orchestrationTemplateStructureElement = buildStructuralElement(ElementType
        .OrchestrationTemplateStructure, Action.CREATE);
    ZusammenElement orchestrationTemplateElement = buildStructuralElement(ElementType
        .OrchestrationTemplate, Action.IGNORE);
    orchestrationTemplateElement.addSubElement(orchestrationTemplateStructureElement);

    ZusammenElement vspModel = buildStructuralElement(ElementType.VspModel, Action.IGNORE);
    vspModel.addSubElement(orchestrationTemplateElement);

    zusammenAdaptor.saveElement(context, elementContext, vspModel, "Update VSP Model Elements");

  }

  private boolean isStructureElementAvailable(Optional<Element> orchestrationTemplate,
                                              SessionContext context,
                                              ElementContext elementContext) {
    if (orchestrationTemplate.isPresent()) {
      Optional<Element> structureElement = zusammenAdaptor
          .getElementByName(context, elementContext, orchestrationTemplate.get().getElementId(),
              ElementType.OrchestrationTemplateStructure.name());
      if (structureElement.isPresent()) {
        return true;
      }
    }
    return false;
  }

  public enum InfoPropertyName {
    FILE_SUFFIX("fileSuffix"),
    FILE_NAME("fileName");

    private String val;

    InfoPropertyName(String val) {
      this.val = val;
    }

    private String getVal() {
      return val;
    }
  }

  private boolean isEmpty(InputStream elementData) {
    byte[] byteElementData;
    if (Objects.isNull(elementData)) {
      return true;
    }
    try {
      byteElementData = IOUtils.toByteArray(elementData);
    } catch (IOException e) {
      return false;
    }
    return ArrayUtils.isEmpty(byteElementData);
  }
}
