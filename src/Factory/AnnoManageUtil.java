package Factory;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
//import java.lang.reflect.Method;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import test.Autowired;

/**
 * Created by wzj on 2016/10/1.
 */
public final class AnnoManageUtil
{
    /**
     * ��ȡ��ǰ��·����ָ����Controllerע�����͵��ļ�
     * @param packageName ����
     * @param annotation ע������
     * @return �ļ�
     */
    public static  List<Class<?>> getPackageController(String packageName, Class<? extends Annotation> annotation)
    {
        List<Class<?>> classList = new ArrayList<Class<?>>();

        String packageDirName = packageName.replace('.', '/');

        Enumeration<URL> dirs = null;

        //��ȡ��ǰĿ¼��������е���Ŀ¼��url
        try
        {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        while (dirs.hasMoreElements())
        {
            URL url = dirs.nextElement();

            //�õ���Ǯurl������
            String protocol = url.getProtocol();

            //�����ǰ�������ļ�����
            if ("file".equals(protocol))
            {
                //��ȡ��������·��
                String filePath = null;
                try
                {
                    filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                }
                catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }

                filePath = filePath.substring(1);
                getFilePathClasses(packageName,filePath,classList,annotation);
            }
        }


            return classList;
    }

    /**
     * ��ָ���İ������ҵ��ļ���
     * @param packageName
     * @param filePath
     * @param classList
     * @param annotation ע������
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     */
    private static void getFilePathClasses(String packageName,String filePath,List<Class<?>> classList,
                                           Class<? extends Annotation> annotation) {
        Path dir = Paths.get(filePath);

        DirectoryStream<Path> stream = null;
        try {
            //��õ�ǰĿ¼�µ��ļ���stream��
            stream = Files.newDirectoryStream(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Path path : stream) {
            String fileName = String.valueOf(path.getFileName());

            String className = fileName.substring(0, fileName.length() - 6);

            Class<?> classes = null;
            try {
                classes = Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            //�жϸ�ע�������ǲ�������Ҫ������
            //System.out.println(classes.isAnnotationPresent(annotation));
            Constructor[] Constructors = classes.getConstructors();
            for (Constructor constructor : Constructors) {
                if (constructor.getAnnotation(annotation) != null) {
                    classList.add(classes);
                    //return;
                }
            }
            if (null != classes && null != classes.getAnnotation(annotation)) {
                //������ļ�����classlist��
                if (!classList.contains(classes)) {
                    classList.add(classes);
                }

            }
        }
    }

}
