package org.openecomp.sdc.fe.config;

import java.util.ArrayList;
import java.util.List;

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

        private String displayName;
        private String designerHost;
        private Integer designerPort;
        private String designerPath;
        private String designerStateUrl;
        private String designerProtocol;
        private List<String> designerButtonLocation;
        private List<String> designerTabPresentation;

        public List<String> getDesignerButtonLocation() {
            return designerButtonLocation;
        }

        public void setDesignerButtonLocation(List<String> designerButtonLocation) {
            this.designerButtonLocation = designerButtonLocation;
        }

        public List<String> getDesignerTabPresentation() {
            return designerTabPresentation;
        }

        public void setDesignerTabPresentation(List<String> designerTabPresentation) {
            this.designerTabPresentation = designerTabPresentation;
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

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
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

}


