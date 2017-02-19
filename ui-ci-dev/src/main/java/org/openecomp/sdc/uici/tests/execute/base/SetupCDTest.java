package org.openecomp.sdc.uici.tests.execute.base;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.rules.TestName;
import org.openecomp.sdc.uici.tests.datatypes.CleanTypeEnum;
import org.openecomp.sdc.uici.tests.datatypes.UserCredentials;
import org.openecomp.sdc.uici.tests.utilities.FileHandling;
import org.openecomp.sdc.uici.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.run.StartTest;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces;
import com.google.common.collect.Lists;
import com.thinkaurelius.titan.core.TitanGraph;

public abstract class SetupCDTest extends ComponentBaseTest {

	private TitanSnapshot snapshot;
	private static CleanTypeEnum cleanType;

	public SetupCDTest() {
		super(new TestName(), SetupCDTest.class.getName());
	}

	public SetupCDTest(TestName name, String className) {
		super(name, className);
	}

	public static Logger logger = Logger.getLogger(SetupCDTest.class.getName());

	/**************** CONSTANTS ****************/
	private static final String CREDENTIALS_FILE = "src/main/resources/ci/conf/credentials.yaml";
	public static final String SELENIUM_NODE_URL = "http://%s:%s/wd/hub";

	/**************** PRIVATES ****************/
	public static Config config;
	private Map<?, ?> credentialsYamlFileMap;

	private static String devUrl, cdUrl;

	/****************
	 * BEFORE
	 * 
	 * @throws FileNotFoundException
	 ****************/

	@BeforeSuite(alwaysRun = true)
	@Parameters({ "clean-type" })
	public void setEnvParameters(@Optional("PARTIAL") String cleanType) throws FileNotFoundException {
		this.cleanType = CleanTypeEnum.findByName(cleanType);
		System.out.println("setup before class");
		config = Utils.getConfig();
		loadCredentialsFile();
		setUrl();
	}

	@BeforeMethod(alwaysRun = true)
	public void setBrowserBeforeTest() {
		setBrowserBeforeTest(getRole());
	}

	/**************** AFTER ****************/
	@AfterMethod(alwaysRun = true)
	public void quitAfterTest() {
		System.out.println("closing browser");
		GeneralUIUtils.getDriver().quit();
	}

	@BeforeMethod(alwaysRun = true)
	public void beforeState() throws Exception {
		CleanTypeEnum cleanType = getCleanMode();
		switch (cleanType) {
		case FULL: {
			super.beforeState(null);
			break;
		}
		case PARTIAL: {
			takeTitanSnapshot();
			break;
		}
		case NONE: {
			// No Clean Up
			break;
		}
		default: {
			throw new NotImplementedException("Enum Value:" + cleanType.name() + " Is not handled");
		}
		}

	}

	@AfterMethod(alwaysRun = true)
	public void afterState() throws Exception {
		CleanTypeEnum cleanType = getCleanMode();
		switch (cleanType) {
		case FULL: {
			super.afterState(null);
			break;
		}
		case PARTIAL: {
			resetToOriginalSnapshot();
			break;
		}
		case NONE: {
			// No Clean Up
			break;
		}
		default: {
			throw new NotImplementedException("Enum Value:" + cleanType.name() + " Is not handled");
		}
		}

	}

	private void takeTitanSnapshot() {
		List<Edge> edgeList = Lists.newArrayList(getTitanGraph().edges(null));
		List<Vertex> verList = Lists.newArrayList(getTitanGraph().vertices(null));
		setSnapshot(new TitanSnapshot(edgeList, verList));

	}

	private static class TitanSnapshot {
		List<Edge> edges;
		List<Vertex> vertices;

		public List<Edge> getEdges() {
			return edges;
		}

		public List<Vertex> getVertices() {
			return vertices;
		}

		private TitanSnapshot(List<Edge> edges, List<Vertex> vertices) {
			super();
			this.edges = edges;
			this.vertices = vertices;
		}
	}

