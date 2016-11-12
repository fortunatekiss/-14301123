package Factory;

import Bean.BeanDefinition;

/**
 * Created by lenovo on 2016/11/12.
 */
public interface BeanFactory {
    Object getBean(String beanName);

    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition, boolean isCreatedBean);
}
