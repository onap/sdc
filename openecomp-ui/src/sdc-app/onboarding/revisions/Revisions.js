/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import {connect} from 'react-redux';
import RevisionsView from './RevisionsView.jsx';
import RevisionsActionHelper from './RevisionsActionHelper.js';

export const mapStateToProps = ({revisions, users}) => {
	return {
		revisions: revisions,
		users: users.usersList
	};
};

export const mapActionsToProps = (dispatch, {itemId, version, itemType}) => {
	return {
		onCancel: () => RevisionsActionHelper.closeRevisionsView(dispatch),
		onRevert: (revisionId) => {
			RevisionsActionHelper.revertToRevision(dispatch, {itemId, version, revisionId, itemType});
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(RevisionsView);
