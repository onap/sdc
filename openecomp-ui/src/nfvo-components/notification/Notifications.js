/*
 * Copyright © 2016-2018 European Support Limited
 * Modifications Copyright (C) 2026 Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React from 'react';
import ReactDOM from 'react-dom';
import { Portal, Notification } from 'onap-ui-react';
import { connect } from 'react-redux';
import { notificationActions } from './NotificationsConstants.js';
import { CSSTransition, TransitionGroup } from 'react-transition-group';

export const mapStateToProps = ({ popupNotifications = [] }) => {
    return {
        notifications: popupNotifications
    };
};

const mapActionToProps = dispatch => {
    return {
        onClick: item => {
            dispatch(notificationActions.removeNotification(item));
        }
    };
};

const openModalDialog = () => document.querySelector('dialog.sdc-modal[open]');

export class Notifications extends React.Component {
    state = { dialog: null };

    componentDidMount() {
        // A notification left in <body> sits under an open modal dialog's backdrop and is
        // inert there, so it moves into the dialog while one is open.
        this.observer = new MutationObserver(() => this.syncDialog());
        this.observer.observe(document.body, {
            subtree: true,
            attributes: true,
            attributeFilter: ['open']
        });
        this.syncDialog();
    }

    componentWillUnmount() {
        this.observer.disconnect();
    }

    syncDialog() {
        const dialog = openModalDialog();
        if (dialog !== this.state.dialog) {
            this.setState({ dialog });
        }
    }

    render() {
        const { notifications, onClick } = this.props;
        const { dialog } = this.state;
        const container = (
            <div
                className={`onboarding-notifications-container position-top-right${
                    dialog ? ' in-dialog' : ''
                }`}>
                <TransitionGroup>
                    {notifications.map(item => (
                        <CSSTransition
                            in={true}
                            timeout={500}
                            unmountOnExit
                            classNames="react-transition"
                            key={`notification-transition-${item.id}`}>
                            <Notification
                                key={item.id}
                                type={item.type}
                                title={item.title}
                                message={item.message}
                                onClick={() => onClick(item)}
                            />
                        </CSSTransition>
                    ))}
                </TransitionGroup>
            </div>
        );
        return dialog ? (
            ReactDOM.createPortal(container, dialog)
        ) : (
            <Portal>{container}</Portal>
        );
    }
}

export default connect(mapStateToProps, mapActionToProps, null)(Notifications);
