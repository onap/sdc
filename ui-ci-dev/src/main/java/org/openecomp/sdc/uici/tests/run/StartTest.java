package org.openecomp.sdc.uici.tests.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.testng.TestNG;

import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.utils.Utils;

public class StartTest {

	public static long timeOfTest = 0;

	public static boolean debug = false;

	public static AtomicBoolean loggerInitialized = new AtomicBoolean(false);

	protected static Logger logger = null;

	public static void main(String[] args) {
		// TODO ui-ci add jar building
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

		TestNG testng = new TestNG();

		List<String> suites = new ArrayList<String>();
		suites.add("testSuites/" + args[0]);
		testng.setTestSuites(suites);
		// testng.setUseDefaultListeners(true);
		testng.setOutputDirectory("target/");

		testng.run();

	}

	public StartTest() {
		logger = Logger.getLogger(StartTest.class.getName());
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

	// private void addUnitTestResult(StringBuilder results,
	// Class<? extends AttSdcTest> testClass, Result unitTestResult) {
	//
	// boolean isSuccess = unitTestResult.wasSuccessful();
	//
	// String result = (isSuccess) ? "success" : "fail";
	// String fileName = FileUtils.getFileName(testClass.getName());
	// results.append("<tr>");
	// //
	// results.append("<td>").append(FileUtils.getFileName(testClass.getName())).append("</td>");
	// results.append("<td class=\"name\">")
	// .append("<a href=\"" + fileName + timeOfTest + ".html\">"
	// + fileName + "</a>").append("</td>");
	// results.append("<td class=\"" + result + "\">").append(result)
	// .append("</td>");
	// results.append("</tr>");
	// }

}
