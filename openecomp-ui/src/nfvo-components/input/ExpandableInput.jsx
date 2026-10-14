/*
 * Copyright © 2016-2018 European Support Limited
 * Modifications Copyright (C) 2026 Deutsche Telekom AG
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
import ReactDOM from 'react-dom';
import { SVGIcon } from 'onap-ui-react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Input from 'nfvo-components/input/validation/InputWrapper.jsx';

const ExpandableInputClosed = ({ iconType, onClick, buttonRef }) => (
    <button
        type="button"
        className="expandable-input-wrapper closed"
        data-test-id="expandable-input-closed"
        aria-label={i18n('Search')}
        ref={buttonRef}
        onClick={onClick}>
        <SVGIcon name={iconType} />
    </button>
);

class ExpandableInputOpened extends React.Component {
    componentDidMount() {
        this.rawDomNode = ReactDOM.findDOMNode(
            this.searchInputNode.inputWrapper
        );
        this.rawDomNode.focus();
    }

    componentDidUpdate() {
        if (!this.props.value) {
            if (!(document.activeElement === this.rawDomNode)) {
                this.props.handleBlur();
            }
        }
    }

    handleClose() {
        this.props.onChange('');
        this.rawDomNode.focus();
    }

    handleKeyDown(e) {
        if (e.key === 'Escape') {
            e.preventDefault();
            if (this.props.value) {
                this.handleClose();
            } else {
                this.props.onCollapse();
            }
        }
    }

    render() {
        let { iconType, value, onChange, handleBlur } = this.props;
        return (
            <div className="expandable-input-wrapper opened" key="expandable">
                <Input
                    type="text"
                    data-test-id="expandable-input-opened"
                    aria-label={i18n('Search')}
                    value={value}
                    ref={input => (this.searchInputNode = input)}
                    className="expandable-active"
                    groupClassName="expandable-input-control"
                    onChange={e => onChange(e)}
                    onKeyDown={e => this.handleKeyDown(e)}
                    onBlur={handleBlur}
                />
                {value && (
                    <button
                        type="button"
                        className="expandable-input-close-btn"
                        data-test-id="expandable-input-close-btn"
                        aria-label={i18n('Clear search')}
                        onClick={() => this.handleClose()}>
                        <SVGIcon name="close" />
                    </button>
                )}
                {!value && <SVGIcon name={iconType} aria-hidden="true" />}
            </div>
        );
    }
}

class ExpandableInput extends React.Component {
    static propTypes = {
        iconType: PropTypes.string,
        onChange: PropTypes.func,
        value: PropTypes.string
    };

    constructor(props) {
        super(props);
        this.state = {
            showInput: !!props.value || false
        };
    }

    showInputChange() {
        if (this.props.value) {
            this.setState({ showInput: true });
        }
    }

    collapse() {
        this.focusOpener = true;
        this.setState({ showInput: false });
    }

    componentDidUpdate() {
        if (this.focusOpener && this.openerButton) {
            this.focusOpener = false;
            this.openerButton.focus();
        }
    }

    getValue() {
        return this.props.value;
    }

    render() {
        let { iconType, value, onChange = false } = this.props;
        return (
            <div className="expandable-input-top">
                {this.state.showInput && (
                    <ExpandableInputOpened
                        key="open"
                        iconType={iconType}
                        onChange={onChange}
                        value={value}
                        handleKeyDown={e => this.handleKeyDown(e)}
                        handleBlur={() => this.showInputChange()}
                        onCollapse={() => this.collapse()}
                    />
                )}
                {!this.state.showInput && (
                    <ExpandableInputClosed
                        key="closed"
                        iconType={iconType}
                        buttonRef={button => (this.openerButton = button)}
                        onClick={() => this.setState({ showInput: true })}
                    />
                )}
            </div>
        );
    }
}

export default ExpandableInput;
