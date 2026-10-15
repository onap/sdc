/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import React from 'react';
import { SVGIcon } from 'onap-ui-react';
import i18n from 'nfvo-utils/i18n/i18n.js';

function SummaryCountItem({
    name,
    counter,
    onAdd,
    onNavigate,
    isReadOnlyMode
}) {
    return (
        <div className="summary-count-item">
            <button
                type="button"
                className="summary-name-and-count"
                onClick={onNavigate}>
                <span className="item-name">{name}</span>
                <span
                    className="item-count"
                    data-test-id={
                        'vlm-summary-vendor-counter-' +
                        name.toLowerCase().replace(/\s/g, '-')
                    }>
                    ({counter})
                </span>
            </button>
            <SVGIcon
                name="plusCircle"
                disabled={isReadOnlyMode}
                className={isReadOnlyMode ? 'disabled' : ''}
                color="secondary"
                ariaLabel={i18n('Add {name}', { name })}
                onClick={onAdd}
                data-test-id={
                    'vlm-summary-vendor-add-btn-' +
                    name.toLowerCase().replace(/\s/g, '-')
                }
            />
        </div>
    );
}

export default SummaryCountItem;
