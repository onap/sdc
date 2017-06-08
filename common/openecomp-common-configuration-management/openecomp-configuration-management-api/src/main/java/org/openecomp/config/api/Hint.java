package org.openecomp.config.api;

public enum Hint {

  DEFAULT(0b0), LATEST_LOOKUP(0b1), EXTERNAL_LOOKUP(0b10), NODE_SPECIFIC(0b100);

  private final int hint;

  private Hint(int hnt) {
    hint = hnt;
  }

  public int value() {
    return hint;
  }


}
