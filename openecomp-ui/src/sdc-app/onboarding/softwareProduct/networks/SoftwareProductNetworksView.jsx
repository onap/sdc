import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';

import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';

class SoftwareProductNetworksView extends React.Component {

	static propTypes = {
		networksList: React.PropTypes.arrayOf(React.PropTypes.shape({
			id: React.PropTypes.string.isRequired,
			name: React.PropTypes.string.isRequired,
			dhcp: React.PropTypes.bool.isRequired
		})).isRequired
	};

	state = {
		localFilter: ''
	};

	render() {
		const {localFilter} = this.state;

		return (
			<div className='vsp-networks-page'>
				<ListEditorView
					title={i18n('Networks')}
					filterValue={localFilter}
					placeholder={i18n('Filter Networks')}
					onFilter={filter => this.setState({localFilter: filter})}>
					{this.filterList().map(network => this.renderNetworksListItem(network))}
				</ListEditorView>
			</div>
		);
	}

	renderNetworksListItem(network) {
		let {id, name, dhcp} = network;
		return (
			<ListEditorItemView
				key={id}
				className='list-editor-item-view'
				isReadOnlyMode={true}>

				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Name')}</div>
					<div className='name'>{name}</div>
				</div>
				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('DHCP')}</div>
					<div className='artifact-name'>{dhcp ? i18n('YES') : i18n('NO')}</div>
				</div>
			</ListEditorItemView>
		);
	}

	filterList() {
		let {networksList} = this.props;

		let {localFilter} = this.state;
		if (localFilter.trim()) {
			const filter = new RegExp(escape(localFilter), 'i');
			return networksList.filter(({name = ''}) => {
				return escape(name).match(filter);
			});
		}
		else {
			return networksList;
		}
	}
}

export default SoftwareProductNetworksView;
