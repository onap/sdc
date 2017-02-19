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
import Application from './components/application/Application';
import Common from './common/Common';
import Options from './common/Options';
import Model from './model/Model';
import Metamodel from './model/Metamodel';
import Metamodels from './model/Metamodels';
import Scenarios from './model/demo/scenarios/Scenarios';
import '../../../../res/sdc-sequencer.scss';
/**
 * ASDC Sequencer entry point.
 */
export default class Sequencer extends React.Component {

  // //////////////////////////////////////////////////////////////////////////////////////////////

  constructor(props, context) {
    super(props, context);


    this.setMetamodel.bind(this);
    this.setModel.bind(this);
    this.getModel.bind(this);
    this.getMetamodel.bind(this);
    this.getSVG.bind(this);
    this.getDemoScenarios.bind(this);
    this.newModel.bind(this);

    // Parse options.

    this.options = new Options(props.options);

    // Default scenarios.

    const scenarios = this.getDemoScenarios();
    this.setMetamodel(scenarios.getMetamodels());

    // this.setModel(scenarios.getBlank());
    this.setModel(scenarios.getDimensions());
    // this.setModel(scenarios.getECOMP());

  }

  // //////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Optionally save known metamodels so that subsequent loading and unloading
   * of models needn't include the corresponding metamodel.
   * @param metamodels array of conformant metamodel JSON definitions.
   * @return this.
   */
  setMetamodel(metamodels) {
    Common.assertType(metamodels, 'Array');
    this.metamodels = new Metamodels(metamodels);
    return this;
  }

  // //////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Set current diagram.
   * @param modelJSON JSON diagram spec.
   * @param metamodelIdOrDefinition optional metamodel definition or reference. Defaults to
   * the model's metadata @ref, or the default (permissive) metamodel.
   * @return this.
   */
  setModel(modelJSON, metamodelIdOrDefinition) {
    Common.assertType(modelJSON, 'Object');
    const ref = (modelJSON.metadata) ? modelJSON.metadata.ref : undefined;
    const metamodel = this.getMetamodel(metamodelIdOrDefinition || ref);
    Common.assertInstanceOf(metamodel, Metamodel);
    this.model = new Model(modelJSON, metamodel);
    if (this.application) {
      this.application.setModel(this.model);
    }
    return this;
  }

  // //////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get current diagram state. At any given instant the diagram might not make *sense*
   * but it should always be syntactically valid.
   * @return current Model.
   */
  getModel() {

    if (this.application) {
      const model = this.application.getModel();
      if (model) {
        return model.unwrap();
      }
    }

    return this.model;
  }

  // //////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Extract SVG element.
   * @return stringified SVG element.
   */
  getSVG() {
    return this.application.getSVG();
  }

  // //////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get demo scenarios, allowing initialization in demo mode from the outside.
   * @returns {Scenarios}
   */
  getDemoScenarios() {
    return new Scenarios();
  }

  // //////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Create new model.
   * @param metamodelIdOrDefinition
   * @return newly-created model.
   */
  newModel(metamodelIdOrDefinition) {
    const metamodel = this.getMetamodel(metamodelIdOrDefinition);
    Common.assertInstanceOf(metamodel, Metamodel);
    const model = new Model({}, metamodel);
    if (this.application) {
      this.application.setModel(model);
    }
    return model;
  }

  // //////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get Metamodel instance corresponding to an ID or JSON definition.
   * @param metamodelIdOrDefinition String ID or JSON definition.
   * @returns Metamodel instance.
   * @private
   */
  getMetamodel(metamodelIdOrDefinition) {
    const metamodelType = Common.getType(metamodelIdOrDefinition);
    if (metamodelType === 'Object') {
      return new Metamodel(metamodelIdOrDefinition);
    }
    return this.metamodels.getMetamodelOrDefault(metamodelIdOrDefinition);
  }

  // //////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Render current diagram state.
   */
  render() {

    if (this.props.model) {

      // If a model was specified as a property, apply it. Otherwise
      // fall back to the demo model.

      const scenarios = this.getDemoScenarios();
      const metamodel = [scenarios.getBlankMetamodel(), scenarios.getECOMPMetamodel()];
      if (this.props.metamodel) {
        metamodel.push(this.props.metamodel);
      }
      this.setMetamodel(metamodel);
      this.setModel(this.props.model);
    }

    return (
      <Application options={this.options} sequencer={this} ref={(a) => { this.application = a; }} />
    );
  }

}

Sequencer.propTypes = {
  options: React.PropTypes.object.isRequired,
  model: React.PropTypes.object,
  metamodel: React.PropTypes.object,
};
