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
import ActivityLogView from './ActivityLogView.jsx';

export const mapStateToProps = ({users: {usersList}, licenseModel: {activityLog}}) => {

	let activities = activityLog;
	return {
		activities: activities.map(activity => ({...activity, user: {id: activity.user, name: usersList.find(userObject => userObject.userId === activity.user).fullName}})),
		usersList
	};
};

export default connect(mapStateToProps, undefined, null, {withRef: true})(ActivityLogView);
