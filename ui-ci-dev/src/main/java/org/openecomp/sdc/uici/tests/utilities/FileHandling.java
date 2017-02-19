package org.openecomp.sdc.uici.tests.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class FileHandling {

	public static Map<?, ?> parseYamlFile(String filePath) throws FileNotFoundException {
		Yaml yaml = new Yaml();
		File file = new File(filePath);
		InputStream inputStream = new FileInputStream(file);
		Map<?, ?> map = (Map<?, ?>) yaml.load(inputStream);
		return map;
	}

	public static String getBasePath() {
		return System.getProperty("user.dir");
	}

	public static String getResourcesFilesPath() {
		return getBasePath() + File.separator + "src" + File.separator + "main" + File.separator + "resources"
				+ File.separator + "Files" + File.separator;
	}

}
