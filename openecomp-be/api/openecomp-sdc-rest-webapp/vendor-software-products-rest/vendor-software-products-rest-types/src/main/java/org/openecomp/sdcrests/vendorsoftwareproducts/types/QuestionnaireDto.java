package org.openecomp.sdcrests.vendorsoftwareproducts.types;

import java.util.HashMap;

/**
 * Created by ayalaben on 9/26/2017
 */
public class QuestionnaireDto {

  private String id;

  public HashMap<String, String> getQuestionareData() {
    return questionareData;
  }

  public void setQuestionareData(HashMap<String, String> questionareData) {
    this.questionareData = questionareData;
  }

  private HashMap<String,String> questionareData;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

}
