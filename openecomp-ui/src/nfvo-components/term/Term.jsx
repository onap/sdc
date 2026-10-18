/*
 * Copyright (C) 2026 Deutsche Telekom AG
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
import Overlay from 'react-bootstrap/lib/Overlay.js';
import Tooltip from 'react-bootstrap/lib/Tooltip.js';
import { GLOSSARY_TERMS, lookup, tooltipText } from './glossaryTerms.js';

const SHOW_DELAY_MS = 300;

/**
 * Same contract as catalog-ui's [sdcTerm]: the child's text and tab order are untouched; only
 * aria-describedby and a hover/focus tooltip are added. Built on Overlay rather than
 * OverlayTrigger, which replaces the child's aria-describedby with the tooltip id while shown.
 */
class Term extends React.Component {
    static propTypes = {
        term: PropTypes.string,
        children: PropTypes.element.isRequired
    };

    state = { show: false };

    componentWillUnmount() {
        this.hide();
    }

    // The target comes from the event rather than a ref: the child may be a function component
    // (onap-ui-react's Button), which cannot hold one.
    chain = (own, theirs) => event => {
        this.target = event.currentTarget;
        own(event);
        if (theirs) {
            theirs(event);
        }
    };

    show = () => {
        clearTimeout(this.timer);
        if (!this.state.show) {
            document.addEventListener('keydown', this.onKeyDown);
            this.setState({ show: true });
        }
    };

    showLater = () => {
        clearTimeout(this.timer);
        this.timer = setTimeout(this.show, SHOW_DELAY_MS);
    };

    hide = () => {
        clearTimeout(this.timer);
        document.removeEventListener('keydown', this.onKeyDown);
        if (this.state.show) {
            this.setState({ show: false });
        }
    };

    onKeyDown = event => {
        if (event.key === 'Escape' || event.key === 'Esc') {
            this.hide();
        }
    };

    render() {
        const { term, children } = this.props;
        const entry = lookup(term);
        const child = React.Children.only(children);
        if (!entry) {
            return child;
        }
        const existing = child.props['aria-describedby'];
        const described = React.cloneElement(child, {
            'aria-describedby': existing
                ? `${existing} ${entry.descriptionId}`
                : entry.descriptionId,
            onMouseEnter: this.chain(this.showLater, child.props.onMouseEnter),
            onMouseLeave: this.chain(this.hide, child.props.onMouseLeave),
            onFocus: this.chain(this.show, child.props.onFocus),
            onBlur: this.chain(this.hide, child.props.onBlur)
        });
        return (
            <React.Fragment>
                {described}
                <Overlay
                    show={this.state.show}
                    placement="bottom"
                    target={() => this.target}>
                    <Tooltip
                        id={`${entry.descriptionId}-tooltip`}
                        aria-hidden="true">
                        {tooltipText(entry)}
                    </Tooltip>
                </Overlay>
            </React.Fragment>
        );
    }
}

export const GlossaryDescriptions = () => (
    <div hidden>
        {GLOSSARY_TERMS.map(def => {
            const entry = lookup(def.aliases[0]);
            return (
                <span key={def.key} id={entry.descriptionId}>
                    {tooltipText(entry)}
                </span>
            );
        })}
    </div>
);

export default Term;
