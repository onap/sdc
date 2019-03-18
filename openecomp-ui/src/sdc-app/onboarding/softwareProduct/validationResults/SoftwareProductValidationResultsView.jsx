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
import React from 'react';
import PropTypes from 'prop-types';
import Accordion from 'sdc-ui/lib/react/Accordion.js';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import i18n from 'nfvo-utils/i18n/i18n.js';
import unCamelCasedString from 'nfvo-utils/unCamelCaseString.js';

const TestResultComponent = ({ tests }) => {
    return (
        <div>
            {tests.map(test => {
                let name = 'errorCircle';
                let color = 'warning';
                if (
                    test.testResult &&
                    test.testResult.toLowerCase() === 'pass'
                ) {
                    color = 'positive';
                    name = 'checkCircle';
                } else if (
                    test.testResult &&
                    test.testResult.toLowerCase() === 'fail'
                ) {
                    name = 'exclamationTriangleFull';
                }
                return (
                    <li type="none">
                        <SVGIcon
                            color={color}
                            name={name}
                            labelPosition="right"
                        />
                        <span className="validation-results-test-result-label">
                            {test.testName +
                                ' | ' +
                                test.testResult +
                                ' | ' +
                                test.notes}
                        </span>
                    </li>
                );
            })}
        </div>
    );
};

class SoftwareProductValidationResultsView extends React.Component {
    static propTypes = {
        softwareProductValidation: PropTypes.object
    };

    constructor(props) {
        super(props);
        this.state = {
            vspId: this.props.softwareProductId,
            versionNumber: this.props.version.name
        };
    }

    buildSubAccordions(result) {
        if (result.status && result.status.toLowerCase() === 'completed') {
            if (!result.results.testResults) {
                return (
                    <div
                        title={
                            'Scenario: ' +
                            result.scenario +
                            ' | Status: ' +
                            result.status
                        }>
                        <SVGIcon
                            color="negative"
                            name="errorCircle"
                            labelPosition="right"
                        />
                        <span className="validation-results-test-result-label">
                            {result.scenario + ' results are not available'}
                        </span>
                    </div>
                );
            }
            return (
                <Accordion
                    dataTestId="vsp-validation-test-result-success"
                    title={
                        'Scenario: ' +
                        result.scenario +
                        ' | Status: ' +
                        result.status
                    }>
                    {Object.keys(result.results.testResults).map(key => {
                        let title = unCamelCasedString(key);
                        if (result.results.testResults[key].length > 0) {
                            return (
                                <Accordion dataTestId={title} title={title}>
                                    <TestResultComponent
                                        tests={result.results.testResults[key]}
                                    />
                                </Accordion>
                            );
                        } else {
                            return (
                                <div>
                                    {title + ' results are not available'}
                                </div>
                            );
                        }
                    })}
                </Accordion>
            );
        } else if (
            result.status &&
            result.status.toLowerCase() === 'failed' &&
            result.results.errors
        ) {
            return (
                <Accordion
                    dataTestId="vsp-validation-test-result-success"
                    title={
                        'Scenario: ' +
                        result.scenario +
                        ' | Status: ' +
                        result.status
                    }>
                    {result.results.errors.map(element => {
                        return (
                            <li type="none">
                                <SVGIcon
                                    color="negative"
                                    name="errorCircle"
                                    labelPosition="right"
                                />
                                <span className="validation-results-test-result-label">
                                    {element.reason + ' | ' + element.advice}
                                </span>
                            </li>
                        );
                    })}
                </Accordion>
            );
        } else if (result.message || result.httpStatus) {
            return (
                <div>
                    <SVGIcon
                        color="negative"
                        name="errorCircle"
                        labelPosition="right"
                    />
                    <span className="validation-results-test-result-label">
                        {result.message + ' | ' + result.httpStatus}
                    </span>
                </div>
            );
        }
    }

    render() {
        let results = this.props.softwareProductValidation.vspTestResults || [];
        if (results.length > 0) {
            return (
                <GridSection title={i18n('Validation Results')}>
                    <GridItem colSpan={10}>
                        <Accordion
                            defaultExpanded
                            dataTestId="vsp-validation-test-result"
                            title="Test Results">
                            {results.map(row => this.buildSubAccordions(row))}
                        </Accordion>
                    </GridItem>
                </GridSection>
            );
        } else {
            return (
                <GridSection title={i18n('Validation Results')}>
                    <h4>No Validation Checks Performed</h4>
                </GridSection>
            );
        }
    }
}

export default SoftwareProductValidationResultsView;
