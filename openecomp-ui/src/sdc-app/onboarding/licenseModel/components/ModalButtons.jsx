import React from 'react';
import PropTypes from 'prop-types';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Button from 'sdc-ui/lib/react/Button.js';
import GridSection from 'nfvo-components/grid/GridSection.jsx';

const ModalButtons = ({
    isFormValid,
    isReadOnlyMode,
    onSubmit,
    selectedLimit,
    onCancel,
    className
}) => (
    <GridSection className={`license-model-modal-buttons ${className}`}>
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
    </GridSection>
);

ModalButtons.propTypes = {
    isFormValid: PropTypes.func,
    isReadOnlyMode: PropTypes.bool,
    onSubmit: PropTypes.func,
    selectedLimit: PropTypes.string,
    onCancel: PropTypes.func,
    className: PropTypes.string
};

export default ModalButtons;
