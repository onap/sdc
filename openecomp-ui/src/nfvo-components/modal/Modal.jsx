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
import ReactDOM from 'react-dom';
import BootstrapModal from 'react-bootstrap/lib/Modal.js';
import { isEqual } from 'lodash';
let nextModalId = 0;

export default class Modal extends React.Component {
    static Header = BootstrapModal.Header;

    static Title = BootstrapModal.Title;

    static Footer = BootstrapModal.Footer;

    static Body = class ModalBody extends React.Component {
        render() {
            let { children, ...props } = this.props;
            return (
                <BootstrapModal.Body {...props}>{children}</BootstrapModal.Body>
            );
        }

        componentDidMount() {
            let element = ReactDOM.findDOMNode(this);
            element.addEventListener('click', event => {
                if (event.target.tagName === 'A') {
                    event.preventDefault();
                }
            });
            ['wheel', 'mousewheel', 'DOMMouseScroll'].forEach(eventType =>
                element.addEventListener(eventType, event =>
                    event.stopPropagation()
                )
            );
        }

        componentWillUnmount() {
            let element = ReactDOM.findDOMNode(this);

            ['wheel', 'mousewheel', 'DOMMouseScroll', 'click'].forEach(
                eventType => element.removeEventListener(eventType)
            );
        }

        shouldComponentUpdate(nextProps) {
            return !isEqual(this.props, nextProps);
        }
    };

    constructor() {
        super();
        this.modalId = `dox-ui-modal-${nextModalId++}`;
    }

    componentDidUpdate() {
        setTimeout(() => this.ensureRootClass(), 0);
    }

    componentDidMount() {
        setTimeout(() => this.ensureRootClass(), 0);
    }

    ensureRootClass() {
        let element = document.getElementById(this.modalId);
        if (element) {
            element = element.parentElement;
        } else {
            return;
        }
        while (element && !(element.getAttribute('role') === 'dialog')) {
            element = element.parentElement;
        }
        if (element && !element.classList.contains('dox-ui')) {
            element.classList.add('dox-ui');
        }
        if (element && !element.getAttribute('data-onboardingroot')) {
            element.setAttribute('data-onboardingroot', '');
        }
    }

    render() {
        let { children, ...props } = this.props;
        return (
            <BootstrapModal {...props} id={this.modalId}>
                {children}
            </BootstrapModal>
        );
    }
}
