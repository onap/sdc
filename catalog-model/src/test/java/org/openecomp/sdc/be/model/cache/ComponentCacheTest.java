package org.openecomp.sdc.be.model.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.resources.data.ComponentCacheData;

import fj.data.Either;
import mockit.Deencapsulation;

@Ignore
public class ComponentCacheTest {

	private ComponentCache createTestSubject() {
		return new ComponentCache();
	}

	@Test
	public void testInit() throws Exception {
		ComponentCache testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.init();
	}

	@Test
	public void testIsEnabled() throws Exception {
		ComponentCache testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isEnabled();
	}

	@Test
	public void testSetEnabled() throws Exception {
		ComponentCache testSubject;
		boolean enabled = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setEnabled(enabled);
	}

	@Test
	public void testGetComponent() throws Exception {
		ComponentCache testSubject;
		String componentUid = "";
		Long lastModificationTime = null;
		Function<Component, Component> filterFieldsFunc = null;
		Either<Component, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponent(componentUid, lastModificationTime, filterFieldsFunc);
	}

	@Test
	public void testGetAllComponentIdTimeAndType() throws Exception {
		ComponentCache testSubject;
		Either<List<ComponentCacheData>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAllComponentIdTimeAndType();
	}

	@Test
	public void testGetComponentsForCatalog() throws Exception {
		ComponentCache testSubject;
		Set<String> components = null;
		ComponentTypeEnum componentTypeEnum = null;
		Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentsForCatalog(components, componentTypeEnum);
	}

