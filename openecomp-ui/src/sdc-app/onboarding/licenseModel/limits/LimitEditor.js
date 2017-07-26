import {connect} from 'react-redux';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import LimitEditor from './LimitEditor.jsx';

const mapStateToProps = ({licenseModel: {limitEditor}}) => {

	let {data, genericFieldInfo, formReady} = limitEditor;	
	let isFormValid = ValidationHelper.checkFormValid(genericFieldInfo);

	return {
		data,		
		genericFieldInfo,
		isFormValid,
		formReady
	};
};

const mapActionsToProps = (dispatch) => {
	return {
		onDataChanged: (deltaData, formName, customValidations) => ValidationHelper.dataChanged(dispatch, {deltaData, formName, customValidations}),
		onValidateForm: (formName) => ValidationHelper.validateForm(dispatch, formName)
	};
};

export default connect(mapStateToProps, mapActionsToProps)(LimitEditor);