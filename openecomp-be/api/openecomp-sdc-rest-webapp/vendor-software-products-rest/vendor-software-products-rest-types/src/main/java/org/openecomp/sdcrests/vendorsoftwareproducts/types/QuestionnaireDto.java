package org.openecomp.sdcrests.vendorsoftwareproducts.types;

import java.util.HashMap;

/**
 * Created by ayalaben on 9/26/2017
 */
import lombok.Data;

@Data
public class QuestionnaireDto {

  private String id;

  public HashMap<String, String> getQuestionareData() {
    return questionareData;
  }

  private HashMap<String,String> questionareData;


}