	private void resetToOriginalSnapshot() {

		List<Edge> joinedEdges = new ArrayList<>();
		List<Vertex> joinedVertices = new ArrayList<>();
		TitanSnapshot original = getSnapshot();
		takeTitanSnapshot();
		TitanSnapshot current = getSnapshot();

		original.getEdges().stream().forEach(e -> addIfIdInList(e, current.getEdges(), joinedEdges, e2 -> e2.id()));
		original.getVertices().stream()
				.forEach(e -> addIfIdInList(e, current.getVertices(), joinedVertices, e2 -> e2.id()));

		List<Edge> edgesToRemove = removeFromList(current.getEdges(), joinedEdges, e2 -> e2.id());
		List<Vertex> verticesToRemove = removeFromList(current.getVertices(), joinedVertices, e2 -> e2.id());

		List<Edge> edgesToAdd = removeFromList(original.getEdges(), joinedEdges, e2 -> e2.id());
		List<Vertex> verticesToAdd = removeFromList(original.getVertices(), joinedVertices, e2 -> e2.id());

		if (edgesToAdd.isEmpty() && verticesToAdd.isEmpty()) {
			edgesToRemove.stream().forEach(e -> e.remove());
			verticesToRemove.stream().forEach(v -> v.remove());
		}

	}

	private <Element, ID> List<Element> removeFromList(List<Element> listToRemoveFrom, List<Element> elementsToRemove,
			Function<Element, ID> idGetter) {
		Set<ID> idSet = new HashSet<>();
		// Fill The Set
		elementsToRemove.stream().map(e -> idGetter.apply(e)).forEach(e2 -> idSet.add(e2));
		return listToRemoveFrom.stream().filter(p -> !idSet.contains(idGetter.apply(p))).collect(Collectors.toList());

	}

	private <Element, ID> void addIfIdInList(Element e, List<Element> listToCheck, List<Element> listToAddTo,
			Function<Element, ID> idGetter) {
		Stream<Element> matchingElements = listToCheck.stream()
				.filter(p -> idGetter.apply(e).equals(idGetter.apply(p)));
		listToAddTo.addAll(matchingElements.collect(Collectors.toList()));
	}

	/**************** MAIN ****************/
	public static void main(String[] args) {
		System.out.println("---------------------");
		System.out.println("running test from CLI");
		System.out.println("---------------------");
		args = new String[] { "ui-ci.xml" };
		StartTest.main(args);
	}

	/***********************************************************************************/

	protected void setBrowserBeforeTest(UserRoleEnum role) {
		System.out.println("setup before test");
		GeneralUIUtils.initDriver();
		setDevUrl(role);
		loginWithUser(role);
	}

	protected void setUrl() {
		cdUrl = config.getUrl();
		setDevUrl(getRole());
	}

	private Map<String, String> loadCredentialsFile() {
		final String credintialsFile = (System.getProperty("credentials.file") != null)
				? System.getProperty("credentials.file") : CREDENTIALS_FILE;
		System.out.println("credentials file is : " + credintialsFile);
		FunctionalInterfaces.swallowException(
				() -> credentialsYamlFileMap = (Map<String, String>) FileHandling.parseYamlFile(credintialsFile));
		System.out.println(credentialsYamlFileMap.toString());
		return (Map<String, String>) credentialsYamlFileMap;
	}

	protected UserCredentials getUserCredentialsFromFile(String userRole) throws Exception {
		Map<String, String> credentialsMap = (Map<String, String>) credentialsYamlFileMap.get(userRole);
		String user = (String) credentialsMap.get("username");
		String password = (String) credentialsMap.get("password");
		String firstname = (String) credentialsMap.get("firstname");
		String lastname = (String) credentialsMap.get("lastname");

		return new UserCredentials(user, password, firstname, lastname);
	}

	public void navigateToUrl(String url) throws InterruptedException {
		WebDriver driver = GeneralUIUtils.getDriver();
		System.out.println("navigating to URL :" + url);
		driver.navigate().to(url);
		driver.manage().window().maximize();
		driver.manage().deleteAllCookies();
	}

