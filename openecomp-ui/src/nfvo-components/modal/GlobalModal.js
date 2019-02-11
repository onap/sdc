/*
 * Copyright © 2016-2017 European Support Limited
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
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import {
    Modal,
    ModalHeader,
    ModalTitle,
    ModalBody,
    ModalFooter
} from 'sdc-ui/lib/react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import { modalContentComponents } from 'sdc-app/common/modal/ModalContentMapper.js';
import { actionTypes, typeEnum } from './GlobalModalConstants.js';

const GlobalModalFooter = ({
    onConfirmed,
    onDeclined,
    onClose,
    confirmationButtonText,
    cancelButtonText,
    confirmDataTestId,
    cancelDataTestId
}) => {
    let actionButtonClick;
    if (onConfirmed) {
        actionButtonClick = () => {
            onConfirmed();
            onClose();
        };
    }
    return (
        <ModalFooter
            actionButtonText={onConfirmed ? confirmationButtonText : undefined}
            actionButtonClick={actionButtonClick}
            closeButtonText={cancelButtonText}
            onClose={
                onDeclined
                    ? () => {
                          onDeclined();
                          onClose();
                      }
                    : () => onClose()
            }
            withButtons
            confirmDataTestId={confirmDataTestId}
            cancelDataTestId={cancelDataTestId}
        />
    );
};

GlobalModalFooter.defaultProps = {
    confirmationButtonText: i18n('OK'),
    cancelButtonText: i18n('Cancel'),
    cancelDataTestId: 'sdc-modal-cancel-button',
    confirmDataTestId: 'sdc-modal-confirm-button'
};

GlobalModalFooter.propTypes = {
    confirmationButtonText: PropTypes.string,
    cancelButtonText: PropTypes.string,
    confirmDataTestId: PropTypes.string,
    cancelDataTestId: PropTypes.string
};

export const mapStateToProps = ({ modal }) => {
    const show = !!modal;
    return {
        show,
        ...modal
    };
};

export const mapActionToProps = dispatch => {
    return {
        onClose: () => dispatch({ type: actionTypes.GLOBAL_MODAL_CLOSE })
    };
};

export class GlobalModalView extends React.Component {
    static propTypes = {
        show: PropTypes.bool,
        type: PropTypes.oneOf(['default', 'error', 'warning', 'success']),
        title: PropTypes.string,
        modalComponentProps: PropTypes.object,
        modalComponentName: PropTypes.string,
        onConfirmed: PropTypes.func,
        onDeclined: PropTypes.func,
        confirmationButtonText: PropTypes.string,
        cancelButtonText: PropTypes.string,
        bodyClassName: PropTypes.string,
        cancelDataTestId: PropTypes.string,
        confirmDataTestId: PropTypes.string
    };

    static defaultProps = {
        show: false,
        type: 'custom',
        title: ''
    };

    render() {
        let {
            title,
            type,
            show,
            modalComponentName,
            modalComponentProps,
            msg,
            onConfirmed,
            onDeclined,
            confirmationButtonText,
            cancelButtonText,
            onClose,
            bodyClassName,
            confirmDataTestId,
            cancelDataTestId
        } = this.props;
        const ComponentToRender = modalContentComponents[modalComponentName];
        return (
            <Modal
                show={show}
                type={type}
                size={modalComponentProps && modalComponentProps.size}>
                <ModalHeader type={type} onClose={onClose}>
                    <ModalTitle>{title}</ModalTitle>
                </ModalHeader>
                <ModalBody className={bodyClassName}>
                    {ComponentToRender ? (
                        <ComponentToRender {...modalComponentProps} />
                    ) : msg && typeof msg === 'string' ? (
                        <div>
                            {' '}
                            {msg.split('\n').map((txt, i) => (
                                <span key={i}>
                                    {' '}
                                    {txt} <br />{' '}
                                </span>
                            ))}{' '}
                        </div>
                    ) : (
                        msg
                    )}
                </ModalBody>
                {(onConfirmed || onDeclined || type !== typeEnum.DEFAULT) && (
                    <GlobalModalFooter
                        onConfirmed={onConfirmed}
                        onDeclined={onDeclined}
                        onClose={onClose}
                        confirmationButtonText={confirmationButtonText}
                        cancelButtonText={cancelButtonText}
                        confirmDataTestId={confirmDataTestId}
                        cancelDataTestId={cancelDataTestId}
                    />
                )}
            </Modal>
        );
    }

    componentDidUpdate() {
        if (this.props.timeout) {
            setTimeout(this.props.onClose, this.props.timeout);
        }
    }
}

GlobalModalView.propTypes = {
    show: PropTypes.bool,
    type: PropTypes.oneOf(['custom', 'error', 'alert', 'info']),
    title: PropTypes.string,
    modalComponentProps: PropTypes.object,
    modalComponentName: PropTypes.string,
    onConfirmed: PropTypes.func,
    onDeclined: PropTypes.func,
    confirmationButtonText: PropTypes.string,
    cancelButtonText: PropTypes.string
};

export default connect(mapStateToProps, mapActionToProps, null, {
    withRef: true
})(GlobalModalView);
