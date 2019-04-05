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
            {tests.map((test, index) => {
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
                    <li type="none" key={index}>
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

    getTitle(result) {
        let { vspTestsMap } = this.props.softwareProductValidation;
        let title = vspTestsMap[result.testCaseName]
            ? vspTestsMap[result.testCaseName].title
            : i18n('Unknown');
        return i18n(
            'Scenario: {scenario} | Title: {title} | Test Case: {testCaseName} | Status: {status}',
            {
                scenario: result.scenario || i18n('Unknown'),
                status: result.status || i18n('Unknown'),
                testCaseName: result.testCaseName || i18n('Unknown'),
                title: title
            }
        );
    }

    renderJSON(result) {
        return (
            <li type="none">
                <textarea
                    disabled={true}
                    className="validation-results-test-result-json"
                    value={JSON.stringify(result, null, 2)}
                />
            </li>
        );
    }

    renderError(result) {
        if (Array.isArray(result)) {
            return result.map((parameter, index) => {
                return (
                    <li type="none" key={index}>
                        <SVGIcon
                            color="negative"
                            name="errorCircle"
                            labelPosition="right"
                        />
                        <span className="validation-results-test-result-label">
                            {(parameter.code || '') +
                                ' | ' +
                                (parameter.advice || parameter.message)}
                        </span>
                    </li>
                );
            });
        } else {
            return (
                <li type="none">
                    <SVGIcon
                        color="negative"
                        name="errorCircle"
                        labelPosition="right"
                    />
                    <span className="validation-results-test-result-label">
                        {(result.code || '') +
                            ' | ' +
                            (result.advice || result.message)}
                    </span>
                </li>
            );
        }
    }

    renderResults(result) {
        if (typeof result === 'string' || result instanceof String) {
            return (
                <div>
                    <SVGIcon
                        color="warning"
                        name="errorCircle"
                        labelPosition="right"
                    />
                    <span className="validation-results-test-result-label">
                        {result}
                    </span>
                </div>
            );
        }
        return Object.keys(result).map((key, index) => {
            let title = unCamelCasedString(key);
            if (
                typeof result[key] === 'string' ||
                result[key] instanceof String
            ) {
                return (
                    <Accordion
                        defaultExpanded
                        dataTestId={title}
                        title={title}
                        key={index}>
                        {this.renderString(result[key])}
                    </Accordion>
                );
            } else if (Array.isArray(result[key])) {
                if (result[key].length > 0) {
                    return (
                        <Accordion
                            defaultExpanded
                            dataTestId={title}
                            title={title}
                            key={index}>
                            <TestResultComponent tests={result[key]} />
                        </Accordion>
                    );
                } else {
                    return (
                        <Accordion
                            defaultExpanded
                            dataTestId={title}
                            title={title}
                            key={index}>
                            {i18n('{title} results are not available', {
                                title: title
                            })}
                        </Accordion>
                    );
                }
            } else {
                return (
                    <Accordion
                        defaultExpanded
                        dataTestId={title}
                        title={title}
                        key={index}>
                        {this.renderJSON(result[key])}
                    </Accordion>
                );
            }
        });
    }

    renderString(result) {
        return (
            <li type="none">
                <textarea
                    type="textarea"
                    disabled={true}
                    className="validation-results-test-result-string"
                    value={result}
                />
            </li>
        );
    }

    buildSubAccordions(result) {
        let results = result.results;

        if (!results) {
            return (
                <Accordion
                    defaultExpanded
                    dataTestId="vsp-test-no-results"
                    title={this.getTitle(result)}>
                    {this.renderJSON(result)}
                </Accordion>
            );
        } else if (typeof results === 'string' || results instanceof String) {
            return (
                <Accordion
                    defaultExpanded
                    dataTestId="vsp-test-string-results"
                    title={this.getTitle(result)}>
                    {this.renderString(results)}
                </Accordion>
            );
        } else {
            return (
                <Accordion
                    defaultExpanded
                    dataTestId="vsp-test-object-results"
                    title={this.getTitle(result)}>
                    {Object.keys(results).length === 0
                        ? this.renderString(
                              i18n('{title} results are not available', {
                                  title: 'Test'
                              })
                          )
                        : Object.keys(results).map(key => {
                              if (key === 'errors' || key === 'error') {
                                  return this.renderError(results[key]);
                              } else if (key === 'testResults') {
                                  return this.renderResults(results[key]);
                              } else {
                                  let title = unCamelCasedString(key);
                                  if (results[key] instanceof Object) {
                                      return (
                                          <Accordion
                                              defaultExpanded
                                              dataTestId={title}
                                              title={title}>
                                              {this.renderJSON(results[key])}
                                          </Accordion>
                                      );
                                  } else {
                                      return (
                                          <Accordion
                                              defaultExpanded
                                              dataTestId={title}
                                              title={title}>
                                              {this.renderString(results[key])}
                                          </Accordion>
                                      );
                                  }
                              }
                          })}
                </Accordion>
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
                            title={i18n('Test Results')}>
                            {results.map(row => this.buildSubAccordions(row))}
                        </Accordion>
                    </GridItem>
                </GridSection>
            );
        } else {
            return (
                <GridSection title={i18n('Validation Results')}>
                    <h4>{i18n('No Validation Checks Performed')}</h4>
                </GridSection>
            );
        }
    }
}

export default SoftwareProductValidationResultsView;
