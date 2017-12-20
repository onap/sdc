import React from 'react';
import {storiesOf, action} from '@kadira/storybook';
import ListEditorView from './ListEditorView.jsx';
import ListEditorItemView from './ListEditorItemView.jsx';
import ListEditorItemField from './ListEditorItemViewField.jsx';
import {text, number} from '@kadira/storybook-addon-knobs';
import {withKnobs} from '@kadira/storybook-addon-knobs';

function makeChildren({onEdit = false, onDelete = false} = {}) {
    return (
        [...Array(number('Items', 2)).keys()].map(index => (
            <ListEditorItemView 
                key={index} 
                onEdit={onEdit ? onEdit : undefined}
                onDelete={onDelete ? onDelete : undefined}>
				<ListEditorItemField>
                    <div>{text('field 1', 'Lorum Ipsum')}</div>
				</ListEditorItemField>
				<ListEditorItemField>
                    <div>{text('field 2', 'Lorum Ipsum')}</div>
				</ListEditorItemField>
            </ListEditorItemView>)
        )
    );
}

const stories = storiesOf('ListEditor', module);
stories.addDecorator(withKnobs);

stories
    .add('regular', () => (
        <ListEditorView title='List Editor'>
        {makeChildren()}
        </ListEditorView>
    ))
    .add('two columns', () => (
        <ListEditorView title='List Editor' twoColumns>
            {makeChildren()}
        </ListEditorView>
    ))
    .add('with add', () => (
        <ListEditorView title='List Editor' onAdd={action('onAdd')} plusButtonTitle='Add' twoColumns>
        {makeChildren()}
        </ListEditorView>
    ))
    .add('with delete', () => (
        <ListEditorView title='List Editor' onAdd={action('onAdd')} plusButtonTitle='Add' twoColumns>
        {makeChildren({onDelete: action('onDelete')})}
        </ListEditorView>
    ))
    .add('with edit', () => (
        <ListEditorView title='List Editor' onAdd={action('onAdd')} plusButtonTitle='Add' twoColumns>
        {makeChildren({onEdit: action('onEdit')})}
        </ListEditorView>
    ))
    .add('with edit and delete', () => (
        <ListEditorView title='List Editor' onAdd={action('onAdd')} plusButtonTitle='Add' twoColumns>
        {makeChildren({onDelete: action('onDelete'), onEdit: action('onEdit')})}
        </ListEditorView>
    ));
