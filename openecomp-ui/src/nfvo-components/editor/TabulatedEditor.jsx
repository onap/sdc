import React from 'react';
import classnames from 'classnames';

import VersionController from 'nfvo-components/panel/versionController/VersionController.jsx';
import NavigationSideBar from 'nfvo-components/panel/NavigationSideBar.jsx';

export default class TabulatedEditor extends React.Component {

	render() {
		const {versionControllerProps, navigationBarProps, onToggle, onVersionSwitching, onCreate, onSave, onClose, onVersionControllerAction, onNavigate, children} = this.props;
		const {className = ''} = React.Children.only(children).props;
		const child = this.prepareChild();

		return (
			<div className='software-product-view'>
				<div className='software-product-navigation-side-bar'>
					<NavigationSideBar {...navigationBarProps} onSelect={onNavigate} onToggle={onToggle}/>
				</div>
				<div className='software-product-landing-view-right-side flex-column'>
					<VersionController
						{...versionControllerProps}
						onVersionSwitching={version => onVersionSwitching(version)}
						callVCAction={onVersionControllerAction}
						onCreate={onCreate && this.handleCreate}
						onSave={onSave && this.handleSave}
						onClose={() => onClose(versionControllerProps)}/>
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
