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
import classnames from 'classnames';

import VersionController from 'nfvo-components/panel/versionController/VersionController.jsx';
import NavigationSideBar from 'nfvo-components/panel/NavigationSideBar.jsx';

export default class TabulatedEditor extends React.Component {

	render() {
		const {navigationBarProps, onToggle, onVersionSwitching, onMoreVersionsClick, onCreate, onSave, onClose,
				onVersionControllerAction, onNavigate, children, meta, onManagePermissions, onOpenCommentCommitModal, onOpenPermissions, onOpenRevisionsModal} = this.props;
		let {versionControllerProps} = this.props;
		const {className = ''} = React.Children.only(children).props;
		const child = this.prepareChild();

		if(onClose) {
			versionControllerProps = {
				...versionControllerProps,
				onClose: () => onClose(versionControllerProps)
			};
		}
		return (
			<div className='software-product-view'>
				<div className='software-product-navigation-side-bar'>
					<NavigationSideBar {...navigationBarProps} onSelect={onNavigate} onToggle={onToggle}/>
				</div>
				<div className='software-product-landing-view-right-side flex-column'>
					<VersionController
						{...versionControllerProps}
						onVersionSwitching={version => onVersionSwitching(version, meta)}
						onMoreVersionsClick={onMoreVersionsClick}
						onManagePermissions={onManagePermissions}
						onOpenCommentCommitModal={onOpenCommentCommitModal}
						onOpenPermissions={onOpenPermissions}
						onOpenRevisionsModal={onOpenRevisionsModal}
						callVCAction={(action, version, comment) => onVersionControllerAction(action, version, comment, meta)}
						onCreate={onCreate && this.handleCreate}
						onSave={onSave && this.handleSave}/>
					<div className={classnames('content-area', `${className}`)}>
					{
						child
					}
					</div>
				</div>
			</div>
		);
	}

	prepareChild() {
		const {onSave, onCreate, children} = this.props;

		const additionalChildProps = {ref: 'editor'};
		if (onSave) {
			additionalChildProps.onSave = onSave;
		}
		if (onCreate) {
			additionalChildProps.onCreate = onCreate;
		}

		const child = React.cloneElement(React.Children.only(children), additionalChildProps);
		return child;
	}



	handleSave = () => {
		const childInstance = this.refs.editor.getWrappedInstance();
		if (childInstance.save) {
			return childInstance.save();
		} else {
			return this.props.onSave();
		}
	};

	handleCreate = () => {
		const childInstance = this.refs.editor.getWrappedInstance();
		if (childInstance.create) {
			childInstance.create();
		} else {
			this.props.onCreate();
		}
	}
}
