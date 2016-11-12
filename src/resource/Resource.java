package resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lenovo on 2016/11/12.
 */
public interface Resource{
    InputStream getInputStream()throws IOException;
}
