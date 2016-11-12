package resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lenovo on 2016/11/12.
 */
public class LocalFileResource implements Resource {
    private final String fileName;

    public LocalFileResource(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(fileName);
    }
}
