package org.openecomp.sdcrests.vsp.rest.services;


import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.logging.context.MdcUtil;
import org.openecomp.sdc.logging.types.LoggerServiceName;
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
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.util.Collection;


@Named
@Service("images")
@Scope(value = "prototype")
public class ImagesImpl implements Images {

  private ImageManager imageManager = ImageManagerFactory.getInstance().createInterface();
  private ComponentManager componentManager =
      ComponentManagerFactory.getInstance().createInterface();

  @Override
  public Response create(ImageRequestDto request, String vspId, String versionId,
                         String componentId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Create_Image.toString());
    ImageEntity image =
        new MapImageRequestDtoToImageEntity().applyMapping(request, ImageEntity.class);
    image.setVspId(vspId);
    image.setComponentId(componentId);
    image.setVersion(new Version(versionId));
    componentManager.validateComponentExistence(vspId, image.getVersion(), componentId);
    ImageEntity createdImage = imageManager.createImage(image);
    MapImageEntityToImageCreationDto mapping = new MapImageEntityToImageCreationDto();
    ImageCreationDto createdImageDto = mapping.applyMapping(createdImage, ImageCreationDto.class);
    return Response
        .ok(createdImage != null ? createdImageDto : null)
        .build();
  }

  @Override
  public Response getImageSchema(String vspId, String versionId, String componentId, String user) {
    MdcUtil.initMdc(LoggerServiceName.GET_Image_Schema.toString());
    CompositionEntityResponse<Image> response = imageManager.getImageSchema(vspId);
    return Response.ok(response).build();
  }

  @Override
  public Response get(String vspId, String versionId, String componentId, String imageId,
                      String user) {
    MdcUtil.initMdc(LoggerServiceName.GET_Image.toString());
    Version version = new Version(versionId);
    componentManager.validateComponentExistence(vspId, version, componentId);
    CompositionEntityResponse<Image> response =
        imageManager.getImage(vspId, version, componentId, imageId);

    return Response.ok(response).build();
  }

  @Override
  public Response list(String vspId, String versionId, String componentId, String user) {
    MdcUtil.initMdc(LoggerServiceName.List_Images.toString());
    Version vspVersion = new Version(versionId);
    componentManager.validateComponentExistence(vspId, vspVersion, componentId);
    Collection<ImageEntity> images = imageManager.listImages(vspId, vspVersion, componentId);

    MapImageEntityToImageDto mapper = new MapImageEntityToImageDto();
    GenericCollectionWrapper<ImageDto> results = new GenericCollectionWrapper<>();
    for (ImageEntity image : images) {
      results.add(mapper.applyMapping(image, ImageDto.class));
    }

    return Response.ok(results).build();
  }

  @Override
  public Response delete(String vspId, String versionId, String componentId, String imageId,
                         String user) {
    MdcUtil.initMdc(LoggerServiceName.Delete_Image.toString());
    Version vspVersion = new Version(versionId);
    componentManager.validateComponentExistence(vspId, vspVersion, componentId);
    imageManager.deleteImage(vspId, vspVersion, componentId, imageId);
    return Response.ok().build();
  }

  @Override
  public Response update(ImageRequestDto request, String vspId, String versionId,
                         String componentId, String imageId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Update_Image.toString());
    ImageEntity imageEntity = new MapImageRequestDtoToImageEntity().applyMapping(request,
        ImageEntity.class);
    imageEntity.setVspId(vspId);
    imageEntity.setVersion(new Version(versionId));
    imageEntity.setComponentId(componentId);
    imageEntity.setId(imageId);
    componentManager.validateComponentExistence(vspId, imageEntity.getVersion(), componentId);

    CompositionEntityValidationData validationData = imageManager.updateImage(imageEntity);
    return validationData != null && CollectionUtils.isNotEmpty(validationData.getErrors())
        ? Response.status(Response.Status.EXPECTATION_FAILED).entity(
        new MapCompositionEntityValidationDataToDto()
            .applyMapping(validationData, CompositionEntityValidationDataDto.class)).build() :
        Response.ok().build();
  }

  @Override
  public Response getQuestionnaire(String vspId, String versionId, String componentId,
                                   String imageId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_Questionnaire_Compute.toString());
    Version vspVersion = new Version(versionId);
    componentManager.validateComponentExistence(vspId, vspVersion, componentId);
    QuestionnaireResponse questionnaireResponse =
        imageManager.getImageQuestionnaire(vspId, vspVersion, componentId, imageId);

    QuestionnaireResponseDto result = new MapQuestionnaireResponseToQuestionnaireResponseDto()
        .applyMapping(questionnaireResponse, QuestionnaireResponseDto.class);
    return Response.ok(result).build();

  }

  @Override
  public Response updateQuestionnaire(String questionnaireData, String vspId, String versionId,
                                      String componentId, String imageId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Update_Questionnaire_Compute.toString());
    Version version = new Version(versionId);
    componentManager.validateComponentExistence(vspId, version, componentId);
    imageManager.updateImageQuestionnaire(vspId, version, componentId, imageId, questionnaireData);
    return Response.ok().build();
  }

}
