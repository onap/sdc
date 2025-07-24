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
package org.openecomp.sdcrests.vsp.rest.services;

import java.util.Collection;
import javax.inject.Named;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.ImageManager;
import org.openecomp.sdc.vendorsoftwareproduct.ImageManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Image;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityValidationDataDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ImageCreationDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ImageDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ImageRequestDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireResponseDto;
import org.openecomp.sdcrests.vsp.rest.Images;
import org.openecomp.sdcrests.vsp.rest.mapping.MapCompositionEntityValidationDataToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapImageEntityToImageCreationDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapImageEntityToImageDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapImageRequestDtoToImageEntity;
import org.openecomp.sdcrests.vsp.rest.mapping.MapQuestionnaireResponseToQuestionnaireResponseDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Named
@Service("images")
@Scope(value = "prototype")
public class ImagesImpl implements Images {

    private final ImageManager imageManager;
    private final ComponentManager componentManager;

    public ImagesImpl() {
        this.imageManager = ImageManagerFactory.getInstance().createInterface();
        this.componentManager = ComponentManagerFactory.getInstance().createInterface();
    }

    public ImagesImpl(ImageManager imageManager, ComponentManager componentManager) {
        this.imageManager = imageManager;
        this.componentManager = componentManager;
    }

    @Override
    public ResponseEntity create(ImageRequestDto request, String vspId, String versionId, String componentId, String user) {
        ImageEntity image = new MapImageRequestDtoToImageEntity().applyMapping(request, ImageEntity.class);
        image.setVspId(vspId);
        image.setComponentId(componentId);
        image.setVersion(new Version(versionId));
        componentManager.validateComponentExistence(vspId, image.getVersion(), componentId);
        ImageEntity createdImage = imageManager.createImage(image);
        MapImageEntityToImageCreationDto mapping = new MapImageEntityToImageCreationDto();
        ImageCreationDto createdImageDto = mapping.applyMapping(createdImage, ImageCreationDto.class);
        return ResponseEntity.ok(createdImage != null ? createdImageDto : null);
    }

    @Override
    public ResponseEntity getImageSchema(String vspId, String versionId, String componentId, String user) {
        CompositionEntityResponse<Image> response = imageManager.getImageSchema(vspId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity get(String vspId, String versionId, String componentId, String imageId, String user) {
        Version version = new Version(versionId);
        componentManager.validateComponentExistence(vspId, version, componentId);
        CompositionEntityResponse<Image> response = imageManager.getImage(vspId, version, componentId, imageId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity list(String vspId, String versionId, String componentId, String user) {
        Version vspVersion = new Version(versionId);
        componentManager.validateComponentExistence(vspId, vspVersion, componentId);
        Collection<ImageEntity> images = imageManager.listImages(vspId, vspVersion, componentId);
        MapImageEntityToImageDto mapper = new MapImageEntityToImageDto();
        GenericCollectionWrapper<ImageDto> results = new GenericCollectionWrapper<>();
        for (ImageEntity image : images) {
            results.add(mapper.applyMapping(image, ImageDto.class));
        }
        return ResponseEntity.ok(results);
    }

    @Override
    public ResponseEntity delete(String vspId, String versionId, String componentId, String imageId, String user) {
        Version vspVersion = new Version(versionId);
        componentManager.validateComponentExistence(vspId, vspVersion, componentId);
        imageManager.deleteImage(vspId, vspVersion, componentId, imageId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity update(ImageRequestDto request, String vspId, String versionId, String componentId, String imageId, String user) {
        ImageEntity imageEntity = new MapImageRequestDtoToImageEntity().applyMapping(request, ImageEntity.class);
        imageEntity.setVspId(vspId);
        imageEntity.setVersion(new Version(versionId));
        imageEntity.setComponentId(componentId);
        imageEntity.setId(imageId);
        componentManager.validateComponentExistence(vspId, imageEntity.getVersion(), componentId);
        CompositionEntityValidationData validationData = imageManager.updateImage(imageEntity);
        return validationData != null && CollectionUtils.isNotEmpty(validationData.getErrors()) ? ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
            .body(new MapCompositionEntityValidationDataToDto().applyMapping(validationData, CompositionEntityValidationDataDto.class))
            : ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity getQuestionnaire(String vspId, String versionId, String componentId, String imageId, String user) {
        Version vspVersion = new Version(versionId);
        componentManager.validateComponentExistence(vspId, vspVersion, componentId);
        QuestionnaireResponse questionnaireResponse = imageManager.getImageQuestionnaire(vspId, vspVersion, componentId, imageId);
        QuestionnaireResponseDto result = new MapQuestionnaireResponseToQuestionnaireResponseDto()
            .applyMapping(questionnaireResponse, QuestionnaireResponseDto.class);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity updateQuestionnaire(String questionnaireData, String vspId, String versionId, String componentId, String imageId, String user) {
        Version version = new Version(versionId);
        componentManager.validateComponentExistence(vspId, version, componentId);
        imageManager.updateImageQuestionnaire(vspId, version, componentId, imageId, questionnaireData);
        return ResponseEntity.ok().build();
    }
}
