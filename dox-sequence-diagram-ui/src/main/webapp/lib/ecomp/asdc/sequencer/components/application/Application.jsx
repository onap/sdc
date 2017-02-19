
import React from 'react';

import Common from '../../common/Common';
import Logger from '../../common/Logger';
import Diagram from '../diagram/Diagram';
import Dialog from '../dialog/Dialog';
import Editor from '../editor/Editor';
import Export from '../export/Export';
import Overlay from '../overlay/Overlay';

/**
 * Application controller, also a view.
 */
export default class Application extends React.Component {

  /**
   * Construct application view.
   * @param props element properties.
   * @param context react context.
   */
  constructor(props, context) {
    super(props, context);

    this.sequencer = Common.assertNotNull(props.sequencer);
    this.model = this.sequencer.getModel();
    this.metamodel = this.sequencer.getMetamodel();
    this.options = props.options;
    Logger.setLevel(this.options.unwrap().log.level);

    // Bindings.

    this.showInfoDialog = this.showInfoDialog.bind(this);
    this.showEditDialog = this.showEditDialog.bind(this);
    this.showConfirmDialog = this.showConfirmDialog.bind(this);
    this.hideOverlay = this.hideOverlay.bind(this);
    this.onMouseMove = this.onMouseMove.bind(this);
    this.onMouseUp = this.onMouseUp.bind(this);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get application options.
   * @returns JSON options, see Options.js.
   */
  getOptions() {
    return this.options.unwrap();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Set diagram name.
   * @param n diagram (human-readable) name.
   */
  setName(n) {
    this.diagram.setName(n);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Set diagram model.
   * @param model diagram instance.
   */
  setModel(model) {

    Common.assertNotNull(model);

    this.model = model;

    if (this.editor) {
      this.editor.render();
    }

    if (this.diagram) {
      this.diagram.render();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get Model wrapper.
   * @returns Model.
   */
  getModel() {
    return this.model;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get SVG element.
   * @returns {*}
   */
  getSVG() {
    return this.diagram.getSVG();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get top-level widget. Provides the demo toolbar with access to the public API.
   * @returns {*}
   */
  getSequencer() {
    return this.sequencer;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Present info dialog.
   * @param msg info message.
   */
  showInfoDialog(msg) {
    this.dialog.showInfoDialog(msg);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Present error dialog.
   * @param msg error message.
   */
  showErrorDialog(msg) {
    this.dialog.showErrorDialog(msg);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Present confirmation dialog.
   * @param msg info message.
   * @param cb callback function to be invoked on OK.
   */
  showConfirmDialog(msg, cb) {
    Common.assertType(cb, 'Function');
    this.dialog.showConfirmDialog(msg, cb);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Present edit (textarea) dialog.
   * @param msg prompt.
   * @param text current edit text.
   * @param cb callback function to be invoked on OK, taking the updated text
   * as an argument.
   */
  showEditDialog(msg, text, cb) {
    this.dialog.showEditDialog(msg, text, cb);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Select lifeline by ID.
   * @param id lifeline ID.
   */
  selectLifeline(id) {
    if (this.editor) {
      this.editor.selectLifeline(id);
    }
    if (this.diagram) {
      this.diagram.selectLifeline(id);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Select message by ID.
   * @param id message ID.
   */
  selectMessage(id) {
    if (this.editor) {
      this.editor.selectMessage(id);
    }
    if (this.diagram) {
      this.diagram.selectMessage(id);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * (Re)render just the diagram.
   */
  renderDiagram() {
    this.diagram.redraw();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Show overlay between application and modal dialog.
   */
  showOverlay() {
    if (this.overlay) {
      this.overlay.setVisible(true);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Hide overlay between application and modal dialog.
   */
  hideOverlay() {
    if (this.overlay) {
      this.overlay.setVisible(false);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Capture mouse move events, for resize.
   * @param event move event.
   */
  onMouseMove(event) {
    if (this.editor) {
      this.editor.onMouseMove(event);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Propagate mouse event to the editor that manages the resize.
   */
  onMouseUp() {
    if (this.editor) {
      this.editor.onMouseUp();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Render current model state.
   */
  render() {

    return (

      <div className="asdcs-control" onMouseMove={this.onMouseMove} onMouseUp={this.onMouseUp}>

        <Editor application={this} ref={(r) => { this.editor = r; }} />
        <Diagram application={this} ref={(r) => { this.diagram = r; }} />
        <Dialog application={this} ref={(r) => { this.dialog = r; }} />
        <Export />
        <Overlay application={this} ref={(r) => { this.overlay = r; }} />

      </div>
    );
  }

}

/** React properties. */
Application.propTypes = {
  options: React.PropTypes.object.isRequired,
  sequencer: React.PropTypes.object.isRequired,
};
