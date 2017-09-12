package org.openecomp.sdc.healing.healers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;

public class VspOnboardingMethodHealerTest{

    @Mock
    private VendorSoftwareProductInfoDao vendorSoftwareProductInfoDao;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(VspOnboardingMethodHealerTest.this);
    }

    @Test
    public void checkHealingWithNullOnboarding() throws Exception{
        VspOnboardingMethodHealer vspOnboardingMethodHealer = new VspOnboardingMethodHealer(vendorSoftwareProductInfoDao);
        Map<String,Object> params = new HashMap<>();
        params.put(SdcCommon.VSP_ID,"1");
        params.put(SdcCommon.VERSION, new Version(1,1));
        VspDetails vspDetails = new VspDetails();
        vspDetails.setOnboardingMethod(null);
        Mockito.when(vendorSoftwareProductInfoDao.get(any())).thenReturn(vspDetails);
        vspOnboardingMethodHealer.heal(params);
        assertEquals(vspDetails.getOnboardingMethod(),"NetworkPackage");
        assertEquals(vspDetails.getOnboardingOrigin(),"zip");
    }

    @Test
    public void checkHealingWithHEATOnboarding() throws Exception{
        VspOnboardingMethodHealer vspOnboardingMethodHealer = new VspOnboardingMethodHealer(vendorSoftwareProductInfoDao);
        Map<String,Object> params = new HashMap<>();
        params.put(SdcCommon.VSP_ID,"1");
        params.put(SdcCommon.VERSION, new Version(1,1));
        VspDetails vspDetails = new VspDetails();
        vspDetails.setOnboardingMethod("HEAT");
        Mockito.when(vendorSoftwareProductInfoDao.get(any())).thenReturn(vspDetails);
        vspOnboardingMethodHealer.heal(params);
        assertEquals(vspDetails.getOnboardingMethod(),"NetworkPackage");
        assertEquals(vspDetails.getOnboardingOrigin(),"zip");
    }

    @Test
    public void checkHealingWithManualOnboarding() throws Exception{
        VspOnboardingMethodHealer vspOnboardingMethodHealer = new VspOnboardingMethodHealer(vendorSoftwareProductInfoDao);
        Map<String,Object> params = new HashMap<>();
        params.put(SdcCommon.VSP_ID,"1");
        params.put(SdcCommon.VERSION, new Version(1,1));
        VspDetails vspDetails = new VspDetails();
        vspDetails.setOnboardingMethod("Manual");
        Mockito.when(vendorSoftwareProductInfoDao.get(any())).thenReturn(vspDetails);
        vspOnboardingMethodHealer.heal(params);
        assertEquals(vspDetails.getOnboardingMethod(),"Manual");
        assertEquals(vspDetails.getOnboardingOrigin(),null);
    }
}