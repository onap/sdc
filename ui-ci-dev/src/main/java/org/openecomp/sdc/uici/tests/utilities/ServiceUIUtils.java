package org.openecomp.sdc.uici.tests.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import org.openecomp.sdc.uici.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.uici.tests.datatypes.DataTestIdEnum.GeneralSection;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces;

public final class ServiceUIUtils {

	public static final String SERVICE_NAME_PREFIX = "ServiceCDTest-";
	public static final String INITIAL_VERSION = "0.1";
	public static final String ICON_SERVICE_NAME = "mobility";

	private ServiceUIUtils() {
		throw new UnsupportedOperationException();
	}

	public static String defineServiceName(String serviceName) {
		WebElement serviceNameElement = GeneralUIUtils.getWebElementWaitForVisible(GeneralSection.NAME.getValue());
		serviceNameElement.clear();
		serviceNameElement.sendKeys(serviceName);
		return serviceName;
	}

	public static void defineTagsList(ServiceReqDetails service, String[] serviceTags) {
		List<String> taglist = new ArrayList<String>();
		;
		WebElement serviceTagsTextbox = GeneralUIUtils.getWebElementWaitForVisible("i-sdc-tag-input");
		for (String tag : serviceTags) {
			serviceTagsTextbox.clear();
			serviceTagsTextbox.sendKeys(tag);
			serviceTagsTextbox.sendKeys(Keys.ENTER);
			taglist.add(tag);
		}
		taglist.add(0, service.getName());
		service.setTags(taglist);
	}

	public static Select defineServiceCategory(String category) {

		return GeneralUIUtils.getSelectList(category, "selectGeneralCategory");
	}

	private static void defineServiceProjectCode(String projectCode) {
		WebElement projectCodeTextbox = GeneralUIUtils.getWebElementWaitForVisible("projectCode");
		projectCodeTextbox.clear();
		projectCodeTextbox.sendKeys(projectCode);
	}

	private static void fillServiceGeneralPage(ServiceReqDetails service, User user) {
		service.setContactId(user.getUserId());
		service.setCreatorUserId(user.getUserId());
		service.setCreatorFullName(user.getFullName());
		defineServiceName(service.getName());
		defineServiceCategory(service.getCategories().get(0).getName());
		GeneralUIUtils.defineDescription(service.getDescription());
		defineTagsList(service,
				new String[] { service.getName(), "This-is-tag", "another-tag", "Test-automation-tag" });
		GeneralUIUtils.defineUserId(service.getCreatorUserId());
		defineServiceProjectCode(service.getProjectCode());

	}

	public static ServiceReqDetails createServiceInUI(User user) {

		ServiceReqDetails defineServiceetails = defineServiceDetails(user);
		GeneralUIUtils.clickAddComponent(DataTestIdEnum.Dashboard.BUTTON_ADD_SERVICE);

		GeneralUIUtils.waitForLoader();
		fillServiceGeneralPage(defineServiceetails, user);

		GeneralUIUtils.clickCreateButton();

		return defineServiceetails;

	}

	public static ServiceReqDetails defineServiceDetails(User user) {
		ServiceReqDetails service = new ServiceReqDetails();
		service = ElementFactory.getDefaultService(ServiceCategoriesEnum.MOBILITY, user);
		service.setVersion(INITIAL_VERSION);
		service.setIcon(ICON_SERVICE_NAME);
		service.setName(getRandomComponentName(SERVICE_NAME_PREFIX));

		return service;
	}

	protected static String getRandomComponentName(String prefix) {
		return prefix + new Random().nextInt(10000);
	}

	/**
	 * Waits Until service changed to requested lifeCycle State
	 * 
	 * @param createServiceInUI
	 * @param requestedLifeCycleState
	 * @param user
	 * @return
	 */
	public static Service waitForState(ServiceReqDetails createServiceInUI, LifecycleStateEnum requestedLifeCycleState,
			User user) {
		Supplier<Service> serviceGetter = () -> {
			String resourceString = RestCDUtils.getService(createServiceInUI, user).getResponse();
			return ResponseParser.convertServiceResponseToJavaObject(resourceString);
		};
		Function<Service, Boolean> verifier = res -> res.getLifecycleState() == requestedLifeCycleState;
		return FunctionalInterfaces.retryMethodOnResult(serviceGetter, verifier);

	}

	/**
	 * This Method Approves service for distribution<br>
	 * It assumes governor role is already logged in
	 * 
	 * @param createServiceInUI
	 */
	public static void approveServiceForDistribution(ServiceReqDetails createServiceInUI) {
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.getWebElementWaitForVisible(createServiceInUI.getName()).click();
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.LifeCyleChangeButtons.APPROVE.getValue()).click();
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.ACCEP_TESTING_MESSAGE.getValue())
				.sendKeys("Service " + createServiceInUI.getName() + " Approved For Distribution");
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.OK.getValue()).click();
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.waitForElementToDisappear(DataTestIdEnum.ModalItems.OK.getValue());
	}

}
