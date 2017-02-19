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
		return fragment && fragment.maximum;
	}

	getMinValue(pointer) {
		const fragment = this._getSchemaFragment(pointer);
		return fragment && fragment.minimum;
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
