/**
 * Copyright (c) 2019 Vodafone Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import React, { Component } from 'react';
import PropTypes from 'prop-types';

import i18n from 'nfvo-utils/i18n/i18n.js';
import Button from 'sdc-ui/lib/react/Button.js';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import Input from 'nfvo-components/input/validation/Input.jsx';
import Form from 'nfvo-components/input/validation/Form.jsx';

class VspInputs extends React.Component {
    constructor(props) {
        super(props);
        this.state = {};
    }

    componentWillMount() {}

    shouldComponentUpdate() {
        return true;
    }

    componentDidMount() {}

    changeTextInputs(inpText, check, parameterName) {
        let { testsRequest, generalInfo, setTestsRequest } = this.props;
        testsRequest[check].parameters[parameterName] = inpText;
        generalInfo[check][parameterName] = { isValid: true, errorText: '' };
        setTestsRequest(testsRequest, generalInfo);
    }

    changeSelectInputs(e, check, parameterName) {
        let { testsRequest, generalInfo, setTestsRequest } = this.props;
        testsRequest[check].parameters[parameterName] = e.target.value;
        generalInfo[check][parameterName] = { isValid: true, errorText: '' };
        setTestsRequest(testsRequest, generalInfo);
    }

    render() {
        let {
            complianceChecked,
            vspFlatTestsMap,
            certificationChecked,
            testsRequest,
            generalInfo
        } = this.props;
        return (
            <div>
                {complianceChecked.map(complianceCheck => {
                    if (
                        vspFlatTestsMap[complianceCheck].parameters.length === 0
                    ) {
                        return <div />;
                    } else {
                        return (
                            <div className="div-clear-both">
                                <GridSection
                                    title={
                                        vspFlatTestsMap[complianceCheck].title +
                                        ' Inputs :'
                                    }>
                                    {vspFlatTestsMap[
                                        complianceCheck
                                    ].parameters.map(parameter => {
                                        if (
                                            parameter.type === 'text' &&
                                            parameter.metadata.choices
                                        ) {
                                            return (
                                                <GridItem>
                                                    <Input
                                                        data-test-id={
                                                            complianceCheck +
                                                            '_' +
                                                            parameter.name +
                                                            '_input'
                                                        }
                                                        isRequired={
                                                            !parameter.isOptional
                                                        }
                                                        label={
                                                            parameter.description
                                                        }
                                                        isValid={
                                                            generalInfo[
                                                                complianceCheck
                                                            ][parameter.name]
                                                                .isValid
                                                        }
                                                        errorText={
                                                            generalInfo[
                                                                complianceCheck
                                                            ][parameter.name]
                                                                .errorText
                                                        }
                                                        type="select"
                                                        defaultValue={
                                                            testsRequest[
                                                                complianceCheck
                                                            ].parameters[
                                                                parameter.name
                                                            ] || ''
                                                        }
                                                        onChange={e =>
                                                            this.changeSelectInputs(
                                                                e,
                                                                complianceCheck,
                                                                parameter.name
                                                            )
                                                        }
                                                        disabled={
                                                            parameter.metadata
                                                                .disabled ||
                                                            false
                                                        }>
                                                        <option
                                                            key="placeholder"
                                                            value="">
                                                            {i18n('Select...')}
                                                        </option>
                                                        {parameter.metadata.choices.map(
                                                            selectOption => (
                                                                <option
                                                                    key={
                                                                        selectOption.key
                                                                    }
                                                                    value={
                                                                        selectOption.key
                                                                    }>
                                                                    {
                                                                        selectOption.label
                                                                    }
                                                                </option>
                                                            )
                                                        )}
                                                    </Input>
                                                </GridItem>
                                            );
                                        } else if (
                                            parameter.type === 'text' &&
                                            !parameter.metadata.hidden
                                        ) {
                                            return (
                                                <GridItem>
                                                    <Input
                                                        data-test-id={
                                                            complianceCheck +
                                                            '_' +
                                                            parameter.name +
                                                            '_input'
                                                        }
                                                        isRequired={
                                                            !parameter.isOptional
                                                        }
                                                        label={
                                                            parameter.description
                                                        }
                                                        placeholder={
                                                            parameter.metadata
                                                                .placeholder ||
                                                            ''
                                                        }
                                                        type="text"
                                                        isValid={
                                                            generalInfo[
                                                                complianceCheck
                                                            ][parameter.name]
                                                                .isValid
                                                        }
                                                        errorText={
                                                            generalInfo[
                                                                complianceCheck
                                                            ][parameter.name]
                                                                .errorText
                                                        }
                                                        value={
                                                            testsRequest[
                                                                complianceCheck
                                                            ].parameters[
                                                                parameter.name
                                                            ] || ''
                                                        }
                                                        disabled={
                                                            parameter.metadata
                                                                .disabled ||
                                                            false
                                                        }
                                                        onChange={e =>
                                                            this.changeTextInputs(
                                                                e,
                                                                complianceCheck,
                                                                parameter.name
                                                            )
                                                        }
                                                    />
                                                </GridItem>
                                            );
                                        }
                                    })}
                                </GridSection>
                            </div>
                        );
                    }
                })}
                {certificationChecked.map(certificateCheck => {
                    if (
                        vspFlatTestsMap[certificateCheck].parameters.length ===
                        0
                    ) {
                        return <div />;
                    } else {
                        return (
                            <div className="div-clear-both">
                                <GridSection
                                    title={
                                        vspFlatTestsMap[certificateCheck]
                                            .title + ' Inputs :'
                                    }>
                                    {vspFlatTestsMap[
                                        certificateCheck
                                    ].parameters.map(parameter => {
                                        if (
                                            parameter.type === 'text' &&
                                            parameter.metadata.choices
                                        ) {
                                            return (
                                                <GridItem>
                                                    <Input
                                                        data-test-id={
                                                            certificateCheck +
                                                            '_' +
                                                            parameter.name +
                                                            '_input'
                                                        }
                                                        isRequired={
                                                            !parameter.isOptional
                                                        }
                                                        label={
                                                            parameter.description
                                                        }
                                                        type="select"
                                                        isValid={
                                                            generalInfo[
                                                                certificateCheck
                                                            ][parameter.name]
                                                                .isValid
                                                        }
                                                        errorText={
                                                            generalInfo[
                                                                certificateCheck
                                                            ][parameter.name]
                                                                .errorText
                                                        }
                                                        defaultValue={
                                                            testsRequest[
                                                                certificateCheck
                                                            ].parameters[
                                                                parameter.name
                                                            ] || ''
                                                        }
                                                        onChange={e =>
                                                            this.changeSelectInputs(
                                                                e,
                                                                certificateCheck,
                                                                parameter.name
                                                            )
                                                        }
                                                        disabled={
                                                            parameter.metadata
                                                                .disabled ||
                                                            false
                                                        }>
                                                        <option
                                                            key="placeholder"
                                                            value="">
                                                            {i18n('Select...')}
                                                        </option>
                                                        {parameter.metadata.choices.map(
                                                            selectOption => (
                                                                <option
                                                                    key={
                                                                        selectOption.key
                                                                    }
                                                                    value={
                                                                        selectOption.key
                                                                    }>
                                                                    {
                                                                        selectOption.label
                                                                    }
                                                                </option>
                                                            )
                                                        )}
                                                    </Input>
                                                </GridItem>
                                            );
                                        } else if (
                                            parameter.type === 'text' &&
                                            !parameter.metadata.hidden
                                        ) {
                                            return (
                                                <GridItem>
                                                    <Input
                                                        data-test-id={
                                                            certificateCheck +
                                                            '_' +
                                                            parameter.name +
                                                            '_input'
                                                        }
                                                        isRequired={
                                                            !parameter.isOptional
                                                        }
                                                        label={
                                                            parameter.description
                                                        }
                                                        placeholder={
                                                            parameter.metadata
                                                                .placeholder ||
                                                            ''
                                                        }
                                                        type="text"
                                                        isValid={
                                                            generalInfo[
                                                                certificateCheck
                                                            ][parameter.name]
                                                                .isValid
                                                        }
                                                        errorText={
                                                            generalInfo[
                                                                certificateCheck
                                                            ][parameter.name]
                                                                .errorText
                                                        }
                                                        value={
                                                            testsRequest[
                                                                certificateCheck
                                                            ].parameters[
                                                                parameter.name
                                                            ] || ''
                                                        }
                                                        disabled={
                                                            parameter.metadata
                                                                .disabled ||
                                                            false
                                                        }
                                                        onChange={e =>
                                                            this.changeTextInputs(
                                                                e,
                                                                certificateCheck,
                                                                parameter.name
                                                            )
                                                        }
                                                    />
                                                </GridItem>
                                            );
                                        }
                                    })}
                                </GridSection>
                            </div>
                        );
                    }
                })}
            </div>
        );
    }
}

class VspValidationInputs extends Component {
    static propTypes = {
        softwareProductValidation: PropTypes.object
    };

    constructor(props) {
        super(props);
        this.state = {};
    }

    componentWillMount() {}

    shouldComponentUpdate() {
        return true;
    }

    componentDidMount() {}

    validateInputs() {
        let areInputsValid = true;
        let { softwareProductValidation, setGeneralInfo } = this.props;
        let generalInfo = softwareProductValidation.generalInfo;
        Object.keys(softwareProductValidation.testsRequest).forEach(
            testCaseName => {
                let requestParameters =
                    softwareProductValidation.testsRequest[testCaseName]
                        .parameters;
                let validationParameters =
                    softwareProductValidation.vspTestsMap[testCaseName]
                        .parameters;
                Object.keys(requestParameters).forEach(parameterName => {
                    let parameter = validationParameters.find(
                        o => o.name === parameterName
                    );
                    let isParameterValid = true;
                    let errorText = '';
                    if (
                        parameter.type === 'text' &&
                        parameter.metadata.choices
                    ) {
                        if (
                            !parameter.isOptional &&
                            !requestParameters[parameterName]
                        ) {
                            isParameterValid = false;
                            errorText =
                                'Please Enter a Value in the Mandatory Field';
                        }
                    } else if (parameter.type === 'text') {
                        if (
                            !parameter.isOptional &&
                            !requestParameters[parameterName]
                        ) {
                            isParameterValid = false;
                            errorText =
                                'Please Enter a Value in the Mandatory Field';
                        } else if (
                            (!parameter.isOptional &&
                                !requestParameters[parameterName]) ||
                            (parameter.metadata.maxLength &&
                                requestParameters[parameterName].length >
                                    parseInt(parameter.metadata.maxLength)) ||
                            (parameter.metadata.minLength &&
                                requestParameters[parameterName].length <
                                    parseInt(parameter.metadata.minLength) &&
                                requestParameters[parameterName].length > 0)
                        ) {
                            isParameterValid = false;
                            errorText =
                                'Value Should Be Minimum of ' +
                                parameter.metadata.minLength +
                                ' characters and a maximum of ' +
                                parameter.metadata.maxLength +
                                ' characters';
                        }
                    }
                    generalInfo[testCaseName][
                        parameterName
                    ].isValid = isParameterValid;
                    generalInfo[testCaseName][
                        parameterName
                    ].errorText = errorText;
                    areInputsValid = areInputsValid && isParameterValid;
                });
            }
        );
        if (!areInputsValid) {
            setGeneralInfo(generalInfo);
        }
        return areInputsValid;
    }

    performVSPTests() {
        let tests = [];
        let {
            version,
            onTestSubmit,
            status,
            softwareProductId,
            softwareProductValidation
        } = this.props;

        Object.keys(softwareProductValidation.testsRequest).forEach(key => {
            tests.push(softwareProductValidation.testsRequest[key]);
        });
        if (this.validateInputs()) {
            onTestSubmit(softwareProductId, version, status, tests);
        }
    }

    render() {
        let { softwareProductValidation, setTestsRequest } = this.props;
        return (
            <div className="vsp-validation-view">
                <Form
                    hasButtons={false}
                    formReady={null}
                    isValid={true}
                    onSubmit={() => this.performVSPTests()}
                    isReadOnlyMode={false}>
                    <VspInputs
                        complianceChecked={
                            softwareProductValidation.complianceChecked || []
                        }
                        certificationChecked={
                            softwareProductValidation.certificationChecked || []
                        }
                        vspFlatTestsMap={softwareProductValidation.vspTestsMap}
                        testsRequest={softwareProductValidation.testsRequest}
                        setTestsRequest={setTestsRequest}
                        generalInfo={softwareProductValidation.generalInfo}
                    />
                    <Button
                        size="default"
                        data-test-id="proceed-to-validation-results-btn"
                        disabled={false}
                        className="proceed-to-validation-monitor-btn"
                        onClick={() => this.performVSPTests()}>
                        {i18n('Submit')}
                    </Button>
                </Form>
            </div>
        );
    }
}

export default VspValidationInputs;
