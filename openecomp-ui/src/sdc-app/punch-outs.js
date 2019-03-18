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
import 'sdc-ui/css/style.css';
import 'react-checkbox-tree/lib/react-checkbox-tree.css';
import '../../resources/scss/onboarding.scss';
import 'dox-sequence-diagram-ui/src/main/webapp/res/sdc-sequencer.scss';

import 'core-js/fn/array/includes.js';
import OnboardingPunchOut from './onboarding/OnboardingPunchOut.jsx';
import FlowsPunchOut from './flows/FlowsPunchOut.jsx';

PunchOutRegistry.register('onboarding/vendor', () => new OnboardingPunchOut());
PunchOutRegistry.register('sequence-diagram', () => new FlowsPunchOut());
