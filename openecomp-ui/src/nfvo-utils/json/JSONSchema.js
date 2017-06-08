/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
// import Ajv from 'ajv';
import cloneDeep from 'lodash/cloneDeep.js';
import JSONPointer from './JSONPointer.js';

export default class JSONSchema {

	setSchema(schema) {
		this._schema = schema;
		this._fragmentsCache = new Map();
		// this._ajv = new Ajv({
		// 	useDefaults: true,
		// 	coerceTypes: true
		// });
		// this._validate = this._ajv.compile(schema);
	}

	processData(data) {
		data = cloneDeep(data);
		// this._validate(data);
		return data;
	}

	// array of names of validation functions
	setSupportedValidationFunctions(supportedValidationFunctions) {
		this._supportedValidationFunctions = supportedValidationFunctions;
	}

	/* FYI - I was going to support "required" but then found out that server never sends it in its schema (it was a business decision. so leaving the code commented for now */
	flattenSchema(supportedValidationFunctions) {
		if (supportedValidationFunctions) { this.setSupportedValidationFunctions(supportedValidationFunctions); }
		let genericFieldInfo = {};
		if (this._schema && this._schema.properties) {
			this.travelProperties(this._schema.properties, genericFieldInfo/*, this._schema.required*/);
		}
		return {genericFieldInfo};
	}

	extractGenericFieldInfo(item) {
		let validationsArr = [];
		let additionalInfo = { isValid: true, errorText: ''};
		for (let value in item) {
			if (this._supportedValidationFunctions.includes(value)) {
				let validationItem = this.extractValidations(item, value);
				validationsArr[validationsArr.length] = validationItem;
			} else {
				let enumResult = this.extractEnum(item, value);
				if (enumResult !== null) {
					additionalInfo.enum = enumResult;
				}
				else {
					additionalInfo[value] = item[value];
				}
				/*if (required.includes (property)) {
				 additionalInfo[value].isRequired = true ;
				 }*/
			}
		}

		additionalInfo.validations = validationsArr;
		return additionalInfo;
	}

	extractValidations(item, value) {
		let validationItem;
		let data = item[value];
		if (value === 'maximum') {
			if (item.exclusiveMaximum) {
				value = 'maximumExclusive';
			}
		}
		if (value === 'minimum') {
			if (item.exclusiveMinimum) {
				value = 'minimumExclusive';
			}
		}
		validationItem = {type: value, data: data};
		return validationItem;
	}

	extractEnum(item, value) {
		let enumResult = null;
		if (value === 'type' && item[value] === 'array') {
			let items = item.items;
			if (items && items.enum && items.enum.length > 0) {
				let values = items.enum
					.filter(value => value)
					.map(value => ({enum: value, title: value}));
				enumResult = values;
			}
		}
		else if (value === 'enum') {
			let items = item[value];
			if (items && items.length > 0) {
				let values = items
					.filter(value => value)
					.map(value => ({enum: value, title: value}));
				enumResult = values;
			}
		}
		return enumResult;
	}

	travelProperties(properties, genericFieldDefs, /*required = [],*/ pointer = ''){
		let newPointer = pointer;
		for (let property in properties) {
			newPointer = newPointer ? newPointer + '/' + property : property;
			if (properties[property].properties) {
				this.travelProperties(properties[property].properties, genericFieldDefs /*, properties[property].required*/, newPointer);
			}
			else if (properties[property].$ref){
				let fragment = this._getSchemaFragmentByRef(properties[property].$ref);
				if (fragment.properties) {
					this.travelProperties(fragment.properties, genericFieldDefs /*, properties[property].required*/, newPointer);
				} else {
					genericFieldDefs[newPointer] = this.extractGenericFieldInfo(fragment.properties);
				}
			}
			else {
				genericFieldDefs[newPointer] = this.extractGenericFieldInfo(properties[property]);
			}
			newPointer = pointer;
		}
	}

	getTitle(pointer) {
		return this._getSchemaFragment(pointer).title;
	}

	exists(pointer) {
		const fragment = this._getSchemaFragment(pointer);
		return !!fragment;
	}

	getDefault(pointer) {
		const fragment = this._getSchemaFragment(pointer);
		return fragment && fragment.default;
	}

	getEnum(pointer) {
		const fragment = this._getSchemaFragment(pointer);
		return fragment && (fragment.type === 'array' ? fragment.items.enum : fragment.enum);
	}

	isRequired(pointer) {
		const parentPointer = JSONPointer.extractParentPointer(pointer);
		const lastPart = JSONPointer.extractLastPart(pointer);
		let parentFragment = this._getSchemaFragment(parentPointer);
		return parentFragment && parentFragment.required && parentFragment.required.includes(lastPart);
	}

	isNumber(pointer) {
		const fragment = this._getSchemaFragment(pointer);
		return fragment && fragment.type === 'number';
	}

	getMaxValue(pointer) {
		const fragment = this._getSchemaFragment(pointer);
		return fragment && fragment.exclusiveMaximum ? fragment.maximum - 1 : fragment.maximum;
	}

	getMinValue(pointer) {
		const fragment = this._getSchemaFragment(pointer);
		return fragment && fragment.exclusiveMinimum ? fragment.minimum : fragment.minimum;
	}

	isString(pointer) {
		const fragment = this._getSchemaFragment(pointer);
		return fragment && fragment.type === 'string';
	}

	getPattern(pointer) {
		const fragment = this._getSchemaFragment(pointer);
		return fragment && fragment.pattern;
	}

	getMaxLength(pointer) {
		const fragment = this._getSchemaFragment(pointer);
		return fragment && fragment.maxLength;
	}

	getMinLength(pointer) {
		const fragment = this._getSchemaFragment(pointer);
		return fragment && fragment.minLength;
	}

	isArray(pointer) {
		const fragment = this._getSchemaFragment(pointer);
		return fragment && fragment.type === 'array';
	}

	_getSchemaFragment(pointer) {
		if (this._fragmentsCache.has(pointer)) {
			return this._fragmentsCache.get(pointer);
		}

		let parts = JSONPointer.extractParts(pointer);

		let fragment = parts.reduce((fragment, part) => {
			if (fragment === undefined) {
				return undefined;
			}

			if (fragment.$ref) {
				fragment = this._getSchemaFragmentByRef(fragment.$ref);
			}

			switch (fragment.type) {
				case 'object':
					return fragment.properties && fragment.properties[part];

				case 'array':
					return fragment.enum && fragment.enum[part];

				default:
					// throw new Error(`Incorrect/unsupported JSONPointer "${pointer}" from "${part}"`);
					return undefined;
			}
		}, this._schema);

		while(fragment && fragment.$ref) {
			fragment = this._getSchemaFragmentByRef(fragment.$ref);
		}

		this._fragmentsCache.set(pointer, fragment);
		return fragment;
	}

	_getSchemaFragmentByRef($ref) {
		let pointer = $ref.substr(1);
		return JSONPointer.getValue(this._schema, pointer);
		// let fragmentAjv = new Ajv();
		// fragmentAjv.addSchema(this._schema);
		// let compiledFragment = fragmentAjv.compile({$ref});
		// let fragment = compiledFragment.refVal[compiledFragment.refs[$ref]];
		// return fragment;
	}
};
