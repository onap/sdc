/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.ci.tests.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.PropertyConfigurator;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.openecomp.sdc.ci.tests.api.AttSdcTest;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartTest2backup {

	private List<Class<? extends AttSdcTest>> testClasses = new ArrayList<Class<? extends AttSdcTest>>();
	public static long timeOfTest = 0;

	public static boolean debug = false;

	public static AtomicBoolean loggerInitialized = new AtomicBoolean(false);

	protected static Logger logger = null;

	public static void main(String[] args) {

		String debugEnabled = System.getProperty("debug");
		if (debugEnabled != null && debugEnabled.equalsIgnoreCase("true")) {
			debug = true;
		}
		System.out.println("Debug mode is " + (debug ? "enabled" : "disabled"));

		enableLogger();

		Config config = null;
		try {
			config = Utils.getConfig();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (config == null) {
			logger.error("Failed to configuration file of ci tests.");
			System.exit(1);
		}

		List<String> packagesToRun = config.getPackages();
		if (packagesToRun == null || true == packagesToRun.isEmpty()) {
			logger.error("No package was configured to be executed.");
			System.exit(2);
		}
		StartTest2backup tests = new StartTest2backup();

		boolean stopOnClassFailure = false;
		String stopOnClassFailureStr = System.getProperty("stopOnClassFailure");
		if (stopOnClassFailureStr != null && stopOnClassFailureStr.equalsIgnoreCase("true")) {
			stopOnClassFailure = true;
		} else {
			Boolean stopOnClassFailureObj = config.isStopOnClassFailure();
			if (stopOnClassFailureObj != null) {
				stopOnClassFailure = stopOnClassFailureObj.booleanValue();
			}
		}

		tests.start(packagesToRun, stopOnClassFailure);
	}

	public StartTest2backup() {
		logger = LoggerFactory.getLogger(StartTest2backup.class.getName());
	}

	public static void enableLogger() {

		if (false == loggerInitialized.get()) {

			loggerInitialized.set(true);

			String log4jPropsFile = System.getProperty("log4j.configuration");
			if (System.getProperty("os.name").contains("Windows")) {
				String logProps = "src/main/resources/ci/conf/log4j.properties";
				if (log4jPropsFile == null) {
					System.setProperty("targetlog", "target/");
					log4jPropsFile = logProps;
				}

			}
			PropertyConfigurator.configureAndWatch(log4jPropsFile);

		}
	}

	public void start(List<String> packages, boolean exitOnFailure) {

		boolean success = true;
		StringBuilder results = new StringBuilder();
		Result result;

		if (packages == null) {
			return;
		}

		for (String packageName : packages) {
			// List<Class> classesForPackage =
			// getClassesForPackage("org.openecomp.sdc.ci.tests.execute");
			List<Class> classesForPackage = getClassesForPackage(packageName);
			if (classesForPackage != null && false == classesForPackage.isEmpty()) {
				for (Class testUnit : classesForPackage) {
					testClasses.add(testUnit);
				}
			}
		}

		System.out.println(testClasses);

		// tsetClasses.add(LogValidatorTest.class);
		// tsetClasses.add(AttNorthboundTest.class);

		results.append(
				"<Html><head><style>th{background-color: gray;color: white;height: 30px;}td {color: black;height: 30px;}.fail {background-color: #FF5555;width: 100px;text-align: center;}.success {background-color: #00FF00;width: 100px;text-align: center;}.name {width: 200px;background-color: #F0F0F0;}.message {width: 300px;background-color: #F0F0F0;}</style></head><body>");

		Calendar calendar = Calendar.getInstance();
		timeOfTest = calendar.getTimeInMillis();
		SimpleDateFormat date_format = new SimpleDateFormat("MMM dd yyyy HH:mm:ss");
		results.append("<br/><h2> This report generated on " + date_format.format(calendar.getTime()) + "</h2><br/>");

		results.append("<table>");
		addTableHead(results);

		int size = testClasses.size();
		int index = 0;

		int totalRunTests = 0;
		int totalFailureTests = 0;
		int totalIgnoreTests = 0;
		int numOfFailureClasses = 0;
		for (Class<? extends AttSdcTest> testClass : testClasses) {

			index++;

			StringBuilder builder = new StringBuilder();
			String str = "***************************************************************************";
			builder.append(str + "\n");
			String current = "class " + index + "/" + size + " failure(" + numOfFailureClasses + ") + RUNS("
					+ totalRunTests + ")" + " FAILURES(" + totalFailureTests + ") IGNORED(" + totalIgnoreTests + ")";
			int interval = ((str.length() - current.length() - 2) / 2);
			String substring = str.substring(0, interval);
			builder.append(substring + " " + current + " " + substring + "\n");
			builder.append(str + "\n");

			System.out.println(builder.toString());

			logger.debug(builder.toString());
			logger.debug("Going to run test class {}",testClass.getName());

			result = JUnitCore.runClasses(testClass);
			if (result.wasSuccessful() == false) {
				numOfFailureClasses++;
			}
			logger.debug("Test class {} finished {}",testClass.getName(),(result.wasSuccessful() ? "OK." : " WITH ERROR."));
			List<Failure> failures = result.getFailures();
			if (failures != null) {
				for (Failure failure : failures) {
					logger.error("Test class {} failure test {}-{}",testClass.getName(),failure.getTestHeader(),failure.getTrace());
				}
			}
			int runsPerClass = result.getRunCount();
			int failuresPerClass = result.getFailureCount();
			int ignoredPerClass = result.getIgnoreCount();

			totalRunTests += runsPerClass;
			totalFailureTests += failuresPerClass;
			totalIgnoreTests += ignoredPerClass;

			logger.debug("class {} Failed tests {} %",testClass.getName(),
					(failuresPerClass * 1.0) / runsPerClass * 100);
			logger.debug("class {} Ignored tests {} %",testClass.getName(),
					(ignoredPerClass * 1.0) / runsPerClass * 100);

			// List<Failure> failures = result.getFailures();
			// if (failures != null) {
			// for (Failure failure : failures) {
			// System.err.println("9999999999" + failure.getTestHeader());
			// }
			// }

			addUnitTestResult(results, testClass, result);
			success &= result.wasSuccessful();

			if (numOfFailureClasses > 0) {
				// if (exitOnFailure) {
				if (exitOnFailure) {
					break;
				}
			}
		}

		results.append("</table>");
		results.append("<br/><h2> Tests Summary: </h2><br/>");
		results.append("Total Runs  : " + totalRunTests + "<br/>");
		results.append("Total Failure  : " + totalFailureTests + "<br/>");
		results.append("Total: " + totalFailureTests + "/" + totalRunTests + "<br/>");
		results.append("</html>");

		FileUtils.writeToFile(Config.instance().getOutputFolder() + File.separator + Config.instance().getReportName(),
				results.toString());

		if (!success) {
			System.out.println("FAILURE");
			logger.error("Failure tests : {} %",((totalFailureTests + totalIgnoreTests) * 1.0) / (totalRunTests + totalIgnoreTests));
			logger.error("Ignored tests : {} %",(totalIgnoreTests * 1.0) / (totalRunTests + totalIgnoreTests));
			System.exit(1);
		}

		System.out.println("SUCCESS");
	}

	private List<Class> getClassesForPackage(String pkgname) {

		List<Class> classes = new ArrayList<Class>();

		// Get a File object for the package
		File directory = null;
		String fullPath;
		String relPath = pkgname.replace('.', '/');

		// System.out.println("ClassDiscovery: Package: " + pkgname +
		// " becomes Path:" + relPath);

		URL resource = ClassLoader.getSystemClassLoader().getResource(relPath);

		// System.out.println("ClassDiscovery: Resource = " + resource);
		if (resource == null) {
			throw new RuntimeException("No resource for " + relPath);
		}
		fullPath = resource.getFile();
		// System.out.println("ClassDiscovery: FullPath = " + resource);

		if (debug) {
			System.out.println("fullPath is " + fullPath);
		}

		try {
			directory = new File(resource.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(
					pkgname + " (" + resource
							+ ") does not appear to be a valid URL / URI.  Strange, since we got it from the system...",
					e);
		} catch (IllegalArgumentException e) {
			directory = null;
		}
		// System.out.println("ClassDiscovery: Directory = " + directory);

		if (directory != null && directory.exists()) {

			// Get the list of the files contained in the package
			String[] files = directory.list();
			for (int i = 0; i < files.length; i++) {

				// we are only interested in .class files
				if (files[i].endsWith(".class") && false == files[i].contains("$")) {

					// removes the .class extension
					String className = pkgname + '.' + files[i].substring(0, files[i].length() - 6);

					// System.out.println("ClassDiscovery: className = " +
					// className);

					if (debug) {
						System.out.println("ClassDiscovery: className = " + className);
					}

					try {
						Class clas = Class.forName(className);
						boolean isAddToRun = false;
						Method[] methods = clas.getMethods();
						for (Method method : methods) {
							Annotation[] anns = method.getAnnotations();
							for (Annotation an : anns) {
								if (an.annotationType().getSimpleName().equalsIgnoreCase("Test")) {
									isAddToRun = true;
									break;
								}
							}
						}
						if (isAddToRun)
							classes.add(clas);
					} catch (ClassNotFoundException e) {
						throw new RuntimeException("ClassNotFoundException loading " + className);
					}
				}
			}
		} else {
			try {
				String jarPath = fullPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");

				if (debug) {
					System.out.println("jarPath is " + jarPath);
				}

				JarFile jarFile = new JarFile(jarPath);
				Enumeration<JarEntry> entries = jarFile.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					String entryName = entry.getName();
					if (entryName.startsWith(relPath) && entryName.length() > (relPath.length() + "/".length())) {

						// System.out.println("ClassDiscovery: JarEntry: " +
						// entryName);
						String className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");

						// System.out.println("ClassDiscovery: className = " +
						// className);

						if (false == className.contains("$")) {

							if (debug) {
								System.out.println("ClassDiscovery: className = " + className);
							}

							try {
								Class clas = Class.forName(className);
								boolean isAddToRun = false;
								Method[] methods = clas.getMethods();
								for (Method method : methods) {
									Annotation[] anns = method.getAnnotations();
									for (Annotation an : anns) {
										if (an.annotationType().getSimpleName().equalsIgnoreCase("Test")) {
											isAddToRun = true;
											break;
										}
									}
								}
								if (isAddToRun)
									classes.add(clas);
							} catch (ClassNotFoundException e) {
								throw new RuntimeException("ClassNotFoundException loading " + className);
							}
						}
					}
				}
				jarFile.close();

			} catch (IOException e) {
				throw new RuntimeException(pkgname + " (" + directory + ") does not appear to be a valid package", e);
			}
		}
		return classes;
	}

	private void addTableHead(StringBuilder results) {
		results.append("<tr>");
		results.append("<th>").append("Unit Test").append("</th>");
		results.append("<th>").append("Result").append("</th>");
		results.append("</tr>");
	}

	private void addUnitTestResult(StringBuilder results, Class<? extends AttSdcTest> testClass,
			Result unitTestResult) {

		boolean isSuccess = unitTestResult.wasSuccessful();

		String result = (isSuccess) ? "success" : "fail";
		String fileName = FileUtils.getFileName(testClass.getName());
		results.append("<tr>");
		// results.append("<td>").append(FileUtils.getFileName(testClass.getName())).append("</td>");
		results.append("<td class=\"name\">")
				.append("<a href=\"" + fileName + timeOfTest + ".html\">" + fileName + "</a>").append("</td>");
		results.append("<td class=\"" + result + "\">").append(result).append("</td>");
		results.append("</tr>");
	}

}
