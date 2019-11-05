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
        default:
            return state;
    }
};
