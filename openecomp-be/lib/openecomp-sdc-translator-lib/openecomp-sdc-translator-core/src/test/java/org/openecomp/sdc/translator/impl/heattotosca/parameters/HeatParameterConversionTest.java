package org.openecomp.sdc.translator.impl.heattotosca.parameters;

import org.openecomp.sdc.translator.services.heattotosca.impl.BaseResourceTranslationTest;
import org.junit.Test;

public class HeatParameterConversionTest extends BaseResourceTranslationTest {

  {
    inputFilesPath = "/mock/heat/parameters/inputs";
    outputFilesPath = "/mock/heat/parameters/expectedoutputfiles";
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
  }


    /*

    private static final String MANIFEST_NAME = "MANIFEST.json";

    @Test
    public void testTranslate_parameters() throws IOException {



        HeatToToscaTranslator heatToToscaTranslator = HeatToToscaTranslatorFactory.getInstance().createInterface();
        URL url = this.getClass().getResource("/mock/heat/parameters");
        File manifestFile = new File(url.getPath());
        File[] files = manifestFile.listFiles();
        FileInputStream fis;
        byte[] fileContent;
        for (File file : files) {
            fis = new FileInputStream(file);
            fileContent = FileUtils.toByteArray(fis);
            if (file.getName().equals(MANIFEST_NAME)) {
                heatToToscaTranslator.addManifest(MANIFEST_NAME, new String(fileContent));
            } else {
                heatToToscaTranslator.addFile(file.getName(), fileContent);
            }
        }

        TranslatorOutput translatorOutput = heatToToscaTranslator.translate();
        Assert.assertNotNull(translatorOutput);
        File file = new File("Parameters.zip");
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(translatorOutput.getTranslationContent());
        fos.close();

    }
    */

}
