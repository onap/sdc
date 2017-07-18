package org.openecomp.sdc.vendorsoftwareproduct.types.composition;

public class VmSizing {
  private int numOfCPUs;
  private int fileSystemSizeGB;
  private int persistentStorageVolumeSize;
  private int ioOperationsPerSec;
  private String cpuOverSubscriptionRatio;
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
