import React from 'react';

const ShowMore = ({children}) => {
	if (children.length > 50) {
		return (<div>Show Message With More Mock</div>);
	} else {
		return (<div>Show Message Mock</div>);
	}
};
export default ShowMore;