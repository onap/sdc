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
import React, { Component } from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Input from 'nfvo-components/input/validation/Input.jsx';
import Form from 'nfvo-components/input/validation/Form.jsx';

class FlowsEditorModalView extends Component {
    render() {
        let {
            onCancel,
            onDataChanged,
            currentFlow,
            genericFieldInfo,
            formReady,
            isFormValid,
            onValidateForm
        } = this.props;
        let { artifactName, description } = currentFlow;
        return (
            <div>
                {genericFieldInfo && (
                    <Form
                        onSubmit={() => this.onSaveClicked()}
                        onReset={onCancel}
                        formReady={formReady}
                        isValid={isFormValid}
                        onValidateForm={() => onValidateForm()}
                        btnClassName="sdc-modal__footer">
                        <Input
                            type="text"
                            name="name"
                            label={i18n('Name')}
                            isValid={genericFieldInfo['artifactName'].isValid}
                            errorText={
                                genericFieldInfo['artifactName'].errorText
                            }
                            isRequired={true}
                            value={artifactName}
                            onChange={artifactName =>
                                onDataChanged({ artifactName })
                            }
                        />
                        <Input
                            type="textarea"
                            name="description"
                            label={i18n('Description')}
                            isValid={genericFieldInfo['description'].isValid}
                            errorText={
                                genericFieldInfo['description'].errorText
                            }
                            isRequired={true}
                            value={description}
                            overlayPos="bottom"
                            onChange={description =>
                                onDataChanged({ description })
                            }
                        />
                    </Form>
                )}
            </div>
        );
    }

    onSaveClicked() {
        let { currentFlow, onSubmit } = this.props;
        if (onSubmit) {
            onSubmit(currentFlow);
        }
    }
}

export default FlowsEditorModalView;
