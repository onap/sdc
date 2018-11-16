package org.openecomp.sdc.fe.config;

import org.junit.Test;
import org.openecomp.sdc.common.api.BasicConfiguration;
import org.openecomp.sdc.common.api.ConfigurationListener;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.FileChangeCallback;
import org.openecomp.sdc.common.config.EcompErrorConfiguration;
import org.openecomp.sdc.common.impl.ConfigFileChangeListener;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.rest.api.RestConfigurationInfo;


public class ConfigurationManagerTest {

    private ConfigurationManager createTestSubject() {
        ConfigurationSource configurationSource = new FSConfigurationSource(new ConfigFileChangeListener(),"abc")  ;
        return new ConfigurationManager(configurationSource);
    }

    @Test
    public void testGetConfiguration() {
        ConfigurationManager testSubject;
        Configuration result;

        testSubject = createTestSubject();
        result = testSubject.getConfiguration();
    }

    @Test
    public void testGetRestClientConfiguration() {
        ConfigurationManager testSubject;
        RestConfigurationInfo result;

        testSubject = createTestSubject();
        result = testSubject.getRestClientConfiguration();
    }

    @Test
    public void testGetEcompErrorConfiguration() {
        ConfigurationManager testSubject;
        EcompErrorConfiguration result;

        testSubject = createTestSubject();
        result = testSubject.getEcompErrorConfiguration();
    }

    @Test
    public void testGetPluginsConfiguration() {
        ConfigurationManager testSubject;
        PluginsConfiguration result;

        testSubject = createTestSubject();
        result = testSubject.getPluginsConfiguration();
    }

}
