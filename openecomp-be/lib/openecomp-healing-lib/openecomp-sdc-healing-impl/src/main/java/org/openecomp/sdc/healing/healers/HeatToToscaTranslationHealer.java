package org.openecomp.sdc.healing.healers;


import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.dao.ServiceModelDaoFactory;
import org.openecomp.core.model.dao.ServiceTemplateDaoFactory;
import org.openecomp.core.model.dao.ServiceTemplateDaoInter;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.translator.datatypes.TranslatorOutput;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.UploadDataEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class HeatToToscaTranslationHealer implements Healer  {

  private static final OrchestrationTemplateDao orchestrationTemplateDao =
      OrchestrationTemplateDaoFactory.getInstance().createInterface();
  private static final ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDao =
      ServiceModelDaoFactory.getInstance().createInterface();
  public static final ServiceTemplateDaoInter
      templateDao = ServiceTemplateDaoFactory.getInstance().createInterface();
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());

  public HeatToToscaTranslationHealer(){

  }

  @Override
  public Object heal(Map<String, Object> healingParams) throws Exception {
    mdcDataDebugMessage.debugEntryMessage(null, null);
    String vspId = (String) healingParams.get(SdcCommon.VSP_ID);
    Version version = (Version) healingParams.get(SdcCommon.VERSION);
    String user = (String) healingParams.get(SdcCommon.USER);
    UploadDataEntity uploadData =
        orchestrationTemplateDao.getOrchestrationTemplate(vspId, version);

    if (Objects.isNull(uploadData) || Objects.isNull(uploadData.getContentData())) {
      return Optional.empty();
    }

    FileContentHandler fileContentHandler;
    TranslatorOutput translatorOutput;
    try {
      fileContentHandler = CommonUtil.validateAndUploadFileContent(OnboardingTypesEnum.ZIP, uploadData
          .getContentData().array());
      translatorOutput =
          HeatToToscaUtil.loadAndTranslateTemplateData(fileContentHandler);
    } catch (Exception e) {
      log.debug("", e);
      return Optional.empty();
    }

    if (Objects.isNull(translatorOutput)) {
      return Optional.empty();
    } else {

      if (translatorOutput.getToscaServiceModel() == null) {
        return Optional.empty();
      }
      //templateDao.deleteAll(vspId, version);
      serviceModelDao.deleteAll(vspId, version);
      serviceModelDao.storeServiceModel(vspId, version, translatorOutput.getToscaServiceModel());
      mdcDataDebugMessage.debugExitMessage("VSP id", vspId);

      return translatorOutput;
    }

  }
}
