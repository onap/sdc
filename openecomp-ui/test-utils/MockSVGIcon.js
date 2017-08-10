import React from 'react';
//import ReactDOMServer from 'react-dom/server';

const SVGIcon = ({name, onClick, label, className, iconClassName, labelClassName, labelPosition, color, disabled, ...other}) => {
	let colorClass = (color !== '') ? '__' + color : '';
	let classes = `svg-icon-wrapper ${iconClassName} ${className} ${colorClass} ${onClick ? 'clickable' : ''} ${disabled ? 'disabled' : ''} ${labelPosition}`;

	let iconMock = (
		<div {...other} onClick={onClick} className={classes}>
			<span className={`svg-icon __${name} ${disabled ? 'disabled' : ''}`} />
			{label && <span className={`svg-icon-label ${labelClassName}`}>{label}</span>}
		</div>
	);
//	console.log(ReactDOMServer.renderToStaticMarkup(iconMock));
	return iconMock;
};
export default SVGIcon;
