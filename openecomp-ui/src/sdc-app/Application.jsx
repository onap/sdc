import React from 'react';
import {Provider} from 'react-redux';
import NotificationModal from 'nfvo-components/notifications/NotificationModal.jsx';
import Loader from 'nfvo-components/loader/Loader.jsx';
import store from './AppStore.js';


class Application extends React.Component {
	render() {
		return (
			<Provider store={store}>
				<div>
					<NotificationModal />
					{this.props.children}
					<Loader />
				</div>
			</Provider>
		);
	}
}

export default Application;

