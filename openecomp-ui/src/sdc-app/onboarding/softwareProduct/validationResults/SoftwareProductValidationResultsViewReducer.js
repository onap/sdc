import { actionTypes } from './SoftwareProductValidationResultsViewConstants.js';

export default (state = {}, action) => {
    switch (action.type) {
        case actionTypes.FETCH_VSP_RESULT: {
            return {
                ...state,
                vspTestResults: action.vspTestResults
            };
        }
        case actionTypes.FETCH_VSP_CHECKS: {
            return {
                ...state,
                vspChecks: action.vspChecks
            };
        }
        case actionTypes.UPDATE_DISPLAY_TEST_RESULT_DATA: {
            return {
                ...state,
                vspTestResults: null,
                testResultToDisplay: action.testResultToDisplay
            };
        }
        default:
            return state;
    }
};
