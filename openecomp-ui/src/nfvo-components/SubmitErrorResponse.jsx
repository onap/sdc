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
import ListGroupItem from 'react-bootstrap/lib/ListGroupItem.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import SVGIcon from 'nfvo-components/icon/SVGIcon.jsx';
import Icon from 'nfvo-components/icon/Icon.jsx';
import {Collapse} from 'react-bootstrap';
/**
 * parsing and showing the following Java Response object
 *
 * public class ValidationResponse {
		private boolean valid = true;
		private Collection<ErrorCode> vspErrors;
		private Collection<ErrorCode> licensingDataErrors;
		private Map<String, List<ErrorMessage>> uploadDataErrors;
		private Map<String, List<ErrorMessage>> compilationErrors;
		private QuestionnaireValidationResult questionnaireValidationResult;
    }

 * public class ErrorCode {
		private String id;
		private String message;
		private ErrorCategory category;
    }

 * public class ErrorMessage {
		private final ErrorLevel level;
		private final String message;
    }
 */
class SubmitErrorResponse extends Component {


	render() {
		let {validationResponse : {vspErrors, licensingDataErrors, questionnaireValidationResult, uploadDataErrors}} = this.props;
		return (
			<div className='submit-error-response-view'>
				{vspErrors && this.renderVspErrors(vspErrors)}
				{licensingDataErrors && this.renderVspErrors(licensingDataErrors)}
				{questionnaireValidationResult && this.renderComponentsErrors(questionnaireValidationResult)}
				{uploadDataErrors && this.renderUploadDataErrors(uploadDataErrors)}
			</div>
		);
	}

	renderVspErrors(errors) {
		return (
			<ErrorBlock errorType={i18n('VSP Errors')}>
				<div>
					{errors.length && errors.map(error=>{return (<ErrorMessage error={error.message}/>);})}
				</div>
			</ErrorBlock>
		);
	}


	renderComponentsErrors(errors) {
		return (
			<ErrorBlock errorType={i18n('Components Errors')}>
				<div>
					{errors.validationData.length && errors.validationData.map(item =>{ return (<ComponentError item={item}/>);})}
				</div>
			</ErrorBlock>
		);
	}

	renderUploadDataErrors(uploadDataErrors) {
		return (
			<ErrorBlock errorType={i18n('Upload Data Errors')}>
				<div>
					<UploadErrorList items={uploadDataErrors}/>
				</div>
			</ErrorBlock>
		);
	}
}


const ComponentError = ({item}) => {
	let i = 0;
	return (
		<div>
			<div className='component-name-header'>{item.entityName}</div>
			{item.errors.map(error => {return(<ErrorMessage key={i++} error={error}/>);})}
		</div>
	);
};

function* entries(obj) {
	for (let key of Object.keys(obj)) {
		yield {header: key, list: obj[key]};
	}
}

const UploadErrorList = ({items}) => {
	let generator = entries(items);

	let errors = [];
	let i = 0;
	for (let item of generator) {errors.push(
		<div>
			<div className='component-name-header'>{item.header}</div>
			{item.list.map(error => <ErrorMessage key={i++} warning={error.level === 'WARNING'} error={error.message}/> )}
		</div>
	);}
	return (
		<div>
			{errors}
		</div>
	);
};

class ErrorBlock extends React.Component {
	state = {
		collapsed: false
	};

	render() {
		let {errorType, children} = this.props;
		return (
			<div className='error-block'>
				<ErrorHeader collapsed={this.state.collapsed} onClick={()=>{this.setState({collapsed: !this.state.collapsed});}} errorType={errorType}/>
				<Collapse in={this.state.collapsed}>
					{children}
				</Collapse>
			</div>
		);
	}
}

const ErrorHeader = ({errorType, collapsed, onClick}) => {
	return(
		<div onClick={onClick} className='error-block-header'>
			<SVGIcon iconClassName={collapsed ? '' : 'right' } name='chevron-down'/>
			{errorType}
		</div>
	);
};

const ErrorMessage = ({error, warning}) => {
	return (
		<ListGroupItem className='error-code-list-item'>
			<Icon image={warning ? 'warning' : 'error'} label={error}/>
		</ListGroupItem>
	);
};

export default SubmitErrorResponse;
