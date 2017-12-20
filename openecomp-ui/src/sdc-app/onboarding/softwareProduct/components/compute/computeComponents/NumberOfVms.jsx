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


const NumberOfVms = ({qgenericFieldInfo, dataMap, onQDataChanged, qValidateData, customValidations}) => {
	return(
		<GridSection titleClassName='software-product-compute-number-of-vms' title={i18n('NUMBER OF VMs')}>
			<GridItem>
				<Input
					data-test-id='numOfVMs-minimum'
					type='number'
					label={i18n('Minimum')}
					onChange={(tools) => { onQDataChanged({'compute/numOfVMs/minimum' : tools}, customValidations);
						qValidateData({'compute/numOfVMs/maximum' : dataMap['compute/numOfVMs/maximum']}, customValidations); } }
					isValid={qgenericFieldInfo['compute/numOfVMs/minimum'].isValid}
					errorText={qgenericFieldInfo['compute/numOfVMs/minimum'].errorText}
					value={dataMap['compute/numOfVMs/minimum']} />
			</GridItem>
			<GridItem>
				<Input
					data-test-id='numOfVMs-maximum'
					type='number'
					label={i18n('Maximum')}
					onChange={(tools) => { onQDataChanged({'compute/numOfVMs/maximum' : tools}, customValidations);
						qValidateData({'compute/numOfVMs/minimum' : dataMap['compute/numOfVMs/minimum']}, customValidations); } }
					isValid={qgenericFieldInfo['compute/numOfVMs/maximum'].isValid}
					errorText={qgenericFieldInfo['compute/numOfVMs/maximum'].errorText}
					value={dataMap['compute/numOfVMs/maximum']} />
			</GridItem>
		</GridSection>
	);
};

NumberOfVms.propTypes = {
	minNumberOfVMsSelectedByUser: PropTypes.number
};

export default NumberOfVms;
