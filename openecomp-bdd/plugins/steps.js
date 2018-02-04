/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */
/**
 * @module plugins/steptag
 */
'use strict';


exports.handlers = {
	/**
	 * Support @step tag.
	 *
	 * @step description
	 */
	newDoclet: function(e) {
		var tags = e.doclet.tags;
		var tag;
		var value;

		// any user-defined tags in this doclet?
		if (typeof tags !== 'undefined') {

			tags = tags.filter(function($) {
				return $.title === 'step' || $.title === 'examplefile';
			});

			if (tags.length) {
				// take the first one
				tag = tags[0];
				let step = null;
				let exampleFile = null;
				for (tag in tags) {
					if (tags[tag].title === "step") {
						step = "<b>" + tags[tag].value + "</b><br>";
					}
					if (tags[tag].title === "examplefile") {
						exampleFile = "<i> Example Features File: " + tags[tag].value + "</i><br>";
					}
				}
				if (exampleFile !== null) {
					step += exampleFile;
				}
				e.doclet.meta = e.doclet.meta || {};
				if (e.doclet.description !== undefined) {
					e.doclet.description =  step +  e.doclet.description;
				} else {
					e.doclet.description =  step;
				}
			}
		}
	}
};