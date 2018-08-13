/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
import React from 'react';
import PropTypes from 'prop-types';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Input from 'nfvo-components/input/validation/Input.jsx';
import Form from 'nfvo-components/input/validation/Form.jsx';

const VersionPropType = PropTypes.shape({
    name: PropTypes.string,
    description: PropTypes.string,
    creationMethod: PropTypes.string
});

class VersionsPageCreationView extends React.Component {
    static propTypes = {
        data: VersionPropType,
        availableMethods: PropTypes.array,
        onDataChanged: PropTypes.func.isRequired,
        onSubmit: PropTypes.func.isRequired,
        onCancel: PropTypes.func.isRequired
    };

    render() {
        let {
            data = {},
            genericFieldInfo,
            baseVersion,
            onDataChanged,
            onCancel
        } = this.props;
        let { additionalInfo: { OptionalCreationMethods } } = baseVersion;
        let { description, creationMethod } = data;

        return (
            <div className="version-creation-page">
                {genericFieldInfo && (
                    <Form
                        ref={validationForm =>
                            (this.validationForm = validationForm)
                        }
                        hasButtons={true}
                        btnClassName="sdc-modal__footer"
                        onSubmit={() => this.submit()}
                        submitButtonText={i18n('Create')}
                        onReset={() => onCancel()}
                        labledButtons={true}
                        isValid={this.props.isFormValid}
                        formReady={this.props.formReady}
                        onValidateForm={() => this.validate()}>
                        <div className="version-form-row">
                            <Input
                                label={i18n('Version Category')}
                                value={creationMethod}
                                onChange={e => this.onSelectMethod(e)}
                                type="select"
                                overlayPos="bottom"
                                data-test-id="new-version-category"
                                isValid={
                                    genericFieldInfo.creationMethod.isValid
                                }
                                errorText={
                                    genericFieldInfo.creationMethod.errorText
                                }
                                isRequired>
                                <option key="" value="">
                                    {i18n('Please select…')}
                                </option>
                                {OptionalCreationMethods.map(method => (
                                    <option key={method} value={method}>
                                        {i18n(method)}
                                    </option>
                                ))}
                            </Input>
                        </div>

                        <div className="version-form-row">
                            <Input
                                label={i18n('Description')}
                                value={description}
                                type="text"
                                overlayPos="bottom"
                                data-test-id="new-version-description"
                                isValid={genericFieldInfo.description.isValid}
                                errorText={
                                    genericFieldInfo.description.errorText
                                }
                                onChange={description =>
                                    onDataChanged({ description })
                                }
                                isRequired
                                groupClassName="no-bottom-margin"
                            />
                        </div>
                    </Form>
                )}
            </div>
        );
    }

    onSelectMethod(e) {
        const selectedIndex = e.target.selectedIndex;
        const creationMethod = e.target.options[selectedIndex].value;
        this.props.onDataChanged({ creationMethod });
    }

    submit() {
        let { baseVersion, data: { description, creationMethod } } = this.props;
        this.props.onSubmit({
            baseVersion,
            payload: { description, creationMethod }
        });
    }

    validate() {
        this.props.onValidateForm();
    }
}

export default VersionsPageCreationView;
