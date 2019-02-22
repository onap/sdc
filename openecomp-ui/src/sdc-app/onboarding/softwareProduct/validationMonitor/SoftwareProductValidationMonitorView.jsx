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
import Checklist from 'sdc-ui/lib/react/Checklist.js';

import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import i18n from 'nfvo-utils/i18n/i18n.js';
import unCamelCasedString from 'nfvo-utils/unCamelCaseString.js';

class SoftwareProductValidationMonitorView extends React.Component {
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

    loadTests(tests) {
        let items = [];
        tests.forEach(test => {
            items.push({
                label:
                    test.testName +
                    ' | ' +
                    test.testResult +
                    ' | ' +
                    test.notes,
                value: test.testName,
                dataTestId: test.testName,
                disabled: true
            });
        });

        return items;
    }

    buildSubAccordions(result) {
        if (result.status.toLowerCase() === 'success') {
            return (
                <Accordion
                    dataTestId="vsp-validation-test-result-success"
                    title={result.test.title + ' | Status: ' + result.status}>
                    {Object.keys(result.details.testResults).map(key => {
                        let title = unCamelCasedString(key);
                        if (result.details.testResults[key].length > 0) {
                            return (
                                <Accordion
                                    dataTestId={result.test.title}
                                    title={title}>
                                    <Checklist
                                        items={this.loadTests(
                                            result.details.testResults[key]
                                        )}
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
        }
        return (
            <div title={result.test.title}>
                {result.test.title +
                    ' | ' +
                    result.details.errors[0].reason +
                    ' | ' +
                    result.details.errors[0].advice}
            </div>
        );
    }

    render() {
        let results =
            this.props.softwareProductValidation.vspTestResults === undefined
                ? []
                : this.props.softwareProductValidation.vspTestResults.results;
        if (results !== null && results !== undefined && results.length > 0) {
            return (
                <GridSection
                    title={i18n('Validation Monitor')}
                    className="grid-section-general">
                    <GridItem colSpan={10}>
                        <Accordion
                            defaultExpanded
                            dataTestId="vsp-validation-test-result"
                            title={`Test ID: ${
                                this.props.softwareProductValidation
                                    .vspTestResults.id
                            } Status: ${
                                this.props.softwareProductValidation
                                    .vspTestResults.status
                            }`}>
                            {results.map(row => this.buildSubAccordions(row))}
                        </Accordion>
                    </GridItem>
                </GridSection>
            );
        } else {
            return (
                <GridSection
                    title={i18n('Validation Monitor')}
                    className="grid-section-general">
                    <h4>No Validation Checks Performed</h4>
                </GridSection>
            );
        }
    }
}

export default SoftwareProductValidationMonitorView;
