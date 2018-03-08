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
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateCandidateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Optional;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

public class OrchestrationTemplateCandidateDaoZusammenImpl
    implements OrchestrationTemplateCandidateDao {

  private static final Logger logger =
      LoggerFactory.getLogger(OrchestrationTemplateCandidateDaoZusammenImpl.class);

  private ZusammenAdaptor zusammenAdaptor;

  private static final String EMPTY_DATA = "{}";

  public OrchestrationTemplateCandidateDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {
    // registerVersioning not implemented for OrchestrationTemplateCandidateDaoZusammenImpl
  }

  @Override
  public OrchestrationTemplateCandidateData get(String vspId, Version version) {
    logger.info("Getting orchestration template for VendorSoftwareProduct id -> " + vspId);

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    Optional<Element> candidateElement =
        zusammenAdaptor.getElementByName(context, elementContext, null,
            ElementType.OrchestrationTemplateCandidate.name());
    if (candidateElement.isPresent()) {
      if (VspZusammenUtil.isEmpty(candidateElement.get().getData())) {
        return null;
      }
      OrchestrationTemplateCandidateData candidateData = new OrchestrationTemplateCandidateData();
      candidateData.setFilesDataStructure(
          new String(FileUtils.toByteArray(candidateElement.get().getData())));

      Collection<Element> subElements = candidateElement.get().getSubElements();
      if (subElements.isEmpty()) {
        return candidateData;
      }

      for (Element element : subElements) {
        Optional<Element> subElement = zusammenAdaptor.getElement(context,
            elementContext, element.getElementId().toString());

        if (subElement.get().getInfo().getName()
            .equals(ElementType.OrchestrationTemplateCandidateContent
                .name())) {
          candidateData.setContentData(
              ByteBuffer.wrap(FileUtils.toByteArray(subElement.get().getData())));
          candidateData.setFileSuffix(subElement.get().getInfo()
              .getProperty(InfoPropertyName.FILE_SUFFIX.getVal()));
          candidateData.setFileName(subElement.get().getInfo()
              .getProperty(InfoPropertyName.FILE_NAME.getVal()));
        } else if (subElement.get().getInfo().getName()
            .equals(ElementType.OrchestrationTemplateCandidateValidationData.name())) {
          candidateData.setValidationData(new String(FileUtils.toByteArray(subElement
              .get().getData())));
        }
      }

      logger
          .info("Finished getting orchestration template for VendorSoftwareProduct id -> " + vspId);
      return candidateData;
    }
    logger.info(String
        .format("Orchestration template for VendorSoftwareProduct id %s does not exist", vspId));
    return null;
  }

  @Override
  public OrchestrationTemplateCandidateData getInfo(String vspId, Version version) {
    logger.info("Getting orchestration template info for VendorSoftwareProduct id -> " + vspId);

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    OrchestrationTemplateCandidateData candidateData = new OrchestrationTemplateCandidateData();

    Optional<ElementInfo> candidateElement =
        zusammenAdaptor.getElementInfoByName(context, elementContext, null,
            ElementType.OrchestrationTemplateCandidate.name());

    if (candidateElement.isPresent()) {
      Collection<ElementInfo> subElements = candidateElement.get().getSubElements();
      if (subElements.isEmpty()) {
        return candidateData;
      }

      for (ElementInfo elementInfo : subElements) {
        Optional<Element> subElement = zusammenAdaptor.getElement(context,
            elementContext, elementInfo.getId().toString());

        if (subElement.get().getInfo().getName().equals(ElementType
            .OrchestrationTemplateCandidateContent.name())) {

          candidateData.setFileSuffix(subElement.get().getInfo()
              .getProperty(InfoPropertyName.FILE_SUFFIX.getVal()));
          candidateData.setFileName(subElement.get().getInfo()
              .getProperty(InfoPropertyName.FILE_NAME.getVal()));
        } else if (subElement.get().getInfo().getName().equals(ElementType
            .OrchestrationTemplateCandidateValidationData.name())) {
          candidateData.setValidationData(new String(FileUtils.toByteArray(subElement.get()
              .getData())));
        }
      }

      logger.info(
          "Finished getting orchestration template info for VendorSoftwareProduct id -> " + vspId);
      return candidateData;
    }
    logger.info(String
        .format("Orchestration template info for VendorSoftwareProduct id %s does not exist",
            vspId));
    return null;
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
  public void update(String vspId, Version version,
                     OrchestrationTemplateCandidateData candidateData) {
    logger.info("Uploading candidate data entity for VendorSoftwareProduct id -> " + vspId);

    ZusammenElement candidateElement =
        buildStructuralElement(ElementType.OrchestrationTemplateCandidate, Action.UPDATE);
    candidateElement
        .setData(new ByteArrayInputStream(candidateData.getFilesDataStructure().getBytes()));

    ZusammenElement candidateContentElement =
        buildStructuralElement(ElementType.OrchestrationTemplateCandidateContent, Action.UPDATE);
    candidateContentElement
        .setData(new ByteArrayInputStream(candidateData.getContentData().array()));
    candidateContentElement.getInfo()
        .addProperty(InfoPropertyName.FILE_SUFFIX.getVal(), candidateData.getFileSuffix());
    candidateContentElement.getInfo()
        .addProperty(InfoPropertyName.FILE_NAME.getVal(), candidateData.getFileName());

    ZusammenElement validationData = buildStructuralElement(ElementType
        .OrchestrationTemplateCandidateValidationData, Action.UPDATE);
    if (candidateData.getValidationData() != null) {
      validationData
          .setData(new ByteArrayInputStream(candidateData.getValidationData().getBytes()));
    }
    candidateElement.addSubElement(candidateContentElement);
    candidateElement.addSubElement(validationData);
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());
    zusammenAdaptor.saveElement(context, elementContext, candidateElement,
        "Update Orchestration Template Candidate");
    logger
        .info("Finished uploading candidate data entity for VendorSoftwareProduct id -> " + vspId);
  }

  @Override
  public void updateValidationData(String vspId, Version version, ValidationStructureList
      validationData) {
    logger.info("Updating validation data of  orchestration template candidate for VSP id -> "
        + vspId);

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
    logger.info("Finished updating validation data of  orchestration template candidate for VSP "
        + "id -> " + vspId);
  }

  @Override
  public void updateStructure(String vspId, Version version, FilesDataStructure fileDataStructure) {
    logger.info("Updating orchestration template for VSP id -> " + vspId);

    ZusammenElement candidateElement =
        buildStructuralElement(ElementType.OrchestrationTemplateCandidate, Action.UPDATE);
    candidateElement
        .setData(new ByteArrayInputStream(JsonUtil.object2Json(fileDataStructure).getBytes()));

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());
    zusammenAdaptor.saveElement(context, elementContext, candidateElement,
        "Update Orchestration Template Candidate structure");
    logger
        .info("Finished uploading candidate data entity for VendorSoftwareProduct id -> " + vspId);
  }


  @Override
  public Optional<String> getStructure(String vspId, Version version) {
    logger.info("Getting orchestration template candidate structure for VendorSoftwareProduct id "
        + "-> " + vspId);

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    Optional<Element> element = zusammenAdaptor.getElementByName(context, elementContext, null,
        ElementType.OrchestrationTemplateCandidate.name());

    if (element.isPresent() && !VspZusammenUtil.hasEmptyData(element.get().getData())) {
      return Optional.of(new String(FileUtils.toByteArray(element.get().getData())));
    }

    logger.info(
        "Finished getting orchestration template candidate structure for VendorSoftwareProduct "
            + "id -> " + vspId);

    return Optional.empty();
  }

  public enum InfoPropertyName {
    FILE_SUFFIX("fileSuffix"),
    FILE_NAME("fileName");

    private String val;

    InfoPropertyName(String val){
      this.val = val;
    }

    public String getVal() {
      return val;
    }
  }
}
