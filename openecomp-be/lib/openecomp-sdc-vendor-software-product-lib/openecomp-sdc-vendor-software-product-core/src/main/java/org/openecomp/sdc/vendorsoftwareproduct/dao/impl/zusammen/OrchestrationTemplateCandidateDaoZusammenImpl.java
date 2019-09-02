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

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.utils.fileutils.FileUtils;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Optional;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateCandidateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.versioning.dao.types.Version;

public class OrchestrationTemplateCandidateDaoZusammenImpl
    implements OrchestrationTemplateCandidateDao {

  private static final Logger logger =
      LoggerFactory.getLogger(OrchestrationTemplateCandidateDaoZusammenImpl.class);

  private final ZusammenAdaptor zusammenAdaptor;

  private static final String EMPTY_DATA = "{}";

  public OrchestrationTemplateCandidateDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {
    // registerVersioning not implemented for OrchestrationTemplateCandidateDaoZusammenImpl
  }

  @Override
  public Optional<OrchestrationTemplateCandidateData> get(String vspId, Version version) {
    logger.info("Getting orchestration template for vsp id {}", vspId);

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    Optional<Element> candidateElement =
        zusammenAdaptor.getElementByName(context, elementContext, null,
            ElementType.OrchestrationTemplateCandidate.name());

    if (!candidateElement.isPresent() ||
        VspZusammenUtil.hasEmptyData(candidateElement.get().getData()) ||
        candidateElement.get().getSubElements().isEmpty()) {
      logger.info("Orchestration template for vsp id {} does not exist / has empty data", vspId);
      return Optional.empty();
    }

    OrchestrationTemplateCandidateData candidate = new OrchestrationTemplateCandidateData();
    candidate.setFilesDataStructure(
        new String(FileUtils.toByteArray(candidateElement.get().getData())));

    candidateElement.get().getSubElements().stream()
        .map(element -> zusammenAdaptor
            .getElement(context, elementContext, element.getElementId().toString()))
        .forEach(element -> element.ifPresent(
            candidateInfoElement -> populateCandidate(candidate, candidateInfoElement, true)));

    logger.info("Finished getting orchestration template for vsp id {}", vspId);
    return candidate.getFileSuffix() == null ? Optional.empty() : Optional.of(candidate);
  }

  @Override
  public Optional<OrchestrationTemplateCandidateData> getInfo(String vspId, Version version) {
    logger.info("Getting orchestration template info for vsp id {}", vspId);

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    Optional<ElementInfo> candidateElement =
        zusammenAdaptor.getElementInfoByName(context, elementContext, null,
            ElementType.OrchestrationTemplateCandidate.name());

    if (!candidateElement.isPresent() || candidateElement.get().getSubElements().isEmpty()) {
      logger.info("Orchestration template info for vsp id {} does not exist", vspId);
      return Optional.empty();
    }

    OrchestrationTemplateCandidateData candidate = new OrchestrationTemplateCandidateData();
    candidateElement.get().getSubElements().stream()
        .map(elementInfo -> zusammenAdaptor
            .getElement(context, elementContext, elementInfo.getId().toString()))
        .forEach(element -> element.ifPresent(
            candidateInfoElement -> populateCandidate(candidate, candidateInfoElement, false)));
    logger.info("Finished getting orchestration template info for vsp id {}", vspId);
    return candidate.getFileSuffix() == null ? Optional.empty() : Optional.of(candidate);
  }

  private void populateCandidate(final OrchestrationTemplateCandidateData candidate,
                                 final Element candidateInfoElement,
                                 final boolean fullData) {
    final String elementName = candidateInfoElement.getInfo().getName();
    if (ElementType.OrchestrationTemplateCandidateContent.name().equals(elementName)) {
      if (fullData) {
        candidate.setContentData(ByteBuffer.wrap(FileUtils.toByteArray(candidateInfoElement.getData())));
      }
      candidate.setFileSuffix(candidateInfoElement.getInfo()
          .getProperty(InfoPropertyName.FILE_SUFFIX.getVal()));
      candidate.setFileName(candidateInfoElement.getInfo()
          .getProperty(InfoPropertyName.FILE_NAME.getVal()));
    } else if (ElementType.OrchestrationTemplateCandidateValidationData.name().equals(elementName)) {
      candidate.setValidationData(new String(FileUtils.toByteArray(candidateInfoElement.getData())));
    } else if (ElementType.ORIGINAL_ONBOARDED_PACKAGE.name().equals(elementName)) {
      candidate.setOriginalFileName(candidateInfoElement.getInfo()
          .getProperty(InfoPropertyName.ORIGINAL_FILE_NAME.getVal()));
      candidate.setOriginalFileSuffix(candidateInfoElement.getInfo()
          .getProperty(InfoPropertyName.ORIGINAL_FILE_SUFFIX.getVal()));
      if (fullData) {
        candidate.setOriginalFileContentData(
            ByteBuffer.wrap(FileUtils.toByteArray(candidateInfoElement.getData()))
        );
      }
    }
  }

  @Override
  public void delete(String vspId, Version version) {
    ByteArrayInputStream emptyData = new ByteArrayInputStream(EMPTY_DATA.getBytes());

    ZusammenElement candidateContentElement =
        buildStructuralElement(ElementType.OrchestrationTemplateCandidateContent, Action.UPDATE);
    candidateContentElement.setData(emptyData);

    ZusammenElement validationData = buildStructuralElement(ElementType
        .OrchestrationTemplateCandidateValidationData, Action.UPDATE);
    validationData.setData(emptyData);

    ZusammenElement candidateElement =
        buildStructuralElement(ElementType.OrchestrationTemplateCandidate, Action.UPDATE);
    candidateElement.setData(emptyData);
    candidateElement.addSubElement(candidateContentElement);
    candidateElement.addSubElement(validationData);

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());
    zusammenAdaptor.saveElement(context, elementContext, candidateElement,
        "Delete Orchestration Template Candidate Elements's content");
  }

  @Override
  public void update(final String vspId, final Version version,
                     final OrchestrationTemplateCandidateData candidateData) {
    logger.info("Uploading candidate data entity for vsp id {}", vspId);
    final ZusammenElement candidateElement =
        buildStructuralElement(ElementType.OrchestrationTemplateCandidate, Action.UPDATE);
    candidateElement
        .setData(new ByteArrayInputStream(candidateData.getFilesDataStructure().getBytes()));

    final ZusammenElement candidateContentElement =
        buildStructuralElement(ElementType.OrchestrationTemplateCandidateContent, Action.UPDATE);
    candidateContentElement
        .setData(new ByteArrayInputStream(candidateData.getContentData().array()));
    candidateContentElement.getInfo()
        .addProperty(InfoPropertyName.FILE_SUFFIX.getVal(), candidateData.getFileSuffix());
    candidateContentElement.getInfo()
        .addProperty(InfoPropertyName.FILE_NAME.getVal(), candidateData.getFileName());

    if (OnboardingTypesEnum.CSAR.toString().equalsIgnoreCase(candidateData.getFileSuffix())) {
      final ZusammenElement originalPackageElement =
          buildStructuralElement(ElementType.ORIGINAL_ONBOARDED_PACKAGE, Action.UPDATE);
      originalPackageElement.getInfo()
          .addProperty(InfoPropertyName.ORIGINAL_FILE_NAME.getVal(), candidateData.getOriginalFileName());
      originalPackageElement.getInfo()
          .addProperty(InfoPropertyName.ORIGINAL_FILE_SUFFIX.getVal(), candidateData.getOriginalFileSuffix());
      originalPackageElement.setData(new ByteArrayInputStream(candidateData.getOriginalFileContentData().array()));
      candidateElement.addSubElement(originalPackageElement);
    }
    final ZusammenElement validationData = buildStructuralElement(ElementType
        .OrchestrationTemplateCandidateValidationData, Action.UPDATE);
    if (candidateData.getValidationData() != null) {
      validationData
          .setData(new ByteArrayInputStream(candidateData.getValidationData().getBytes()));
    }
    candidateElement.addSubElement(validationData);
    candidateElement.addSubElement(candidateContentElement);
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());
    zusammenAdaptor.saveElement(context, elementContext, candidateElement,
        "Update Orchestration Template Candidate");
    logger.info("Finished uploading candidate data entity for vsp id {}", vspId);
  }

  @Override
  public void updateValidationData(String vspId, Version version,
                                   ValidationStructureList validationData) {
    logger
        .info("Updating validation data of orchestration template candidate for VSP id {} ", vspId);

    ZusammenElement validationDataElement = buildStructuralElement(ElementType
        .OrchestrationTemplateCandidateValidationData, Action.UPDATE);
    validationDataElement.setData(validationData == null ? new ByteArrayInputStream(EMPTY_DATA
        .getBytes()) : new ByteArrayInputStream(JsonUtil.object2Json(validationData).getBytes()));

    ZusammenElement candidateElement =
        buildStructuralElement(ElementType.OrchestrationTemplateCandidate, Action.IGNORE);
    candidateElement.addSubElement(validationDataElement);

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());
    zusammenAdaptor.saveElement(context, elementContext, candidateElement,
        "Update Orchestration Template Candidate validation data");
    logger
        .info("Finished updating validation data of orchestration template candidate for VSP id {}",
            vspId);
  }

  @Override
  public void updateStructure(String vspId, Version version, FilesDataStructure fileDataStructure) {
    logger.info("Updating orchestration template for VSP id {}", vspId);

    ZusammenElement candidateElement =
        buildStructuralElement(ElementType.OrchestrationTemplateCandidate, Action.UPDATE);
    candidateElement
        .setData(new ByteArrayInputStream(JsonUtil.object2Json(fileDataStructure).getBytes()));

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());
    zusammenAdaptor.saveElement(context, elementContext, candidateElement,
        "Update Orchestration Template Candidate structure");
    logger.info("Finished uploading candidate data entity for vsp id {}", vspId);
  }


  @Override
  public Optional<String> getStructure(String vspId, Version version) {
    logger.info("Getting orchestration template candidate structure for vsp id {}", vspId);

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    Optional<Element> element = zusammenAdaptor.getElementByName(context, elementContext, null,
        ElementType.OrchestrationTemplateCandidate.name());

    if (element.isPresent() && !VspZusammenUtil.hasEmptyData(element.get().getData())) {
      return Optional.of(new String(FileUtils.toByteArray(element.get().getData())));
    }

    logger.info("Finished getting orchestration template candidate structure for vsp id {}", vspId);

    return Optional.empty();
  }

  public enum InfoPropertyName {
    FILE_SUFFIX("fileSuffix"),
    FILE_NAME("fileName"),
    ORIGINAL_FILE_NAME("originalFilename"),
    ORIGINAL_FILE_SUFFIX("originalFileSuffix");

    private final String val;

    InfoPropertyName(String val) {
      this.val = val;
    }

    private String getVal() {
      return val;
    }
  }
}
