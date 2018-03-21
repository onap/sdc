package org.openecomp.sdc.ci.tests.config;

import org.openecomp.sdc.ci.tests.datatypes.UserCredentials;
import org.openecomp.sdc.ci.tests.utils.general.FileHandling;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class UserCredentialsFromFile {

    private static final String CREDENTIALS_FILE = "credentials.yaml";
    private static Map<?, ?> credentials;
    private static Yaml yaml = new Yaml();

    private static UserCredentialsFromFile credentialsFromFile;
//    private UserCredentialsFromFile() {
//
//    }

    public synchronized static UserCredentialsFromFile getInstance() {
        if (credentialsFromFile == null) {
            try {
                credentialsFromFile = new UserCredentialsFromFile();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return credentialsFromFile;
    }

    private void UserCredentialsFromFile() throws IOException {

        credentials = null;

        File credentialsFileRemote = new File(FileHandling.getBasePath() + File.separator + "conf" + File.separator + CREDENTIALS_FILE);
//		File credentialsFileLocal = new File(FileHandling.getConfFilesPath() + CREDENTIALS_FILE);
        File credentialFile = new File(FileHandling.getSdcVnfsPath() + File.separator + "conf"
                + File.separator + CREDENTIALS_FILE);

        if (false == credentialFile.exists()) {
            throw new RuntimeException("The config file " + credentialFile + " cannot be found.");
        }


        File[] credentialFiles = {credentialsFileRemote, credentialFile};

        for (File credentialsFile : credentialFiles){
            if (credentialsFile.exists()){
                try {
                    credentials = FileHandling.parseYamlFile(credentialsFile.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }



    }


    public static UserCredentials getUserCredentialsByRole(String userRole) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, String> credentialsMap = (Map<String, String>) credentials.get(userRole);
        String user = (String) credentialsMap.get("username");
        String password = (String) credentialsMap.get("password");
        String firstname = (String) credentialsMap.get("firstname");
        String lastname = (String) credentialsMap.get("lastname");

        return new UserCredentials(user, password, firstname, lastname, userRole);
    }

}
