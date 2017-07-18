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
		<SVGIcon name='errorCircle'/>
	</OverlayTrigger>
);

function renderErrorOrCheck({hasError, overlayMsg}) {
	if (hasError === undefined) {
		return <SVGIcon name='angleRight' className='dummy-icon' />;
	}

	if (hasError) {
		return overlayMsg ? <IconWithOverlay overlayMsg={overlayMsg}/> :  <SVGIcon name='errorCircle'/>;
	}

	return <SVGIcon name='checkCircle'/>;
}

const SelectActionTableRow = ({children, onDelete, hasError, hasErrorIndication, overlayMsg}) => (
	<div className='select-action-table-row-wrapper'>
		<div className={`select-action-table-row ${hasError ? 'has-error' : ''}`}>
			{children}
		</div>
		{onDelete && <SVGIcon name='trashO' data-test-id='select-action-table-delete' onClick={onDelete} />}
		{hasErrorIndication && renderErrorOrCheck({hasError, overlayMsg})}
	</div>
);

export default SelectActionTableRow;
