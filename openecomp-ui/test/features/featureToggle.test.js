import React from 'react';
import {mount} from 'enzyme';
import deepFreeze from 'deep-freeze';
import FeatureFactorie  from 'test-utils/factories/features/FeaturesFactorie.js';
import {FeatureComponent} from 'sdc-app/features/featureToggle.js';

describe('feature toggle decorator test', () => {
	it('feature on toggle test', () => {
		const featuresList = [FeatureFactorie.build({name: 'TEST', active: true})];
		deepFreeze(featuresList);		
		const wrapper = mount(<FeatureComponent features={featuresList} featureName='TEST' InnerComponent={() => (<div className='feature'></div>)}/>);		
		expect(wrapper.find('.feature').length).toBe(1);		
	});

	it('feature off toggle test', () => {
		const featuresList = [FeatureFactorie.build({name: 'TEST', active: false})];
		deepFreeze(featuresList);		
		const wrapper = mount(<FeatureComponent features={featuresList} featureName='TEST' InnerComponent={() => (<div className='feature'></div>)}/>);		
		expect(wrapper.find('.feature').length).toBe(0);		
	});
});