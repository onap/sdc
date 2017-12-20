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
import React from 'react';
import PropTypes from 'prop-types';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Input from 'nfvo-components/input/validation/Input.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';

const PointerInput = ({label, value, onQDataChanged, qgenericFieldInfo, dataMap, lastColInRow}) => {
	return (
		<GridItem lastColInRow={lastColInRow}>
			<Input
				label={i18n(label)}
				type='number'
				data-test-id={`${value}`}
				isValid={qgenericFieldInfo[value].isValid}
				errorText={qgenericFieldInfo[value].errorText}
				value={dataMap[value]}
				onChange={val => onQDataChanged({[value]: val})} />
		</GridItem>
	);
};

PointerInput.PropTypes = {
	label: PropTypes.string,
	value: PropTypes.string
};

const PacketsBytes = ({title, pointers = [], qgenericFieldInfo, dataMap, onQDataChanged}) => {
	return(
		<GridSection title={title} hasLastColSet>
				<GridItem colSpan={2}>
					<div className='part-title-small packets'>{i18n('Packets')}</div>
				</GridItem>
				<GridItem colSpan={2} lastColInRow>
					<div className='part-title-small bytes'>{i18n('Bytes')}</div>
				</GridItem>
				{pointers.map((pointer, i) => {return (<PointerInput key={i} label={pointer.label} value={pointer.value}
					qgenericFieldInfo={qgenericFieldInfo} onQDataChanged={onQDataChanged} dataMap={dataMap} lastColInRow={i === 3} />);})}
		</GridSection>
	);
};

PacketsBytes.PropTypes = {
	title: PropTypes.string,
	pointers: PropTypes.array,
	onQDataChanged:  PropTypes.function,
	dataMap: PropTypes.object,
	qgenericFieldInfo: PropTypes.object
};

export default PacketsBytes;
