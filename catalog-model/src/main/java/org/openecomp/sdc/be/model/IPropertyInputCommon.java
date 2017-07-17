package org.openecomp.sdc.be.model;

import java.util.List;

import org.openecomp.sdc.be.datatypes.elements.PropertyRule;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;

public interface IPropertyInputCommon {

	String getType();
	SchemaDefinition getSchema();
	List<PropertyRule> getRules();
	String getName();
}
