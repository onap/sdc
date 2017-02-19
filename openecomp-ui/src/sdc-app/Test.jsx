import React from 'react';
import Tabs from 'react-bootstrap/lib/Tabs.js';
import Tab from 'react-bootstrap/lib/Tab.js';
import Button from 'react-bootstrap/lib/Button.js';
import ButtonGroup from 'react-bootstrap/lib/ButtonGroup.js';
import DropdownButton from 'react-bootstrap/lib/DropdownButton.js';
import MenuItem from 'react-bootstrap/lib/MenuItem.js';

import Modal from 'nfvo-components/modal/Modal.jsx';
import ValidationForm from 'nfvo-components/input/validation/ValidationForm.jsx';
import ValidationInput from 'nfvo-components/input/validation/ValidationInput.jsx';
import ToggleInput from 'nfvo-components/input/ToggleInput.jsx';

export default class Test extends React.Component {

	render() {
		return (
			<div>
				<Tabs defaultActiveKey={2}>
					<Tab eventKey={1} title='Tab 1'>Tab 1 content</Tab>
					<Tab eventKey={2} title='Tab 2'>Tab 2 content</Tab>
					<Tab eventKey={3} title='Tab 3' disabled>Tab 3 content</Tab>
				</Tabs>
				<div style={{marginTop: 20, marginBottom: 20}}></div>
				<Button>Default</Button>
				<span style={{marginLeft: 20}}></span>
				<Button bsStyle='primary'>Primary</Button>
				<span style={{marginLeft: 20}}></span>
				<Button bsStyle='success'>Success</Button>
				<span style={{marginLeft: 20}}></span>
				<Button bsStyle='info'>Info</Button>
				<span style={{marginLeft: 20}}></span>
				<Button bsStyle='warning'>Warning</Button>
				<span style={{marginLeft: 20}}></span>
				<Button bsStyle='danger'>Danger</Button>
				<span style={{marginLeft: 20}}></span>
				<Button bsStyle='link'>Link</Button>
				<div style={{marginTop: 20, marginBottom: 20}}></div>
				<ButtonGroup>
					<Button>Left</Button>
					<Button>Middle</Button>
					<Button>Right</Button>
				</ButtonGroup>
				<div style={{marginTop: 20, marginBottom: 20}}></div>
				<DropdownButton title='title' id='dropdown-basic'>
					<MenuItem eventKey='1'>Action</MenuItem>
					<MenuItem eventKey='2'>Another action</MenuItem>
					<MenuItem eventKey='3' active>Active Item</MenuItem>
					<MenuItem divider/>
					<MenuItem eventKey='4'>Separated link</MenuItem>
				</DropdownButton>

				<div style={{marginTop: 20, marginBottom: 20}}></div>
				<Modal show={false}>
					<Modal.Header closeButton>
						<Modal.Title>Modal title</Modal.Title>
					</Modal.Header>

					<Modal.Body>
						One fine body...
					</Modal.Body>

					<Modal.Footer>
						<Button>Close</Button>
						<Button bsStyle='primary'>Save changes</Button>
					</Modal.Footer>

				</Modal>

				<div style={{marginTop: 20, marginBottom: 20}}></div>

				<ValidationForm>
					<ValidationInput
						type='text'
						label='Required'
						placeholder='Enter text'
						validations={{required: true}}/>
					<ValidationInput
						type='text'
						label='Text'
						placeholder='Enter text'
						validations={{required: true, minLength:5}}/>
					<ValidationInput
						type='email'
						label='Email Address'
						placeholder='Enter email'
						validations={{required: true, email: true}}/>
					<ValidationInput type='password' label='Password'/>
					<ValidationInput type='file' label='File' help='[Optional] Block level help text'/>
					<ValidationInput type='checkbox' label='Checkbox2' name='ziv'/>
					<ValidationInput type='radio' label='Radio' name='zzz'/>
					<ValidationInput type='select' label='Select' placeholder='select'>
						<option value='select'>select</option>
						<option value='other'>...</option>
					</ValidationInput>
					<ValidationInput type='select' label='Multiple Select' multiple>
						<option value='select'>select (multiple)</option>
						<option value='other'>...</option>
					</ValidationInput>
					<ValidationInput type='textarea' label='Text Area' placeholder='textarea'/>
					<ToggleInput value={true}/>
					<ToggleInput />
					<ToggleInput label='ziv' value={true}/>
					<ToggleInput label='ziv'/>
				</ValidationForm>
			</div>
		);
	}

	doSomething(a) {
		if (a) {
			this.doSomething2();
		}
		else {
			return 1;
		}
	}

	doSomething2() {
		return 2;
	}
}
