/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
import {connect} from 'react-redux';

const mapStateToProps = ({loader}) => {
	return {
		isLoading: loader.isLoading
	};
};

class Loader extends React.Component {

	static propTypes = {
		isLoading: React.PropTypes.bool.isRequired
	};

	static defaultProps = {
		isLoading: false
	};

	render() {
		let {isLoading} = this.props;

		return (
			<div className='onboarding-loader'>
				{
					isLoading && <div className='onboarding-loader-backdrop'>
						<div className='tlv-loader large'></div>
					</div>
				}
			</div>
		);
	}
}

export default connect(mapStateToProps) (Loader);
