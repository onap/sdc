package org.openecomp.config.util;

import org.openecomp.config.ConfigurationUtils;
import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationManager;
import org.junit.Assert;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.openecomp.config.util.ConfigTestConstant.*;
import static org.openecomp.config.util.ConfigTestConstant.ARTIFACT_ENCODED;
import static org.openecomp.config.util.ConfigTestConstant.ARTIFACT_MANDATORY_NAME;

/**
 * Created by sheetalm on 10/13/2016.
 */
public class TestUtil {

    public final static String jsonSchemaLoc = System.getProperty("user.home")+"/TestResources/";
    public static FileWriter fileWriter ;

    public static void writeFile(String data) throws IOException {
        File dir = new File(jsonSchemaLoc);
        File file = null;
        dir.mkdirs();
        file = new File(jsonSchemaLoc+"/GeneratorsList.json");
        file.createNewFile();
        fileWriter = new FileWriter(file);
        fileWriter.write(data);
        fileWriter.close();
    }

    public static void cleanUp() throws Exception {
        String data = "{name:\"SCM\"}";
        TestUtil.writeFile(data);
        //ConfigurationUtils.executeDdlSql("truncate dox.configuration");
        try{
            ConfigurationUtils.executeDdlSql("truncate dox.configuration_change");
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    public static void validateConfiguraton(String nameSpace) {
        Configuration config = ConfigurationManager.lookup();

        Assert.assertEquals(config.getAsString(nameSpace, ARTIFACT_NAME_MAXLENGTH ), "14");

        // First value from list is picked from Merge properties
        Assert.assertEquals(config.getAsString(nameSpace, ARTIFACT_MAXSIZE ), "1048576");

        List<String> expectedExtList = new ArrayList<String>();
        expectedExtList.add("pdf");
        expectedExtList.add("zip");
        expectedExtList.add("xml");
        expectedExtList.add("pdf");
        expectedExtList.add("tgz");
        expectedExtList.add("xls");
        List<String> extList = config.getAsStringValues(nameSpace, ConfigTestConstant.ARTIFACT_EXT);
        Assert.assertEquals(expectedExtList, extList);

        List<String> expectedEncList = new ArrayList<String>();
        expectedEncList.add("Base64");
        expectedEncList.add("MD5");
        List<String> encList = config.getAsStringValues(nameSpace, ConfigTestConstant.ARTIFACT_ENC);
        Assert.assertEquals(expectedEncList, encList);

        Assert.assertEquals(config.getAsString(nameSpace, ARTIFACT_NAME_UPPER ), "a-zA-Z_0-9");
        Assert.assertEquals(config.getAsString(nameSpace, ARTIFACT_NAME_LOWER ), "a-zA-Z");
        Assert.assertEquals(config.getAsString(nameSpace, ARTIFACT_STATUS ), "deleted");

        List<String> expectedLocList = new ArrayList<String>();
        expectedLocList.add("/opt/spool");
        expectedLocList.add(System.getProperty("user.home")+"/asdc");
        List<String> locList = config.getAsStringValues(nameSpace, ConfigTestConstant.ARTIFACT_LOC);
        Assert.assertEquals(expectedLocList, locList);

        Assert.assertEquals(config.getAsString(nameSpace, ARTIFACT_JSON_SCHEMA ), "@GeneratorList.json");

        Assert.assertEquals("@"+System.getenv("Path")+"/myschema.json",config.getAsString(nameSpace, ConfigTestConstant.ARTIFACT_XML_SCHEMA));

        List<String> artifactConsumer = config.getAsStringValues(nameSpace, ConfigTestConstant.ARTIFACT_CONSUMER );
        Assert.assertEquals(config.getAsStringValues(nameSpace, ConfigTestConstant.ARTIFACT_CONSUMER_APPC ), artifactConsumer);

        Assert.assertEquals(config.getAsString(nameSpace, ARTIFACT_NAME_MINLENGTH ), "6");
        Assert.assertEquals(config.getAsString(nameSpace, ARTIFACT_MANDATORY_NAME ), "true");
        Assert.assertEquals(config.getAsString(nameSpace, ARTIFACT_ENCODED ), "true");
    }
}
