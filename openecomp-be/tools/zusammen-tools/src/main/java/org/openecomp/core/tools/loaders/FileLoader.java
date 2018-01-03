package org.openecomp.core.tools.loaders;

import org.openecomp.core.tools.exceptions.AddContributorRuntimeException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileLoader<C> {

  private C fileContent;
  private String filePath;
  private AbstractFileLoader loader;

  private static final String UNABLE_TO_READ_FILE = "Unable to read file";
  private static final String UNABLE_TO_INIT_FILE_READER = "Unable to initial reader for file:%s";

  public FileLoader(String filePath, AbstractFileLoader loader) {
    this.filePath = filePath;
    this.loader = loader;
  }

  public void load() {
    fileContent = (C) loader.load(filePath);
  }

  public C get() {
    return fileContent;
  }


  public static class SimpleListFileLoader extends AbstractFileLoader<List<String>> {


    @Override
    List<String> read(FileReader fileReader) {
      List<String> list = new ArrayList<>();
      try (BufferedReader br = new BufferedReader(fileReader)) {
        String line;

        while ((line = br.readLine()) != null) {
          list.add(line);
        }
      } catch (
          IOException e)

      {
        throw new AddContributorRuntimeException(String.format(UNABLE_TO_READ_FILE)
            , e);
      }
      return list;
    }
  }

  public abstract static class AbstractFileLoader<T> {

    T load(String filePath) {
      try (FileReader reader = initReader(filePath)) {
        return read(reader);
      } catch (Exception e) {
        throw new AddContributorRuntimeException(String.format(UNABLE_TO_INIT_FILE_READER, filePath)
            , e);
      }
    }

    FileReader initReader(String filePath) throws FileNotFoundException {
      return new FileReader(filePath);
    }

    abstract T read(FileReader fileReader);
  }


}
