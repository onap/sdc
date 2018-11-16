package org.openecomp.sdc.fe.config;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class PluginConfigurationTest {

	private PluginsConfiguration createTestSubject() {
		return new PluginsConfiguration();
	}


	@Test
	public void testGetPluginsList() throws Exception {
		PluginsConfiguration testSubject;
		List<PluginsConfiguration.Plugin> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPluginsList();
	}

	@Test
	public void testSetPluginsList() throws Exception {
		PluginsConfiguration testSubject;
		List<PluginsConfiguration.Plugin> result = new ArrayList<>() ;

		// default test
		testSubject = createTestSubject();
		testSubject.setPluginsList(result);
	}

	@Test
	public void testGetConnectionTimeout() throws Exception {
		PluginsConfiguration testSubject;
		// default test
		testSubject = createTestSubject();
		testSubject.getConnectionTimeout();
	}

	@Test
	public void testSetConnectionTimeout() throws Exception {
		PluginsConfiguration testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.setConnectionTimeout(10);
	}

    @Test
    public void testToString() throws Exception {
        PluginsConfiguration testSubject;

        // default test
        testSubject = createTestSubject();
        testSubject.toString();
    }

	@Test
	public void testPlugins() throws Exception {

		PluginsConfiguration.Plugin plugin = new PluginsConfiguration.Plugin();

		String pluginId = plugin.getPluginId();
		plugin.setPluginId(pluginId);

		String pluginDiscoveryUrl = plugin.getPluginDiscoveryUrl();
		plugin.setPluginDiscoveryUrl(pluginDiscoveryUrl);

		String pluginSourceUrl = plugin.getPluginSourceUrl();
		plugin.setPluginSourceUrl(pluginSourceUrl);

		String pluginStateUrl = plugin.getPluginStateUrl();
		plugin.setPluginStateUrl(pluginStateUrl);

		Map<String, PluginsConfiguration.PluginDisplayOptions>  pluginDisplayOption =  plugin.getPluginDisplayOptions();
		plugin.setPluginDisplayOptions(pluginDisplayOption);

	}

	@Test
	public void testPluginDisplayOptions() throws Exception {
        PluginsConfiguration.PluginDisplayOptions pluginDisplayOptions = new PluginsConfiguration.PluginDisplayOptions();

        String displayName = pluginDisplayOptions.getDisplayName();
        pluginDisplayOptions.setDisplayName(displayName);

        List<String> displayContext = pluginDisplayOptions.getDisplayContext();
        pluginDisplayOptions.setDisplayContext(displayContext);

        List<String> displayRoles = pluginDisplayOptions.getDisplayRoles();
        pluginDisplayOptions.setDisplayRoles(displayRoles);

        pluginDisplayOptions.toString();

	}

}