/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.sdc.vendorsoftwareproduct.impl;


import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.vendorsoftwareproduct.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.ImageManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.ImageErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.NotSupportedHeatOnboardMethodErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator.SchemaGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Image;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ImageFormat;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.image.ImageDetails;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.ImageCompositionSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateInput;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;
import java.util.stream.Collectors;

public class ImageManagerImpl implements ImageManager {
  private final VendorSoftwareProductInfoDao vspInfoDao;
  private final ImageDao imageDao;
  private final CompositionEntityDataManager compositionEntityDataManager;

  public ImageManagerImpl(VendorSoftwareProductInfoDao vspInfoDao,
                          ImageDao imageDao,
                          CompositionEntityDataManager compositionEntityDataManager) {
    this.vspInfoDao = vspInfoDao;
    this.imageDao = imageDao;
    this.compositionEntityDataManager = compositionEntityDataManager;
  }

  @Override
  public ImageEntity createImage(ImageEntity imageEntity) {
    boolean isManual = vspInfoDao.isManual(imageEntity.getVspId(), imageEntity.getVersion());
    if (!isManual) {
      ErrorCode errorCode = NotSupportedHeatOnboardMethodErrorBuilder
          .getAddImageNotSupportedHeatOnboardMethodErrorBuilder();
      throw new CoreException(errorCode);
    }
    compositionEntityDataManager.createImage(imageEntity);
    return imageEntity;
  }

  @Override
  public Collection<ImageEntity> listImages(String vspId, Version version, String componentId) {
    return imageDao.list(new ImageEntity(vspId, version, componentId, null));
  }

  @Override
  public CompositionEntityResponse<Image> getImageSchema(String vspId) {
    CompositionEntityResponse<Image> response = new CompositionEntityResponse<>();
    ImageCompositionSchemaInput inputSchema = new ImageCompositionSchemaInput();
    Image image = new Image();
    inputSchema.setImage(image);
    response.setSchema(getImageCompositionSchema(inputSchema));
    return response;
  }

  @Override
  public CompositionEntityResponse<Image> getImage(String vspId, Version version, String
      componentId, String imageId) {
    ImageEntity imageEntity = getImageEntity(vspId, version, componentId, imageId);

    Image image = imageEntity.getImageCompositionData();

    ImageCompositionSchemaInput schemaInput = new ImageCompositionSchemaInput();
    schemaInput.setImage(image);

    CompositionEntityResponse<Image> response = new CompositionEntityResponse<>();
    response.setId(imageId);
    response.setData(image);
    response.setSchema(getImageCompositionSchema(schemaInput));
    return response;
  }

  @Override
  public QuestionnaireResponse getImageQuestionnaire(String vspId, Version version, String
      componentId, String imageId) {
    QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();

    ImageEntity retrieved = imageDao.getQuestionnaireData(vspId, version, componentId, imageId);
    VersioningUtil.validateEntityExistence(retrieved, new ImageEntity(vspId, version, componentId,
        imageId), ComponentEntity.ENTITY_TYPE);
    questionnaireResponse.setData(retrieved.getQuestionnaireData());
    questionnaireResponse.setSchema(getImageQuestionnaireSchema(null));
    return questionnaireResponse;
  }

  @Override
  public void deleteImage(String vspId, Version version, String componentId, String imageId) {
    ImageEntity imageEntity = getImageEntity(vspId, version, componentId, imageId);
    if (!vspInfoDao.isManual(vspId, version)) {
      final ErrorCode deleteImageErrorBuilder =
          NotSupportedHeatOnboardMethodErrorBuilder
              .getDelImageNotSupportedHeatOnboardMethodErrorBuilder();
      throw new CoreException(deleteImageErrorBuilder);
    }
    if (imageEntity != null) {
      imageDao.delete(new ImageEntity(vspId, version, componentId, imageId));
    }
  }

  private void validateHeatVspImageUpdate(String name, String value, String retrievedValue) {
    if (value != null && !value.equals(retrievedValue)) {
      final ErrorCode updateHeatImageErrorBuilder =
          ImageErrorBuilder.getImageHeatReadOnlyErrorBuilder(name);
      throw new CoreException(updateHeatImageErrorBuilder);
    }
  }

