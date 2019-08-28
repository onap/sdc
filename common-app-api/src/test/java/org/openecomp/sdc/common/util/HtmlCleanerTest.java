package org.openecomp.sdc.common.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HtmlCleanerTest {

    @Test
    public void validateStripHtmlReturnsStringWithNoHtml() {
        final String testInput = "  <div>testing <p>element</p> &value</div> ";

        String result = HtmlCleaner.stripHtml(testInput);

        assertEquals(result, "  testing element &value ");
    }

    @Test
    public void validateStripHtmlReturnsInputIfIsEmpty() {
        final String testInput = "";

        String result = HtmlCleaner.stripHtml(testInput);

        assertEquals(result, testInput);
    }

    @Test
    public void validateStripHtmlReturnsInputIfTagsAreEmpty() {
        final String testInput = "<>emptyTags<>";

        String result = HtmlCleaner.stripHtml(testInput);

        assertEquals(result, testInput);
    }

    @Test
    public void validateStripHtmlReturnsStripedHtmlWithEscapeTrue() {
        final String testInput = "  <div>testing <p>element</p> &value</div> ";

        String result = HtmlCleaner.stripHtml(testInput, true);

        assertEquals(result, "  testing element &amp;value ");
    }
}
