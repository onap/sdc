

import React from 'react';

import Icon from '../icons/Icon';
import iconQuestion from '../../../../../../res/ecomp/asdc/sequencer/sprites/icon/question.svg';
import iconExclaim from '../../../../../../res/ecomp/asdc/sequencer/sprites/icon/exclaim.svg';
import iconInfo from '../../../../../../res/ecomp/asdc/sequencer/sprites/icon/info.svg';
import iconEdit from '../../../../../../res/ecomp/asdc/sequencer/sprites/icon/edit.svg';
import iconClose from '../../../../../../res/ecomp/asdc/sequencer/sprites/icon/close.svg';

/**
 * Multi-purpose dialog. Rendered into the page on initialization, and then
 * configured, shown and hidden as required.
 */
export default class Dialog extends React.Component {

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Construct view.
   */
  constructor(props, context) {

    super(props, context);

    this.MODE = {
      INFO: {
        icon: 'asdcs-icon-info',
        heading: 'Information',
      },
      ERROR: {
        icon: 'asdcs-icon-exclaim',
        heading: 'Error',
      },
      EDIT: {
        icon: 'asdcs-icon-edit',
        heading: 'Edit',
        edit: true,
        confirm: true,
      },
      CONFIRM: {
        icon: 'asdcs-icon-question',
        heading: 'Confirm',
        confirm: true,
      },
    };

    this.state = {
      mode: this.MODE.INFO,
      message: '',
      text: '',
      visible: false,
    };

    // Bindings.

    this.onClickOK = this.onClickOK.bind(this);
    this.onClickCancel = this.onClickCancel.bind(this);
    this.onChangeText = this.onChangeText.bind(this);
    this.showConfirmDialog = this.showConfirmDialog.bind(this);
    this.showInfoDialog = this.showInfoDialog.bind(this);
    this.showEditDialog = this.showEditDialog.bind(this);
    this.showErrorDialog = this.showErrorDialog.bind(this);
    this.showDialog = this.showDialog.bind(this);

  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Show info dialog.
   * @param message info message.
   */
  showInfoDialog(message) {
    this.showDialog(this.MODE.INFO, { message });
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Show error dialog.
   * @param message error message.
   */
  showErrorDialog(message) {
    this.showDialog(this.MODE.ERROR, { message });
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Show edit dialog.
   * @param message dialog message.
   * @param text current edit text.
   * @param callback callback function to be invoked on OK.
   */
  showEditDialog(message, text, callback) {
    this.showDialog(this.MODE.EDIT, { message, text, callback });
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Show confirmation dialog.
   * @param message dialog message.
   * @param callback callback function to be invoked on OK.
   */
  showConfirmDialog(message, callback) {
    this.showDialog(this.MODE.CONFIRM, { message, callback });
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle buttonclick.
   */
  onClickOK() {
    this.props.application.hideOverlay();
    this.setState({ visible: false });
    if (this.callback) {

      // So far the only thing we can return is edit text, but send it back
      // as properties to allow for future return values.

      this.callback({ text: this.state.text });
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle buttonclick.
   */
  onClickCancel() {
    this.props.application.hideOverlay();
    this.setState({ visible: false });
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle text changes.
   * @param event update event.
   */
  onChangeText(event) {
    this.setState({ text: event.target.value });
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Show dialog in specified configuration.
   * @param mode dialog mode.
   * @param args dialog parameters, varying slightly by dialog type.
   * @private
   */
  showDialog(mode, args) {
    this.props.application.showOverlay();
    this.callback = args.callback;
    this.setState({
      mode,
      visible: true,
      message: args.message || '',
      text: args.text || '',
    });
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Render dialog into the page, initially hidden.
   */
  render() {

    const dialogClass = (this.state.visible) ? '' : 'asdcs-hidden';
    const cancelClass = (this.callback) ? '' : 'asdcs-hidden';
    const textClass = (this.state.mode === this.MODE.EDIT) ? '' : 'asdcs-hidden';

    return (
      <div className={`asdcs-dialog ${dialogClass}`}>
        <div className="asdcs-dialog-header">{this.state.mode.heading}</div>
        <div className="asdcs-dialog-close" onClick={this.onClickCancel} >
          <Icon glyph={iconClose} className={this.MODE.CONFIRM.icon} />
        </div>
        <div className={`asdcs-dialog-icon ${this.state.mode.icon}`}>
          <Icon glyph={iconQuestion} className={this.MODE.CONFIRM.icon} />
          <Icon glyph={iconExclaim} className={this.MODE.ERROR.icon} />
          <Icon glyph={iconInfo} className={this.MODE.INFO.icon} />
          <Icon glyph={iconEdit} className={this.MODE.EDIT.icon} />
        </div>
        <div className="asdcs-dialog-message">
          {this.state.message}
        </div>
        <div className={`asdcs-dialog-text ${textClass}`}>
          <textarea
            maxLength="255"
            value={this.state.text}
            onChange={this.onChangeText}
          />
        </div>
        <div className="asdcs-dialog-buttonbar">
          <button
            className={`asdcs-dialog-button-cancel ${cancelClass}`}
            onClick={this.onClickCancel}
          >
            Cancel
          </button>
          <button
            className="asdcs-dialog-button-ok"
            onClick={this.onClickOK}
          >
            OK
          </button>
        </div>
      </div>
    );
  }
}

Dialog.propTypes = {
  application: React.PropTypes.object.isRequired,
};
