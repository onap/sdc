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
import ValidationButtons from './ValidationButtons.jsx';

class Form extends React.Component {
    static defaultProps = {
        hasButtons: true,
        onSubmit: null,
        onReset: null,
        labledButtons: true,
        onValidChange: null,
        isValid: true,
        submitButtonText: null,
        cancelButtonText: null
    };

    static propTypes = {
        isValid: PropTypes.bool,
        formReady: PropTypes.bool,
        isReadOnlyMode: PropTypes.bool,
        hasButtons: PropTypes.bool,
        onSubmit: PropTypes.func,
        onReset: PropTypes.func,
        labledButtons: PropTypes.bool,
        submitButtonText: PropTypes.string,
        cancelButtonText: PropTypes.string,
        onValidChange: PropTypes.func,
        onValidityChanged: PropTypes.func,
        onValidateForm: PropTypes.func,
        btnClassName: PropTypes.string
    };

    constructor(props) {
        super(props);
    }
    render() {
        /* eslint-disable no-unused-vars */
        let {
            isValid,
            onValidChange,
            onValidityChanged,
            onDataChanged,
            formReady,
            onValidateForm,
            isReadOnlyMode,
            hasButtons,
            onSubmit,
            labledButtons,
            submitButtonText,
            cancelButtonText,
            children,
            btnClassName,
            ...formProps
        } = this.props;
        /* eslint-enable no-unused-vars */
        return (
            <form
                {...formProps}
                ref={this.setFormRef}
                onSubmit={this.handleFormValidation}>
                <div className="validation-form-content">
                    <fieldset disabled={isReadOnlyMode}>{children}</fieldset>
                </div>
                {hasButtons && (
                    <ValidationButtons
                        labledButtons={labledButtons}
                        submitButtonText={submitButtonText}
                        cancelButtonText={cancelButtonText}
                        ref={this.setButtonsRef}
                        isReadOnlyMode={isReadOnlyMode}
                        className={btnClassName}
                    />
                )}
            </form>
        );
    }

    handleFormValidation = event => {
        event.preventDefault();
        if (this.props.onValidateForm && !this.props.formReady) {
            return this.props.onValidateForm();
        } else {
            return this.handleFormSubmit(event);
        }
    };

    setButtonsRef = buttons => (this.buttons = buttons);

    setFormRef = form => (this.form = form);

    handleFormSubmit(event) {
        if (event) {
            event.preventDefault();
        }
        if (this.props.onSubmit) {
            return this.props.onSubmit(event);
        }
    }

    componentDidMount() {
        if (this.props.hasButtons) {
            this.buttons.setState({ isValid: this.props.isValid });
        }
    }

    componentDidUpdate(prevProps) {
        // only handling this programatically if the validation of the form is done outside of the view
        // (example with a form that is dependent on the state of other forms)
        if (prevProps.isValid !== this.props.isValid) {
            if (this.props.hasButtons) {
                this.buttons.setState({ isValid: this.props.isValid });
            }
            // callback in case form is part of bigger picture in view
            if (this.props.onValidChange) {
                this.props.onValidChange(this.props.isValid);
            }

            // TODO - maybe this has to be part of componentWillUpdate
            if (this.props.onValidityChanged) {
                this.props.onValidityChanged(this.props.isValid);
            }
        }
        if (
            this.props.formReady &&
            this.props.formReady !== prevProps.formReady
        ) {
            // if form validation succeeded -> continue with submit
            this.handleFormSubmit();
        }
    }
}

export class TabsForm extends Form {
    render() {
        /* eslint-disable no-unused-vars */
        let {
            submitButtonText,
            cancelButtonText,
            isValid,
            formReady,
            onValidateForm,
            isReadOnlyMode,
            hasButtons,
            onSubmit,
            labledButtons,
            onValidChange,
            onValidityChanged,
            onDataChanged,
            btnClassName,
            children,
            ...formProps
        } = this.props;
        /* eslint-enable no-unused-vars */
        return (
            <form
                {...formProps}
                ref={form => (this.form = form)}
                onSubmit={event => this.handleFormValidation(event)}>
                <div className="validation-form-content">{children}</div>
                {hasButtons && (
                    <ValidationButtons
                        labledButtons={labledButtons}
                        submitButtonText={submitButtonText}
                        cancelButtonText={cancelButtonText}
                        ref={buttons => (this.buttons = buttons)}
                        isReadOnlyMode={isReadOnlyMode}
                        className={btnClassName}
                    />
                )}
            </form>
        );
    }
}

export default Form;
