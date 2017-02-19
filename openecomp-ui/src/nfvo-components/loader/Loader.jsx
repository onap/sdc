import React from 'react';
import {connect} from 'react-redux';

const mapStateToProps = ({loader}) => {
	return {
		isLoading: loader.isLoading
	};
};

class Loader extends React.Component {

	static propTypes = {
		isLoading: React.PropTypes.bool.isRequired
	};

	static defaultProps = {
		isLoading: false
	};

	render() {
		let {isLoading} = this.props;

		return (
			<div className='onboarding-loader'>
				{
					isLoading && <div className='onboarding-loader-backdrop'>
						<div className='tlv-loader large'></div>
					</div>
				}
			</div>
		);
	}
}

export default connect(mapStateToProps) (Loader);
