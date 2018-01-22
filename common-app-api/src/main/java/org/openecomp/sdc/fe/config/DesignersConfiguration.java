package org.openecomp.sdc.fe.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.common.api.BasicConfiguration;

public class DesignersConfiguration extends BasicConfiguration {

    private List<Designer> designersList;

    public List<Designer> getDesignersList() {
        return designersList;
    }

    public void setDesignersList(List<Designer> designersList) {
        this.designersList = designersList;
    }

    public DesignersConfiguration() {
        this.designersList = new ArrayList<>();
    }

    public static class Designer {

        private String designerId;
        private String designerHost;
        private Integer designerPort;
        private String designerPath;
        private String designerStateUrl;
        private String designerProtocol;
        private Map<String, DesignerDisplayOptions> designerDisplayOptions;

        public Map<String, DesignerDisplayOptions> getDesignerDisplayOptions() {
            return designerDisplayOptions;
        }

        public void setDesignerDisplayOptions(Map<String, DesignerDisplayOptions> designerDisplayOptions) {
            this.designerDisplayOptions = designerDisplayOptions;
        }

        public String getDesignerStateUrl() {
            return designerStateUrl;
        }

        public void setDesignerStateUrl(String designerStateUrl) {
            this.designerStateUrl = designerStateUrl;
        }

        public String getDesignerProtocol() {
            return designerProtocol;
        }

        public void setDesignerProtocol(String designerProtocol) {
            this.designerProtocol = designerProtocol;
        }

        public String getDesignerId() {
            return designerId;
        }

        public void setDesignerId(String designerId) {
            this.designerId = designerId;
        }

        public String getDesignerHost() {
            return designerHost;
        }

        public void setDesignerHost(String designerHost) {
            this.designerHost = designerHost;
        }

        public Integer getDesignerPort() {
            return designerPort;
        }

        public void setDesignerPort(Integer designerPort) {
            this.designerPort = designerPort;
        }

        public String getDesignerPath() {
            return designerPath;
        }

        public void setDesignerPath(String designerPath) {
            this.designerPath = designerPath;
        }

    }

    public static class DesignerDisplayOptions {

        private String displayName;
        private List<String> displayContext;

        public List<String> getDisplayContext() {
            return displayContext;
        }

        public void setDisplayContext(List<String> displayContext) {
            this.displayContext = displayContext;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

    }

}