  @Override
  public CompositionEntityValidationData updateImage(ImageEntity image) {
    boolean isManual = vspInfoDao.isManual(image.getVspId(), image.getVersion());
    ImageEntity retrieved =
        getImageEntity(image.getVspId(), image.getVersion(), image.getComponentId(),
            image.getId());

    Collection<ImageEntity> vfcImageList = listImages(image.getVspId(),
        image.getVersion(), image.getComponentId());

    //Set to null so that retrieved object is equal to one in list and gets removed.
    retrieved.setQuestionnaireData(null);
    vfcImageList.remove(retrieved);


    ImageCompositionSchemaInput schemaInput = new ImageCompositionSchemaInput();
    schemaInput.setManual(isManual);
    schemaInput.setImage(retrieved.getImageCompositionData());

    CompositionEntityValidationData validationData = compositionEntityDataManager
        .validateEntity(image, SchemaTemplateContext.composition, schemaInput);
    if (CollectionUtils.isEmpty(validationData.getErrors())) {
      imageDao.update(image);
    }
    return validationData;
  }

  @Override
  public void updateImageQuestionnaire(String vspId, Version version, String componentId, String
      imageId, String questionnaireData) {
    getImageEntity(vspId, version, componentId, imageId);


    final ImageDetails image = JsonUtil.json2Object(questionnaireData, ImageDetails.class);
    final String format = image.getFormat();
    try {
      if (format != null) {
        ImageFormat.valueOf(format);
      }
    } catch (IllegalArgumentException exception) {
      ErrorCode errorCode = ImageErrorBuilder.getInvalidImageFormatErrorBuilder();
      throw new CoreException(errorCode, exception);
    }

    //Validate Format is read only for HEAT Onboarding
    if (!vspInfoDao.isManual(vspId, version)) {
      final QuestionnaireResponse imageQuestionnaire = getImageQuestionnaire(vspId, version,
          componentId, imageId);
      final String data = imageQuestionnaire.getData();
      if (data != null) {
        String retrievedFormat = JsonUtil.json2Object(data, ImageDetails.class).getFormat();
        validateHeatVspImageUpdate("format", format, retrievedFormat);
      }
    }

    if (!isImageVersionUnique(vspId, version, componentId, imageId, image)) {
      ErrorCode errorCode = ImageErrorBuilder.getDuplicateImageVersionErrorBuilder(image
          .getVersion(), componentId);
      throw new CoreException(errorCode);
    }

    imageDao.updateQuestionnaireData(vspId, version, componentId, imageId, questionnaireData);
  }

  private boolean isImageVersionUnique(String vspId, Version version, String componentId,
                                       String imageId,
                                       ImageDetails image) {
    boolean isPresent = true;
    if (image != null && image.getVersion() != null) {
      Collection<ImageEntity> imageEntities =
          imageDao.list(new ImageEntity(vspId, version, componentId, null));
      if (CollectionUtils.isNotEmpty(imageEntities)) {
        imageEntities =
            imageEntities.stream().filter(imageEntity -> image.getVersion().trim().equalsIgnoreCase(
                getImageVersion(vspId, version, componentId, imageEntity))
                && !imageEntity.getId().equals(imageId)).collect(Collectors.toList());

        isPresent = CollectionUtils.isEmpty(imageEntities);
      }
    }

    return isPresent;
  }

  private String getImageVersion(String vspId, Version version, String componentId,
                                 ImageEntity imageEntity) {
    QuestionnaireResponse imageQuestionnaire = getImageQuestionnaire(vspId, version,
        componentId, imageEntity.getId());
    ImageDetails imageDetails =
        JsonUtil.json2Object(imageQuestionnaire.getData(), ImageDetails.class);

    return imageDetails == null ? null
        : imageDetails.getVersion() != null ? imageDetails.getVersion().trim() : null;
  }

  private ImageEntity getImageEntity(String vspId, Version version, String componentId,
                                     String imageId) {

    ImageEntity imageEntity = imageDao.get(new ImageEntity(vspId, version, componentId, imageId));

    VersioningUtil.validateEntityExistence(imageEntity, new ImageEntity(vspId, version, componentId,
        imageId), VspDetails.ENTITY_TYPE);
    return imageEntity;
  }

  protected String getImageCompositionSchema(SchemaTemplateInput schemaInput) {
    return SchemaGenerator
        .generate(SchemaTemplateContext.composition, CompositionEntityType.image,
            schemaInput);
  }

  protected String getImageQuestionnaireSchema(SchemaTemplateInput schemaInput) {
    return SchemaGenerator
        .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.image,
            schemaInput);
  }
}
