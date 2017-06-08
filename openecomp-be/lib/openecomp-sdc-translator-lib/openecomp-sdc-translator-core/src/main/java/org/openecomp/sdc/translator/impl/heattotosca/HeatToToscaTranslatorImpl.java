/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.translator.impl.heattotosca;

import org.apache.commons.collections4.MapUtils;
import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.core.translator.api.HeatToToscaTranslator;
import org.openecomp.core.translator.datatypes.TranslatorOutput;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.validation.api.ValidationManager;
import org.openecomp.core.validation.factory.ValidationManagerFactory;
import org.openecomp.core.validation.util.MessageContainerUtil;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.configuration.ImplementationConfiguration;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestFile;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.services.heattotosca.ConfigConstants;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationService;
import org.openecomp.sdc.translator.services.heattotosca.TranslationService;
import org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionManager;
import org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionService;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeatToToscaTranslatorImpl implements HeatToToscaTranslator {

  private TranslationContext translationContext = new TranslationContext();
  private ValidationManager validationManager =
      ValidationManagerFactory.getInstance().createInterface();
  private boolean isValid = false;


  @Override
  public void addManifest(String name, byte[] content) {
    ManifestContent manifestData = JsonUtil.json2Object(new String(content), ManifestContent.class);
    ManifestFile manifest = new ManifestFile();
    manifest.setName(name);
    manifest.setContent(manifestData);
    translationContext.setManifest(manifest);
    translationContext.addFile(name, content);
    validationManager.addFile(SdcCommon.MANIFEST_NAME, content);
    addFilesFromManifestToTranslationContextManifestFilesMap(manifestData.getData());
    isValid = false;
  }

  @Override
  public void addFile(String name, byte[] content) {
    translationContext.addFile(name, content);
    validationManager.addFile(name, content);
    isValid = false;
  }

  @Override
  public void addFile(String name, InputStream content) {
    addFile(name, FileUtils.toByteArray(content));
  }


  @Override
  public Map<String, List<ErrorMessage>> validate() {

    Map<String, List<ErrorMessage>> errors = new HashMap<>();
    if (translationContext.getManifest() == null) {
      ErrorMessage.ErrorMessageUtil.addMessage(SdcCommon.MANIFEST_NAME, errors)
          .add(new ErrorMessage(ErrorLevel.ERROR, Messages.MANIFEST_NOT_EXIST.getErrorMessage()));
      return errors;
    }

    if (MapUtils.isEmpty(errors)) {
      errors = validationManager.validate();
    }
    if (MapUtils.isEmpty(MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, errors))) {
      isValid = true;
    }
    return errors;
  }

  @Override
  public TranslatorOutput translate() {
    TranslationService translationService = new TranslationService();
    TranslatorOutput translatorOutput = new TranslatorOutput();
    UnifiedCompositionManager unifiedCompositionManager = new UnifiedCompositionManager(new
        ConsolidationService(new UnifiedCompositionService()));
    if (!isValid) {
      Map<String, List<ErrorMessage>> errors = validate();

      if (MapUtils.isNotEmpty(MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, errors))) {
        translatorOutput.setErrorMessages(errors);
        return translatorOutput;
      }
    }

    translatorOutput = translationService.translateHeatFiles(translationContext);
    ToscaServiceModel unifiedToscaServiceModel = unifiedCompositionManager
        .createUnifiedComposition(translatorOutput.getToscaServiceModel(), translationContext);
    translatorOutput.setToscaServiceModel(unifiedToscaServiceModel);
    return translatorOutput;
  }

  @Override
  public void addExternalArtifacts(String name, byte[] content) {
    translationContext.addExternalArtifacts(name, content);
  }

  @Override
  public void addExternalArtifacts(String name, InputStream content) {
    addExternalArtifacts(name, FileUtils.toByteArray(content));
  }

  private void addFilesFromManifestToTranslationContextManifestFilesMap(
      List<FileData> fileDataListFromManifest) {
    for (FileData fileFromManfiest : fileDataListFromManifest) {
      translationContext.addManifestFile(fileFromManfiest.getFile(), fileFromManfiest.getType());
    }
  }


}
