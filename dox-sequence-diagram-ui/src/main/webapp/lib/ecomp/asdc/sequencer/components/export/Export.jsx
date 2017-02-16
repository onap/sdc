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

const Export = function Export() {
  return (
    <form className="asdcs-export" action="/ossui-svg/services/ossui/svg/export" method="post">
      <input name="svg" type="hidden" value="" />
      <input name="css" type="hidden" value="sdc/sequencer/default" />
      <input name="type" type="hidden" value="PDF" />
      <input name="height" type="hidden" value="1920" />
      <input name="width" type="hidden" value="1080" />
    </form>
  );
};

export default Export;
