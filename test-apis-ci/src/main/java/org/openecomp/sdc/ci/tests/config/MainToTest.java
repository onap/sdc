package org.openecomp.sdc.ci.tests.config;


import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.openecomp.sdc.ci.tests.utils.general.FileHandling;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

public class MainToTest {

    private static final String CREDENTIALS_FILE = "credentials.yaml";

//    public static void main(String[] args) throws Exception {
//        System.out.println("Hello World!"); // Display the string.
//        System.out.println("user.dir: " + System.getProperty("user.dir"));
//        System.out.println(UserRoleEnum.DESIGNER.getFirstName());
//        String file = readFile();
//        convertToJson(file);
//        Either<Service, RestResponse> createDefaultService1e = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true);
//
//
//
//    }

    private static String convertToJson(String yamlString) {
        Yaml yaml = new Yaml();
        Map<String, Object> map = (Map<String, Object>) yaml.load(yamlString);

        JSONObject jsonObject = new JSONObject(map);
        return jsonObject.toString();
    }

    private static String readFile() {

    File credentialsFileLocal = new File(FileHandling.getSdcVnfsPath() + File.separator + "conf"
            + File.separator + CREDENTIALS_FILE);

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(credentialsFileLocal);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            try {
                String mystr = IOUtils.toString(inputStream, Charset.forName("UTF-8"));
                inputStream.close();
                return mystr;
            } catch(IOException e) {
            }
        }

        return null;
    }


}
