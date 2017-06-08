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
import React, { Component, PropTypes } from 'react';


export default class Icon extends Component {

	static propTypes = {
		image: PropTypes.string.isRequired,
		onClick: PropTypes.func,
		label: PropTypes.oneOfType([PropTypes.string, PropTypes.node]),
		className: PropTypes.string,
		iconClassName: PropTypes.string
	};

	static defaultProps = {
		label: '',
		className: '',
		iconClassName: ''
	};

	render() {
		let {image, onClick, label, className, iconClassName, ...other} = this.props;
		let classes = `icon-component ${className} ${onClick ? 'clickable' : ''}`;
		return (
			<div {...other} onClick={onClick} className={classes}>
				<span className={`icon ${image} ${iconClassName}`}></span>
				<span className='icon-label'>{label}</span>
			</div>
		);
	}
}
