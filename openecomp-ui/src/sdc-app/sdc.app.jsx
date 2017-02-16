import '../../resources/scss/bootstrap.scss';
import '../../resources/css/font-awesome.min.css';
import 'react-select/dist/react-select.min.css';
import 'dox-sequence-diagram-ui/src/main/webapp/res/sdc-sequencer.scss';
import '../../resources/scss/style.scss';

import React from 'react';
import ReactDOM from 'react-dom';

import Application from './Application.jsx';
import Modules from './ModulesOptions.jsx';

//chrome 48 remove svg method which is used in jointjs core -> https://github.com/cpettitt/dagre-d3/issues/202 --> http://jointjs.com/blog/get-transform-to-element-polyfill.html
SVGElement.prototype.getTransformToElement = SVGElement.prototype.getTransformToElement || function(toElement) {
	return toElement.getScreenCTM().inverse().multiply(this.getScreenCTM());
};

ReactDOM.render(<Application><Modules/></Application>, document.getElementById('sdc-app'));
