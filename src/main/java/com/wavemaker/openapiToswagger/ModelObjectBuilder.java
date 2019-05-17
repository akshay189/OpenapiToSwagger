package com.wavemaker.openapiToswagger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.wavemaker.tools.apidocs.tools.core.model.Model;
import com.wavemaker.tools.apidocs.tools.core.model.ModelImpl;
import com.wavemaker.tools.apidocs.tools.core.model.Xml;
import com.wavemaker.tools.apidocs.tools.core.model.properties.ArrayProperty;
import com.wavemaker.tools.apidocs.tools.core.model.properties.BooleanProperty;
import com.wavemaker.tools.apidocs.tools.core.model.properties.DateTimeProperty;
import com.wavemaker.tools.apidocs.tools.core.model.properties.IntegerProperty;
import com.wavemaker.tools.apidocs.tools.core.model.properties.MapProperty;
import com.wavemaker.tools.apidocs.tools.core.model.properties.ObjectProperty;
import com.wavemaker.tools.apidocs.tools.core.model.properties.Property;
import com.wavemaker.tools.apidocs.tools.core.model.properties.RefProperty;
import com.wavemaker.tools.apidocs.tools.core.model.properties.StringProperty;
import com.wavemaker.tools.apidocs.tools.core.model.properties.UUIDProperty;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.XML;

public class ModelObjectBuilder {

    public Model getModelFromSchema(Schema schema) {
        ModelImpl model = new ModelImpl();
        if (schema == null) {
            return null;
        }
        if (schema.getRequired() == null) {
            model.setRequired(new ArrayList<String>());
        } else {
            model.setRequired(schema.getRequired());
        }
        ((ModelImpl) model).setAdditionalProperties((Property) schema.getAdditionalProperties());
        model.setDescription(schema.getDescription());
//        com.wavemaker.model.setExample(schema.getExample().toString());
        model.setName(schema.getName());
//        com.wavemaker.model.setSimple(schem);
        model.setType(schema.getType());
        //Todo : do for xml builder.........
//        com.wavemaker.model.setXml(schema.getXml());
        Map<String, Schema> schemaMap = schema.getProperties();
        model.setProperties(propertyBuilder(schemaMap));
        model.setXml(xmlBuilder(schema.getXml()));
        return model;
    }

    private Xml xmlBuilder(XML xml) {
        if (xml != null) {
            Xml xmlv2 = new Xml();
            xmlv2.setAttribute(xml.getAttribute());
            xmlv2.setName(xml.getName());
            xmlv2.setNamespace(xml.getNamespace());
            xmlv2.setPrefix(xml.getPrefix());
            xmlv2.setWrapped(xml.getWrapped());
            return xmlv2;
        }
        return null;
    }

    private Map<String, Property> propertyBuilder(Map<String, Schema> schemaMap) {
        if (schemaMap != null) {
            Map<String, Property> propertyMap = new HashMap<>();
            for (Map.Entry<String, Schema> entry : schemaMap.entrySet()) {
                propertyMap.put(entry.getKey(), getproperty(entry.getValue()));
            }
            return propertyMap;
        }
        return null;
    }

