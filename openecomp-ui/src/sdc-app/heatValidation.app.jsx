import '../../resources/scss/bootstrap.scss';
import '../../resources/css/font-awesome.min.css';
import 'react-select/dist/react-select.min.css';
import 'dox-sequence-diagram-ui/src/main/webapp/res/sdc-sequencer.scss';
import '../../resources/scss/style.scss';

import React from 'react';
import ReactDOM from 'react-dom';
import UploadScreen from './heatvalidation/UploadScreen.jsx';
import Application from './Application.jsx';


ReactDOM.render(<Application><UploadScreen/></Application>, document.getElementById('heat-validation-app'));
