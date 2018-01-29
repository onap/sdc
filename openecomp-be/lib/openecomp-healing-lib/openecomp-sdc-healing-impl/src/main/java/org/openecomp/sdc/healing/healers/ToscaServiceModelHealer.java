package org.openecomp.sdc.healing.healers;

import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.converter.ToscaConverter;
import org.openecomp.core.converter.factory.ToscaConverterFactory;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.dao.ServiceModelDaoFactory;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.translator.datatypes.TranslatorOutput;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.core.validation.util.MessageContainerUtil;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class ToscaServiceModelHealer implements Healer {
  private ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDao;
  private OrchestrationTemplateDao orchestrationTemplateDao;
  private static final String VALIDATION_FAILURE_MESSAGE = "Sdc product was updated. Please " +
      "update the uploaded heat file according to these validation errors: \n";

  public ToscaServiceModelHealer() {
    this.serviceModelDao = ServiceModelDaoFactory.getInstance().createInterface();
    this.orchestrationTemplateDao = OrchestrationTemplateDaoFactory.getInstance().createInterface();
  }

  public ToscaServiceModelHealer(
      ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDao,
      OrchestrationTemplateDao orchestrationTemplateDao) {
    this.serviceModelDao = serviceModelDao;
    this.orchestrationTemplateDao = orchestrationTemplateDao;
  }

  @Override
  public Object heal(String itemId, Version version) throws Exception {
    OrchestrationTemplateEntity orchestrationTemplateEntity =
        orchestrationTemplateDao.get(itemId, version);
    OnboardingTypesEnum type =
        OnboardingTypesEnum.getOnboardingTypesEnum(orchestrationTemplateEntity.getFileSuffix());

    if (Objects.isNull(type)
        || Objects.isNull(orchestrationTemplateEntity.getContentData())) {
      return Optional.empty();
    }

    Optional<ToscaServiceModel> healedServiceModel =
        healServiceModel(orchestrationTemplateEntity, type);

    healedServiceModel.ifPresent(serviceModel -> serviceModelDao
        .overrideServiceModel(itemId, version, serviceModel));

    return Optional.of(healedServiceModel);

  }

  Optional<ToscaServiceModel> healServiceModel(
      OrchestrationTemplateEntity orchestrationTemplateEntity,
      OnboardingTypesEnum type) throws IOException {
    byte[] uploadedFileContent = orchestrationTemplateEntity.getContentData().array();
    FileContentHandler contentMap =
        CommonUtil.validateAndUploadFileContent(type, uploadedFileContent);

    switch (type) {
      case ZIP:
        return Optional.of(healServiceModelFromZip(contentMap));

      case CSAR:
        return Optional.of(healServiceModelFromCsar(contentMap));

      default:
        return Optional.empty();
    }

  }

  private ToscaServiceModel healServiceModelFromZip(FileContentHandler contentMap) {
    TranslatorOutput translatorOutput =
        HeatToToscaUtil.loadAndTranslateTemplateData(contentMap);

    if (areThereValidationErrors(translatorOutput)) {
      String validationErrorsAsString = MessageContainerUtil.getErrorMessagesListAsString
          (MessageContainerUtil
              .getMessageByLevel(ErrorLevel.ERROR, translatorOutput.getErrorMessages()));
      throw new RuntimeException(VALIDATION_FAILURE_MESSAGE + validationErrorsAsString);
    }

    return translatorOutput.getToscaServiceModel();
  }

  private boolean areThereValidationErrors(TranslatorOutput translatorOutput) {
    return MapUtils.isNotEmpty(MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR,
        translatorOutput.getErrorMessages()));
  }

  private ToscaServiceModel healServiceModelFromCsar(FileContentHandler contentMap)
      throws IOException {
    ToscaConverter toscaConverter = ToscaConverterFactory.getInstance().createInterface();
    return toscaConverter.convert(contentMap);
  }
}
