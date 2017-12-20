package org.openecomp.sdc.healing.healers;


import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateCandidateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateCandidateDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OnboardingMethod;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Objects;

public class VspOnboardingMethodHealer implements Healer {

  private static final String DEFAULT_FILE_NAME = "Upload File";
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private VendorSoftwareProductInfoDao vendorSoftwareProductInfoDao;
  private OrchestrationTemplateDao orchestrationTemplateDao;
  private OrchestrationTemplateCandidateDao candidateDao;

  public VspOnboardingMethodHealer(){
    this(VendorSoftwareProductInfoDaoFactory.getInstance().createInterface(),
        OrchestrationTemplateDaoFactory.getInstance().createInterface(),
        OrchestrationTemplateCandidateDaoFactory.getInstance().createInterface());
  }

  public VspOnboardingMethodHealer(VendorSoftwareProductInfoDao vspInfoDao,
                                   OrchestrationTemplateDao orchestrationTemplateDao,
                                   OrchestrationTemplateCandidateDao candidateDao) {
    this.vendorSoftwareProductInfoDao = vspInfoDao;
    this.orchestrationTemplateDao = orchestrationTemplateDao;
    this.candidateDao = candidateDao;
  }

  @Override
  public Object heal(String vspId, Version version) throws Exception {
    mdcDataDebugMessage.debugEntryMessage(null);

    VspDetails vsp = vendorSoftwareProductInfoDao.get(new VspDetails(vspId, version));
    healOnboardingMethod(vsp);

    if (!OnboardingMethod.Manual.name().equals(vsp.getOnboardingMethod())) {
      healOrchestrationTemplateFileName(vspId, version);
      healOrchestrationTemplateCandidateFileName(vspId, version);
    }

    mdcDataDebugMessage.debugExitMessage(null);
    return null;
  }

  private void healOnboardingMethod(VspDetails vsp) {
    if (Objects.isNull(vsp.getOnboardingMethod()) || "HEAT".equals(vsp.getOnboardingMethod())) {
      vsp.setOnboardingMethod(OnboardingMethod.NetworkPackage.name());
      vendorSoftwareProductInfoDao.update(vsp);
    }
  }

  private void healOrchestrationTemplateFileName(String vspId, Version version) {
    OrchestrationTemplateEntity orchestrationTemplate =
        orchestrationTemplateDao.get(vspId, version);

    if (orchestrationTemplate == null || orchestrationTemplate.getContentData() == null ||
        orchestrationTemplate.getFileSuffix() != null) {
      return;
    }
    orchestrationTemplate.setFileSuffix(OnboardingTypesEnum.ZIP.toString());
    orchestrationTemplate.setFileName(DEFAULT_FILE_NAME);

    orchestrationTemplateDao.update(vspId, version, orchestrationTemplate);
  }

  private void healOrchestrationTemplateCandidateFileName(String vspId, Version version) {
    OrchestrationTemplateCandidateData candidate = candidateDao.get(vspId, version);

    if (candidate == null || candidate.getContentData() == null ||
        candidate.getFileSuffix() != null) {
      return;
    }
    candidate.setFileSuffix(OnboardingTypesEnum.ZIP.toString());
    candidate.setFileName(DEFAULT_FILE_NAME);

    candidateDao.update(vspId, version, candidate);
  }
}
