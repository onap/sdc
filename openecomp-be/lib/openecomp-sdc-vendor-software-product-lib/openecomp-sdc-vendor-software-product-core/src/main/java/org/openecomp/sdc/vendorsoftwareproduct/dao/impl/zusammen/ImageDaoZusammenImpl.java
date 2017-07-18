package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;


import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

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
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(image.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(image.getVersion()));

    return listImages(context, elementContext, image);
  }

  private Collection<ImageEntity> listImages(SessionContext context,
                                                 ElementContext elementContext, ImageEntity image) {
    return zusammenAdaptor
        .listElementsByName(context, elementContext, new Id(image.getComponentId()),
            StructureElement.Images.name())
        .stream().map(elementInfo -> mapElementInfoToImage(
            image.getVspId(), image.getVersion(), image.getComponentId(), elementInfo))
        .collect(Collectors.toList());
  }

  private static ImageEntity mapElementInfoToImage(String vspId, Version version,
                                                       String componentId, ElementInfo elementInfo) {
    ImageEntity imageEntity =
        new ImageEntity(vspId, version, componentId, elementInfo.getId().getValue());
    imageEntity.setCompositionData(
        elementInfo.getInfo().getProperty(ElementPropertyName.compositionData.name()));
    return imageEntity;
  }

  @Override
  public void create(ImageEntity image) {
    ZusammenElement imageElement = imageToZusammen(image, Action.CREATE);

    ZusammenElement imagesElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.Images, null);
    imagesElement.setSubElements(Collections.singletonList(imageElement));

    ZusammenElement componentElement = new ZusammenElement();
    componentElement.setElementId(new Id(image.getComponentId()));
    componentElement.setAction(Action.IGNORE);
    componentElement.setSubElements(Collections.singletonList(imagesElement));

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(image.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    Optional<Element> savedElement =
        zusammenAdaptor.saveElement(context, elementContext, componentElement, "Create image");
    savedElement.ifPresent(element ->
        image.setId(element.getSubElements().iterator().next()
            .getSubElements().iterator().next().getElementId().getValue()));
  }

  @Override
  public void update(ImageEntity image) {
    ZusammenElement imageElement = imageToZusammen(image, Action.UPDATE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(image.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));
    zusammenAdaptor.saveElement(context, elementContext, imageElement,
        String.format("Update image with id %s", image.getId()));
  }

  @Override
  public ImageEntity get(ImageEntity image) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(image.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(image.getVersion()));
    Optional<Element> element = zusammenAdaptor.getElement(context, elementContext, image.getId());

    if (element.isPresent()) {
      image.setCompositionData(new String(FileUtils.toByteArray(element.get().getData())));
      return image;
    } else {
      return null;
    }
  }

  @Override
  public void delete(ImageEntity image) {
    ZusammenElement imageElement = new ZusammenElement();
    imageElement.setElementId(new Id(image.getId()));
    imageElement.setAction(Action.DELETE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(image.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));
    zusammenAdaptor.saveElement(context, elementContext, imageElement,
        String.format("Delete image with id %s", image.getId()));
  }

  @Override
  public void deleteByVspId(String vspId, Version version) {
    ZusammenElement imagesElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.Images, Action.DELETE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    zusammenAdaptor.saveElement(context,
        new ElementContext(itemId,
            VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor)),
        imagesElement, String.format("Delete all images with vsp id %s", vspId));
  }

  @Override
  public Collection<ImageEntity> listByVsp(String vspId, Version version) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(version));

    Collection<ComponentEntity> components = ComponentDaoZusammenImpl
        .listComponents(zusammenAdaptor, context, elementContext, vspId, version);

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
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(version));

    return getQuestionnaire(context, elementContext,
        new ImageEntity(vspId, version, componentId, imageId));
  }

  private ImageEntity getQuestionnaire(SessionContext context, ElementContext elementContext,
                                       ImageEntity image) {
    Optional<Element> questionnaireElement = zusammenAdaptor
        .getElementByName(context, elementContext, new Id(image.getId()),
            StructureElement.Questionnaire.name());
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

    ZusammenElement imageElement = new ZusammenElement();
    imageElement.setAction(Action.IGNORE);
    imageElement.setElementId(new Id(imageId));
    imageElement.setSubElements(Collections.singletonList(questionnaireElement));

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));
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
        VspZusammenUtil.buildStructuralElement(StructureElement.Questionnaire, action);
    questionnaireElement.setData(new ByteArrayInputStream(questionnaireData.getBytes()));
    return questionnaireElement;
  }

  private ZusammenElement buildImageElement(ImageEntity image, Action action) {
    ZusammenElement imageElement = new ZusammenElement();
    imageElement.setAction(action);
    if (image.getId() != null) {
      imageElement.setElementId(new Id(image.getId()));
    }
    Info info = new Info();
    info.addProperty(ElementPropertyName.type.name(), ElementType.Image);
    info.addProperty(ElementPropertyName.compositionData.name(), image.getCompositionData());
    imageElement.setInfo(info);
    imageElement.setData(new ByteArrayInputStream(image.getCompositionData().getBytes()));
    return imageElement;
  }


}
