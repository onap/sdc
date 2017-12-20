package org.openecomp.sdc.healing.healers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateCandidateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.versioning.dao.types.Version;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;

public class VspOnboardingMethodHealerTest {
  private static final String vspId = "1";
  private static final Version version = new Version(1, 1);

  @Mock
  private VendorSoftwareProductInfoDao vendorSoftwareProductInfoDao;
  @Mock
  private OrchestrationTemplateDao orchestrationTemplateDao;
  @Mock
  private OrchestrationTemplateCandidateDao candidateDao;
  @InjectMocks
  private VspOnboardingMethodHealer vspOnboardingMethodHealer;

  private OrchestrationTemplateEntity orchestrationTemplateEntity = new OrchestrationTemplateEntity();
  private OrchestrationTemplateCandidateData candidateData = new OrchestrationTemplateCandidateData();
  private static final String NETWORK_PACKAGE = "NetworkPackage";
  private static final String HEAT = "HEAT";
  private static final String MANUAL = "Manual";

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(VspOnboardingMethodHealerTest.this);
  }

  @Test
  public void checkHealingWithNullOnboarding() throws Exception {
    VspDetails vspDetails = initAndExecuteHealer(null);
    assertEquals(vspDetails.getOnboardingMethod(), NETWORK_PACKAGE);
  }

  @Test
  public void checkHealingWithHEATOnboarding() throws Exception {
    VspDetails vspDetails = initAndExecuteHealer(HEAT);
    assertEquals(vspDetails.getOnboardingMethod(), NETWORK_PACKAGE);
  }

  @Test
  public void checkHealingWithManualOnboarding() throws Exception {
    VspDetails vspDetails = initAndExecuteHealer(MANUAL);
    assertEquals(vspDetails.getOnboardingMethod(), MANUAL);
  }

  private VspDetails initAndExecuteHealer(String onboardingMethod) throws Exception {
    VspDetails vspDetails = new VspDetails();
    vspDetails.setOnboardingMethod(onboardingMethod);

    setMockActions(vspDetails);
    vspOnboardingMethodHealer.heal(vspId, version);
    return vspDetails;
  }

  private void setMockActions(VspDetails vspDetails) {
    Mockito.doReturn(vspDetails).when(vendorSoftwareProductInfoDao).get(any());
    Mockito.doReturn(orchestrationTemplateEntity).when(orchestrationTemplateDao).get(any(), any());
    Mockito.doNothing().when(orchestrationTemplateDao).update(any(), any(), any());
    Mockito.doReturn(candidateData).when(candidateDao).get(any(), any());
    Mockito.doNothing().when(candidateDao).update(any(), any(), any());
  }
}