package org.openecomp.sdc.be.ui.model;

import java.util.List;

import org.openecomp.sdc.be.model.category.CategoryDefinition;

public class UiCategories {
	
	private List<CategoryDefinition> resourceCategories;
	private List<CategoryDefinition> serviceCategories;
	private List<CategoryDefinition> productCategories;
	
	public List<CategoryDefinition> getResourceCategories() {
		return resourceCategories;
	}
	public void setResourceCategories(List<CategoryDefinition> resourceCategories) {
		this.resourceCategories = resourceCategories;
	}
	public List<CategoryDefinition> getServiceCategories() {
		return serviceCategories;
	}
	public void setServiceCategories(List<CategoryDefinition> serviceCategories) {
		this.serviceCategories = serviceCategories;
	}
	public List<CategoryDefinition> getProductCategories() {
		return productCategories;
	}
	public void setProductCategories(List<CategoryDefinition> productCategories) {
		this.productCategories = productCategories;
	}
}
