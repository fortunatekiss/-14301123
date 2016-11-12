package Factory;

import Bean.BeanDefinition;
import Bean.BeanUtil;
import Bean.PropertyValue;
import Bean.PropertyValues;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import resource.LocalFileResource;
import resource.Resource;
import test.Autowired;
import test.Component;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by lenovo on 2016/11/12.
 */
public class XMLBeanFactory extends AbstractBeanFactory{

    public XMLBeanFactory(String[] locations){
        handleComponentAnnotation();
        initxml(locations);
    }
    private void handleComponentAnnotation(){
        ScanPackage sp = new ScanPackage("test");
        try {
            List<String> list = sp.getFullyQualifiedClassNameList();
            Iterator<String> cIt = list.iterator();
            String cName;
            Class<?> c;
            while(cIt.hasNext()){
                cName = cIt.next();
                c = Class.forName(cName);
                if(c.isAnnotationPresent(Component.class)){
                    BeanDefinition beandef = new BeanDefinition();
                    String beanClassName = cName;
                    String beanName = c.getAnnotation(Component.class).value();
                    Object bean = c.newInstance();
                    beandef.setBeanClass(c);
                    beandef.setBeanClassName(beanClassName);
                    beandef.setBean(bean);
                    this.registerBeanDefinationWithClass(c, beandef);
                    this.registerBeanDefinition(beanName, beandef, true);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    private void initxml(String[] locations){
        Map<String,Set<String>> beanRefMap = new HashMap<String,Set<String>>();
        Map<String,Set<Class<?>>> beanAutowiredMap = new HashMap<String,Set<Class<?>>>();
        Map<String,BeanDefinition> unfinished = new HashMap<String,BeanDefinition>();
        Set<String> autoConsSet = new HashSet<String>();

        for(String loc : locations){
            Resource lfr = new LocalFileResource(loc);
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
                Document document = dbBuilder.parse(lfr.getInputStream());
                NodeList beanList = document.getElementsByTagName("bean");
                for(int i = 0 ; i < beanList.getLength(); i++)
                {
                    boolean flag = false;
                    Set<String> refSet = new HashSet<String>();
                    Set<Class<?>> autowiredSet = new HashSet<Class<?>>();
                    Node bean = beanList.item(i);
                    BeanDefinition beandef = new BeanDefinition();
                    String beanClassName = bean.getAttributes().getNamedItem("class").getNodeValue();
                    String beanName = bean.getAttributes().getNamedItem("id").getNodeValue();
                    beanRefMap.put(beanName, refSet);
                    beanAutowiredMap.put(beanName, autowiredSet);

                    beandef.setBeanClassName(beanClassName);
                    try {
                        Class<?> beanClass = Class.forName(beanClassName);
                        beandef.setBeanClass(beanClass);
                        Field[] fields = beanClass.getDeclaredFields();
                        for(Field f : fields){
                            if(f.isAnnotationPresent(Autowired.class)){
                                autowiredSet.add(f.getDeclaringClass());
                            }
                        }
                        Constructor<?>[] cons = beanClass.getConstructors();
                        for(Constructor<?> con : cons){
                            if(con.isAnnotationPresent(Autowired.class)){
                                flag = true;
                                autoConsSet.add(beanName);
                                break;
                            }
                        }

                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    PropertyValues propertyValues = new PropertyValues();
                    NodeList propertyList = bean.getChildNodes();
                    for(int j = 0 ; j < propertyList.getLength(); j++)
                    {
                        Node property = propertyList.item(j);
                        if (property instanceof Element) {
                            Element ele = (Element) property;
                            String name = ele.getAttribute("name");
                            Class<?> type;
                            if(ele.hasAttribute("ref")){
                                String ref = ele.getAttribute("ref");
                                refSet.add(ref);
                            } else{
                                try {
                                    type = beandef.getBeanClass().getDeclaredField(name).getType();
                                    Object value = ele.getAttribute("value");

                                    if(type == Integer.class)
                                    {
                                        value = Integer.parseInt((String) value);
                                    }

                                    propertyValues.AddPropertyValue(new PropertyValue(name,value));
                                } catch (NoSuchFieldException e) {
                                    e.printStackTrace();
                                } catch (SecurityException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    beandef.setPropertyValues(propertyValues);

                    this.registerBeanDefinationWithClass(beandef.getBeanClass(), beandef);

                    if(!flag){
                        this.registerBeanDefinition(beanName, beandef, false);
                    }else {
                        unfinished.put(beanName, beandef);
                    }
                }
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Iterator<String> atit = autoConsSet.iterator();
            while(atit.hasNext()){
                String bName = atit.next();
                BeanDefinition bd = unfinished.get(bName);
                Class<?> bClass;
                try {
                    bClass = Class.forName(bd.getBeanClassName());
                    Constructor<?>[] cons = bClass.getConstructors();
                    for(Constructor<?> con : cons){
                        if(con.isAnnotationPresent(Autowired.class)){
                            Class<?>[] types = con.getParameterTypes();
                            List<Object> bList = new ArrayList<Object>();
                            for(Class<?> t : types){
                                BeanDefinition bean = findBeanByClass(t);
                                bList.add(bean.getBean());
                            }
                            bd.setBean(con.newInstance(bList.toArray()));
                            this.registerBeanDefinition(bName, bd, true);
                            break;
                        }
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            Set<String> beanNames = beanRefMap.keySet();
            Iterator<String> it = beanNames.iterator();
            while(it.hasNext()){
                String beanName = it.next();
                Set<String> refSet = beanRefMap.get(beanName);
                Iterator<String> refit = refSet.iterator();
                while(refit.hasNext()){
                    String ref = refit.next();
                    Object refBean = this.getBean(ref);
                    Object bean = this.getBean(beanName);
                    String[] temp = refBean.getClass().getName().split("\\.");
                    System.out.print(temp[temp.length - 1]);
                    BeanUtil.invokeSetterMethod(bean, temp[temp.length-1], refBean);
                }
            }
        }
    }
//
//    public XMLBeanFactory(Resource resource)
//    {
//        try {
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
//            Document document = dbBuilder.parse(resource.getInputStream());
//            NodeList beanList = document.getElementsByTagName("bean");
//            for(int i = 0 ; i < beanList.getLength(); i++)
//            {
//                Node bean = beanList.item(i);
//                BeanDefinition beandef = new BeanDefinition();
//                String beanClassName = bean.getAttributes().getNamedItem("class").getNodeValue();
//                String beanName = bean.getAttributes().getNamedItem("id").getNodeValue();
//
//                beandef.setBeanClassName(beanClassName);
//
//                try {
//                    Class<?> beanClass = Class.forName(beanClassName);
//                    beandef.setBeanClass(beanClass);
//                } catch (ClassNotFoundException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//
//                PropertyValues propertyValues = new PropertyValues();
//
//                NodeList propertyList = bean.getChildNodes();
//                for(int j = 0 ; j < propertyList.getLength(); j++)
//                {
//                    Node property = propertyList.item(j);
//                    if (property instanceof Element) {
//                        Element ele = (Element) property;
//
//                        String name = ele.getAttribute("name");
//                        Class<?> type;
//                        try {
//                            type = beandef.getBeanClass().getDeclaredField(name).getType();
//                            Object value = ele.getAttribute("value");
//
//                            if(type == Integer.class)
//                            {
//                                value = Integer.parseInt((String) value);
//                            }
//                            Object ref = ele.getAttribute("ref");
//
//                            propertyValues.AddPropertyValue(new PropertyValue(name,value,ref));
//                        } catch (NoSuchFieldException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        } catch (SecurityException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//
//
//                    }
//                }
//                beandef.setPropertyValues(propertyValues);
//                System.out.print(propertyValues);
//                this.registerBeanDefinition(beanName, beandef);
//            }
//
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        } catch (SAXException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

    @Override
    protected BeanDefinition GetCreatedBean(BeanDefinition beanDefinition) {

        try {
            // set BeanClass for BeanDefinition

            Class<?> beanClass = beanDefinition.getBeanClass();
            // set Bean Instance for BeanDefinition
            Object bean = beanClass.newInstance();

            List<PropertyValue> fieldDefinitionList = beanDefinition.getPropertyValues().GetPropertyValues();
            for(PropertyValue propertyValue: fieldDefinitionList)
            {
                if (propertyValue.getValue() != null){
                    BeanUtil.invokeSetterMethod(bean, propertyValue.getName(), propertyValue.getValue());
                }else if (propertyValue.getRef() != null){
                    BeanUtil.invokeSetterMethod(bean, propertyValue.getName(), propertyValue.getRef());
                }
            }

            beanDefinition.setBean(bean);

            return beanDefinition;

        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
