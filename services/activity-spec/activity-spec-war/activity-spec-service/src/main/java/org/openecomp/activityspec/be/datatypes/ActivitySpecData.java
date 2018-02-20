package org.openecomp.activityspec.be.datatypes;

import java.util.LinkedList;
import java.util.List;

public class ActivitySpecData {
  private List<ActivitySpecParameter> inputParameters = new LinkedList<>();
  private List<ActivitySpecParameter> outputParameters = new LinkedList<>();

  public List<ActivitySpecParameter> getOutputParameters() {
    return outputParameters;
  }

  public void setOutputParameters(List<ActivitySpecParameter> outputParameters) {
    this.outputParameters = outputParameters;
  }

  public List<ActivitySpecParameter> getInputParameters() {
    return inputParameters;
  }

  public void setInputParameters(List<ActivitySpecParameter> inputParameters) {
    this.inputParameters = inputParameters;
  }
}
