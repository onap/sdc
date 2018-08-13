/*!
 * Copyright © 2016-2018 European Support Limited
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
import i18n from 'nfvo-utils/i18n/i18n.js';
import Button from 'sdc-ui/lib/react/Button.js';

const ModalButtons = ({
    isFormValid,
    isReadOnlyMode,
    onSubmit,
    selectedLimit,
    onCancel,
    className
}) => (
    <div className={`${className}`}>
        {!selectedLimit && (
            <Button
                btnType="primary"
                disabled={!isFormValid || isReadOnlyMode}
                onClick={() => onSubmit()}
                type="reset">
                {i18n('Save')}
            </Button>
        )}
        <Button
            btnType={selectedLimit ? 'primary' : 'secondary'}
            onClick={() => onCancel()}
            type="reset">
            {i18n('Cancel')}
        </Button>
    </div>
);

ModalButtons.propTypes = {
    isFormValid: PropTypes.bool,
    isReadOnlyMode: PropTypes.bool,
    onSubmit: PropTypes.func,
    selectedLimit: PropTypes.string,
    onCancel: PropTypes.func,
    className: PropTypes.string
};

export default ModalButtons;