	@Test
	public void testUpdateCatalogInMemoryCacheWithCertified() throws Exception {
		ComponentCache testSubject;
		List<Component> foundComponents = null;
		ComponentTypeEnum componentTypeEnum = null;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "updateCatalogInMemoryCacheWithCertified", List.class,
				ComponentTypeEnum.class);
	}

	@Test
	public void testGetDataFromInMemoryCache() throws Exception {
		ComponentCache testSubject;
		Set<String> components = null;
		ComponentTypeEnum componentTypeEnum = null;
		List<Component> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "getDataFromInMemoryCache", Set.class, ComponentTypeEnum.class);
	}

	@Test
	public void testGetComponents() throws Exception {
		ComponentCache testSubject;
		Set<String> components = null;
		Function<List<Component>, List<Component>> filterFieldsFunc = null;
		Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponents(components, filterFieldsFunc);
	}

	@Test
	public void testGetComponentsForLeftPanel() throws Exception {
		ComponentCache testSubject;
		ComponentTypeEnum componentTypeEnum = null;
		String internalComponentType = "";
		Set<String> filteredResources = null;
		Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentsForLeftPanel(componentTypeEnum, internalComponentType, filteredResources);
	}

	@Test
	public void testFilterForLeftPanel() throws Exception {
		ComponentCache testSubject;
		List<Component> components = null;
		List<Component> result;

		// test 1
		testSubject = createTestSubject();
		components = null;
		result = Deencapsulation.invoke(testSubject, "filterForLeftPanel", List.class);
		Assert.assertEquals(null, result);
	}

	@Test
	public void testFilterForCatalog() throws Exception {
		ComponentCache testSubject;
		List<Component> components = null;
		List<Component> result;

		// test 1
		testSubject = createTestSubject();
		components = null;
		result = Deencapsulation.invoke(testSubject, "filterForCatalog", List.class);
		Assert.assertEquals(null, result);
	}

	@Test
	public void testFilterFieldsForLeftPanel() throws Exception {
		ComponentCache testSubject;
		Component component = null;
		Component result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "filterFieldsForLeftPanel", new Object[] { Component.class });
	}

	@Test
	public void testFilterFieldsForCatalog() throws Exception {
		ComponentCache testSubject;
		Component component = null;
		Component result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "filterFieldsForCatalog", new Object[] { Component.class });
	}

	@Test
	public void testCopyFieldsForLeftPanel() throws Exception {
		ComponentCache testSubject;
		Component component = null;
		Component filteredComponent = null;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "copyFieldsForLeftPanel",
				new Object[] { Component.class, Component.class });
	}

	@Test
	public void testCopyFieldsForCatalog() throws Exception {
		ComponentCache testSubject;
		Component component = null;
		Component filteredComponent = null;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "copyFieldsForCatalog", new Object[] { Component.class, Component.class });
	}

	@Test
	public void testGetComponentsFull() throws Exception {
		ComponentCache testSubject;
		Set<String> filteredResources = null;
		Either<ImmutableTriple<List<Component>, List<Component>, Set<String>>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "getComponentsFull", Set.class);
	}

	@Test
	public void testConvertComponentCacheToComponent() throws Exception {
		ComponentCache testSubject;
		ComponentCacheData componentCacheData = null;
		Either<? extends Component, Boolean> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "convertComponentCacheToComponent",
				new Object[] { ComponentCacheData.class });
	}

	@Test
	public void testDeserializeComponent() throws Exception {
		ComponentCache testSubject;
		ComponentCacheData componentCacheData = null;
		byte[] dataAsArray = new byte[] { ' ' };
		Either<? extends Component, Boolean> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "deserializeComponent",
				new Object[] { ComponentCacheData.class, dataAsArray });
	}

	@Test
	public void testGetComponent_1() throws Exception {
		ComponentCache testSubject;
		String componentUid = "";
		Either<Component, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponent(componentUid);
	}

	@Test
	public void testGetComponent_2() throws Exception {
		ComponentCache testSubject;
		String componentUid = "";
		Long lastModificationTime = null;
		Either<Component, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponent(componentUid, lastModificationTime);
	}

	@Test
	public void testSetComponent() throws Exception {
		ComponentCache testSubject;
		String componentUid = "";
		Long lastModificationTime = null;
		NodeTypeEnum nodeTypeEnum = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setComponent(componentUid, lastModificationTime, nodeTypeEnum);
	}

	@Test
	public void testSaveComponent() throws Exception {
		ComponentCache testSubject;
		String componentUid = "";
		Long lastModificationTime = null;
		NodeTypeEnum nodeTypeEnum = null;
		Component component = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "saveComponent", componentUid, Long.class, NodeTypeEnum.class,
				Component.class);
	}

	@Test
	public void testSetComponent_1() throws Exception {
		ComponentCache testSubject;
		Component component = null;
		NodeTypeEnum nodeTypeEnum = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setComponent(component, nodeTypeEnum);
	}

	@Test
	public void testGetComponentsFull_1() throws Exception {
	ComponentCache testSubject;Map<String,Long> filteredResources = null;
	Either<ImmutablePair<List<Component>,Set<String>>,ActionStatus> result;
	
	// default test
	testSubject=createTestSubject();result=Deencapsulation.invoke(testSubject, "getComponentsFull", Map.class);
	}

	@Test
	public void testGetComponentsForCatalog_1() throws Exception {
		ComponentCache testSubject;
		Map<String, Long> components = null;
		ComponentTypeEnum componentTypeEnum = null;
		Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentsForCatalog(components, componentTypeEnum);
	}

	@Test
	public void testGetComponents_1() throws Exception {
		ComponentCache testSubject;
		Map<String, Long> components = null;
		Function<List<Component>, List<Component>> filterFieldsFunc = null;
		Either<ImmutablePair<List<Component>, Set<String>>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponents(components, filterFieldsFunc);
	}

	@Test
	public void testGetComponentAndTime() throws Exception {
		ComponentCache testSubject;
		String componentUid = "";
		Function<Component, Component> filterFieldsFunc = null;
		Either<ImmutablePair<Component, Long>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentAndTime(componentUid, filterFieldsFunc);
	}

	@Test
	public void testGetComponentFromCache() throws Exception {
	ComponentCache testSubject;String componentUid = "";
	Long lastModificationTime = null;
	Function<Component,Component> filterFieldsFunc = null;
	Either<ImmutablePair<Component,ComponentCacheData>,ActionStatus> result;
	
	// test 1
	testSubject=createTestSubject();lastModificationTime = null;
	result=Deencapsulation.invoke(testSubject, "getComponentFromCache", new Object[]{componentUid, Long.class, Function.class});
	Assert.assertEquals(null, result);
	
	// test 2
	testSubject=createTestSubject();filterFieldsFunc = null;
	result=Deencapsulation.invoke(testSubject, "getComponentFromCache", new Object[]{componentUid, Long.class, Function.class});
	Assert.assertEquals(null, result);
	}

	@Test
	public void testDeleteComponentFromCache() throws Exception {
		ComponentCache testSubject;
		String id = "";
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.deleteComponentFromCache(id);
	}
}