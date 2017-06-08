package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

/**
 * The type get attribute data.
 */
public class GetAttrFuncData {

  private String fieldName;
  private String attributeName;

  public GetAttrFuncData(){}

  public GetAttrFuncData(String fieldName, String attributeName) {
    this.fieldName = fieldName;
    this.attributeName = attributeName;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GetAttrFuncData that = (GetAttrFuncData) o;

    if (fieldName != null ? !fieldName.equals(that.fieldName) : that.fieldName != null) {
      return false;
    }
    if (attributeName != null ? !attributeName.equals(that.attributeName)
        : that.attributeName != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = fieldName != null ? fieldName.hashCode() : 0;
    result = 31 * result + (attributeName != null ? attributeName.hashCode() : 0);
    return result;
  }
}
