/*
 * Copyright © 2019 iconectiv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.core.externaltesting.api;

import org.junit.Assert;
import org.junit.Test;

public class ErrorBodyTests {

  @Test
  public void testErrorBody() throws Exception {
    TestErrorBody b = new TestErrorBody();
    b.setCode(404);
    b.setDetail("detail");
    b.setTitle("title");

    Assert.assertEquals("code match", new Integer(404), b.getCode());
    Assert.assertEquals("code detail", "detail", b.getDetail());
    Assert.assertEquals("code title", "title", b.getTitle());

    TestErrorBody b2 = new TestErrorBody("title", 404, "detail");

    Assert.assertEquals("code match", new Integer(404), b2.getCode());
    Assert.assertEquals("code detail", "detail", b2.getDetail());
    Assert.assertEquals("code title", "title", b2.getTitle());

  }
}
