package test;

import Factory.BeanFactory;
import Factory.XMLBeanFactory;
import resource.LocalFileResource;

public class test {

    public static void main(String[] args) {
        String locations[] = {"bean.xml"};
//        LocalFileResource resource = new LocalFileResource(locations);
//        ApplicationContext ctx = new ClassPathXmlApplicationContext(locations);
//        boss boss = (boss) ctx.getBean("boss");
        BeanFactory beanFactory = new XMLBeanFactory(locations);
        boss boss = (boss) beanFactory.getBean("boss");
        boss.tostring();
    }
}









