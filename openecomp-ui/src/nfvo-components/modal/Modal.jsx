import React from 'react';
import ReactDOM from 'react-dom';
import BootstrapModal from 'react-bootstrap/lib/Modal.js';

let nextModalId = 0;

export default class Modal extends React.Component {

	static Header = BootstrapModal.Header;

	static Title = BootstrapModal.Title;

	static Footer = BootstrapModal.Footer;

	static Body = class ModalBody extends React.Component {

		render() {
			let {children, ...props} = this.props;
			return (
				<BootstrapModal.Body {...props}>
					{children}
				</BootstrapModal.Body>
			);
		}

		componentDidMount() {
			let element = ReactDOM.findDOMNode(this);
			element.addEventListener('click', event => {
				if (event.target.tagName === 'A') {
					event.preventDefault();
				}
			});
			['wheel', 'mousewheel', 'DOMMouseScroll'].forEach(eventType =>
				element.addEventListener(eventType, event => event.stopPropagation())
			);
		}
	};

	componentWillMount() {
		this.modalId = `dox-ui-modal-${nextModalId++}`;
	}

	componentDidMount() {
		this.ensureRootClass();
	}

	componentDidUpdate() {
		this.ensureRootClass();
	}

	ensureRootClass() {
		let element = document.getElementById(this.modalId);
		while(element && !element.hasAttribute('data-reactroot')) {
			element = element.parentElement;
		}
		if (element && !element.classList.contains('dox-ui')) {
			element.classList.add('dox-ui');
		}
	}

	render() {
		let {children, ...props} = this.props;
		return (
			<BootstrapModal {...props} id={this.modalId}>
				{children}
			</BootstrapModal>
		);
	}
}
