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
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger.js';
import Tooltip from 'react-bootstrap/lib/Tooltip.js';

const tooltip = name => <Tooltip id="tooltip-bottom">{name}</Tooltip>;

const UnassignedFile = props => (
    <OverlayTrigger
        placement="bottom"
        overlay={tooltip(props.name)}
        delayShow={1000}>
        <li
            data-test-id="unassigned-files"
            className="unassigned-files-list-item">
            {props.name}
        </li>
    </OverlayTrigger>
);

export default UnassignedFile;
