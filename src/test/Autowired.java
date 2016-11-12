package test;

import java.lang.annotation.*;

/**
 * Created by lenovo on 2016/11/12.
 */
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD,
        ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
    boolean required() default true;
}
