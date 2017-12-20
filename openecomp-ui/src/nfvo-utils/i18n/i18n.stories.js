import React from 'react';
import {storiesOf, action} from '@kadira/storybook';
import {text, number} from '@kadira/storybook-addon-knobs';
import {withKnobs} from '@kadira/storybook-addon-knobs';
import i18n from 'nfvo-utils/i18n/i18n.js';
import i18nJson from 'nfvo-utils/i18n/en.json';

const stories = storiesOf('i18n', module);
stories.addDecorator(withKnobs);


i18nJson['added'] = 'this is my test';
i18nJson['added with {param}'] = 'this is my test with {param}';

stories
	.add('i18n tests', () => {
		let keys = [
			'I do not exist',
			'Delete',
			'OrchestrationTemplateCandidate/File Structure'
		];
		let translations = [];
		let i=0;
		translations.push(<div id={i++}>KEY: VALUE</div>)
		keys.forEach((key) => {
			translations.push((<div id={i++}>{key} : {i18n(key)} </div>));
		});
		var param = 'param';
		translations.push((<div id={i++}>added : {i18n('added')} </div>));
		translations.push((<div id={i++}><font color="red"><b>WRONG</b></font> - added with ${param} in translation : {i18n(`added with ${param}`)} </div>));
		translations.push((<div id={i++}><font color="green"><b>RIGHT</b></font> - added with ${param} and options object {JSON.stringify({param:param})}: {i18n('added with {param}', {param: param})} </div>));

		return (<div>{translations}</div>);
	})
;
