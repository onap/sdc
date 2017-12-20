import React from 'react';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger.js';
import Tooltip from 'react-bootstrap/lib/Tooltip.js';

function tooltip (msg)  {
	return (
  		<Tooltip className='select-action-table-error-tooltip' id='error-tooltip'>{msg}</Tooltip>
	);
};

const IconWithOverlay = ({overlayMsg}) => (
	<OverlayTrigger placement='bottom' overlay={tooltip(overlayMsg)}>
		<SVGIcon name='errorCircle' color='negative'/>
	</OverlayTrigger>
);

function renderErrorOrCheck({hasError, overlayMsg}) {
	if (hasError === undefined) {
		return <SVGIcon name='angleRight' className='dummy-icon' />;
	}

	if (hasError) {
		return overlayMsg ? <IconWithOverlay overlayMsg={overlayMsg}/> :  <SVGIcon color='negative' name='errorCircle'/>;
	}

	return <SVGIcon name='checkCircle' color='positive'/>;
}

const SelectActionTableRow = ({children, actionIcon, onAction, showAction, hasError, hasErrorIndication, overlayMsg}) => (
	<div className='select-action-table-row-wrapper'>
		<div className={`select-action-table-row ${hasError ? 'has-error' : ''}`}>
			{children}
		</div>
		{onAction && <SVGIcon color='secondary' name={actionIcon} data-test-id={`select-action-table-${actionIcon}`} onClick={onAction} iconClassName={(showAction) ? '' : 'hideDelete'}/>}
		{hasErrorIndication && renderErrorOrCheck({hasError, overlayMsg})}
	</div>
);

export default SelectActionTableRow;
