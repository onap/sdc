import React from 'react';
import SVGIcon from '../icon/SVGIcon.jsx';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger.js';
import Tooltip from 'react-bootstrap/lib/Tooltip.js';

function tooltip (msg)  {
	return (
  		<Tooltip className='select-action-table-error-tooltip' id='error-tooltip'>{msg}</Tooltip>
	);
};

const IconWithOverlay = ({overlayMsg}) => (
	<OverlayTrigger placement='bottom' overlay={tooltip(overlayMsg)}>
		<SVGIcon name='error-circle'/>
	</OverlayTrigger>
);

const SelectActionTableRow = ({children, onDelete, hasError, overlayMsg}) => (
	<div className='select-action-table-row-wrapper'>
		<div className={`select-action-table-row ${hasError ? 'has-error' : ''}`}>
			{children}
		</div>
		{onDelete ? <SVGIcon name='trash-o' data-test-id='select-action-table-delete' onClick={onDelete} /> : <SVGIcon name='angle-left' className='dummy-icon' />}		
		{hasError ? overlayMsg ? <IconWithOverlay overlayMsg={overlayMsg}/> :  <SVGIcon name='error-circle'/>
					: hasError === undefined ? <SVGIcon name='angle-left' className='dummy-icon'/> : <SVGIcon name='check-circle'/>}		
		
	</div>
);

export default SelectActionTableRow;
