package org.openecomp.sdc.ci.tests.config;

import org.openecomp.sdc.ci.tests.datatypes.UserCredentials;
import org.openecomp.sdc.ci.tests.utils.general.FileHandling;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.Map;

public class UserCredentialsFromFile {

    private static final String CREDENTIALS_FILE = "credentials.yaml";
    private static Map<String, UserCredentials> credentials;
    private static Yaml yaml = new Yaml();

    private static final UserCredentialsFromFile instance = new UserCredentialsFromFile();

    public static UserCredentialsFromFile getInstance(){
        return instance;
    }

    private UserCredentialsFromFile() {

        credentials = null;

        File credentialsFileRemote = new File(FileHandling.getBasePath() + File.separator + "conf" + File.separator + CREDENTIALS_FILE);
//		File credentialsFileLocal = new File(FileHandling.getConfFilesPath() + CREDENTIALS_FILE);
        File credentialsFileLocal = new File(FileHandling.getSdcVnfsPath() + File.separator + "conf"
                + File.separator + CREDENTIALS_FILE);
        File[] credentialFiles = {credentialsFileRemote, credentialsFileLocal};
        for (File credentialsFile : credentialFiles){
            if (credentialsFile.exists()){
                try {
                    credentials = (Map<String, UserCredentials>) FileHandling.parseYamlFile(credentialsFile.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }


    }

    public UserCredentials getUserCredentialsByRole(String userRole) {
        Map<String, String> credentialsMap = (Map<String, String>) credentials.get(userRole);
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setUserId(credentialsMap.get("username"));
        userCredentials.setFirstName(credentialsMap.get("firstname"));
        userCredentials.setLastName(credentialsMap.get("lastname"));
        userCredentials.setPassword(credentialsMap.get("password"));
        return  userCredentials;
    }

}
