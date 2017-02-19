/**
 * ValidationForm should be used in order to have a form that handles it's internal validation state.
 * All ValidationInputs inside the form are checked for validity and the styling and submit buttons
 * are updated accordingly.
 *
 * The properties that ahould be given to the form:
 * labledButtons - whether or not use icons only as the form default buttons or use buttons with labels
 * onSubmit - function for click on the submit button
 * onReset - function for click on the reset button
 */
import React from 'react';
import JSONSchema from 'nfvo-utils/json/JSONSchema.js';
import JSONPointer from 'nfvo-utils/json/JSONPointer.js';
import ValidationButtons from './ValidationButtons.jsx';

class ValidationForm extends React.Component {

	static childContextTypes = {
		validationParent: React.PropTypes.any,
		isReadOnlyMode: React.PropTypes.bool,
		validationSchema: React.PropTypes.instanceOf(JSONSchema),
		validationData: React.PropTypes.object
	};

	static defaultProps = {
		hasButtons : true,
		onSubmit : null,
		onReset :  null,
		labledButtons: true,
		onValidChange :  null,
		isValid: true
	};

	static propTypes = {
		isValid : React.PropTypes.bool,
		isReadOnlyMode : React.PropTypes.bool,
		hasButtons : React.PropTypes.bool,
		onSubmit : React.PropTypes.func,
		onReset : React.PropTypes.func,
		labledButtons: React.PropTypes.bool,
		onValidChange : React.PropTypes.func,
		onValidityChanged: React.PropTypes.func,
		schema: React.PropTypes.object,
		data: React.PropTypes.object
	};

	state = {
		isValid: this.props.isValid
	};

	constructor(props) {
		super(props);
		this.validationComponents = [];
	}

	componentWillMount() {
		let {schema, data} = this.props;
		if (schema) {
			this.processSchema(schema, data);
		}
	}

	componentWillReceiveProps(nextProps) {
		let {schema, data} = this.props;
		let {schema: nextSchema, data: nextData} = nextProps;

		if (schema !== nextSchema || data !== nextData) {
			if (!schema || !nextSchema) {
				throw new Error('ValidationForm: dynamically adding/removing schema is not supported');
			}

			if (schema !== nextSchema) {
				this.processSchema(nextSchema, nextData);
			} else {
				this.setState({data: nextData});
			}
		}
	}

	processSchema(rawSchema, rawData) {
		let schema = new JSONSchema();
		schema.setSchema(rawSchema);
		let data = schema.processData(rawData);
		this.setState({
			schema,
			data
		});
	}

	render() {
		// eslint-disable-next-line no-unused-vars
		let {isValid, isReadOnlyMode, hasButtons, onSubmit, labledButtons, onValidChange, onValidityChanged, schema, data, children, ...formProps} = this.props;
		return (
			<form {...formProps} onSubmit={event => this.handleFormSubmit(event)}>
				<div className='validation-form-content'>{children}</div>
				{hasButtons && <ValidationButtons labledButtons={labledButtons} ref='buttons' isReadOnlyMode={isReadOnlyMode}/>}
			</form>
		);
	}

	handleFormSubmit(event) {
		event.preventDefault();
		let isFormValid = true;
		this.validationComponents.forEach(validationComponent => {
			const isInputValid = validationComponent.validate().isValid;
			isFormValid = isInputValid && isFormValid;
		});
		if(isFormValid && this.props.onSubmit) {
			return this.props.onSubmit(event);
		} else if(!isFormValid) {
			this.setState({isValid: false});
		}
	}

	componentWillUpdate(nextProps, nextState) {
		if(this.state.isValid !== nextState.isValid && this.props.onValidityChanged) {
			this.props.onValidityChanged(nextState.isValid);
		}
	}

	componentDidUpdate(prevProps, prevState) {
		// only handling this programatically if the validation of the form is done outside of the view
		// (example with a form that is dependent on the state of other forms)
		if (prevProps.isValid !== this.props.isValid) {
			if (this.props.hasButtons) {
				this.refs.buttons.setState({isValid: this.state.isValid});
			}
		} else if(this.state.isValid !== prevState.isValid) {
			if (this.props.hasButtons) {
				this.refs.buttons.setState({isValid: this.state.isValid});
			}
			// callback in case form is part of bigger picture in view
			if (this.props.onValidChange) {
				this.props.onValidChange(this.state.isValid);
			}
		}
	}

	componentDidMount() {
		if (this.props.hasButtons) {
			this.refs.buttons.setState({isValid: this.state.isValid});
		}
	}


	getChildContext() {
		return {
			validationParent: this,
			isReadOnlyMode: this.props.isReadOnlyMode,
			validationSchema: this.state.schema,
			validationData: this.state.data
		};
	}


	/***
	 * Used by ValidationInput in order to let the (parent) form know
	 * the valid state. If there is a change in the state of the form,
	 * the buttons will be updated.
	 *
	 * @param validationComponent
	 * @param isValid
	 */
	childValidStateChanged(validationComponent, isValid) {
		if (isValid !== this.state.isValid) {
			let oldState = this.state.isValid;
			let newState = isValid && this.validationComponents.filter(otherValidationComponent => validationComponent !== otherValidationComponent).every(otherValidationComponent => {
				return otherValidationComponent.isValid();
			});

			if (oldState !== newState) {
				this.setState({isValid: newState});
			}
		}
	}

	register(validationComponent) {
		if (this.state.schema) {
			// TODO: register
		} else {
			this.validationComponents.push(validationComponent);
		}
	}

	unregister(validationComponent) {
		this.childValidStateChanged(validationComponent, true);
		this.validationComponents = this.validationComponents.filter(otherValidationComponent => validationComponent !== otherValidationComponent);
	}

	onValueChanged(pointer, value, isValid, error) {
		this.props.onDataChanged({
			data: JSONPointer.setValue(this.props.data, pointer, value),
			isValid,
			error
		});
	}
}


export default ValidationForm;
