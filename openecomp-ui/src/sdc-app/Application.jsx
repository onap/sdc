/*!
 * Copyright © 2016-2018 European Support Limited
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
import PropTypes from 'prop-types';
import { Provider } from 'react-redux';
import GlobalModal from 'nfvo-components/modal/GlobalModal.js';
import Notifications from 'nfvo-components/notification/Notifications.js';
import Loader from 'nfvo-components/loader/Loader.jsx';
import WebSocketUtil from 'nfvo-utils/WebSocketUtil.js';
import UserNotificationsActionHelper from 'sdc-app/onboarding/userNotifications/UserNotificationsActionHelper.js';
import store from './AppStore.js';
import FeaturesActionHelper from 'sdc-app/features/FeaturesActionHelper.js';

class Application extends React.Component {
    static propTypes = {
        openSocket: PropTypes.bool
    };
    componentDidMount() {
        const { openSocket = true } = this.props;
        if (openSocket) {
            UserNotificationsActionHelper.notificationsFirstHandling(
                store.dispatch
            );
        }
        FeaturesActionHelper.getFeaturesList(store.dispatch);
    }
    componentWillUnmount() {
        WebSocketUtil.close();
    }
    render() {
        return (
            <Provider store={store}>
                <div data-onboardingroot="">
                    <GlobalModal />
                    <Notifications />
                    {this.props.children}
                    <Loader />
                </div>
            </Provider>
        );
    }
}

export default Application;
