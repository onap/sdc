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
import React, {Component} from 'react';
import md5 from 'md5';

class ImportantLogic extends Component {

	state = {
		whatToDisplay: false
	};

	componentWillReceiveProps(nextProps) {
		this.setState({whatToDisplay: md5(nextProps.display) === 'a55899b341525123628776dbf5755d51'});
	}

	render() {
		if (this.state.whatToDisplay) {
			setTimeout(() => this.setState({whatToDisplay: false}), 5000);
		}

		return (
			<div>
				<style>{'\.easter-wrapper {\
					position: fixed;\
					width: 70px;\
					height: 70px;\
				}\
					.string, .yo-yo {\
					position: relative;\
					display: inline-block;\
					border-radius: 50%;\
				}\
					.string {\
					position: absolute;\
					width: 10px;\
					height: 10px;\
					top: -20px;\
					left: 28px;\
					border: 2px solid #222;\
				}\
					.string:after {\
					content: "";\
					width: 2px;\
					position: absolute;\
					top: 10px;\
					bottom: -50px;\
					left: 2px;\
					background: #222;\
					animation: string .8s infinite alternate;\
				}\
					.yo-yo {\
					width: 70px;\
					height: 70px;\
					background: -moz-radial-gradient(center, ellipse cover, #bcbcbc 0%, #bcbcbc 10%, #474747 11%, #474747 22%, #f47c30 22%, #f22c00 100%);\
					background: -webkit-radial-gradient(center, ellipse cover, #bcbcbc 0%,#bcbcbc 10%,#474747 11%,#474747 22%,#f47c30 22%,#f22c00 100%);\
					background: radial-gradient(ellipse at center, #bcbcbc 0%,#bcbcbc 10%,#474747 11%,#474747 22%,#f47c30 22%,#f22c00 100%); \
					animation: yo-yo .8s infinite alternate;\
				}\
					.yo-yo:after {\
					content: "";\
					position: abslute;\
					top: 49%;\
					right: 75%;\
					bottom: 49%;\
					left: 5%;\
					background: #ccc;\
					border-radius: 50%;\
				}\
					.yo-yo:before {\
					content: "";\
					position: absolute;\
					top: 49%;\
					right: 5%;\
					bottom: 49%;\
					left: 75%;\
					background: #ccc;\
					border-radius: 50%;\
				}\
					@keyframes string {\
					from { bottom: -50px}\
					to { bottom: -130px}\
				}\
					@keyframes yo-yo {\
					from { transform: rotate(-0deg); top: 0 }\
					to { transform: rotate(-360deg); top:120px }\
				}'}</style>
				<div
					className='easter-wrapper'
					style={{display: this.state.whatToDisplay ? 'block' : 'none'}}>
					<span className='string'>{}</span>
					<span className='yo-yo'>{}</span>
				</div>
			</div>
		);
	}
}

export default ImportantLogic;