	protected void loginToSystem(UserCredentials credentials) throws Exception {

		sendUserAndPasswordKeys(credentials);
		WebElement submitButton = GeneralUIUtils.getDriver().findElement(By.name("btnSubmit"));
		submitButton.click();
		WebElement buttonOK = GeneralUIUtils.getDriver().findElement(By.name("successOK"));
		AssertJUnit.assertTrue(buttonOK.isDisplayed());
		buttonOK.click();
		System.out.println("Entering to design studio");
		Thread.sleep(2000);
		WebElement enterToUserWorkspaceButton = GeneralUIUtils.getDriver()
				.findElement(By.xpath("//button[@data-tests-id='Design Studio']"));
		enterToUserWorkspaceButton.click();
	}

	private void sendUserAndPasswordKeys(UserCredentials userId) {
		System.out.println("Login to system with user : " + userId.getUserId());
		WebElement userNameTextbox = GeneralUIUtils.getDriver().findElement(By.name("userid"));
		userNameTextbox.sendKeys(userId.getUserId());
		WebElement passwordTextbox = GeneralUIUtils.getDriver().findElement(By.name("password"));
		passwordTextbox.sendKeys(userId.getPassword());
	}

	public String getUrl() {
		String url;
		final CleanTypeEnum workMode = getCleanMode();
		switch (workMode) {
		case FULL: {
			url = devUrl;
			break;
		}
		case PARTIAL: {
			url = devUrl;
			break;
		}
		case NONE: {
			url = cdUrl;
			break;
		}
		default: {
			throw new NotImplementedException(workMode.name());
		}

		}
		return url;
	}

	public static void setDevUrl(UserRoleEnum role) {
		String url = SetupCDTest.devUrl;
		switch (role) {
		case ADMIN: {
			url = "http://localhost:8181/sdc1/proxy-admin1#/dashboard";
			break;
		}
		case DESIGNER: {
			url = "http://localhost:8181/sdc1/proxy-designer1#/dashboard";
			// url = "http://localhost:9000/#/dashboard";
			break;
		}
		case GOVERNOR: {
			url = "http://localhost:8181/sdc1/proxy-governor1#/dashboard";
			break;
		}
		case OPS: {
			url = "http://localhost:8181/sdc1/proxy-ops1#/dashboard";
			break;
		}
		case TESTER: {
			url = "http://localhost:8181/sdc1/proxy-tester1#/dashboard";
			break;
		}
		default: {
			break;
		}
		}
		SetupCDTest.devUrl = url;
	}

	public static Config getConfig() {
		return config;
	}

	private User user;

	public void loginWithUser(UserRoleEnum role) {

		setUser(role);
		String url = getUrl();
		System.out.println("URL is : " + url);
		try {
			navigateToUrl(url);
			if (url.contains("https://www.e-access")) {
				System.out.println("going to update designer user to mechIDs form...");
				UserCredentials credentials = getUserCredentialsFromFile(role.name().toLowerCase());
				loginToSystem(credentials);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void setUser(UserRoleEnum role) {
		user = new User();
		user.setUserId(role.getUserId());
		user.setFirstName(role.getFirstName());
		user.setRole(role.name());
	}

	/**
	 * Current User Role
	 * 
	 * @return
	 */
	public UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

	/**
	 * To change clean type update configuration.<br>
	 * Do not override this method.
	 * 
	 * @return
	 */
	protected final CleanTypeEnum getCleanMode() {
		return cleanType;
	}

	public User getUser() {
		return user;
	}

	protected void quitAndReLogin(UserRoleEnum role) {
		quitAfterTest();
		setBrowserBeforeTest(role);
		GeneralUIUtils.waitForLoader(30);
	}

	public TitanSnapshot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(TitanSnapshot snapshot) {
		this.snapshot = snapshot;
	}

	public static TitanGraph getTitanGraph() {
		return titanGraph;
	}

}
