import React from 'react';
import {storiesOf} from '@kadira/storybook';
import {withKnobs} from '@kadira/storybook-addon-knobs';

import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';


const stories = storiesOf('GridColumns', module);
stories.addDecorator(withKnobs);

var divStyle = {
	'border-style': 'solid',
	'border-size': 1
};

const myDiv =  (<div style={divStyle}>Text Text Text</div>);

stories
	.add('Grid Options', () => (
		<div>
			<GridSection title='No last column set on item'>
				<GridItem colSpan={2}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={2}>
					{myDiv}
				</GridItem>
			</GridSection>

			<GridSection hasLastColSet={true} title='With last column set on item and gridsection'>
				<GridItem colSpan={2}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={2} lastColInRow={true}>
					{myDiv}
				</GridItem>
			</GridSection>

			<GridSection title='With last column set on item and NOT on gridsection'>
				<GridItem colSpan={2}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={2} lastColInRow={true}>
					{myDiv}
				</GridItem>
			</GridSection>

		</div>
	))
	.add('Last Column', () => (

		<div>
			<GridSection hasLastColSet={true} title='Testing different configurations with all settings'>
				<GridItem colSpan={2}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={2} lastColInRow={true}>
					{myDiv}
				</GridItem>
			</GridSection>
			<GridSection hasLastColSet={true}>
				<GridItem colSpan={1}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={1}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={1}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={1} lastColInRow={true}>
					{myDiv}
				</GridItem>
			</GridSection>
			<GridSection hasLastColSet={true}>
				<GridItem colSpan={1}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={1}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={2} lastColInRow={true}>
					{myDiv}
				</GridItem>
			</GridSection>
			<GridSection hasLastColSet={true}>
				<GridItem colSpan={2}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={1}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={1} lastColInRow={true}>
					{myDiv}
				</GridItem>
			</GridSection>
			<GridSection hasLastColSet={true}>
				<GridItem colSpan={4} lastColInRow={true}>
					{myDiv}
				</GridItem>
			</GridSection>
			<GridSection hasLastColSet={true}>
				<GridItem colSpan={3}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={1} lastColInRow={true}>
					{myDiv}
				</GridItem>
			</GridSection>
			<GridSection hasLastColSet={true}>
				<GridItem colSpan={1}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={3} lastColInRow={true}>
					{myDiv}
				</GridItem>
			</GridSection>
		</div>
	))
	.add('No Last Column', () => (
		<div>
			<GridSection title='Testing different configurations'>
				<GridItem colSpan={2}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={2}>
					{myDiv}
				</GridItem>
			</GridSection>
			<GridSection>
				<GridItem colSpan={1}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={1}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={1}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={1}>
					{myDiv}
				</GridItem>
			</GridSection>
			<GridSection>
				<GridItem colSpan={1}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={1}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={2}>
					{myDiv}
				</GridItem>
			</GridSection>
			<GridSection>
				<GridItem colSpan={2}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={1}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={1}>
					{myDiv}
				</GridItem>
			</GridSection>
			<GridSection>
				<GridItem colSpan={4}>
					{myDiv}
				</GridItem>
			</GridSection>
			<GridSection>
				<GridItem colSpan={3}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={1}>
					{myDiv}
				</GridItem>
			</GridSection>
			<GridSection>
				<GridItem colSpan={1}>
					{myDiv}
				</GridItem>
				<GridItem colSpan={3}>
					{myDiv}
				</GridItem>
			</GridSection>
		</div>
	))	.add('Test LKG form', () => (
	<div>
		<GridSection title='Testing VLM LKG configurations'>
			<GridItem colSpan={2}>
				{myDiv}
			</GridItem>
			<GridItem colSpan={2} lastColInRow={true}>
				{myDiv}
			</GridItem>
			<GridItem colSpan={2}>
				{myDiv}
			</GridItem>
			<GridItem colSpan={2} lastColInRow={true}>
				{myDiv}
			</GridItem>
			<GridItem colSpan={1}>
				{myDiv}
			</GridItem>
			<GridItem colSpan={1}>
				{myDiv}
			</GridItem>
			<GridItem colSpan={1}>
				{myDiv}
			</GridItem>
			<GridItem colSpan={1} lastColInRow={true}>
				{myDiv}
			</GridItem>
			<GridItem colSpan={2}>
				<div style={divStyle}>1</div>
			</GridItem>
		</GridSection>
	</div>
));