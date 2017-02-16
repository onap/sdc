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

/**
 * Example scenarios, for development, testing and demos.
 */
export default class Scenarios {

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Construct scenarios; read model and metamodel templates.
   */
  constructor() {
    this.templates = {
      model: {
        ecomp: require('./model/ECOMP.json'),
        blank: require('./model/BLANK.json'),
        dimensions: require('./model/DIMENSIONS.json'),
      },
      metamodel: {
        ecomp: require('./metamodel/ECOMP.json'),
        blank: require('./metamodel/BLANK.json'),
      },
    };
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get ECOMP scenario.
   * @return ECOMP scenario JSON.
   */
  getECOMP() {
    return JSON.parse(JSON.stringify(this.templates.model.ecomp));
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get ECOMP scenario metamodel.
   * @return scenario metamodel JSON.
   */
  getECOMPMetamodel() {
    return JSON.parse(JSON.stringify(this.templates.metamodel.ecomp));
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get blank scenario.
   * @return blank scenario JSON.
   */
  getBlank() {
    return JSON.parse(JSON.stringify(this.templates.model.blank));
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get empty scenario metamodel.
   * @return empty metamodel JSON.
   */
  getBlankMetamodel() {
    return JSON.parse(JSON.stringify(this.templates.metamodel.blank));
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get scenario.
   * @return scenario JSON.
   */
  getDimensions() {
    return JSON.parse(JSON.stringify(this.templates.model.dimensions));
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get scenario metamodel.
   * @return metamodel JSON.
   */
  getDimensionsMetamodel() {
    return JSON.parse(JSON.stringify(this.templates.metamodel.blank));
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get demo metamodels.
   * @returns {*[]}
   */
  getMetamodels() {
    return [this.getBlankMetamodel(), this.getDimensionsMetamodel(), this.getECOMPMetamodel()];
  }
}
