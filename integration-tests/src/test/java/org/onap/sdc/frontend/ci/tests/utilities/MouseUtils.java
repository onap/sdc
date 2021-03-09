/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.sdc.frontend.ci.tests.utilities;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class MouseUtils {

    private MouseUtils() {

    }

    /**
     * Using Javascript, it creates a mouse pointer image that will follow the mouse pointer on screen, making the mouse visible to the user during
     * the test.
     *
     * @param webDriver the selenium webdriver
     * @see <a href="https://gist.github.com/primaryobjects/70087610d9aef0f4bddbe2101dda7649">github gist</a>
     * @see <a href="https://stackoverflow.com/questions/35867776/visualize-show-mouse-cursor-position-in-selenium-2-tests-for-example-phpunit/35867777#35867777">stack
     * overflow</a>
     */
    public static void enableMouseDebug(final WebDriver webDriver) {
        final String mousePointerScript = "// Create mouse following image.\n"
            + "var seleniumFollowerImg = document.createElement(\"img\");\n"
            + "\n"
            + "// Set image properties.\n"
            + "seleniumFollowerImg.setAttribute('src', 'data:image/png;base64,'\n"
            + "    + 'iVBORw0KGgoAAAANSUhEUgAAABQAAAAeCAQAAACGG/bgAAAAAmJLR0QA/4ePzL8AAAAJcEhZcwAA'\n"
            + "    + 'HsYAAB7GAZEt8iwAAAAHdElNRQfgAwgMIwdxU/i7AAABZklEQVQ4y43TsU4UURSH8W+XmYwkS2I0'\n"
            + "    + '9CRKpKGhsvIJjG9giQmliHFZlkUIGnEF7KTiCagpsYHWhoTQaiUUxLixYZb5KAAZZhbunu7O/PKf'\n"
            + "    + 'e+fcA+/pqwb4DuximEqXhT4iI8dMpBWEsWsuGYdpZFttiLSSgTvhZ1W/SvfO1CvYdV1kPghV68a3'\n"
            + "    + '0zzUWZH5pBqEui7dnqlFmLoq0gxC1XfGZdoLal2kea8ahLoqKXNAJQBT2yJzwUTVt0bS6ANqy1ga'\n"
            + "    + 'VCEq/oVTtjji4hQVhhnlYBH4WIJV9vlkXLm+10R8oJb79Jl1j9UdazJRGpkrmNkSF9SOz2T71s7M'\n"
            + "    + 'SIfD2lmmfjGSRz3hK8l4w1P+bah/HJLN0sys2JSMZQB+jKo6KSc8vLlLn5ikzF4268Wg2+pPOWW6'\n"
            + "    + 'ONcpr3PrXy9VfS473M/D7H+TLmrqsXtOGctvxvMv2oVNP+Av0uHbzbxyJaywyUjx8TlnPY2YxqkD'\n"
            + "    + 'dAAAAABJRU5ErkJggg==');\n"
            + "seleniumFollowerImg.setAttribute('id', 'selenium_mouse_follower');\n"
            + "seleniumFollowerImg.setAttribute('style', 'position: absolute; z-index: 99999999999; pointer-events: none;');\n"
            + "\n"
            + "// Add mouse follower to the web page.\n"
            + "document.body.appendChild(seleniumFollowerImg);\n"
            + "\n"
            + "document.onmousemove = function(e) {\n"
            + "  const mousePointer = document.getElementById('selenium_mouse_follower');\n"
            + "  mousePointer.style.left = e.pageX + 'px';\n"
            + "  mousePointer.style.top = e.pageY + 'px';\n"
            + "}";

        ((JavascriptExecutor) webDriver).executeScript(mousePointerScript);
    }

}
