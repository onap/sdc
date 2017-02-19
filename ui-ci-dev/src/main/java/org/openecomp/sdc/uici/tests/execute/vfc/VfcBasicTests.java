package org.openecomp.sdc.uici.tests.execute.vfc;

import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpStatus;
import org.openecomp.sdc.uici.tests.datatypes.CreateAndUpdateStepsEnum;
import org.openecomp.sdc.uici.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.uici.tests.execute.base.SetupCDTest;
import org.openecomp.sdc.uici.tests.utilities.FileHandling;
import org.openecomp.sdc.uici.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.uici.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.uici.tests.utilities.RestCDUtils;
import org.openecomp.sdc.uici.tests.verificator.VfVerificator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.Test;

import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces;

public class VfcBasicTests extends SetupCDTest {

	@Test
	public void testRequirementsAndCapabilitiesSectionOfVfc() {
		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "mycompute.yml";
		ResourceUIUtils.importVfcInUI(getUser(), filePath, fileName);
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.REQUIREMENTS_AND_CAPABILITIES);
		// all expected requirements
		assertTrue("Not all expected requirements are displayed.", GeneralUIUtils.isElementPresent("dependency") && GeneralUIUtils.isElementPresent("local_storage"));
		// filter requirements
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ReqAndCapabilitiesSection.SEARCH_BOX.getValue()).sendKeys("root");
		Supplier<Boolean> supplier = () -> !GeneralUIUtils.isElementPresent("local_storage");
		Function<Boolean, Boolean> resultVerifier = isNotPresent -> isNotPresent;
		Boolean isFilteredRowNotPresent = FunctionalInterfaces.retryMethodOnResult(supplier, resultVerifier);
		assertTrue("The new property was not inserted to the properties table.", isFilteredRowNotPresent);
		assertTrue("Filter problem.", GeneralUIUtils.isElementPresent("dependency") && isFilteredRowNotPresent);
		// move to cap tab
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ReqAndCapabilitiesSection.CAP_TAB.getValue()).click();
		GeneralUIUtils.getWebElementWaitForVisible("endpoint").click();
		GeneralUIUtils.getWebElementWaitForVisible("initiator").click();
		supplier = () -> GeneralUIUtils.isElementPresent(DataTestIdEnum.PropertyForm.FORM_CONTAINER.getValue());
		resultVerifier = isPresent -> isPresent;
		Boolean isPopupOpen = FunctionalInterfaces.retryMethodOnResult(supplier, resultVerifier);
		assertTrue("The update property popup was not opened.", isPopupOpen);
	}

	@Test
	public void testCreatePropertyTypeListForVfc() {
		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "VFCWithAttributes.yml";
		ResourceUIUtils.importVfcInUI(getUser(), filePath, fileName);
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.PROPERTIES);
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertiesSection.ADD_BUTTON.getValue()).click();
		// fill in fields
		String newPropName = "listProperty";
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.NAME_FIELD.getValue()).sendKeys(newPropName);
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.DESCRIPTION_FIELD.getValue()).sendKeys("desc");
		Select typeField = new Select(GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.TYPE_FIELD.getValue()));
		typeField.selectByVisibleText("list");
		Select schemaTypeField = new Select(GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.SCHEMA_FIELD.getValue()));
		schemaTypeField.selectByVisibleText("string");
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.LIST_TYPE_DEFAULT_VAL_FIELD.getValue()).sendKeys("first");
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.ADD_ITEM_TO_LIST_BUTTON.getValue()).click();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.LIST_TYPE_DEFAULT_VAL_FIELD.getValue()).sendKeys("second");
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.ADD_ITEM_TO_LIST_BUTTON.getValue()).click();
		// save
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.SAVE_BUTTON.getValue()).click();
		Supplier<Boolean> supplier = () -> GeneralUIUtils.isElementPresent(newPropName);
		Function<Boolean, Boolean> resultVerifier = isPresent -> isPresent;
		Boolean isPresent = FunctionalInterfaces.retryMethodOnResult(supplier, resultVerifier);
		assertTrue("The new property was not inserted to the properties table.", isPresent);
	}

	@Test
	public void testCreatePropertyTypeMapForVfc() {
		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "VFCWithAttributes.yml";
		ResourceUIUtils.importVfcInUI(getUser(), filePath, fileName);
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.PROPERTIES);
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertiesSection.ADD_BUTTON.getValue()).click();
		// fill in fields
		String newPropName = "mapProperty";
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.NAME_FIELD.getValue()).sendKeys(newPropName);
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.DESCRIPTION_FIELD.getValue()).sendKeys("desc");
		Select typeField = new Select(GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.TYPE_FIELD.getValue()));
		typeField.selectByVisibleText("map");
		Select schemaTypeField = new Select(GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.SCHEMA_FIELD.getValue()));
		schemaTypeField.selectByVisibleText("string");
		// insert item to map
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.MAP_TYPE_DEFAULT_VAL_KEY_FIELD_FOR_FIRST_ITEM.getValue()).sendKeys("key1");
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.MAP_TYPE_DEFAULT_VAL_VALUE_FIELD_FOR_FIRST_ITEM.getValue()).sendKeys("val1");
		// insert item to map
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.ADD_ITEM_TO_MAP_BUTTON.getValue()).click();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.MAP_TYPE_DEFAULT_VAL_KEY_FIELD_FOR_SECOND_ITEM.getValue()).sendKeys("key2");
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.MAP_TYPE_DEFAULT_VAL_VALUE_FIELD_FOR_SECOND_ITEM.getValue()).sendKeys("val2");
		// delete item from map
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.DELETE_FIRST_ITEM_FROM_MAP_BUTTON.getValue()).click();
		// save
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.SAVE_BUTTON.getValue()).click();
		Supplier<Boolean> supplier = () -> GeneralUIUtils.isElementPresent(newPropName);
		Function<Boolean, Boolean> resultVerifier = isPresent -> isPresent;
		Boolean isPresent = FunctionalInterfaces.retryMethodOnResult(supplier, resultVerifier);
		assertTrue("The new property was not inserted to the properties table.", isPresent);
	}

	@Test
	public void testCreatePropertyTypeDTForVfc() {
		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "VFCWithAttributes.yml";
		ResourceUIUtils.importVfcInUI(getUser(), filePath, fileName);
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.PROPERTIES);
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertiesSection.ADD_BUTTON.getValue()).click();
		// fill in fields
		String newPropName = "dt";
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.NAME_FIELD.getValue()).sendKeys(newPropName);
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.DESCRIPTION_FIELD.getValue()).sendKeys("desc");
		Select typeField = new Select(GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.TYPE_FIELD.getValue()));
		typeField.selectByValue("org.openecomp.datatypes.heat.contrail.network.rule.PortPairs");
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.START_PORT_FIELD_FOR_PORT_PAIRS_DT.getValue()).sendKeys("first");
		// save
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.PropertyForm.SAVE_BUTTON.getValue()).click();
		Supplier<Boolean> supplier = () -> GeneralUIUtils.isElementPresent(newPropName);
		Function<Boolean, Boolean> resultVerifier = isPresent -> isPresent;
		Boolean isPresent = FunctionalInterfaces.retryMethodOnResult(supplier, resultVerifier);
		assertTrue("The new property was not inserted to the properties table.", isPresent);
	}

	@Test
	public void testViewAttributesTabForVfc() {
		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "VFCWithAttributes.yml";
		ResourceReqDetails importVfcResourceInUI = ResourceUIUtils.importVfcInUI(getUser(), filePath, fileName);
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.ATTRIBUTES);

		List<WebElement> attributesRows = GeneralUIUtils.getWebElementsListWaitForVisible(DataTestIdEnum.AttributesSection.TABLE_ROWS.getValue());
		assertTrue("There is not any row in the table.", !CollectionUtils.isEmpty(attributesRows));
		// display editable buttons
		assertTrue("The Add button is not dispaly.", GeneralUIUtils.isElementPresent(DataTestIdEnum.AttributesSection.ADD_BUTTON.getValue()));
		assertTrue("The Edit button is not dispaly for 'network' attribute.", GeneralUIUtils.isElementPresent(DataTestIdEnum.AttributesSection.EDIT_BUTTON_FOR_NETWORK_ATTR.getValue()));
		assertTrue("The Remove button is not dispaly for 'network' attribute.", GeneralUIUtils.isElementPresent(DataTestIdEnum.AttributesSection.DELETE_BUTTON_FOR_NETWORK_ATTR.getValue()));
		// click checkin
		GeneralUIUtils.checkIn();
		// enter again
		GeneralUIUtils.getWebElementWaitForVisible(importVfcResourceInUI.getName()).click();
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.ATTRIBUTES);
		// the editable buttons disappear
		assertTrue("The Add button is not dispaly.", !GeneralUIUtils.isElementPresent(DataTestIdEnum.AttributesSection.ADD_BUTTON.getValue()));
		assertTrue("The Edit button is not dispaly for 'network' attribute.", !GeneralUIUtils.isElementPresent(DataTestIdEnum.AttributesSection.EDIT_BUTTON_FOR_NETWORK_ATTR.getValue()));
		assertTrue("The Remove button is not dispaly for 'network' attribute.", !GeneralUIUtils.isElementPresent(DataTestIdEnum.AttributesSection.DELETE_BUTTON_FOR_NETWORK_ATTR.getValue()));
	}

	@Test
	public void testCreateAttributeForVfc() {
		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "VFCWithAttributes.yml";
		ResourceReqDetails importVfcResourceInUI = ResourceUIUtils.importVfcInUI(getUser(), filePath, fileName);
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.ATTRIBUTES);
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.AttributesSection.ADD_BUTTON.getValue()).click();
		// fill in fields
		String newAttrName = "attr";
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.AttributeForm.NAME_FIELD.getValue()).sendKeys(newAttrName);
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.AttributeForm.DESCRIPTION_FIELD.getValue()).sendKeys("desc");
		Select typeField = new Select(GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.AttributeForm.TYPE_FIELD.getValue()));
		typeField.selectByVisibleText("integer");
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.AttributeForm.DEFAULT_VAL_FIELD.getValue()).sendKeys("2");
		// click ok
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.AttributeForm.DONE_BUTTON.getValue()).click();
		Supplier<Boolean> supplier = () -> GeneralUIUtils.isElementPresent(newAttrName);
		Function<Boolean, Boolean> resultVerifier = isPresent -> isPresent;
		Boolean isPresent = FunctionalInterfaces.retryMethodOnResult(supplier, resultVerifier);
		assertTrue("The new attribute was not inserted to the attributes table.", isPresent);
	}

	@Test
	public void testUpdateTypeForAttributeOfVfc() {
		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "VFCWithAttributes.yml";
		ResourceReqDetails importVfcResourceInUI = ResourceUIUtils.importVfcInUI(getUser(), filePath, fileName);
		VfVerificator.verifyResourceIsCreated(importVfcResourceInUI);
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.ATTRIBUTES);
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.AttributesSection.EDIT_BUTTON_FOR_NETWORK_ATTR.getValue()).click();
		// fill in fields
		Select typeField = new Select(GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.AttributeForm.TYPE_FIELD.getValue()));
		typeField.selectByVisibleText("float");
		// click ok
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.AttributeForm.DONE_BUTTON.getValue()).click();
		Supplier<Boolean> supplier = () -> GeneralUIUtils.isElementPresent("float");
		Function<Boolean, Boolean> resultVerifier = isPresent -> isPresent;
		Boolean isPresent = FunctionalInterfaces.retryMethodOnResult(supplier, resultVerifier);
		assertTrue("The attribute type was not updated.", isPresent);
	}

	@Test
	public void testDeleteAttributeForVfc() {
		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "VFCWithAttributes.yml";
		ResourceReqDetails importVfcResourceInUI = ResourceUIUtils.importVfcInUI(getUser(), filePath, fileName);
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.ATTRIBUTES);
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.AttributesSection.DELETE_BUTTON_FOR_NETWORK_ATTR.getValue()).click();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.OK.getValue()).click();
		GeneralUIUtils.waitForLoader();
		Boolean retryResult = FunctionalInterfaces.retryMethodOnResult(() -> !GeneralUIUtils.isElementPresent("networks"), boolResult -> boolResult);
		assertTrue("The attribute is shown in the attributes table.", retryResult);
	}

	@Test
	public void testImportVfc() {
		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "CP.yml";
		ResourceReqDetails importVfcResourceInUI = ResourceUIUtils.importVfcInUI(getUser(), filePath, fileName);
		assertTrue(RestCDUtils.getResource(importVfcResourceInUI).getErrorCode() == HttpStatus.SC_OK);
	}
}
