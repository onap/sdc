/*!
 * Copyright © 2016-2017 European Support Limited
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
import PropTypes from 'prop-types';
import { DragSource, DropTarget } from 'react-dnd';

import Common from '../../../../../../common/Common';

import Icon from '../../../../../icons/Icon';
import iconHandle from '../../../../../../../../../../res/ecomp/asdc/sequencer/sprites/icons/handle.svg';
import iconDelete from '../../../../../../../../../../res/ecomp/asdc/sequencer/sprites/icons/delete.svg';

/**
 * LHS lifeline row view.
 */
class Lifeline extends React.Component {
    // ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Construct editor view.
     * @param props element properties.
     * @param context react context.
     */
    constructor(props, context) {
        super(props, context);

        this.state = {
            active: false,
            name: props.lifeline.name
        };

        const metamodel = Common.assertNotNull(this.props.metamodel).unwrap();
        this.canReorder = metamodel.diagram.lifelines.constraints.reorder;
        this.canDelete = metamodel.diagram.lifelines.constraints.delete;

        // Bindings.

        this.onChangeName = this.onChangeName.bind(this);
        this.onBlurName = this.onBlurName.bind(this);
        this.onClickDelete = this.onClickDelete.bind(this);
        this.onMouseEnter = this.onMouseEnter.bind(this);
        this.onMouseLeave = this.onMouseLeave.bind(this);
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handle name change.
     * @param event change event.
     */
    onChangeName(event) {
        this.setState({ name: event.target.value });
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handle name change.
     * @param event change event.
     */
    onBlurName(event) {
        const options = this.props.application.getOptions();
        const sanitized = Common.sanitizeText(
            event.target.value,
            options,
            'lifeline'
        );
        const props = {
            id: this.props.lifeline.id,
            name: sanitized
        };
        this.props.designer.updateLifeline(props);
        this.setState({ name: sanitized });
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handle lifeline delete.
     */
    onClickDelete() {
        this.props.designer.deleteLifeline(this.props.lifeline.id);
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handle mouseover event.
     */
    onMouseEnter() {
        this.setState({ active: true });
        this.props.designer.onMouseEnterLifeline(this.props.lifeline.id);
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handle mouseleave event.
     */
    onMouseLeave() {
        this.setState({ active: false });
        this.props.designer.onMouseLeaveLifeline(this.props.lifeline.id);
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get whether metadata permits reorder.
     * @returns true if reorderable.
     */
    isCanReorder() {
        return this.canReorder;
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get whether metadata permits delete.
     * @returns true if lifeline can be deleted.
     */
    isCanDelete() {
        return this.canDelete;
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * React render.
     * @returns {*}
     */
    render() {
        const id = this.props.lifeline.id;
        const activeClass = this.props.active === true ? 'asdcs-active' : '';
        const { connectDragSource, connectDropTarget } = this.props;
        return connectDragSource(
            connectDropTarget(
                <div
                    className={`asdcs-designer-lifeline ${activeClass}`}
                    data-id={id}
                    onMouseEnter={this.onMouseEnter}
                    onMouseLeave={this.onMouseLeave}>
                    <table className="asdcs-designer-layout asdcs-designer-lifeline-row1">
                        <tbody>
                            <tr>
                                <td>
                                    <div className="asdcs-designer-sort asdcs-designer-icon">
                                        <Icon glyph={iconHandle} />
                                    </div>
                                </td>
                                <td>
                                    <div className="asdcs-designer-lifeline-index">
                                        {this.props.lifeline.index}.
                                    </div>
                                </td>
                                <td>
                                    <div className="asdcs-designer-lifeline-name">
                                        <input
                                            type="text"
                                            className="asdcs-editable"
                                            placeholder="Unnamed"
                                            value={this.state.name}
                                            onChange={this.onChangeName}
                                            onBlur={this.onBlurName}
                                        />
                                    </div>
                                </td>
                                <td>
                                    <div
                                        className="asdcs-designer-delete asdcs-designer-icon"
                                        onClick={this.onClickDelete}>
                                        <Icon glyph={iconDelete} />
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            )
        );
    }
}

/**
 * Declare properties.
 */
Lifeline.propTypes = {
    application: PropTypes.object.isRequired,
    designer: PropTypes.object.isRequired,
    lifeline: PropTypes.object.isRequired,
    active: PropTypes.bool.isRequired,
    metamodel: PropTypes.object.isRequired,
    id: PropTypes.any.isRequired,
    index: PropTypes.number.isRequired,
    lifelines: PropTypes.object.isRequired,
    isDragging: PropTypes.bool.isRequired,
    connectDragSource: PropTypes.func.isRequired,
    connectDropTarget: PropTypes.func.isRequired
};

/** DND. */
const source = {
    beginDrag(props) {
        return {
            id: props.id,
            index: props.index
        };
    }
};

/** DND. */
const sourceCollect = function collection(connect, monitor) {
    return {
        connectDragSource: connect.dragSource(),
        isDragging: monitor.isDragging()
    };
};

/** DND. */
const target = {
    drop(props, monitor, component) {
        Common.assertNotNull(props);
        Common.assertNotNull(monitor);
        const decorated = component.getDecoratedComponentInstance();
        if (decorated) {
            const lifelines = decorated.props.lifelines;
            if (lifelines) {
                const dragIndex = monitor.getItem().index;
                const hoverIndex = lifelines.getHoverIndex();
                lifelines.onDrop(dragIndex, hoverIndex);
            }
        }
    },
    hover(props, monitor, component) {
        Common.assertNotNull(props);
        Common.assertNotNull(monitor);
        if (component) {
            const decorated = component.getDecoratedComponentInstance();
            if (decorated) {
                const lifelines = decorated.props.lifelines;
                if (lifelines) {
                    lifelines.setHoverIndex(decorated.props.index);
                }
            }
        }
    }
};

/** DND. */
function targetCollect(connect, monitor) {
    return {
        connectDropTarget: connect.dropTarget(),
        isOver: monitor.isOver()
    };
}
/* eslint-disable new-cap */
const wrapper1 = DragSource('lifeline', source, sourceCollect)(Lifeline);
export default DropTarget(['lifeline', 'lifeline-new'], target, targetCollect)(
    wrapper1
);
/* eslint-enable new-cap */
