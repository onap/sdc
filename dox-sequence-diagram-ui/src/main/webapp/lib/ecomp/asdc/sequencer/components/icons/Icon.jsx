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

/**
 * Simple icon view.
 * @param glyph glyph definition, from import.
 * @param className optional classname, for svg element.
 * @returns {XML}
 * @constructor
 */
const Icon = function Icon({ glyph, className }) {
  return (
    <svg viewBox="0 0 1000 1000" className={className} >
      <use xlinkHref={glyph} className="asdcs-icon" />
    </svg>
  );
};

/** Declare properties. */
Icon.propTypes = {
  className: React.PropTypes.string,
  glyph: React.PropTypes.string.isRequired,
};

export default Icon;

