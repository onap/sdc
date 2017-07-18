package org.openecomp.sdcrests.vendorsoftwareproducts.types;

import org.openecomp.sdcrests.vendorsoftwareproducts.types.validation.ValidateString;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class VmSizing {
  @Min(value = 1, message = "should be integer and > 0")
  @Max(value = 16, message = "should be integer and <= 16")
  private int numOfCPUs;
  @Min(value = 1, message = "should be integer and > 0")
  private int fileSystemSizeGB;
  @Min(value = 1, message = "should be integer and > 0")
  private int persistentStorageVolumeSize;
  @Min(value = 1, message = "should be integer and > 0")
  private int ioOperationsPerSec;
  @ValidateString(acceptedValues = {"1:1", "4:1", "16:1"}, message = "doesn't meet the expected "
      + "attribute value.")
  private String cpuOverSubscriptionRatio;
  @ValidateString(acceptedValues = {"1", "2", "4", "8"}, message = "doesn't meet the expected "
      + "attribute value.")
  private String memoryRAM;

  public int getNumOfCPUs() {
    return numOfCPUs;
  }

  public void setNumOfCPUs(int numOfCPUs) {
    this.numOfCPUs = numOfCPUs;
  }

  public int getFileSystemSizeGB() {
    return fileSystemSizeGB;
  }

  public void setFileSystemSizeGB(int fileSystemSizeGB) {
    this.fileSystemSizeGB = fileSystemSizeGB;
  }

  public int getPersistentStorageVolumeSize() {
    return persistentStorageVolumeSize;
  }

  public void setPersistentStorageVolumeSize(int persistentStorageVolumeSize) {
    this.persistentStorageVolumeSize = persistentStorageVolumeSize;
  }

  public int getIoOperationsPerSec() {
    return ioOperationsPerSec;
  }

  public void setIoOperationsPerSec(int ioOperationsPerSec) {
    this.ioOperationsPerSec = ioOperationsPerSec;
  }

  public String getCpuOverSubscriptionRatio() {
    return cpuOverSubscriptionRatio;
  }

  public void setCpuOverSubscriptionRatio(String cpuOverSubscriptionRatio) {
    this.cpuOverSubscriptionRatio = cpuOverSubscriptionRatio;
  }

  public String getMemoryRAM() {
    return memoryRAM;
  }

  public void setMemoryRAM(String memoryRAM) {
    this.memoryRAM = memoryRAM;
  }
}
