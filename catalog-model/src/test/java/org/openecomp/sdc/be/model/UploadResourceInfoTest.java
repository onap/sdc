package org.openecomp.sdc.be.model;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.common.api.UploadArtifactInfo;

import java.util.LinkedList;
import java.util.List;

public class UploadResourceInfoTest {

	private UploadResourceInfo createTestSubject() {
		return new UploadResourceInfo();
	}

	@Test
	public void testCtor() throws Exception {
		new UploadResourceInfo("mock", "mock", "mock", "mock/mock/mock", new LinkedList<>(), new LinkedList<>());

	}

	@Test
	public void testGetPayloadData() throws Exception {
		UploadResourceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPayloadData();
	}

	@Test
	public void testSetPayloadData() throws Exception {
		UploadResourceInfo testSubject;
		String payload = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPayloadData(payload);
	}

	@Test
	public void testGetPayloadName() throws Exception {
		UploadResourceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPayloadName();
	}

	@Test
	public void testSetPayloadName() throws Exception {
		UploadResourceInfo testSubject;
		String payloadName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPayloadName(payloadName);
	}

	@Test
	public void testGetDescription() throws Exception {
		UploadResourceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	@Test
	public void testSetDescription() throws Exception {
		UploadResourceInfo testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	@Test
	public void testGetTags() throws Exception {
		UploadResourceInfo testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTags();
	}

	@Test
	public void testSetTags() throws Exception {
		UploadResourceInfo testSubject;
		List<String> tags = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTags(tags);
	}

	@Test
	public void testGetArtifactList() throws Exception {
		UploadResourceInfo testSubject;
		List<UploadArtifactInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactList();
	}

	@Test
	public void testSetArtifactList() throws Exception {
		UploadResourceInfo testSubject;
		List<UploadArtifactInfo> artifactsList = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactList(artifactsList);
	}

	@Test
	public void testHashCode() throws Exception {
		UploadResourceInfo testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	@Test
	public void testEquals() throws Exception {
		UploadResourceInfo testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		result = testSubject.equals(new Object());
		Assert.assertEquals(false, result);
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		result = testSubject.equals(createTestSubject());
		Assert.assertEquals(true, result);
	}

	@Test
	public void testGetContactId() throws Exception {
		UploadResourceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getContactId();
	}

	@Test
	public void testSetContactId() throws Exception {
		UploadResourceInfo testSubject;
		String userId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setContactId(userId);
	}

	@Test
	public void testGetName() throws Exception {
		UploadResourceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testSetName() throws Exception {
		UploadResourceInfo testSubject;
		String resourceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(resourceName);
	}

	@Test
	public void testGetResourceIconPath() throws Exception {
		UploadResourceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceIconPath();
	}

	@Test
	public void testSetResourceIconPath() throws Exception {
		UploadResourceInfo testSubject;
		String resourceIconPath = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceIconPath(resourceIconPath);
	}

	@Test
	public void testGetVendorName() throws Exception {
		UploadResourceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVendorName();
	}

	@Test
	public void testSetVendorName() throws Exception {
		UploadResourceInfo testSubject;
		String vendorName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVendorName(vendorName);
	}

	@Test
	public void testGetVendorRelease() throws Exception {
		UploadResourceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVendorRelease();
	}

	@Test
	public void testSetVendorRelease() throws Exception {
		UploadResourceInfo testSubject;
		String vendorRelease = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVendorRelease(vendorRelease);
	}

	@Test
	public void testGetResourceVendorModelNumber() throws Exception {
		UploadResourceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceVendorModelNumber();
	}

	@Test
	public void testSetResourceVendorModelNumber() throws Exception {
		UploadResourceInfo testSubject;
		String resourceVendorModelNumber = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceVendorModelNumber(resourceVendorModelNumber);
	}

	@Test
	public void testSetIcon() throws Exception {
		UploadResourceInfo testSubject;
		String icon = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setIcon(icon);
	}

	@Test
	public void testGetResourceType() throws Exception {
		UploadResourceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceType();
	}

	@Test
	public void testSetResourceType() throws Exception {
		UploadResourceInfo testSubject;
		String resourceType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceType(resourceType);
	}

	@Test
	public void testGetCategories() throws Exception {
		UploadResourceInfo testSubject;
		List<CategoryDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCategories();
	}

	@Test
	public void testSetCategories() throws Exception {
		UploadResourceInfo testSubject;
		List<CategoryDefinition> categories = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCategories(categories);
	}

	@Test
	public void testAddSubCategory() throws Exception {
		UploadResourceInfo testSubject;
		String category = "";
		String subCategory = "";

		// test 1
		testSubject = createTestSubject();
		category = null;
		subCategory = null;
		testSubject.addSubCategory(category, subCategory);

		// test 2
		testSubject = createTestSubject();
		category = "";
		subCategory = null;
		testSubject.addSubCategory(category, subCategory);

		// test 3
		testSubject = createTestSubject();
		subCategory = "";
		category = null;
		testSubject.addSubCategory(category, subCategory);

		// test 4
		testSubject = createTestSubject();
		subCategory = "mock";
		category = "mock";
		testSubject.addSubCategory(category, subCategory);
		testSubject.addSubCategory(category, subCategory);
	}
}