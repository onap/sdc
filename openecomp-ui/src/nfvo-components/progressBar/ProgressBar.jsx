import React from 'react';

class ProgressBar extends React.Component {
	static propTypes = {
		label: React.PropTypes.string,
		now: React.PropTypes.string.isRequired
	}
	render() {
		let {label, now} = this.props;

		return(
			<div className='progress-bar-view'>
				<div className='progress-bar-outside'>
					<div style={{width: now + '%'}} className='progress-bar-inside'></div>
				</div>
				<div className='progress-bar-view-label'>{label}</div>
			</div>
		);
	}
}

export default ProgressBar;
