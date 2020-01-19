package org.openecomp.sdc.be.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.common.util.ICategorizedElement;

import java.util.List;

public class UiLeftPaletteComponent implements ICategorizedElement {
    private String uniqueId;
    private String name; // archiveName
    private String version; // archiveVersion
    private String description;
    private List<String> tags;
    private String icon;
    private String UUID;
    private String systemName;
    private String invariantUUID;
    private ComponentTypeEnum componentType;
    private String resourceType;
    private String categoryName;
    private String subCategoryName;
    private String searchFilterTerms;
    private List<CategoryDefinition> categories;

    public UiLeftPaletteComponent(Component component) {
        this.uniqueId = component.getUniqueId();
        this.name = component.getName();
        this.version = component.getVersion();
        this.description = component.getDescription();
        this.tags = component.getTags();
        this.icon = component.getIcon();
        this.UUID = component.getUUID();
        this.systemName = component.getSystemName();
        this.invariantUUID = component.getInvariantUUID();
        this.componentType = component.getComponentType();
        this.resourceType = component.getActualComponentType();
        this.categories = component.getCategories();
        this.categoryName = getCategoryName();
        this.subCategoryName = getSubcategoryName();
        String tagString = convertListResultToString(tags);
        setSearchFilterTerms(name + " " + description + " " + tagString + version);
        this.searchFilterTerms = getSearchFilterTerms();
    }

    private String convertListResultToString(List<String> tags) {
        StringBuilder sb = new StringBuilder();
        tags.forEach(t->sb.append(t + " "));
        return sb.toString().toLowerCase();
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTags() {
        return tags;
    }
    public String getIcon() {
        return icon;
    }

    public String getUUID() {
        return UUID;
    }

    public String getSystemName() {
        return systemName;
    }

    public String getInvariantUUID() {
        return invariantUUID;
    }

    public ComponentTypeEnum getComponentType() {
        return componentType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public List<CategoryDefinition> getCategories() {
        return categories;
    }

    public String getSearchFilterTerms() {
        return searchFilterTerms;
    }

    public void setSearchFilterTerms(String searchFilterTerms) {
        this.searchFilterTerms = searchFilterTerms;
    }

    @JsonIgnore
    @Override
    public String getComponentTypeAsString() {
        return getComponentType().name();
    }

    @JsonIgnore
    public String getCategoryName() {
        return getCategories().get(0).getName();
    }

    @JsonIgnore
    public String getSubcategoryName() {
        if(componentType == ComponentTypeEnum.SERVICE){
            return null;
        }
        return getCategories().get(0).getSubcategories().get(0).getName();
    }
}
