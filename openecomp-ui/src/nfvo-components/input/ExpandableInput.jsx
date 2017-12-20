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
import PropTypes from 'prop-types';
import ReactDOM from 'react-dom';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import Input from 'nfvo-components/input/validation/InputWrapper.jsx';

const ExpandableInputClosed = ({iconType, onClick}) => (
	<SVGIcon className='expandable-input-wrapper closed'  data-test-id='expandable-input-closed' name={iconType} onClick={onClick} />
);

class ExpandableInputOpened extends React.Component {
	componentDidMount(){
		this.rawDomNode = ReactDOM.findDOMNode(this.searchInputNode.inputWrapper);
		this.rawDomNode.focus();
	}

	componentWillReceiveProps(newProps){
		if (!newProps.value){
			if (!(document.activeElement === this.rawDomNode)){
				this.props.handleBlur();
			}
		}
	}

	handleClose(){
		this.props.onChange('');
		this.rawDomNode.focus();
	}

	handleKeyDown(e){
		if (e.key === 'Escape'){
			e.preventDefault();
			if (this.props.value) {
				this.handleClose();
			} else {
				this.rawDomNode.blur();
			}
		};
	}

	render() {
		let {iconType, value, onChange, handleBlur} = this.props;
		return (
				<div className='expandable-input-wrapper opened' key='expandable'>
					<Input
						type='text'
						data-test-id='expandable-input-opened'
						value={value}
						ref={(input) => this.searchInputNode = input}
						className='expandable-active'
						groupClassName='expandable-input-control'
						onChange={e => onChange(e)}
						onKeyDown={e => this.handleKeyDown(e)}
						onBlur={handleBlur}/>
					{value && <SVGIcon data-test-id='expandable-input-close-btn' onClick={() => this.handleClose()} name='close' />}
					{!value && <SVGIcon name={iconType} onClick={handleBlur}/>}
				</div>
		);
	}
}

class ExpandableInput extends React.Component {

	static propTypes = {
		iconType: PropTypes.string,
		onChange: PropTypes.func,
		value: PropTypes.string
	};

	state = {showInput: false};

	closeInput(){
		if (!this.props.value) {
			this.setState({showInput: false});
		}
	}

	getValue(){
		return this.props.value;
	}

	render(){
		let {iconType, value, onChange = false} = this.props;
		return (
			<div className='expandable-input-top'>
				{this.state.showInput &&
					<ExpandableInputOpened
						key='open'
						iconType={iconType}
						onChange={onChange}
						value={value}
						handleKeyDown={(e) => this.handleKeyDown(e)}
						handleBlur={() => this.closeInput()}/>
				}
				{!this.state.showInput && <ExpandableInputClosed key='closed' iconType={iconType} onClick={() => this.setState({showInput: true})} />}
			</div>
				);
	}
}


export default ExpandableInput;
