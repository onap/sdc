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
import VersionList from './components/VersionList.jsx';
import PermissionsView from './components/PermissionsView.jsx';
import Tree from 'nfvo-components/tree/Tree.jsx';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import i18n from 'nfvo-utils/i18n/i18n.js';

class VersionsPage extends React.Component {
	state = {
		showExpanded : false
	}
	render() {
		let { versions, owner, contributors, currentUser, isCollaborator, itemName = '', viewers, onSelectVersion, onNavigateToVersion,
		onTreeFullScreen, onManagePermissions, onCreateVersion, selectedVersion, onModalNodeClick, isManual} = this.props;
		return (
			<div className='versions-page-view'>
				<div className='versions-page-title'>{i18n('Available Versions - {itemName}', {itemName: itemName})}</div>
				<PermissionsView
					owner={owner}
					contributors={contributors}
					viewers={viewers}
					currentUser={currentUser}
					isManual={isManual}
					onManagePermissions={onManagePermissions}/>
				<div className='versions-page-list-and-tree'>
					<div className='version-tree-wrapper'>
						<div className='version-tree-title-container'>
							<div className='version-tree-title'>{i18n('Version Tree')}</div>
							{this.state.showExpanded && <SVGIcon name='expand' className='version-tree-full-screen' onClick={() => onTreeFullScreen({
								name: 'versions-tree-popup',
								width: 798,
								height: 500,
								nodes: versions.map(version => ({id: version.id, name: version.name, parent: version.baseId || ''})),
								onNodeClick: (version) => onModalNodeClick({version}),
								selectedNodeId: selectedVersion,
								scrollable: true,
								toWiden: true
							})} />}
						</div>
						<Tree
							name={'versions-tree'}
							width={200}
							allowScaleWidth={false}
							nodes={versions.map(version => ({id: version.id, name: version.name, parent: version.baseId || ''}))}
							onNodeClick={version => onSelectVersion({version})}
							onRenderedBeyondWidth={() => {this.setState({showExpanded : true});}}
							selectedNodeId={selectedVersion}/>
					</div>
					<VersionList
						versions={versions}
						onSelectVersion={onSelectVersion}
						onNavigateToVersion={onNavigateToVersion}
						onCreateVersion={onCreateVersion}
						selectedVersion={selectedVersion}
						isCollaborator={isCollaborator} />
				</div>
			</div>
		);
	}
}

export default VersionsPage;
