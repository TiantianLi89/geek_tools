package tech.geekcity.open.geek.tools.common;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author ben.wangz
 */
public class ResourceUtils {

    public static String readResourceContent(String pathInResource) throws IOException {
        String filePath = getResourcePath(pathInResource);
        return FileUtils.readFileToString(new File(filePath));
    }

    public static String getResourcePath(String pathInResource) throws IOException {
        URL url = ResourceUtils.class
                .getClassLoader()
                .getResource(pathInResource);
        if (null == url) {
            throw new IOException(String.format("pathInResource(%s) not found", pathInResource));
        }
        return url.getPath();
    }
}