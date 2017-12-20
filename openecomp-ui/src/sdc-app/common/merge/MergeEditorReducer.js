/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import {actionTypes} from './MergeEditorConstants.js';

export default (state = [], action) => {
	switch (action.type) {
		case actionTypes.LOAD_CONFLICT: {
			let cdata = {...action.data};
			// let data = state.conflicts ? {...state.conflicts.data} : {} ;
			// data[cdata.id] = cdata;
			let conflicts = state.conflicts ? {...state.conflicts} : {};
			conflicts[cdata.id] = cdata;
			return {
				...state,
				conflicts
			};
		}
		case actionTypes.DATA_PROCESSED: {
			let conflicts = {...state.conflicts};
			let {data} = action;
			if (data && data.cid) {
				let yours = {...conflicts[data.cid].yours};
				let theirs = {...conflicts[data.cid].theirs};
				let {yoursField, theirsField} = data;
				if (yoursField) {
					yours[yoursField.name] = yoursField.value;
					conflicts[data.cid].yours = yours;
				}
				if (theirsField) {
					theirs[theirsField.name] = theirsField.value;
					conflicts[data.cid].theirs = theirs;
				}
			}
			return {
				...state,
				conflicts: {
					...conflicts
				}
			};
		}
		case actionTypes.LOAD_CONFLICTS:
			let conflictFiles = [];
			if (action.data) {
				conflictFiles = [...action.data.conflictInfoList];
			}
			return {
				inMerge: conflictFiles.length > 0,
				conflictFiles
			};
		default:
			return state;
	}
};
