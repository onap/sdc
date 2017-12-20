/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
import Tooltip from 'react-bootstrap/lib/Tooltip.js';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger.js';

const tooltip = (msg, className = '') => (
	<Tooltip className={className} id={className}>{msg}</Tooltip>
);

export const TooltipWrapper = ({placement = 'top', className = '', tooltipClassName = '', dataTestId, delayShow = 0, children}) => (
	<OverlayTrigger placement={placement} overlay={tooltip(children, tooltipClassName)} delayShow={delayShow}>
		<div className={className} data-test-id={dataTestId}>{children}</div>
	</OverlayTrigger>
);

export default tooltip;
