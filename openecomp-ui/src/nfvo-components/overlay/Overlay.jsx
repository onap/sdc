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
import enhanceWithClickOutside from 'react-click-outside';

class Overlay extends React.Component {
    handleClickOutside() {
        if (this.props.onClose) {
            this.props.onClose();
        }
    }

    render() {
        return (
            <div className="onboarding-overlay">
                <div className="arrow-up" />
                <div className="arrow-border" />
                {this.props.children}
            </div>
        );
    }
}

export default enhanceWithClickOutside(Overlay);
