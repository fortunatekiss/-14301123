package Bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2016/11/12.
 */
public class PropertyValues {
    private List<PropertyValue> propertyValues = new ArrayList<PropertyValue>();
    public void AddPropertyValue(PropertyValue propertyValue){
        propertyValues.add(propertyValue);
    }
    public List<PropertyValue> GetPropertyValues()
    {
        return propertyValues;
    }
}
