import React from 'react';
import ReactDOM from 'react-dom';
import Configuration from 'sdc-app/config/Configuration.js';
import Application from 'sdc-app/Application.jsx';
import store from 'sdc-app/AppStore.js';
import FlowsListEditor from './FlowsListEditor.js';
import FlowsActions from './FlowsActions.js';

class FlowsListEditorPunchOutWrapper extends React.Component {

	componentDidMount() {
		let element = ReactDOM.findDOMNode(this);
		element.addEventListener('click', event => {
			if (event.target.tagName === 'A') {
				event.preventDefault();
			}
		});
		['wheel', 'mousewheel', 'DOMMouseScroll'].forEach(eventType =>
			element.addEventListener(eventType, event => event.stopPropagation())
		);
	}

	render() {
		return <FlowsListEditor/>;
	}
}

export default class DiagramPunchOut {

	render({options: {data, apiRoot, apiHeaders}, onEvent}, element) {

		if (!this.isConfigSet) {
			Configuration.setATTApiRoot(apiRoot);
			Configuration.setATTApiHeaders(apiHeaders);
			this.isConfigSet = true;
		}

		this.onEvent = onEvent;
		this.handleData(data);

		if (!this.rendered) {
			ReactDOM.render(<Application><div className='dox-ui'><FlowsListEditorPunchOutWrapper/></div></Application>, element);
			this.rendered = true;
		}
	}

	unmount(element) {
		let dispatch = action => store.dispatch(action);
		ReactDOM.unmountComponentAtNode(element);
		FlowsActions.reset(dispatch);
	}

	handleData(data) {
		let {serviceID, diagramType} = data;
		let dispatch = action => store.dispatch(action);

		if (serviceID !== this.prevServiceID || diagramType !== this.prevDiagramType) {
			this.prevServiceID = serviceID;
			this.prevDiagramType = diagramType;
			FlowsActions.fetchFlowArtifacts(dispatch, {...data});
		}
	}
}
