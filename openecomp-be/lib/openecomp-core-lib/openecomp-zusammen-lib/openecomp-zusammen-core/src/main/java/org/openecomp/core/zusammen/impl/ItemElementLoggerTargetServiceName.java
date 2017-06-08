package org.openecomp.core.zusammen.impl;

/**
 * @author Avrahamg.
 * @since February 19, 2017
 */
public enum ItemElementLoggerTargetServiceName {
  ITEM_CREATION("Item Creation"),
  ITEM_VERSION_CREATION("Item Version Creation"),
  ELEMENT_CREATION("Element Creation"),
  ELEMENT_UPDATE("Element Update"),
  ELEMENT_LIST("Element List"),
  ELEMENT_GET("Element Get"),
  ELEMENT_GET_BY_PROPERTY("Element Get By Property"),
  ITEM_RETRIEVAL("Item retrieval"),
  ITEM_VERSION_RETRIEVAL("Item version retrieval)");

  private final String description;

  public String getDescription() {
    return description;
  }

  ItemElementLoggerTargetServiceName(String description) {
    this.description = description;
  }
}
