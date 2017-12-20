package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToVSPGeneralConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToVSPQuestionnaireConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;
import org.openecomp.sdc.versioning.types.VersionableEntityStoreType;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

public class VendorSoftwareProductInfoDaoZusammenImpl implements VendorSoftwareProductInfoDao {
  private static final String EMPTY_DATA = "{}";

  private ZusammenAdaptor zusammenAdaptor;

  public VendorSoftwareProductInfoDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {
    VersionableEntityMetadata metadata =
        new VersionableEntityMetadata(VersionableEntityStoreType.Zusammen, "VendorSoftwareProduct",
            null, null);

    VersioningManagerFactory.getInstance().createInterface()
        .register(versionableEntityType, metadata);
  }

  @Override
  public Collection<VspDetails> list(VspDetails entity) {
    ElementToVSPGeneralConvertor convertor = new ElementToVSPGeneralConvertor();


    return zusammenAdaptor.listItems(createSessionContext()).stream()
        .filter(item -> "VendorSoftwareProduct".equals(item.getInfo().getProperty("item_type")))
        .map(item -> convertor.convert(item))
        .collect(Collectors.toList());
  }


  @Override
  public void create(VspDetails vspDetails) {
    ZusammenElement generalElement = mapVspDetailsToZusammenElement(vspDetails, Action.CREATE);

    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(vspDetails.getId(), vspDetails.getVersion().getId());
    zusammenAdaptor.saveElement(context, elementContext, generalElement,
        "Create VSP General Info Element");

    createVspStructure(context, elementContext);
  }

  private void createVspStructure(SessionContext context, ElementContext elementContext) {
    createOrchestrationTemplateCandidateStructure(context, elementContext);
    createVspModelStructure(context, elementContext);

    zusammenAdaptor.saveElement(context, elementContext,
        buildStructuralElement(ElementType.DeploymentFlavors, Action.CREATE),
        "Create VSP Deployment Flavors Element");

    zusammenAdaptor.saveElement(context, elementContext,
        buildStructuralElement(ElementType.Processes, Action.CREATE),
        "Create VSP Processes Element");
  }

  private void createOrchestrationTemplateCandidateStructure(SessionContext context,
                                                             ElementContext elementContext) {
    ByteArrayInputStream emptyData = new ByteArrayInputStream(EMPTY_DATA.getBytes());

    ZusammenElement candidateContentElement =
        buildStructuralElement(ElementType.OrchestrationTemplateCandidateContent, Action.CREATE);
    candidateContentElement.setData(emptyData);

    ZusammenElement candidateElement =
        buildStructuralElement(ElementType.OrchestrationTemplateCandidate, Action.CREATE);
    candidateElement.setData(emptyData);
    candidateElement.addSubElement(candidateContentElement);

    zusammenAdaptor.saveElement(context, elementContext, candidateElement,
        "Create Orchestration Template Candidate Elements");
  }

  private void createVspModelStructure(SessionContext context, ElementContext elementContext) {
    ZusammenElement vspModel = buildStructuralElement(ElementType.VspModel, Action.CREATE);
    vspModel.addSubElement(buildOrchestrationTemplateStructure());
    vspModel.addSubElement(buildStructuralElement(ElementType.Networks, Action.CREATE));
    vspModel.addSubElement(buildStructuralElement(ElementType.Components, Action.CREATE));
    vspModel
        .addSubElement(buildStructuralElement(ElementType.ComponentDependencies, Action.CREATE));

    ZusammenElement templates = buildStructuralElement(ElementType.Templates, Action.CREATE);
    ZusammenElement artifacts = buildStructuralElement(ElementType.Artifacts, Action.CREATE);
    vspModel.addSubElement(
        buildServiceModelStructure(ElementType.ServiceModel, templates, artifacts));
    vspModel.addSubElement(
        buildServiceModelStructure(ElementType.EnrichedServiceModel, templates, artifacts));

    zusammenAdaptor.saveElement(context, elementContext, vspModel, "Create VSP Model Elements");
  }

  private ZusammenElement buildOrchestrationTemplateStructure() {
    ByteArrayInputStream emptyData = new ByteArrayInputStream(EMPTY_DATA.getBytes());

    ZusammenElement validationData =
        buildStructuralElement(ElementType.OrchestrationTemplateValidationData, Action.CREATE);
    validationData.setData(emptyData);

    ZusammenElement orchestrationTemplate =
        buildStructuralElement(ElementType.OrchestrationTemplate, Action.CREATE);
    orchestrationTemplate.setData(emptyData);
    orchestrationTemplate.addSubElement(validationData);

    return orchestrationTemplate;
  }

  private ZusammenElement buildServiceModelStructure(ElementType serviceModelElementType,
                                                     ZusammenElement templates,
                                                     ZusammenElement artifacts) {
    ZusammenElement serviceModel = buildStructuralElement(serviceModelElementType, Action.CREATE);
    serviceModel.addSubElement(templates);
    serviceModel.addSubElement(artifacts);
    return serviceModel;
  }

