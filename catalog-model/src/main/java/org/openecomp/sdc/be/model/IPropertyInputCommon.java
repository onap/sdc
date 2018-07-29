package org.openecomp.sdc.be.model;

import org.openecomp.sdc.be.datatypes.elements.PropertyRule;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;

import java.util.List;

public interface IPropertyInputCommon {

    String getType();
    SchemaDefinition getSchema();
    List<PropertyRule> getRules();
    String getName();
}
