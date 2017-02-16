import React from 'react';
import FontAwesome from 'react-fontawesome';
import classnames from 'classnames';
import Input from 'react-bootstrap/lib/Input';


class ExpandableInput extends React.Component {
	constructor(props){
		super(props);
		this.state = {showInput: false, value: ''};
		this.toggleInput = this.toggleInput.bind(this);
		this.handleFocus = this.handleFocus.bind(this);
		this.handleInput = this.handleInput.bind(this);
		this.handleClose = this.handleClose.bind(this);
	}

	toggleInput(){
		if (!this.state.showInput){
			this.searchInputNode.refs.input.focus();
		} else {
			this.setState({showInput: false});
		}
	}

	handleInput(e){
		let {onChange} = this.props;

		this.setState({value: e.target.value});
		onChange(e);
	}

	handleClose(){
		this.handleInput({target: {value: ''}});
		this.searchInputNode.refs.input.focus();
	}

	handleFocus(){
		if (!this.state.showInput){
			this.setState({showInput: true});
		}
	}

	getValue(){
		return this.state.value;
	}

	render(){
		let {iconType} = this.props;

		let inputClasses = classnames({
			'expandable-active': this.state.showInput,
			'expandable-not-active': !this.state.showInput
		});

		let iconClasses = classnames(
			'expandable-icon',
			{'expandable-icon-active': this.state.showInput}
		);

		return (
			<div className='expandable-input-wrapper'>
				<Input
					type='text'
					value={this.state.value}
					ref={(input) => this.searchInputNode = input}
					className={inputClasses}
					groupClassName='expandable-input-control'
					onChange={e => this.handleInput(e)}
					onFocus={this.handleFocus}/>
				{this.state.showInput && this.state.value && <FontAwesome onClick={this.handleClose} name='close' className='expandable-close-button'/>}
				{!this.state.value && <FontAwesome onClick={this.toggleInput} name={iconType} className={iconClasses}/>}
			</div>
		);
	}
}

export default ExpandableInput;
