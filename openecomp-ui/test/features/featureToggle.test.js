/*!
 * Copyright © 2016-2017 European Support Limited
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
import {mount} from 'enzyme';
import deepFreeze from 'deep-freeze';
import FeatureFactory  from 'test-utils/factories/features/FeaturesFactory.js';
import {FeatureComponent} from 'sdc-app/features/featureToggle.js';

describe('feature toggle decorator test', () => {
	it('feature on toggle test', () => {
		const featuresList = [FeatureFactory.build({name: 'TEST', active: true})];
		deepFreeze(featuresList);		
		const wrapper = mount(<FeatureComponent features={featuresList} featureName='TEST' InnerComponent={() => (<div className='feature'></div>)}/>);		
		expect(wrapper.find('.feature').length).toBe(1);		
	});

	it('feature off toggle test', () => {
		const featuresList = [FeatureFactory.build({name: 'TEST', active: false})];
		deepFreeze(featuresList);		
		const wrapper = mount(<FeatureComponent features={featuresList} featureName='TEST' InnerComponent={() => (<div className='feature'></div>)}/>);		
		expect(wrapper.find('.feature').length).toBe(0);		
	});
});