    public Property getproperty(Schema schema) {
        if (schema.getType() != null && schema.getType().equals("integer")) {
            Property property = new IntegerProperty();
            ((IntegerProperty) property).setType(schema.getType());
//            property.setExample(schema.getExample().toString());
            property.setName(schema.getName());
            property.setDescription(schema.getDescription());
            ((IntegerProperty) property).setFormat(schema.getFormat());
            BigDecimal max = schema.getMaximum();
            BigDecimal min = schema.getMinimum();
            if (schema.getMaximum() == null) {
                ((IntegerProperty) property).setMaximum(null);
            } else {
                ((IntegerProperty) property).setMaximum(schema.getMaximum().doubleValue());
            }
//            ((IntegerProperty) property).setMaximum(schema.getMaximum().doubleValue());
            if (schema.getMinimum() == null) {
                ((IntegerProperty) property).setMinimum(null);
            } else {
                ((IntegerProperty) property).setMaximum(schema.getMinimum().doubleValue());
            }
// ((IntegerProperty) property).setPattern(schema.getPattern());
            return property;
        } else if (schema.getType() != null && schema.getType().equals("string")) {
            Property property = new StringProperty();
            ((StringProperty) property).setType(schema.getType());
//            property.setExample(schema.getExample().toString());
            property.setName(schema.getName());
            property.setDescription(schema.getDescription());
//            property.setExample(schema.getExample().toString());

            ((StringProperty) property).setEnum(schema.getEnum());
            ((StringProperty) property).setFormat(schema.getFormat());
            ((StringProperty) property).setMaxLength(schema.getMaxLength());
            ((StringProperty) property).setMinLength(schema.getMinLength());
            ((StringProperty) property).setPattern(schema.getPattern());
            return property;
        } else if (schema.getType() == null && schema.getFormat() == null && schema.get$ref() != null) {
            Property property = new RefProperty();
            ((RefProperty) property).set$ref(schema.get$ref().replace("#/components/schemas/", ""));
//            property.setExample(schema.getExample().toString());
            property.setName(schema.getName());
            property.setDescription(schema.getDescription());
//            ((RefProperty) property).setMaxLength(schema.getMaxLength());
//            ((RefProperty) property).setMinLength(schema.getMinLength());
//            ((RefProperty) property).setPattern(schema.getPattern());
            return property;
        } else if (schema.getType().equals("array")) {
            Property property = new ArrayProperty();
            ((ArrayProperty) property).setType(schema.getType());
//            property.setExample(schema.getExample().toString());
            property.setName(schema.getName());
            ((ArrayProperty) property).setFormat(schema.getFormat());
            property.setDescription(schema.getDescription());
            property.setXml(xmlBuilder(schema.getXml()));

            if (schema instanceof ArraySchema) {
                if (((ArraySchema) schema).getItems().getType() != null) {
                    Property property1 = new ObjectProperty();
                    ((ObjectProperty) property1).setType(((ArraySchema) schema).getItems().getType());
                    ((ArrayProperty) property).setItems(property1);
                } else if (((ArraySchema) schema).getItems().get$ref() != null) {
                    Property property1 = new RefProperty();
                    ((RefProperty) property1).set$ref(((ArraySchema) schema).getItems().get$ref().replace("#/components/schemas/", ""));
                    ((ArrayProperty) property).setItems(property1);
                }
            }

//            ArraySchema arraySchema = ((ArraySchema)schema);

//            ((ArrayProperty) property).setItems(getproperty(arraySchema.getItems()));
//            ((ArrayProperty) property).setItems();

//            ((ArrayProperty) property).setItems(schema.ge);
//            ((ArrayProperty) property).setMaxLength(schema.getMaxLength());
//            ((ArrayProperty) property).setMinLength(schema.getMinLength());
//            ((ArrayProperty) property).setPattern(schema.getPattern());
            return property;
        } else if (schema.getType().equals("boolean")) {
            Property property = new BooleanProperty();
            ((BooleanProperty) property).setType(schema.getType());
//            property.setExample(schema.getExample().toString());
            property.setName(schema.getName());
            ((BooleanProperty) property).setFormat(schema.getFormat());
            property.setDescription(schema.getDescription());
//            property.setDefault(schema.getDefault());
//            ((BooleanProperty) property).setMaxLength(schema.getMaxLength());
//            ((BooleanProperty) property).setMinLength(schema.getMinLength());
//            ((BooleanProperty) property).setPattern(schema.getPattern());
            return property;
        } else if (schema.getType().equals("object")) {
            Property property = new MapProperty();
            ((MapProperty) property).setType(schema.getType());
//            property.setExample(schema.getExample().toString());
            property.setName(schema.getName());
            ((MapProperty) property).setFormat(schema.getFormat());
            property.setDescription(schema.getDescription());
            property.setXml(xmlBuilder(schema.getXml()));
            ((MapProperty) property).setAdditionalProperties(additionalPropertiesBuilder((Schema) schema.getAdditionalProperties()));
            //todo : do it...........
            return property;

        } else {
            return null;
        }
    }

    private Property additionalPropertiesBuilder(Schema schema) {
        Property property = new ObjectProperty();
        ((ObjectProperty) property).setType(schema.getType());
        ((ObjectProperty) property).setFormat(schema.getFormat());
        return property;
    }
}
