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
package org.openecomp.sdc.healing.healers;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
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

public class ToscaServiceModelHealer implements Healer {

    private static final String VALIDATION_FAILURE_MESSAGE =
        "Product was updated. Please " + "update the uploaded Heat file according to these validation errors: \n";
    private ServiceModelDao<ToscaServiceModel> serviceModelDao;
    private OrchestrationTemplateDao orchestrationTemplateDao;

    public ToscaServiceModelHealer() {
        this.serviceModelDao = ServiceModelDaoFactory.getInstance().createInterface();
        this.orchestrationTemplateDao = OrchestrationTemplateDaoFactory.getInstance().createInterface();
    }

    public ToscaServiceModelHealer(ServiceModelDao<ToscaServiceModel> serviceModelDao,
                                   OrchestrationTemplateDao orchestrationTemplateDao) {
        this.serviceModelDao = serviceModelDao;
        this.orchestrationTemplateDao = orchestrationTemplateDao;
    }

    @Override
    public boolean isHealingNeeded(String itemId, Version version) {
        OrchestrationTemplateEntity orchestrationTemplate = orchestrationTemplateDao.get(itemId, version);
        OnboardingTypesEnum onboardingTypes = OnboardingTypesEnum.getOnboardingTypesEnum(orchestrationTemplate.getFileSuffix());
        return Objects.nonNull(onboardingTypes) && Objects.nonNull(orchestrationTemplate.getContentData());
    }

    @Override
    public void heal(String itemId, Version version) throws Exception {
        OrchestrationTemplateEntity orchestrationTemplateEntity = orchestrationTemplateDao.get(itemId, version);
        OnboardingTypesEnum type = OnboardingTypesEnum.getOnboardingTypesEnum(orchestrationTemplateEntity.getFileSuffix());
        Optional<ToscaServiceModel> healedServiceModel = healServiceModel(orchestrationTemplateEntity, type);
        healedServiceModel.ifPresent(serviceModel -> serviceModelDao.overrideServiceModel(itemId, version, serviceModel));
    }

    private Optional<ToscaServiceModel> healServiceModel(OrchestrationTemplateEntity orchestrationTemplateEntity, OnboardingTypesEnum type)
        throws IOException {
        switch (type) {
            case ZIP:
                return Optional.of(healServiceModelFromZip(getFileContentHandlerForHealing(orchestrationTemplateEntity, type)));
            case CSAR:
                return Optional.of(healServiceModelFromCsar(getFileContentHandlerForHealing(orchestrationTemplateEntity, type)));
            default:
                return Optional.empty();
        }
    }

    private FileContentHandler getFileContentHandlerForHealing(OrchestrationTemplateEntity orchestrationTemplateEntity, OnboardingTypesEnum type)
        throws IOException {
        byte[] uploadedFileContent = orchestrationTemplateEntity.getContentData().array();
        return CommonUtil.validateAndUploadFileContent(type, uploadedFileContent);
    }

    private ToscaServiceModel healServiceModelFromZip(FileContentHandler contentMap) {
        TranslatorOutput translatorOutput = HeatToToscaUtil.loadAndTranslateTemplateData(contentMap);
        if (areThereValidationErrors(translatorOutput)) {
            String validationErrorsAsString = MessageContainerUtil
                .getErrorMessagesListAsString(MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, translatorOutput.getErrorMessages()));
            throw new RuntimeException(VALIDATION_FAILURE_MESSAGE + validationErrorsAsString);
        }
        return translatorOutput.getToscaServiceModel();
    }

    private boolean areThereValidationErrors(TranslatorOutput translatorOutput) {
        return MapUtils.isNotEmpty(MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, translatorOutput.getErrorMessages()));
    }

    private ToscaServiceModel healServiceModelFromCsar(FileContentHandler contentMap) throws IOException {
        ToscaConverter toscaConverter = ToscaConverterFactory.getInstance().createInterface();
        return toscaConverter.convert(contentMap);
    }
}
