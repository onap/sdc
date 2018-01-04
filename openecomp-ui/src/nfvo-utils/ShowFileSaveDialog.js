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

function getTimestampString() {
	let date = new Date();
	let z = n => n < 10 ? '0' + n : n;
	return `${date.getFullYear()}-${z(date.getMonth())}-${z(date.getDate())}_${z(date.getHours())}-${z(date.getMinutes())}`;
}


export default function showFileSaveDialog({blob, headers, defaultFilename, addTimestamp}) {
	let filename;
	let contentDisposition = headers['content-disposition'] ? headers['content-disposition'] : '';
	let match = contentDisposition ? contentDisposition.match(/filename=(.*?)(;|$)/) : false;
	if (match) {
		filename = match[1];
	} else {
		filename = defaultFilename;
	}

	if (addTimestamp) {
		filename = filename.replace(/(^.*?)\.([^.]+$)/, `$1_${getTimestampString()}.$2`);
	}

	let link = document.createElement('a');
	let url = URL.createObjectURL(blob);
	link.href = url;
	link.download = filename;
	link.style.display = 'none';
	document.body.appendChild(link);
	link.click();
	setTimeout(function(){
		document.body.removeChild(link);
		URL.revokeObjectURL(url);
	}, 0);
};
