package org.openecomp.sdc.vendorsoftwareproduct.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.config.api.Configuration;
import org.openecomp.sdc.vendorsoftwareproduct.types.helmvalidator.HelmValidatorConfig;

@RunWith(MockitoJUnitRunner.class)
public class HelmValidatorConfigReaderTest {

    private final static String CONFIG_NAMESPACE = "helmvalidator";

    @Mock
    private Configuration configuration;

    @Test
    public void shouldReadHelmValidatorConfig() {
        //given
        when(configuration.getAsString(CONFIG_NAMESPACE, "hValidatorVersion")).thenReturn("V2test");
        when(configuration.getAsBooleanValue(CONFIG_NAMESPACE, "hValidatorEnabled")).thenReturn(true);
        when(configuration.getAsBooleanValue(CONFIG_NAMESPACE, "hValidatorDeployable")).thenReturn(true);
        when(configuration.getAsBooleanValue(CONFIG_NAMESPACE, "hValidatorLintable")).thenReturn(true);
        when(configuration.getAsBooleanValue(CONFIG_NAMESPACE, "hValidatorStrictLintable")).thenReturn(true);
        HelmValidatorConfigReader helmValidatorConfigReader = new HelmValidatorConfigReader(configuration);
        //when
        HelmValidatorConfig helmValidatorConfig = helmValidatorConfigReader.getHelmValidatorConfig();
        //then
        assertEquals("V2test", helmValidatorConfig.getVersion());
        assertTrue(helmValidatorConfig.isEnabled());
        assertTrue(helmValidatorConfig.isDeployable());
        assertTrue(helmValidatorConfig.isLintable());
        assertTrue(helmValidatorConfig.isStrictLintable());
    }
}
