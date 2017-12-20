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
import '../../resources/scss/bootstrap.scss';
import 'react-select/dist/react-select.min.css';
import 'dox-sequence-diagram-ui/src/main/webapp/res/sdc-sequencer.scss';
import '../../resources/scss/style.scss';

import React from 'react';
import ReactDOM from 'react-dom';
import UploadScreen from './heatvalidation/UploadScreen.jsx';
import Application from './Application.jsx';


ReactDOM.render(<Application openSocket={false}><UploadScreen/></Application>, document.getElementById('heat-validation-app'));