  @Override
  public void update(VspDetails vspDetails) {
    ZusammenElement generalElement = mapVspDetailsToZusammenElement(vspDetails, Action.UPDATE);

    SessionContext context = createSessionContext();
    zusammenAdaptor.saveElement(context,
        new ElementContext(vspDetails.getId(), vspDetails.getVersion().getId()), generalElement,
        "Update VSP General Info Element");
  }

  @Override
  public VspDetails get(VspDetails vspDetails) {
    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(vspDetails.getId(), vspDetails.getVersion().getId());
    VspDetails vsp = zusammenAdaptor.getElementInfoByName(context, elementContext, null,
        ElementType.VendorSoftwareProduct.name())
        .map(new ElementToVSPGeneralConvertor()::convert)
        .orElse(null);
    vsp.setId(vspDetails.getId());
    vsp.setVersion(vspDetails.getVersion());
    return vsp;
  }

  @Override
  public void delete(VspDetails vspDetails) {
    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(vspDetails.getId(), vspDetails.getVersion().getId());

    zusammenAdaptor.saveElement(context, elementContext,
        buildStructuralElement(ElementType.VspModel, Action.DELETE),
        "Delete VSP Model Elements");

    createVspModelStructure(context, elementContext);
  }

  @Override
  public void updateQuestionnaireData(String vspId, Version version, String questionnaireData) {
    SessionContext context = createSessionContext();

    ZusammenElement questionnaireElement = mapQuestionnaireToZusammenElement(questionnaireData);
    zusammenAdaptor.saveElement(context, new ElementContext(vspId, version.getId()),
        questionnaireElement, "Update VSP Questionnaire");
  }


 /* @Override
  public String getQuestionnaireData(String vspId, Version version) {
    SessionContext context = createSessionContext();

    return zusammenAdaptor
        .getElementByName(context, new ElementContext(vspId, version.getId()), null,
            ElementType.Questionnaire.name())
        .map(questionnaireElement ->
            new String(FileUtils.toByteArray(questionnaireElement.getData())))
        .orElse(null);
  }*/

  @Override
  public VspQuestionnaireEntity getQuestionnaire(String vspId, Version version) {

    SessionContext context = createSessionContext();
    ElementToVSPQuestionnaireConvertor convertor = new ElementToVSPQuestionnaireConvertor();
    VspQuestionnaireEntity entity = convertor.convert(zusammenAdaptor
        .getElementByName(context, new ElementContext(vspId, version.getId()), null,
            ElementType.VSPQuestionnaire.name()).map(element -> element).orElse(null));
    entity.setId(vspId);
    entity.setVersion(version);
    return entity;
  }

  @Override
  public boolean isManual(String vspId, Version version) {
    final VspDetails vspDetails = get(new VspDetails(vspId, version));
    if (vspDetails != null) {
      if ("Manual".equals(vspDetails.getOnboardingMethod())) {
        return true;
      }
    }
    return false;
  }

  private ZusammenElement mapVspDetailsToZusammenElement(VspDetails vspDetails, Action action) {
    ZusammenElement generalElement =
        buildStructuralElement(ElementType.VendorSoftwareProduct, action);
    addVspDetailsToInfo(generalElement.getInfo(), vspDetails);
    return generalElement;
  }

  private ZusammenElement mapQuestionnaireToZusammenElement(String questionnaireData) {
    ZusammenElement questionnaireElement =
        buildStructuralElement(ElementType.VSPQuestionnaire, Action.UPDATE);
    questionnaireElement.setData(new ByteArrayInputStream(questionnaireData.getBytes()));
    return questionnaireElement;
  }

  private ZusammenElement mapTestElementToZusammenElement(String elementData) {
    ZusammenElement testElement =
        buildStructuralElement(ElementType.test, Action.UPDATE);
    testElement.setData(new ByteArrayInputStream(elementData.getBytes()));
    return testElement;
  }

  private void addVspDetailsToInfo(Info info, VspDetails vspDetails) {
    info.addProperty(InfoPropertyName.name.name(), vspDetails.getName());
    info.addProperty(InfoPropertyName.description.name(), vspDetails.getDescription());
    info.addProperty(InfoPropertyName.icon.name(), vspDetails.getIcon());
    info.addProperty(InfoPropertyName.category.name(), vspDetails.getCategory());
    info.addProperty(InfoPropertyName.subCategory.name(), vspDetails.getSubCategory());
    info.addProperty(InfoPropertyName.vendorId.name(), vspDetails.getVendorId());
    info.addProperty(InfoPropertyName.vendorName.name(), vspDetails.getVendorName());
    if (vspDetails.getVlmVersion() != null) {
      info.addProperty(InfoPropertyName.vendorVersion.name(), vspDetails.getVlmVersion().getId());
    }
    info.addProperty(InfoPropertyName.licenseAgreement.name(), vspDetails.getLicenseAgreement());
    info.addProperty(InfoPropertyName.featureGroups.name(), vspDetails.getFeatureGroups());
    info.addProperty(InfoPropertyName.onboardingMethod.name(), vspDetails.getOnboardingMethod());
  }

  public enum InfoPropertyName {
    name,
    description,
    icon,
    category,
    subCategory,
    vendorId,
    vendorName,
    vendorVersion,
    licenseAgreement,
    featureGroups,
    onboardingMethod
  }

}
