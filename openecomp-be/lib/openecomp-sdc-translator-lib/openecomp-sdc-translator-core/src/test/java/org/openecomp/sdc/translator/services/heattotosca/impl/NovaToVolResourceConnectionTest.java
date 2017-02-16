package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author avrahamg
 * @since July 26, 2016
 */
public class NovaToVolResourceConnectionTest extends BaseResourceTranslationTest {
  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testNovaToVolumeConnectionMultiConnection() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/novatovolumeconnection/multiconnection/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/multiconnection/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testNovaToVolumeConnectionMultiNotCreatedIfVolPorpertyInVolAttacheIsNotAReferenceToVolume()
      throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/multinotconnected/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/multinotconnected/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testNovaToVolumeConnectionNestedNotCreatedIfVolPorpertyInVolAttacheIsNotAReferenceToVolume()
      throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/nestednotconnected/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/nestednotconnected/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateNovaToVolumeNestedConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/nestedconnection/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/nestedconnection/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateNovaToVolumeSharedNestedConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/sharednestedconnection/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/sharednestedconnection/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateNovaToVolumeSharedNestedNotCreatedIfVolPorpertyInVolAttacheIsNotAReferenceToVolume()
      throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/sharednestednotconnected/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/sharednestednotconnected/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateNovaToVolumeInnerNestedConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/innernestedconnection/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/innernestedconnection/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

}