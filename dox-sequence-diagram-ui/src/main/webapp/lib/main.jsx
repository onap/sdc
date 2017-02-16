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
import { render } from 'react-dom';
import Sequencer from './ecomp/asdc/sequencer/Sequencer';
import '../res/ecomp/asdc/sequencer/sequencer-development.scss';
import '../res/thirdparty/react-select/react-select.min.css';

function renderApplication() {
  const shell = document.createElement('div');
  shell.setAttribute('style', 'height:100%;width:100%;margin:0;padding:0');
  document.body.appendChild(shell);
  const options = { demo: true };
  render(<Sequencer options={options} />, shell);
}

if (window.addEventListener) {
  window.addEventListener('DOMContentLoaded', renderApplication);
} else {
  window.attachEvent('onload', renderApplication);
}
