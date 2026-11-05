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

const mapStateToProps = ({ loader }) => {
    return {
        isLoading: loader.isLoading
    };
};

export class Loader extends React.Component {
    static propTypes = {
        isLoading: PropTypes.bool.isRequired
    };

    static defaultProps = {
        isLoading: false
    };

    state = { isVisible: false };

    componentDidMount() {
        this.updateVisibility();
    }

    componentDidUpdate(previousProps) {
        if (previousProps.isLoading !== this.props.isLoading) {
            this.updateVisibility();
        }
    }

    componentWillUnmount() {
        clearTimeout(this.showTimer);
        clearTimeout(this.hideTimer);
    }

    updateVisibility() {
        clearTimeout(this.hideTimer);
        if (this.props.isLoading) {
            if (!this.state.isVisible && !this.showTimer) {
                this.showTimer = setTimeout(() => {
                    this.showTimer = null;
                    if (this.props.isLoading) {
                        this.setState({ isVisible: true });
                    }
                }, 300);
            }
        } else {
            this.hideTimer = setTimeout(() => {
                clearTimeout(this.showTimer);
                this.showTimer = null;
                this.setState({ isVisible: false });
            }, 100);
        }
    }

    render() {
        const { isLoading } = this.props;
        const { isVisible } = this.state;
        return (
            <div className="onboarding-loader">
                {(isLoading || isVisible) && (
                    <div
                        className={`onboarding-loader-backdrop${
                            isVisible ? '' : ' onboarding-loader-pending'
                        }`}>
                        {isVisible && <div className="tlv-loader large" />}
                    </div>
                )}
            </div>
        );
    }
}

export default connect(mapStateToProps)(Loader);
