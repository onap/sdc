import React from 'react';
import {storiesOf, action} from '@kadira/storybook';
import {select, text, withKnobs} from '@kadira/storybook-addon-knobs';
import SVGIcon from './SVGIcon.jsx';

const stories = storiesOf('SVGIcon', module);

const iconNames = ['locked',
    'pencil',
    'plus-circle',
    'plus',
    'search',
    'sliders',
    'trash-o',
    'unlocked',
    'vendor',
    'version-controller-lock-closed',
    'version-controller-lock-open',
    'version-controller-revert',
    'version-controller-save',
    'version-controller-submit',
    'vlm',
    'vsp' ];

function colorChanger() {
    return {fill: text('Color', '')};
}

function iconName() {
    return select('Icon name' , iconNames, iconNames[0]);
}

stories.addDecorator(withKnobs);

stories
    .add('icon', () => {
        return (
            <SVGIcon name={iconName()} style={colorChanger()}/>
        );
    })
    .add('icon with label', () => {
        return (
            <SVGIcon name={iconName()} label={iconName()} style={colorChanger()}/>
        );
    })
    .add('locked clickable', () => {
        return (
            <SVGIcon name={iconName()} onClick={action('clicked')} style={colorChanger()}/>
        );
    });