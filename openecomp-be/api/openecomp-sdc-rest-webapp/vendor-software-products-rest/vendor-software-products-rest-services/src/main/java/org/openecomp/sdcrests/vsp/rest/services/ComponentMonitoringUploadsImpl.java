package org.openecomp.sdcrests.vsp.rest.services;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.openecomp.core.enrichment.types.MonitoringUploadType;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.MdcUtil;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.messages.AuditMessages;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.MonitoringUploadsManager;
import org.openecomp.sdc.vendorsoftwareproduct.MonitoringUploadsManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.MonitoringUploadStatus;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.MonitoringUploadStatusDto;
import org.openecomp.sdcrests.vsp.rest.ComponentMonitoringUploads;
import org.openecomp.sdcrests.vsp.rest.mapping.MapMonitoringUploadStatusToDto;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.io.InputStream;

/**
 * @author katyr
 * @since June 26, 2017
 */

@Named
@Service("componentMonitoringUploads")
@Scope(value = "prototype")
//@Validated
public class ComponentMonitoringUploadsImpl implements ComponentMonitoringUploads {
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private MonitoringUploadsManager
      monitoringUploadsManager = MonitoringUploadsManagerFactory.getInstance().createInterface();
  private ComponentManager componentManager =
      ComponentManagerFactory.getInstance().createInterface();
  private static final Logger logger =
      LoggerFactory.getLogger(ComponentMonitoringUploadsImpl.class);

  @Override
  public Response upload(Attachment attachment,
                         String vspId, String versionId, String componentId, String type,
                         String user) throws Exception {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId + "," + componentId);
    MdcUtil.initMdc(LoggerServiceName.Upload_Monitoring_Artifact.toString());
    logger.audit(AuditMessages.AUDIT_MSG + String.format(AuditMessages
        .UPLOAD_MONITORING_FILE, type, vspId, componentId));

    Version version = new Version(versionId);
    componentManager.validateComponentExistence(vspId, version, componentId);

    MonitoringUploadType monitoringUploadType = getMonitoringUploadType(vspId, componentId, type);
    monitoringUploadsManager.upload(attachment.getObject(InputStream.class),
        attachment.getContentDisposition().getParameter("filename"), vspId, version, componentId,
        monitoringUploadType);

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId + "," + componentId);
    return Response.ok().build();
  }

  private MonitoringUploadType getMonitoringUploadType(String vspId, String componentId,
                                                       String type) throws Exception {
    MonitoringUploadType monitoringUploadType;
    try {
      monitoringUploadType = MonitoringUploadType.valueOf(type);
    } catch (IllegalArgumentException exception) {
      String errorWithParameters = ErrorMessagesFormatBuilder
          .getErrorWithParameters(Messages.ILLEGAL_MONITORING_ARTIFACT_TYPE.getErrorMessage(),
              componentId, vspId);
      throw new Exception(errorWithParameters, exception);
    }
    return monitoringUploadType;
  }

  @Override
  public Response delete(String vspId, String versionId, String componentId,
                         String type, String user) throws Exception {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId + "," + componentId);
    MdcUtil.initMdc(LoggerServiceName.Delete_Monitoring_Artifact.toString());

    MonitoringUploadType monitoringUploadType = getMonitoringUploadType(vspId, componentId, type);

    Version version = new Version(versionId);
    componentManager.validateComponentExistence(vspId, version, componentId);
    monitoringUploadsManager.delete(vspId, version, componentId, monitoringUploadType);

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId + "," + componentId);
    return Response.ok().build();
  }

  @Override
  public Response list(String vspId, String versionId, String componentId,
                       String user) {
    MdcUtil.initMdc(LoggerServiceName.List_Monitoring_Artifacts.toString());

    Version version = new Version(versionId);
    componentManager.validateComponentExistence(vspId, version, componentId);

    MonitoringUploadStatus response =
        monitoringUploadsManager.listFilenames(vspId, version, componentId);

    MonitoringUploadStatusDto returnEntity =
        new MapMonitoringUploadStatusToDto()
            .applyMapping(response, MonitoringUploadStatusDto.class);
    return Response.status(Response.Status.OK).entity(returnEntity).build();
  }
}
