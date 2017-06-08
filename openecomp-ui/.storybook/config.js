import React from 'react';
import {configure, addDecorator} from '@kadira/storybook';
import {withKnobs} from '@kadira/storybook-addon-knobs';
import { setOptions } from '@kadira/storybook-addon-options';
import './storybook.scss';
import '../resources/scss/onboarding.scss';


const req = require.context('../src', true, /.stories.js$/)
const namespaceDecorator = (story) => (
    <div className='dox-ui'>
        {story()}
    </div>
);

addDecorator(namespaceDecorator);

setOptions({
    name: 'custom',
    downPanelInRight: true
});

function loadStories() {
  req.keys().forEach((filename) => req(filename))
}

configure(loadStories, module);