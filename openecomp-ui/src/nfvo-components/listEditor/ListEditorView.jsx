import React from 'react';
import FontAwesome from 'react-fontawesome';
import Input from 'react-bootstrap/lib/Input.js';


class ListEditorView extends React.Component {

	static defaultProps = {
		className: ''
	};

	static propTypes = {
		title: React.PropTypes.string,
		plusButtonTitle: React.PropTypes.string,
		children: React.PropTypes.node,
		filterValue: React.PropTypes.string,
		onFilter: React.PropTypes.func,
		className: React.PropTypes.string,
		isReadOnlyMode: React.PropTypes.bool,
		placeholder: React.PropTypes.string
	};

	render() {
		let {title, plusButtonTitle, onAdd, children, filterValue, onFilter, className, placeholder, isReadOnlyMode} = this.props;
		return (
			<div className={`list-editor-view ${className}`}>
				{title && onAdd && <div className='list-editor-view-title'>{title}</div>}
				<div className='list-editor-view-actions'>
					{title && !onAdd && <div className='list-editor-view-title-inline'>{title}</div>}
					<div className={`list-editor-view-add-controller${isReadOnlyMode ? ' disabled' : ''}`} >
						{ onAdd &&
							<div onClick={onAdd}>
								<span className='plus-icon-button pull-left'/>
								<span>{plusButtonTitle}</span>
							</div>
						}
					</div>

					{
						onFilter &&
							<div className='list-editor-view-search search-wrapper'>
								<Input
									ref='filter'
									type='text'
									value={filterValue}
									name='list-editor-view-search'
									placeholder={placeholder}
									groupClassName='search-input-control'
									onChange={() => onFilter(this.refs.filter.getValue())}/>
								<FontAwesome name='filter' className='filter-icon'/>
							</div>
					}
				</div>
				<div className='list-editor-view-list-scroller'>
					<div className='list-editor-view-list'>
						{children}
					</div>
				</div>
			</div>
		);
	}

}
export default ListEditorView;
