import React, {Component} from 'react';
import ListGroupItem from 'react-bootstrap/lib/ListGroupItem.js';
import ListGroup from 'react-bootstrap/lib/ListGroup.js';
import Panel from 'react-bootstrap/lib/Panel.js';
import i18n from 'nfvo-utils/i18n/i18n.js';

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
		let {validationResponse} = this.props;
		return (
			<div className='submit-error-response-view'>
				{validationResponse.vspErrors && this.renderVspErrors(validationResponse.vspErrors)}
				{validationResponse.licensingDataErrors && this.renderVspErrors(validationResponse.licensingDataErrors)}
				{validationResponse.compilationErrors && this.renderCompilationErrors(validationResponse.compilationErrors)}
				{validationResponse.uploadDataErrors && this.renderUploadDataErrors(validationResponse.uploadDataErrors)}
				{validationResponse.questionnaireValidationResult && this.renderQuestionnaireValidationResult(validationResponse.questionnaireValidationResult)}
			</div>
		);
	}

	renderVspErrors(vspErrors) {
		return (
			<Panel header={i18n('VSP Errors')} collapsible>{this.parseErrorCodeCollection(vspErrors)}</Panel>
		);
	}

	renderLicensingDataErrors(licensingDataErrors) {
		return (
			<Panel
				header={i18n('Licensing Data Errors')}
				collapsible>{this.parseErrorCodeCollection(licensingDataErrors)}
			</Panel>
		);
	}

	renderUploadDataErrors(uploadDataErrors) {
		return (
			<Panel
				header={i18n('Upload Data Errors')}
				collapsible>{this.parseMapOfErrorMessagesList(uploadDataErrors)}
			</Panel>
		);
	}

	renderCompilationErrors(compilationErrors) {
		return (
			<Panel
				header={i18n('Compilation Errors')}
				collapsible>{this.parseMapOfErrorMessagesList(compilationErrors)}
			</Panel>
		);
	}

	parseErrorCodeCollection(errors) {
		return (
			<ListGroup>{errors.map(error =>
				<ListGroupItem className='error-code-list-item'>
					<div><span>{i18n('Category: ')}</span>{error.category}</div>
					<div><span>{i18n('Message: ')}</span>{error.message}</div>
				</ListGroupItem>
			)}</ListGroup>
		);
	}

	parseMapOfErrorMessagesList(errorMap) {
		return (
			<ListGroup>
				{Object.keys(errorMap).map(errorStringKey =>
					<Panel header={errorStringKey} collapsible>
						<ListGroup>{errorMap[errorStringKey].map(error =>
							<ListGroupItem className='error-code-list-item'>
								<div><span>{i18n('Level: ')}</span>{error.level}</div>
								<div><span>{i18n('Message: ')}</span>{error.message}</div>
							</ListGroupItem>
						)}</ListGroup>
					</Panel>
				)}
			</ListGroup>
		);
	}


	renderQuestionnaireValidationResult(questionnaireValidationResult) {
		if (!questionnaireValidationResult.valid) {
			return this.parseAndRenderCompositionEntityValidationData(questionnaireValidationResult.validationData);
		}
	}

	parseAndRenderCompositionEntityValidationData(validationData) {
		let {entityType, entityId, errors = [], subEntitiesValidationData = []} = validationData;
		return (
			<ListGroup>
				<Panel header={`${entityType}: ${entityId}`} collapsible>
					<ListGroup>{errors.map(error =>
						<ListGroupItem className='error-code-list-item'>
							<div>{error}</div>
						</ListGroupItem>
					)}</ListGroup>
					{subEntitiesValidationData.map(subValidationData => this.parseAndRenderCompositionEntityValidationData(subValidationData))}
				</Panel>
			</ListGroup>
		);
	}


}

export default SubmitErrorResponse;
