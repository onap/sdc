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
import '../../resources/scss/bootstrap.scss';
import 'react-select/dist/react-select.min.css';
import 'dox-sequence-diagram-ui/src/main/webapp/res/sdc-sequencer.scss';
import '../../resources/scss/style.scss';

import React from 'react';
import ReactDOM from 'react-dom';

import Application from './Application.jsx';
import Modules from './ModulesOptions.jsx';

//chrome 48 remove svg method which is used in jointjs core -> https://github.com/cpettitt/dagre-d3/issues/202 --> http://jointjs.com/blog/get-transform-to-element-polyfill.html
SVGElement.prototype.getTransformToElement =
    SVGElement.prototype.getTransformToElement ||
    function(toElement) {
        return toElement
            .getScreenCTM()
            .inverse()
            .multiply(this.getScreenCTM());
    };

ReactDOM.render(
    <Application>
        <Modules />
    </Application>,
    document.getElementById('sdc-app')
);
