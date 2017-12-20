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

import React from 'react';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger.js';
import Tooltip from 'react-bootstrap/lib/Tooltip.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';

const formatTime = (time) => {
	if (!time) { return ''; }

	const date = new Date(time);
	const options = {
		year: 'numeric',
		month: 'short',
		day: 'numeric',
		hour: '2-digit',
		minute: '2-digit'
	};
	const newDate = date.toLocaleTimeString('en-US', options);

	return newDate;
};

const DescriptionField = ({ className, text, useTooltip }) => {
	if (useTooltip) {
		return (
			<div className={className}>
				<OverlayTrigger
					placement='bottom'
					overlay={<Tooltip className='version-description-tooltip' id='version-description-tooltip'>{text}</Tooltip>}>
					<div className='description-text'>{text}</div>
				</OverlayTrigger>
			</div>
		);
	}
	return <div className={className}>{text}</div>;
};

const VersionListItem = ({ data, onSelectVersion, onNavigateToVersion, onCreateVersion, isHeader, isSelected, isCollaborator }) => {

	let {modificationTime, name, status, description, additionalInfo} = data;
	const modificationText = !isHeader ? formatTime(modificationTime) : i18n('Last Edited On');

	return (
		<div
			data-test-id='version-item-row'
			className={`version-item-row ${isHeader ? 'header-row' : 'clickable'} ${isSelected ? 'selected' : ''}`}
			onClick={e => {
				e.stopPropagation();
				onSelectVersion();
				onNavigateToVersion();
			}}>
			<div className={`version-item-field ${isHeader ? 'header-field item-version' : 'item-version'}`}>{name}</div>
			<div className={`version-item-field ${isHeader ? 'header-field item-status' : 'item-status'}`}>{status}</div>
			<div className={`version-item-field ${isHeader ? 'header-field' : 'item-last-edited'}`}>{modificationText}</div>
			<DescriptionField
				className={`version-item-field ${isHeader ? 'header-field header-description' : 'item-description'}`}
				useTooltip={!isHeader && description}
				text={description} />

				{
					isHeader ?
						<div className='version-item-field header-field actions'>{i18n('Actions')}</div>
					:
						<div className='version-item-field item-actions'>
							<div className='version-item-field item-select'>
								<SVGIcon
									name='check-circle'
									data-test-id='versions-page-select-version'
									onClick={e => {e.stopPropagation(); onNavigateToVersion();}}
									label={i18n('Go to this Version')}
									labelPosition='right' />
							</div>
							<div className='version-item-field item-create'>
								{!isHeader && isCollaborator && additionalInfo.OptionalCreationMethods.length > 0 &&
									<SVGIcon
										name='plus-circle'
										data-test-id='versions-page-create-version'
										onClick={e => { e.stopPropagation(); onCreateVersion(); }}
										label={i18n('Create New Version')}
										labelPosition='right'
										disabled={!isCollaborator} />
								}
							</div>
						</div>
				}


		</div>
	);

};

const VersionList = ({ versions, onSelectVersion, onNavigateToVersion, onCreateVersion, selectedVersion, isCollaborator }) => (
	<div className='version-list'>
		<VersionListItem
			data={{ name: i18n('Version'), status: i18n('Status'), description: i18n('Description') }}
			isHeader />
		<div className='version-list-items'>
			{versions.map(version =>
				<VersionListItem
					key={version.id}
					data={version}
					onSelectVersion={() => onSelectVersion({version})}
					onNavigateToVersion={() => onNavigateToVersion({version})}
					onCreateVersion={() => onCreateVersion({version})}
					isSelected={selectedVersion === version.id}
					isCollaborator={isCollaborator} />
			)}
		</div>
	</div>
);

export default VersionList;
