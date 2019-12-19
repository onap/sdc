/**
 * Copyright (c) 2019 Vodafone Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { connect } from 'react-redux';
import SoftwareProductValidationResultsView from './SoftwareProductValidationResultsView.jsx';
import SoftwareProductValidationResultsViewActionHelper from './SoftwareProductValidationResultsViewActionHelper.js';

export const mapStateToProps = ({ softwareProduct }) => {
    let { softwareProductValidationResult } = softwareProduct;
    let { softwareProductValidation } = softwareProduct;
    return {
        softwareProductValidationResult,
        softwareProductValidation
    };
};
export const mapActionsToProps = dispatch => {
    return {
        refreshValidationResults: (requestId, endPoints) => {
            return SoftwareProductValidationResultsViewActionHelper.refreshValidationResults(
                dispatch,
                { requestId, endPoints }
            );
        },
        updateDisplayTestResultData: testResultToDisplay => {
            return SoftwareProductValidationResultsViewActionHelper.updateDisplayTestResultData(
                dispatch,
                { testResultToDisplay }
            );
        }
    };
};
export default connect(mapStateToProps, mapActionsToProps, null, {
    withRef: true
})(SoftwareProductValidationResultsView);
