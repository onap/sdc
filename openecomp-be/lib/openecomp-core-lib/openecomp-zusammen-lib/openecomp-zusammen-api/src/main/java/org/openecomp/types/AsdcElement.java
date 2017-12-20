package org.openecomp.types;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Relation;
import com.amdocs.zusammen.utils.fileutils.FileUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class AsdcElement implements Element {

  private String type;
  private String name;
  private String description;

  private Map<String, Object> properties;
  private byte[] data;
  private Collection<Relation> relations;
  private Collection<Element> subElements = new ArrayList<>();
  private Action action;
  private Id elementId;

  @Override
  public Action getAction() {
    return this.action;
  }

  @Override
  public Id getElementId() {
    return this.elementId;
  }

  @Override
  public Info getInfo() {
    Info info = new Info();
    info.setProperties(this.properties);
    info.addProperty(ElementPropertyName.elementType.name(), this.type != null ? this.type : this.name);
    info.setName(this.name);
    info.setDescription(this.description);

    return info;
  }

  @Override
  public Collection<Relation> getRelations() {
    return this.relations;
  }

  @Override
  public InputStream getData() {
    return FileUtils.toInputStream(this.data);
  }

  @Override
  public InputStream getSearchableData() {
    return null;
  }

  @Override
  public InputStream getVisualization() {
    return null;
  }


  @Override
  public Collection<Element> getSubElements() {
    return this.subElements;
  }

  public void setElementId(Id elementId) {
    this.elementId = elementId;
  }

  public void setData(InputStream data) {
    this.data = FileUtils.toByteArray(data);
  }

  public void setRelations(Collection<Relation> relations) {
    this.relations = relations;
  }

  public void setSubElements(Collection<Element> subElements) {
    this.subElements = subElements;
  }

  public void setAction(Action action) {
    this.action = action;
  }

  public AsdcElement addSubElement(Element element) {
    this.subElements.add(element);
    return this;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }
}
