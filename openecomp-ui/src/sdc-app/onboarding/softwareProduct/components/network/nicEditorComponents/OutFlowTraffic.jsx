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
import i18n from 'nfvo-utils/i18n/i18n.js';
import PacketsBytes from './PacketsBytes.jsx';

const pointers = [
		{label: 'Peak', value: 'sizing/outflowTrafficPerSecond/packets/peak'},
		{label: 'Avg', value: 'sizing/outflowTrafficPerSecond/packets/avg'},
		{label: 'Peak', value: 'sizing/outflowTrafficPerSecond/bytes/peak'},
		{label: 'Avg', value: 'sizing/outflowTrafficPerSecond/bytes/avg'},
];

const OutFlowTraffic = (props) => {
	return(
		<PacketsBytes {...props} title={i18n('Outflow Traffic per second')} pointers={pointers}/>
	);
};

export default OutFlowTraffic;


