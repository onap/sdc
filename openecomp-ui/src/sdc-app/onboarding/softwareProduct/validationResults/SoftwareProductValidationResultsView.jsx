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
import { Accordion } from 'onap-ui-react';
import { SVGIcon } from 'onap-ui-react';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import i18n from 'nfvo-utils/i18n/i18n.js';

class SoftwareProductValidationResultsView extends React.Component {
    static propTypes = {
        softwareProductValidation: PropTypes.object,
        refreshValidationResults: PropTypes.func
    };

    constructor(props) {
        super(props);
        this.state = {
            vspId: this.props.softwareProductId,
            versionNumber: this.props.version.name,
            refreshValidationResults: this.props.refreshValidationResults,
            vspTestResults: this.props.vspTestResults,
            flatTestsMap: {},
            generalInfo: {}
        };
    }
    componentDidMount() {
        let { softwareProductValidationResult } = this.props;
        if (
            softwareProductValidationResult.vspChecks !== undefined &&
            softwareProductValidationResult.vspChecks.children !== undefined
        ) {
            this.prepareDataForCheckboxesUpdated(
                this.props.softwareProductValidationResult.vspChecks.children
            );
        }
    }
    prepareDataForCheckboxesUpdated(children) {
        for (var val of children) {
            if (val.children) {
                this.prepareDataForCheckboxesUpdated(val.children);
            } else if (val.tests) {
                for (var test of val.tests) {
                    var ftm = this.state.flatTestsMap;
                    ftm[test.testCaseName] = test.description;
                    this.setState({
                        flatTestsMap: ftm
                    });
                }
            }
        }
    }

    getTitle(result) {
        let { flatTestsMap: vspTestsMap } = this.state;
        let title = vspTestsMap[result.testCaseName]
            ? vspTestsMap[result.testCaseName].split(/\r?\n/)[0]
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

    renderJSON(result, indexKey) {
        if (result.status === 'in-progress') {
            return this.renderInprogress('Test is In-progress', indexKey);
        } else {
            return (
                <li key={indexKey} type="none">
                    <textarea
                        disabled={true}
                        className="validation-results-test-result-json"
                        value={JSON.stringify(result, null, 2)}
                    />
                </li>
            );
        }
    }
    renderInprogress(result, indexKey) {
        return (
            <li key={indexKey} type="none">
                <SVGIcon
                    color="warning"
                    name="exclamationTriangleLine"
                    labelPosition="right"
                />
                <span className="validation-results-test-result-label">
                    {result}
                </span>
            </li>
        );
    }
    renderError(result, indexKey) {
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
        } else if (
            typeof result === 'string' ||
            result.hasOwnProperty('code') ||
            result.hasOwnProperty('advice') ||
            result.hasOwnProperty('message') ||
            result.hasOwnProperty('error')
        ) {
            return (
                <li key={indexKey} type="none">
                    <SVGIcon
                        color="negative"
                        name="errorCircle"
                        labelPosition="right"
                    />
                    <span className="validation-results-test-result-label">
                        {typeof result === 'string'
                            ? result
                            : (result.code || '') +
                              ' | ' +
                              (result.advice || result.message || result.error)}
                    </span>
                </li>
            );
        } else {
            return (
                <Accordion key={indexKey} defaultExpanded>
                    {this.renderJSON(result)}
                </Accordion>
            );
        }
    }

    renderResults(result, indexKey) {
        return (
            <li key={indexKey} type="none">
                <SVGIcon
                    color="positive"
                    name="checkCircle"
                    labelPosition="right"
                />
                <span className="validation-results-test-result-label">
                    {result}
                </span>
            </li>
        );
    }

    renderString(result, indexKey) {
        return (
            <li key={indexKey} type="none">
                <textarea
                    type="textarea"
                    disabled={true}
                    className="validation-results-test-result-string"
                    value={result}
                />
            </li>
        );
    }

    buildSubAccordions(result, indexKey) {
        let results = result.results;

        if (!results) {
            return (
                <Accordion
                    key={indexKey}
                    defaultExpanded
                    dataTestId="vsp-test-no-results"
                    title={this.getTitle(result)}>
                    {this.renderJSON(result, indexKey)}
                </Accordion>
            );
        } else if (typeof results === 'string' || results instanceof String) {
            return (
                <Accordion
                    key={indexKey}
                    defaultExpanded
                    dataTestId="vsp-test-string-results"
                    title={this.getTitle(result)}>
                    {this.renderString(results, indexKey)}
                </Accordion>
            );
        } else {
            return (
                <Accordion
                    key={indexKey}
                    defaultExpanded
                    dataTestId="vsp-test-object-results"
                    title={this.getTitle(result)}>
                    {Object.keys(results).length === 0
                        ? this.renderString(
                              i18n(
                                  '{title} results are not available',
                                  {
                                      title: 'Test'
                                  },
                                  indexKey
                              )
                          )
                        : Array.isArray(results)
                          ? Object.keys(results).map((key, indexKey) => {
                                if (Object.keys(results[key]).length === 0) {
                                    return this.renderResults(
                                        result.testCaseName +
                                            ' has passed all checks',
                                        indexKey
                                    );
                                } else {
                                    return this.renderError(
                                        results[key],
                                        indexKey
                                    );
                                }
                            })
                          : this.renderError(results, indexKey)}
                </Accordion>
            );
        }
    }
    refreshValidationResult(thisObj) {
        let { refreshValidationResults } = thisObj.props;
        refreshValidationResults(
            this.props.softwareProductId,
            this.props.version.id
        );
    }
    render() {
        let results =
            this.props.softwareProductValidationResult.vspTestResults || [];
        if (results.length > 0) {
            return (
                <div>
                    <div
                        onClick={() => this.refreshValidationResult(this)}
                        data-test-id="vsp-validation-refresh-btn"
                        className={'vcp-validation-refresh-btn'}>
                        <SVGIcon
                            label="Refresh"
                            labelPosition="left"
                            color=""
                            iconClassName="vcp-validation-refresh-icon"
                            name="versionControllerSync"
                        />
                    </div>
                    <GridSection title={i18n('Test Results')}>
                        <GridItem colSpan={10}>
                            <Accordion
                                defaultExpanded
                                dataTestId="vsp-validation-test-result"
                                title={i18n('Test Results')}>
                                {results.map((row, index) =>
                                    this.buildSubAccordions(row, index)
                                )}
                            </Accordion>
                        </GridItem>
                    </GridSection>
                </div>
            );
        } else {
            return (
                <GridSection title={i18n('Test Results')}>
                    <h4>{i18n('No Test Performed')}</h4>
                </GridSection>
            );
        }
    }
}

export default SoftwareProductValidationResultsView;
