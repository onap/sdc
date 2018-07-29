package org.openecomp.sdc.dmaap;

import com.google.common.base.MoreObjects;
import org.kohsuke.args4j.Option;

public class CliArgs {

    @Option(name="yml",aliases = {"-YML","YML","-yml","-YAML","YAML","-yaml"}, usage="mandatory arg. YAML filename", required=true)
    private String yamlFilename;

    @Option(name="path",aliases = {"-path","PATH","-PATH"}, usage="mandatory arg. path to the yaml file which contains topic config (publisher data + messages)", required=true)
    private String yamlPath;

    @Option(name="cr",aliases = {"CR","-cr","-CR"}, usage="optional arg. concurrent requests", required=false)
    private String concurrentRequests;

    @Option(name="notification",aliases = {"NOTIFICATION","-NOTIFICATION","-notification"}, usage="optional load dynamic messages", required=false)
    private String notificationData;

    public String getYamlPath() {
        return yamlPath;
    }

    public String getYamlFilename() {
        return yamlFilename;
    }

    public void setYamlPath(String yamlPath) {
        this.yamlPath = yamlPath;
    }


    public String getConcurrentRequests() {
        return concurrentRequests;
    }

    public void setConcurrentRequests(String concurrentRequests) {
        this.concurrentRequests = concurrentRequests;
    }

    public String getNotificationData() {
        return notificationData;
    }


    public void setYamlFilename(String yamlFilename) {
        this.yamlFilename = yamlFilename;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("yamlPath", yamlPath)
                .add("concurrentRequests", concurrentRequests)
                .toString();
    }
    
    
}
