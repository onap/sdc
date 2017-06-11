/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.model.tosca;

import org.openecomp.sdc.be.model.tosca.converters.BooleanConverter;
import org.openecomp.sdc.be.model.tosca.converters.DefaultConverter;
import org.openecomp.sdc.be.model.tosca.converters.FloatConverter;
import org.openecomp.sdc.be.model.tosca.converters.IntegerConverter;
import org.openecomp.sdc.be.model.tosca.converters.JsonConverter;
import org.openecomp.sdc.be.model.tosca.converters.ListConverter;
import org.openecomp.sdc.be.model.tosca.converters.MapConverter;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.be.model.tosca.converters.StringConvertor;
import org.openecomp.sdc.be.model.tosca.converters.ToscaBooleanConverter;
import org.openecomp.sdc.be.model.tosca.converters.ToscaFloatConverter;
import org.openecomp.sdc.be.model.tosca.converters.ToscaJsonValueConverter;
import org.openecomp.sdc.be.model.tosca.converters.ToscaListValueConverter;
import org.openecomp.sdc.be.model.tosca.converters.ToscaMapValueConverter;
import org.openecomp.sdc.be.model.tosca.converters.ToscaStringConvertor;
import org.openecomp.sdc.be.model.tosca.converters.ToscaValueConverter;
import org.openecomp.sdc.be.model.tosca.converters.ToscaValueDefaultConverter;
import org.openecomp.sdc.be.model.tosca.validators.BooleanValidator;
import org.openecomp.sdc.be.model.tosca.validators.FloatValidator;
import org.openecomp.sdc.be.model.tosca.validators.IntegerValidator;
import org.openecomp.sdc.be.model.tosca.validators.JsonValidator;
import org.openecomp.sdc.be.model.tosca.validators.KeyValidator;
import org.openecomp.sdc.be.model.tosca.validators.ListValidator;
import org.openecomp.sdc.be.model.tosca.validators.MapValidator;
import org.openecomp.sdc.be.model.tosca.validators.PropertyTypeValidator;
import org.openecomp.sdc.be.model.tosca.validators.StringValidator;

/**
 * The primitive type that TOSCA YAML supports.
 * 
 * @author esofer
 */
public enum ToscaPropertyType {

	Root("tosca.datatypes.Root", null, null, null, true),

	STRING("string", StringValidator.getInstance(), StringConvertor.getInstance(), ToscaStringConvertor.getInstance()),

	BOOLEAN("boolean", BooleanValidator.getInstance(), ToscaBooleanConverter.getInstance(), BooleanConverter.getInstance()),

	FLOAT("float", FloatValidator.getInstance(), ToscaFloatConverter.getInstance(), FloatConverter.getInstance()),

	INTEGER("integer", IntegerValidator.getInstance(), DefaultConverter.getInstance(), IntegerConverter.getInstance()),
	
	SCALAR_UNIT("scalar-unit", StringValidator.getInstance(), DefaultConverter.getInstance(), ToscaValueDefaultConverter.getInstance()),

	SCALAR_UNIT_SIZE("scalar-unit.size", StringValidator.getInstance(), DefaultConverter.getInstance(), ToscaValueDefaultConverter.getInstance()),

	SCALAR_UNIT_TIME("scalar-unit.time", StringValidator.getInstance(), DefaultConverter.getInstance(), ToscaValueDefaultConverter.getInstance()),

	SCALAR_UNIT_FREQUENCY("scalar-unit.frequency", StringValidator.getInstance(), DefaultConverter.getInstance(), ToscaValueDefaultConverter.getInstance()),

	RANGE("range", StringValidator.getInstance(), DefaultConverter.getInstance(), ToscaValueDefaultConverter.getInstance()),

	TIMESTAMP("timestamp", StringValidator.getInstance(), DefaultConverter.getInstance(), ToscaValueDefaultConverter.getInstance()),

	MAP("map", MapValidator.getInstance(), MapConverter.getInstance(), ToscaMapValueConverter.getInstance()),

	LIST("list", ListValidator.getInstance(), ListConverter.getInstance(), ToscaListValueConverter.getInstance()),

	VERSION("version", StringValidator.getInstance(), DefaultConverter.getInstance(), ToscaValueDefaultConverter.getInstance()),

	KEY("key", KeyValidator.getInstance(), StringConvertor.getInstance(), ToscaValueDefaultConverter.getInstance()),

	JSON("json", JsonValidator.getInstance(), JsonConverter.getInstance(), ToscaJsonValueConverter.getInstance());

	// CREDENTIAL("tosca.datatypes.Credential", StringValidator.getInstance(),
	// DefaultConverter.getInstance());

	private String type;
	private PropertyTypeValidator validator;
	private PropertyValueConverter converter;
	private ToscaValueConverter valueConverter;
	private boolean isAbstract = false;

	ToscaPropertyType(String type, PropertyTypeValidator validator, PropertyValueConverter converter, ToscaValueConverter valueConverter) {
		this.type = type;
		this.validator = validator;
		this.converter = converter;
		this.valueConverter = valueConverter;
	}

	ToscaPropertyType(String type, PropertyTypeValidator validator, PropertyValueConverter converter, ToscaValueConverter valueConverter, boolean isAbstract) {
		this(type, validator, converter, valueConverter);
		this.isAbstract = isAbstract;
	}

	// private static final Pattern TIMESTAMP_REGEX = Pattern
	// .compile("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]|[0-9][0-9][0-9][0-9]-[0-9][0-9]?-[0-9][0-9]?([Tt]|[
	// \\t]+)[0-9][0-9]?:[0-9][0-9]:[0-9][0-9](\\.[0-9]*)?(([ \\t]*)Z|([
	// \\t]*)[-+][0-9][0-9]?(:[0-9][0-9])?)?");

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public PropertyTypeValidator getValidator() {
		return validator;
	}

	public void setValidator(PropertyTypeValidator validator) {
		this.validator = validator;
	}

	public PropertyValueConverter getConverter() {
		return converter;
	}

	public void setConverter(PropertyValueConverter converter) {
		this.converter = converter;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	public ToscaValueConverter getValueConverter() {
		return valueConverter;
	}

	public void setValueConverter(ToscaValueConverter valueConverter) {
		this.valueConverter = valueConverter;
	}

	public static ToscaPropertyType isValidType(String typeName) {
		if (typeName == null) {
			return null;
		}

		for (ToscaPropertyType type : ToscaPropertyType.values()) {
			if (type.getType().equals(typeName)) {
				return type;
			}
		}
		return null;
	}

	public static boolean isScalarType(String dataTypeName) {

		ToscaPropertyType isPrimitiveToscaType = ToscaPropertyType.isValidType(dataTypeName);

		return isPrimitiveToscaType != null && isPrimitiveToscaType.isAbstract() == false;

	}
	
	public static boolean isPrimitiveType(String dataTypeName) {
		
		if (ToscaPropertyType.MAP.getType().equals(dataTypeName) || ToscaPropertyType.LIST.getType().equals(dataTypeName)){
			return false;
		}
		if(isScalarType(dataTypeName)){
			return true;
		}
		return false;
	}

	public static ToscaPropertyType getTypeIfScalar(String dataTypeName) {

		ToscaPropertyType isPrimitiveToscaType = ToscaPropertyType.isValidType(dataTypeName);

		if (isPrimitiveToscaType != null && isPrimitiveToscaType.isAbstract() == false) {
			return isPrimitiveToscaType;
		} else {
			return null;
		}

	}

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
