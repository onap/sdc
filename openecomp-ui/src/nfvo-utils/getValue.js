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
import {other as optionInputOther} from 'nfvo-components/input/validation/InputOptions.jsx';

function getValueFromObject(element) {
	return element.choices && element.choices.length > 0 && element.choices[0] !== '' && element.choices[0] !== optionInputOther.OTHER ||
			element.other && element.choices[0] === optionInputOther.OTHER ?
			element : undefined;
}

function getValueFromVariable(variable) {
	return variable ? variable : undefined;
}

function getArrayData(variable) {
	return variable.length ? variable : undefined;
}

 let getValue = element => {
	return typeof element === 'object' ?
		element instanceof Array ? getArrayData(element) : getValueFromObject(element) :
		getValueFromVariable(element);
 };

export function getStrValue(choiceObject) {
	if (!choiceObject) {
		return undefined;
	}
	if (choiceObject.choice && choiceObject.choice !== '' && choiceObject.choice !== optionInputOther.OTHER) {
		return choiceObject.choice;
	}
	else if (choiceObject.other && choiceObject.choice === optionInputOther.OTHER) {
		return choiceObject.other;
	}
}

 export default getValue;
