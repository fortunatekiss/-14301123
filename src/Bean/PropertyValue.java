package Bean;

/**
 * Created by lenovo on 2016/11/12.
 */
public class PropertyValue {
    public PropertyValue(String name, Object value) {
        this.name = name;
        this.value = value;
//        this.ref = ref;
    }

    private String name;

    private Object value;

    private Object ref;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }


}
