/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.core.utilities;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class CommonMethodsTest {

  private static final String[] ARRAY = { "A", "B", "C" };

  @Test
  public void testPrintStackTrace() {

    String trace = CommonMethods.printStackTrace();
    assertTrue(trace.contains("org.openecomp.core.utilities" +
        ".CommonMethods.printStackTrace(CommonMethods.java:"));
    assertTrue(trace.contains("org.openecomp.core.utilities" +
        ".CommonMethodsTest.testPrintStackTrace(CommonMethodsTest.java"));
  }

  @Test
  public void testArrayToCommaSeparatedString() {
    assertEquals(CommonMethods.arrayToCommaSeparatedString(ARRAY), "A,B,C");
  }

  @Test
  public void testArrayToCommaSeparatedStringEmpty() {
    assertEquals(CommonMethods.arrayToCommaSeparatedString(new String[0]), "");
  }

  @Test
  public void testArrayToCommaSeparatedStringNulls() {
    assertEquals(CommonMethods.arrayToCommaSeparatedString(new String[] { null, null }), "null,null");
  }

  @Test
  public void testArrayToCommaSeparatedStringEmptyStrings() {
    assertEquals(CommonMethods.arrayToCommaSeparatedString(new String[] { "", "" }), ",");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testArrayToCommaSeparatedStringNull() {
    CommonMethods.arrayToCommaSeparatedString(null);
  }

  @Test
  public void testArrayToSeparatedString() {
    assertEquals(CommonMethods.arrayToSeparatedString(ARRAY, '/'),"A/B/C");
  }

  @Test
  public void testArrayToSeparatedStringEmpty() {
    assertEquals(CommonMethods.arrayToSeparatedString(new String[0], '/'),"");
  }

  @Test
  public void testArrayToSeparatedStringNulls() {
    assertEquals(CommonMethods.arrayToSeparatedString(new String[] {null, null}, '/'),"null/null");
  }

  @Test
  public void testArrayToSeparatedStringEmptyStrings() {
    assertEquals(CommonMethods.arrayToSeparatedString(new String[] {"", ""}, '/'),"/");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testArrayToSeparatedStringNull() {
    CommonMethods.arrayToSeparatedString(null, '/');
  }

  @Test
  public void testCollectionToCommaSeparatedString() {
    assertEquals(CommonMethods.collectionToCommaSeparatedString(Arrays.asList(ARRAY)), "A,B,C");
  }

  @Test
  public void testCollectionToCommaSeparatedStringNulls() {
    assertEquals(CommonMethods.collectionToCommaSeparatedString(Arrays.asList(null, null)), "null,null");
  }

  @Test
  public void testCollectionToCommaSeparatedStringEmptyStrings() {
    assertEquals(CommonMethods.collectionToCommaSeparatedString(Arrays.asList("", "")), ",");
  }

  @Test
  public void testCollectionToCommaSeparatedStringEmtpy() {
    assertEquals(CommonMethods.collectionToCommaSeparatedString(Collections.emptySet()), "");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testCollectionToCommaSeparatedStringNull() {
    assertNull(CommonMethods.collectionToCommaSeparatedString(null));
  }

}
