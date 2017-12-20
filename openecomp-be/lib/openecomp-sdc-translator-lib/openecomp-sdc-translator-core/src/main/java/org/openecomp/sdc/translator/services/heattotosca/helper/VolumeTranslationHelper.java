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

package org.openecomp.sdc.translator.services.heattotosca.helper;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Output;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.ResourceFileDataAndIDs;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.ResourceTranslationBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE;

public class VolumeTranslationHelper {
  private final Logger logger;
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  public VolumeTranslationHelper(Logger logger) {
    this.logger = logger;
  }

  /**
   * Gets file data containing volume.
   *
   * @param filesToSearch the files to search
   * @param resourceId    the resource id
   * @param translateTo   the translate to
   * @param types         the types
   * @return the file data containing volume
   */
  public Optional<ResourceFileDataAndIDs> getFileDataContainingVolume(List<FileData> filesToSearch,
                                                                      String resourceId,
                                                                      TranslateTo translateTo,
                                                                      FileData.Type... types) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (CollectionUtils.isEmpty(filesToSearch)) {
      mdcDataDebugMessage.debugExitMessage(null, null);
      return Optional.empty();
    }

    List<FileData> fileDatas = Objects.isNull(types) ? filesToSearch : HeatToToscaUtil
        .getFilteredListOfFileDataByTypes(filesToSearch, types);
    Optional<ResourceFileDataAndIDs> fileDataAndIDs =
        getResourceFileDataAndIDsForVolumeConnection(resourceId, translateTo, fileDatas);
    if (fileDataAndIDs.isPresent()) {
      mdcDataDebugMessage.debugExitMessage(null, null);
      return fileDataAndIDs;
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return Optional.empty();
  }

  private Optional<ResourceFileDataAndIDs> getResourceFileDataAndIDsForVolumeConnection(
      String resourceId, TranslateTo translateTo, List<FileData> fileDatas) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    for (FileData data : fileDatas) {
      HeatOrchestrationTemplate heatOrchestrationTemplate = new YamlUtil()
          .yamlToObject(translateTo.getContext().getFiles().getFileContent(data.getFile()),
              HeatOrchestrationTemplate.class);
      Map<String, Output> outputs = heatOrchestrationTemplate.getOutputs();
      if (Objects.isNull(outputs)) {
        continue;
      }
      Output output = outputs.get(resourceId);
      if (Objects.nonNull(output)) {
        Optional<AttachedResourceId> attachedOutputId = HeatToToscaUtil
            .extractAttachedResourceId(data.getFile(), heatOrchestrationTemplate,
                translateTo.getContext(), output.getValue());
        if (attachedOutputId.isPresent()) {
          AttachedResourceId attachedResourceId = attachedOutputId.get();
          if (!isOutputIsGetResource(resourceId, data, attachedResourceId)) {
            continue;
          }
          String translatedId = (String) attachedResourceId.getTranslatedId();
          if (isOutputOfTypeCinderVolume(translateTo, data, heatOrchestrationTemplate,
              translatedId)) {
            ResourceFileDataAndIDs fileDataAndIDs =
                new ResourceFileDataAndIDs((String) attachedResourceId.getEntityId(),
                    translatedId,
                    data);
            return Optional.of(fileDataAndIDs);
          } else {
            logger.warn(
                "output: '" + resourceId + "' in file '" + data.getFile() + "' is not of type '"
                    + CINDER_VOLUME_RESOURCE_TYPE.getHeatResource() + "'");
          }
        }
      } else {
        logger.warn("output: '" + resourceId + "' in file '" + data.getFile() + "' is not found");
      }
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return Optional.empty();
  }

  private boolean isOutputOfTypeCinderVolume(TranslateTo translateTo, FileData data,
                                             HeatOrchestrationTemplate heatOrchestrationTemplate,
                                             String translatedId) {
    return getResourceByTranslatedResourceId(data.getFile(), heatOrchestrationTemplate,
        translatedId, translateTo, CINDER_VOLUME_RESOURCE_TYPE.getHeatResource()).isPresent();
  }

  private Optional<List<Map.Entry<String, Resource>>> getResourceByTranslatedResourceId(
      String fileName, HeatOrchestrationTemplate heatOrchestrationTemplate,
      String translatedResourceId, TranslateTo translateTo, String heatResourceType) {


    mdcDataDebugMessage.debugEntryMessage("file", fileName);

    List<Map.Entry<String, Resource>> list = heatOrchestrationTemplate.getResources().entrySet()
        .stream()
        .filter(
            entry -> getPredicatesForTranslatedIdToResourceId(fileName, heatOrchestrationTemplate,
                translatedResourceId, translateTo.getContext(), heatResourceType)
                .stream()
                    .allMatch(p -> p.test(entry)))
        .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(list)) {
      mdcDataDebugMessage.debugExitMessage("file", fileName);
      return Optional.empty();
    } else {
      mdcDataDebugMessage.debugExitMessage("file", fileName);
      return Optional.of(list);
    }
  }

  private List<Predicate<Map.Entry<String, Resource>>> getPredicatesForTranslatedIdToResourceId(
      String fileName, HeatOrchestrationTemplate heatOrchestrationTemplate,
      String translatedResourceId, TranslationContext context, String heatResourceType) {
    List<Predicate<Map.Entry<String, Resource>>> list = new ArrayList<>();
    list.add(entry -> entry.getValue().getType().equals(heatResourceType));
    list.add(entry -> {
      Optional<String> resourceTranslatedId = ResourceTranslationBase
          .getResourceTranslatedId(fileName, heatOrchestrationTemplate, entry.getKey(), context);
      return resourceTranslatedId.isPresent()
          && resourceTranslatedId.get().equals(translatedResourceId);
    });
    return list;
  }

  private boolean isOutputIsGetResource(String resourceId, FileData data,
                                        AttachedResourceId attachedResourceId) {
    if (attachedResourceId.isGetResource()) {
      return true;
    } else {
      logger.warn("output: '" + resourceId + "' in file '" + data.getFile()
          + "' is not defined as get_resource and therefore not supported as shared resource.");
      return false;
    }
  }
}
