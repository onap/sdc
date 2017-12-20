package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.utils.fileutils.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateCandidateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

public class OrchestrationTemplateCandidateDaoZusammenImpl
    implements OrchestrationTemplateCandidateDao {

  private static final Logger logger =
      LoggerFactory.getLogger(OrchestrationTemplateCandidateDaoZusammenImpl.class);

  private ZusammenAdaptor zusammenAdaptor;

  public OrchestrationTemplateCandidateDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {

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
      if (hasEmptyData(candidateElement.get().getData())) {
        return null;
      }
      OrchestrationTemplateCandidateData candidateData = new OrchestrationTemplateCandidateData();
      candidateData.setFilesDataStructure(
          new String(FileUtils.toByteArray(candidateElement.get().getData())));

      Optional<Element> candidateContentElement = zusammenAdaptor
          .getElementByName(context, elementContext, candidateElement.get().getElementId(),
              ElementType.OrchestrationTemplateCandidateContent.name());

      if (candidateContentElement.isPresent()) {
        candidateData.setContentData(
            ByteBuffer.wrap(FileUtils.toByteArray(candidateContentElement.get().getData())));
        candidateData.setFileSuffix(candidateContentElement.get().getInfo()
            .getProperty(InfoPropertyName.fileSuffix.name()));
        candidateData.setFileName(candidateContentElement.get().getInfo()
            .getProperty(InfoPropertyName.fileName.name()));
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

    Optional<ElementInfo> candidateElement =
        zusammenAdaptor.getElementInfoByName(context, elementContext, null,
            ElementType.OrchestrationTemplateCandidate.name());
    if (candidateElement.isPresent()) {
      OrchestrationTemplateCandidateData candidateData = new OrchestrationTemplateCandidateData();

      Optional<Element> candidateContentElement = zusammenAdaptor
          .getElementByName(context, elementContext, candidateElement.get().getId(),
              ElementType.OrchestrationTemplateCandidateContent.name());

      if (candidateContentElement.isPresent()) {
        candidateData.setFileSuffix(candidateContentElement.get().getInfo()
            .getProperty(InfoPropertyName.fileSuffix.name()));
        candidateData.setFileName(candidateContentElement.get().getInfo()
            .getProperty(InfoPropertyName.fileName.name()));
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
        .addProperty(InfoPropertyName.fileSuffix.name(), candidateData.getFileSuffix());
    candidateContentElement.getInfo()
        .addProperty(InfoPropertyName.fileName.name(), candidateData.getFileName());
    candidateElement.addSubElement(candidateContentElement);

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());
    zusammenAdaptor.saveElement(context, elementContext, candidateElement,
        "Update Orchestration Template Candidate");
    logger
        .info("Finished uploading candidate data entity for VendorSoftwareProduct id -> " + vspId);
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
    logger
        .info("Getting orchestration template structure for VendorSoftwareProduct id -> " + vspId);

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    logger.info(
        "Finished getting orchestration template structure for VendorSoftwareProduct id -> " +
            vspId);
    Optional<Element> element = zusammenAdaptor.getElementByName(context, elementContext, null,
        ElementType.OrchestrationTemplateCandidate.name());
    if (element.isPresent()) {
      if (hasEmptyData(element.get().getData())) {
        return Optional.empty();
      }
      return Optional.of(new String(FileUtils.toByteArray(element.get().getData())));
    } else {
      return Optional.empty();
    }
  }

  private boolean hasEmptyData(InputStream elementData) {
    String emptyData = "{}";
    byte[] byteElementData;
    try {
      byteElementData = IOUtils.toByteArray(elementData);
    } catch (IOException ex) {
      ex.printStackTrace();
      return false;
    }
    if (Arrays.equals(emptyData.getBytes(), byteElementData)) {
      return true;
    }
    return false;
  }

  public enum InfoPropertyName {
    fileSuffix,
    fileName
  }
}
