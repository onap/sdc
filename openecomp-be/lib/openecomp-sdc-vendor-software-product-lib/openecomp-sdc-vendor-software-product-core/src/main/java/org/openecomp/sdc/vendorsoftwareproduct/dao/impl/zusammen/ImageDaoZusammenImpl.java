package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;


import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToImageConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.types.ElementPropertyName;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

public class ImageDaoZusammenImpl implements ImageDao {

  private ZusammenAdaptor zusammenAdaptor;

  public ImageDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {
  }

  @Override
  public Collection<ImageEntity> list(ImageEntity image) {
    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(image.getVspId(), image.getVersion().getId());

    return listImages(context, elementContext, image);
  }

  private Collection<ImageEntity> listImages(SessionContext context,
                                             ElementContext elementContext, ImageEntity image) {
    ElementToImageConvertor convertor = new ElementToImageConvertor();
    return zusammenAdaptor
        .listElementsByName(context, elementContext, new Id(image.getComponentId()),
            ElementType.Images.name())
        .stream().map(convertor::convert)
        .map(imageEntity -> {
          imageEntity.setComponentId(image.getComponentId());
          imageEntity.setVspId(image.getVspId());
          imageEntity.setVersion(image.getVersion());
          return imageEntity;
        })
        .collect(Collectors.toList());
  }

  @Override
  public void create(ImageEntity image) {
    ZusammenElement imageElement = imageToZusammen(image, Action.CREATE);

    ZusammenElement imagesElement =
        buildStructuralElement(ElementType.Images, Action.IGNORE);
    imagesElement.setSubElements(Collections.singletonList(imageElement));

    ZusammenElement componentElement = buildElement(new Id(image.getComponentId()), Action.IGNORE);
    componentElement.setSubElements(Collections.singletonList(imagesElement));

    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(image.getVspId(), image.getVersion().getId());

    Element savedElement =
        zusammenAdaptor.saveElement(context, elementContext, componentElement, "Create image");
    image.setId(savedElement.getSubElements().iterator().next()
        .getSubElements().iterator().next().getElementId().getValue());
  }

  @Override
  public void update(ImageEntity image) {
    ZusammenElement imageElement = imageToZusammen(image, Action.UPDATE);

    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(image.getVspId(), image.getVersion().getId());
    zusammenAdaptor.saveElement(context, elementContext, imageElement,
        String.format("Update image with id %s", image.getId()));
  }

  @Override
  public ImageEntity get(ImageEntity image) {
    SessionContext context = createSessionContext();

    ElementContext elementContext =
        new ElementContext(image.getVspId(), image.getVersion().getId());
    Optional<Element> element = zusammenAdaptor.getElement(context, elementContext, image.getId());

    if (element.isPresent()) {
      ElementToImageConvertor convertor = new ElementToImageConvertor();
      ImageEntity entity = convertor.convert(element.get());
      entity.setComponentId(image.getComponentId());
      entity.setVspId(image.getVspId());
      entity.setVersion(image.getVersion());
      return entity;
    } else {
      return null;
    }
  }

  @Override
  public void delete(ImageEntity image) {
    ZusammenElement imageElement = buildElement(new Id(image.getId()), Action.DELETE);

    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(image.getVspId(), image.getVersion().getId());
    zusammenAdaptor.saveElement(context, elementContext, imageElement,
        String.format("Delete image with id %s", image.getId()));
  }

  @Override
  public void deleteByVspId(String vspId, Version version) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    Optional<Element> elementOptional = zusammenAdaptor.getElementByName(context, elementContext,
        null, ElementType.Images.name());

    if (elementOptional.isPresent()) {
      Element ImagesElement = elementOptional.get();
      Collection<Element> Images = ImagesElement.getSubElements();
      Images.forEach(image -> {
        ZusammenElement imageZusammenElement = buildElement(image.getElementId(), Action.DELETE);
        zusammenAdaptor.saveElement(context, elementContext, imageZusammenElement, "Delete image " +
            "with id " + image.getElementId());
      });
    }
  }

  @Override
  public Collection<ImageEntity> listByVsp(String vspId, Version version) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    Collection<ComponentEntity> components = ComponentDaoZusammenImpl
        .listComponents(zusammenAdaptor, context, vspId, version);

    return components.stream()
        .map(component ->
            listImages(context, elementContext,
                new ImageEntity(vspId, version, component.getId(), null)).stream()
                .map(image -> getQuestionnaire(context, elementContext, image))
                .collect(Collectors.toList()))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  @Override
  public ImageEntity getQuestionnaireData(String vspId, Version version, String componentId,
                                          String imageId) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    return getQuestionnaire(context, elementContext,
        new ImageEntity(vspId, version, componentId, imageId));
  }

  private ImageEntity getQuestionnaire(SessionContext context, ElementContext elementContext,
                                       ImageEntity image) {
    Optional<Element> questionnaireElement = zusammenAdaptor
        .getElementByName(context, elementContext, new Id(image.getId()),
            ElementType.ImageQuestionnaire.name());
    return questionnaireElement.map(
        element -> element.getData() == null
            ? null
            : new String(FileUtils.toByteArray(element.getData())))
        .map(questionnaireData -> {
          image.setQuestionnaireData(questionnaireData);
          return image;
        })
        .orElse(null);
  }

  @Override
  public void updateQuestionnaireData(String vspId, Version version, String componentId,
                                      String imageId, String questionnaireData) {
    ZusammenElement questionnaireElement =
        imageQuestionnaireToZusammen(questionnaireData, Action.UPDATE);

    ZusammenElement imageElement = buildElement(new Id(imageId), Action.IGNORE);
    imageElement.setSubElements(Collections.singletonList(questionnaireElement));

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());
    zusammenAdaptor.saveElement(context, elementContext, imageElement, "Update image "
        + "questionnaire");
  }


  private ZusammenElement imageToZusammen(ImageEntity image, Action action) {
    ZusammenElement imageElement = buildImageElement(image, action);
    if (action == Action.CREATE) {
      imageElement.setSubElements(Collections.singletonList(
          imageQuestionnaireToZusammen(image.getQuestionnaireData(), Action.CREATE)));
    }
    return imageElement;
  }

  private ZusammenElement imageQuestionnaireToZusammen(String questionnaireData,
                                                       Action action) {
    ZusammenElement questionnaireElement =
        buildStructuralElement(ElementType.ImageQuestionnaire, action);
    questionnaireElement.setData(new ByteArrayInputStream(questionnaireData.getBytes()));
    return questionnaireElement;
  }

  private ZusammenElement buildImageElement(ImageEntity image, Action action) {
    ZusammenElement imageElement =
        buildElement(image.getId() == null ? null : new Id(image.getId()), action);
    Info info = new Info();
    info.addProperty(ElementPropertyName.elementType.name(), ElementType.Image);
    info.addProperty(ElementPropertyName.compositionData.name(), image.getCompositionData());
    imageElement.setInfo(info);
    imageElement.setData(new ByteArrayInputStream(image.getCompositionData().getBytes()));
    return imageElement;
  }


}
