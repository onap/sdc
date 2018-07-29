package org.openecomp.sdc.be.model;

import java.util.ArrayList;
import java.util.List;


public class ComponentDependency {
    private String name;
    private String version;
    private String uniqueId;
    private String type;
    private String icon;
    private String state;
    private List<String> instanceNames;
    
    private List<ComponentDependency> dependencies;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ComponentDependency> getDependencies() {
        return dependencies;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<String> getInstanceNames() {
        return instanceNames;
    }

    public void setInstanceNames(List<String> instanceNames) {
        this.instanceNames = instanceNames;
    }

    public void setDependencies(List<ComponentDependency> dependencies) {
        this.dependencies = dependencies;
    }
    public void addDependencies(List<ComponentDependency> dependencies) {
        if ( this.dependencies == null ){
            this.dependencies = new ArrayList<>();
        }
        this.dependencies.addAll(dependencies);
   }

    public void addDependency(ComponentDependency dependency){
        if ( dependencies == null ){
            dependencies = new ArrayList<>();
        }
        dependencies.add(dependency);
    }
}
