/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.vendorsoftwareproduct.utils;

import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipOutputStream;

/**
 * @author Avrahamg
 * @since November 08, 2016
 */
public class ZipFileUtils {
  private static final Logger log = (Logger) LoggerFactory.getLogger(ZipFileUtils.class.getName());
  public InputStream getZipInputStream(String name) {
    URL url = getClass().getResource(name);
    File templateDir = new File(url.getFile());

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(baos);

    VSPCommon.zipDir(templateDir, "", zos, true);
    try {
      zos.close();
    } catch (IOException exception) {
      log.debug("",exception);
    }
    return new ByteArrayInputStream(baos.toByteArray());
  }
}
