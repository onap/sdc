/*!
 * Copyright © 2016-2018 European Support Limited
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
import PropTypes from 'prop-types';
import i18n from 'nfvo-utils/i18n/i18n.js';

const UuidElement = ({ title, value }) => (
    <div className="uuid-container">
        <div className="uuid-title">{title}</div>
        <div className="uuid-value" selectable="true">
            {value}
        </div>
    </div>
);

UuidElement.propTypes = {
    title: PropTypes.string,
    value: PropTypes.string
};

const Uuid = ({ id, versionUUID }) => (
    <div className="uuid-row-wrapper">
        <UuidElement title={i18n('UUID:')} value={versionUUID} />
        <div className="separator" />
        <UuidElement title={i18n('Invariant UUID:')} value={id} />
    </div>
);

export default Uuid;
