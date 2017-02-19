/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import emptyModel from './emptyModel.json';

function mergeLifelines(oldLifelines, newLifelines) {
	let oldLifelinesMap = new Map(oldLifelines.map(lifeline => [lifeline.id, lifeline]));
	let newLifelinesMap = new Map(newLifelines.map(lifeline => [lifeline.id, lifeline]));

	let updatedLifelines = oldLifelines.map(lifeline => {
		let newLifeline = newLifelinesMap.get(lifeline.id);
		return {
			...lifeline,
			name: newLifeline ? newLifeline.name : lifeline.name
		};
	});

	let addedLifelines = newLifelines.filter(lifeline => !oldLifelinesMap.has(lifeline.id));

	return [
		...updatedLifelines,
		...addedLifelines
	];
}


const SequenceDiagramModelHelper = Object.freeze({

	createModel(options) {
		return SequenceDiagramModelHelper.updateModel(emptyModel, options);
	},

	updateModel(model, options) {
		const diagram = model.diagram;
		const metadata = diagram.metadata || model.metadata;
		const id = options.id || metadata.id;
		const name = options.name || metadata.name;
		const lifelines = options.lifelines ? mergeLifelines(diagram.lifelines, options.lifelines) : diagram.lifelines;

		return {
			diagram: {
				...diagram,
				metadata: {
					...metadata,
					id,
					name
				},
				lifelines
			}
		};
	}
});

export default SequenceDiagramModelHelper;
