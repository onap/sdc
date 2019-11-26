package org.openecomp.sdc.fe.config;

import org.openecomp.sdc.common.api.BasicConfiguration;

import java.util.List;
import java.util.Map;

public class WorkspaceConfiguration extends BasicConfiguration {

    private Map<String, List<MenuConfiguration>> workspaceMenuConfiguration;

    public Map<String, List<MenuConfiguration>> getWorkspaceMenuConfiguration() {
        return workspaceMenuConfiguration;
    }

    public void setWorkspaceMenuConfiguration(Map<String, List<MenuConfiguration>> workspaceMenuConfiguration) {
        this.workspaceMenuConfiguration = workspaceMenuConfiguration;
    }

    public static class MenuConfiguration {
        String text;
        String action;
        String state;
        Integer menuIndex;
        List<String> disabledRoles;
        Map<String, String> initialData;

        public List<String> getDisabledRoles() {
            return disabledRoles;
        }

        public void setDisabledRoles(List<String> disabledRoles) {
            this.disabledRoles = disabledRoles;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public Integer getMenuIndex() {
            return menuIndex;
        }

        public void setMenuIndex(Integer menuIndex) {
            this.menuIndex = menuIndex;
        }

        public Map<String, String> getInitialData() {
            return initialData;
        }

        public void setInitialData(Map<String, String> initialData) {
            this.initialData = initialData;
        }
    }
}
