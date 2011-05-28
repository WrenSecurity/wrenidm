package com.forgerock.openidm.web.component.formgenerator;

import com.forgerock.openidm.provisioning.schema.ResourceAttributeDefinition;
import com.forgerock.openidm.provisioning.schema.SimpleTypeRestriction;
import com.forgerock.openidm.web.component.formgenerator.converter.FormAnyURIConverter;
import com.forgerock.openidm.web.component.formgenerator.converter.FormIntegerConverter;
import com.forgerock.openidm.web.component.formgenerator.converter.FormShortConverter;
import com.icesoft.faces.component.ext.HtmlCommandLink;
import com.icesoft.faces.component.ext.HtmlGraphicImage;
import com.icesoft.faces.component.ext.HtmlInputText;
import com.icesoft.faces.component.ext.HtmlMessage;
import com.icesoft.faces.component.ext.HtmlOutputLabel;
import com.icesoft.faces.component.ext.HtmlOutputText;
import com.icesoft.faces.component.ext.HtmlPanelGrid;
import com.icesoft.faces.component.ext.HtmlSelectBooleanCheckbox;
import com.icesoft.faces.component.ext.HtmlSelectManyListbox;
import com.icesoft.faces.component.ext.HtmlSelectOneMenu;
import com.icesoft.faces.component.selectinputdate.SelectInputDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.faces.application.Application;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItem;
import javax.faces.convert.BooleanConverter;
import javax.faces.convert.Converter;
import javax.faces.convert.IntegerConverter;
import javax.faces.convert.LongConverter;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.xml.namespace.QName;
import static javax.xml.XMLConstants.*;

/**
 *
 * @author Vilo Repan
 */
public final class SchemaFormGridFactory {

    private SchemaFormGridFactory() {
    }

    private static QName createXsdType(String type) {
        return new QName(W3C_XML_SCHEMA_NS_URI, type);
    }

    static Converter getInputConverter(Application application, ResourceAttributeDefinition attribute) {
        Converter converter = null;

        QName type = attribute.getType();
        if (createXsdType("integer").equals(type) || createXsdType("int").equals(type)) {
            converter = application.createConverter(IntegerConverter.CONVERTER_ID);
        } else if (createXsdType("boolean").equals(type)) {
            converter = application.createConverter(BooleanConverter.CONVERTER_ID);
        } else if (createXsdType("long").equals(type)) {
            converter = application.createConverter(LongConverter.CONVERTER_ID);
        } else if (createXsdType("negativeInteger").equals(type)) {
            FormIntegerConverter con = (FormIntegerConverter) application.createConverter(FormIntegerConverter.CONVERTER_ID);
            con.init(null, -1);
            converter = con;
        } else if (createXsdType("nonNegativeInteger").equals(type)) {
            FormIntegerConverter con = (FormIntegerConverter) application.createConverter(FormIntegerConverter.CONVERTER_ID);
            con.init(0, null);
            converter = con;
        } else if (createXsdType("nonPositiveInteger").equals(type)) {
            FormIntegerConverter con = (FormIntegerConverter) application.createConverter(FormIntegerConverter.CONVERTER_ID);
            con.init(null, 0);
            converter = con;
        } else if (createXsdType("positiveInteger").equals(type)) {
            FormIntegerConverter con = (FormIntegerConverter) application.createConverter(FormIntegerConverter.CONVERTER_ID);
            con.init(1, null);
            converter = con;
        } else if (createXsdType("short").equals(type)) {
            converter = application.createConverter(FormShortConverter.CONVERTER_ID);
        } else if (createXsdType("anyURI").equals(type)) {
            converter = application.createConverter(FormAnyURIConverter.CONVERTER_ID);
        }

        return converter;
    }

    static boolean isAttributeValueNull(Object value) {
        return (value == null || (value instanceof NullAttributeValue));
    }

    static UIComponent createOutputText(Application application, Object value) {
        HtmlOutputText text = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
        if (!isAttributeValueNull(value)) {
            text.setValue(value);
        }

        return text;
    }

    static UIComponent createBlankComponent(Application application) {
        return application.createComponent(HtmlOutputText.COMPONENT_TYPE);
    }

    static UIComponent createErrorMessage(Application application, UIComponent component) {
        HtmlMessage message = (HtmlMessage) application.createComponent(HtmlMessage.COMPONENT_TYPE);
        message.setStyle("color: red;");
        message.setFor(component.getId());
        message.setId("message_" + component.getId());

        return message;
    }

    static UICommand createAddImage(Application application) {
        return createButtonImage(application, "/resources/images/add.png", "Add");
    }

    static UICommand createDeleteImage(Application application) {
        return createButtonImage(application, "/resources/images/delete.png", "Delete");
    }

    static UICommand createHelpImage(Application application, String value) {
        return createButtonImage(application, "/resources/images/help.png", "Help");
    }

    static UIComponent createGrid(Application application, int columns) {
        HtmlPanelGrid grid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
        grid.setColumns(columns);

        return grid;
    }

    static UIComponent createBlankImage(Application application) {
        HtmlOutputText comp = (HtmlOutputText) createOutputText(application, null);
        comp.setStyle("width: 16px; height: 16px;");
        return comp;
//        return createImage(application, "/resources/images/blank.png");
    }

    private static UIComponent createImage(Application application, String image) {
        HtmlGraphicImage img = (HtmlGraphicImage) application.createComponent(HtmlGraphicImage.COMPONENT_TYPE);
        img.setMimeType("image/png");
        img.setStyle("border: 0px;");
        img.setValue(image);

        return img;
    }

    private static HtmlCommandLink createButtonImage(Application application, String image, String title) {
        HtmlCommandLink link = (HtmlCommandLink) application.createComponent(HtmlCommandLink.COMPONENT_TYPE);
        link.setImmediate(true);
        link.setTitle(title);
        link.getChildren().add(createImage(application, image));

        return link;
    }

    static UIComponent createLabel(Application application, String name) {
        HtmlOutputLabel label = (HtmlOutputLabel) application.createComponent(HtmlOutputLabel.COMPONENT_TYPE);
        label.setValue(name);

        return label;
    }

    static UIComponent createRequiredLabel(Application application, String name) {
        HtmlOutputLabel label = (HtmlOutputLabel)application.createComponent(HtmlOutputLabel.COMPONENT_TYPE);
        label.setValue(name);
        label.setStyleClass("required-field");
        
        return label;
    }

    static UIInput createComplexInput(Application application, final SchemaAttributeBean bean, Object value) {
//        if (bean.getAttribute().isSimple()) {
//            throw new IllegalArgumentException("Schema attribute bean contains simple attribute.");
//        }

//        ComplexSchemaAttribute complexAttribute = (ComplexSchemaAttribute) bean.getAttribute();

        HtmlSelectManyListbox listbox = (HtmlSelectManyListbox) application.createComponent(HtmlSelectManyListbox.COMPONENT_TYPE);
        listbox.setStyle("width: 98%;");
        listbox.setSize(3);
        listbox.setReadonly(isOnlyReadable(bean.getAttribute()));

        return listbox;
    }

    static UIInput createSelectInputDate(Application application, final SchemaAttributeBean bean, Object value) {
        ResourceAttributeDefinition attribute = bean.getAttribute();

        SelectInputDate date = (SelectInputDate) application.createComponent(SelectInputDate.COMPONENT_TYPE);
        final String dateFormat = "dd. MMMM, yyyy";
        date.setPopupDateFormat(dateFormat);
        date.setRenderAsPopup(true);
        date.setPartialSubmit(true);
        date.setStyleClass("iceInpTxt");
        date.setReadonly(isOnlyReadable(attribute));
        date.setImageDir("/resources/css/rime/css-images/");

        final Key key = createKey(bean, value);
        if (!isAttributeValueNull(value) && (value instanceof Long)) {
            date.setValue(new Date((Long) value));
        }

        date.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void processValueChange(ValueChangeEvent event) throws AbortProcessingException {
                Date date = (Date) event.getNewValue();

                if (date != null) {
                    bean.setValue(key, date.getTime());
                } else {
                    bean.setValue(key, new NullAttributeValue());
                }
            }
        });

        return date;
    }

    static UIInput createSelectOneMenu(Application application, final SchemaAttributeBean bean, Object value) {
//        if (!bean.getAttribute().isSimple()) {
//            throw new IllegalArgumentException("Can't create SelectOneMenu for complex attribute: " + bean.getAttribute());
//        }

        ResourceAttributeDefinition attribute = bean.getAttribute();

        HtmlSelectOneMenu combo = (HtmlSelectOneMenu) application.createComponent(HtmlSelectOneMenu.COMPONENT_TYPE);
        combo.setReadonly(isOnlyReadable(attribute));
        combo.setPartialSubmit(true);
        UISelectItem selectItem = null;
        SimpleTypeRestriction restriction = attribute.getRestriction();

        List<UISelectItem> items = new ArrayList<UISelectItem>();
        for (String item : restriction.getEnumeration()) {
            selectItem = (UISelectItem) application.createComponent(UISelectItem.COMPONENT_TYPE);
            selectItem.setItemLabel(item);
            selectItem.setItemValue(item);

            items.add(selectItem);
        }

        selectItem = (UISelectItem) application.createComponent(UISelectItem.COMPONENT_TYPE);
        selectItem.setItemLabel("");
        selectItem.setItemValue(null);
        combo.getChildren().add(selectItem);

        Collections.sort(items, new Comparator<UISelectItem>() {

            @Override
            public int compare(UISelectItem item1, UISelectItem item2) {
                return String.CASE_INSENSITIVE_ORDER.compare(item1.getItemLabel(), item2.getItemLabel());
            }
        });
        combo.getChildren().addAll(items);

        final Key key = createKey(bean, value);
        if (!isAttributeValueNull(value)) {
            combo.setValue(value);
        }

        combo.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void processValueChange(ValueChangeEvent event) throws AbortProcessingException {
                if (event.getNewValue() == null || "".equals(event.getNewValue())) {
                    bean.setValue(key, new NullAttributeValue());
                } else {
                    bean.setValue(key, event.getNewValue());
                }
            }
        });

        return combo;
    }

    static UIInput createInputText(Application application, final SchemaAttributeBean bean, Object value) {
        ResourceAttributeDefinition attribute = bean.getAttribute();

        HtmlInputText text = (HtmlInputText) application.createComponent(HtmlInputText.COMPONENT_TYPE);
        text.setPartialSubmit(true);
        text.setStyle("width: 95%;");
        text.setReadonly(isOnlyReadable(attribute));
        if (!isAttributeValueNull(value)) {
            text.setValue(value);
        }

        final Key key = createKey(bean, value);
        text.addValueChangeListener(new ValueChangeListenerImpl() {

            @Override
            public void processValueChange(ValueChangeEvent event) throws AbortProcessingException {
                if (event.getNewValue() == null || "".equals(event.getNewValue())) {
                    bean.setValue(key, new NullAttributeValue());
                } else {
                    bean.setValue(key, event.getNewValue());
                }
            }
        });
//        text.addValueChangeListener(new ValueChangeListener() {
//
//            @Override
//            public void processValueChange(ValueChangeEvent event) throws AbortProcessingException {
//                if (event.getNewValue() == null || "".equals(event.getNewValue())) {
//                    bean.setValue(key, new NullAttributeValue());
//                } else {
//                    bean.setValue(key, event.getNewValue());
//                }
//            }
//        });

        return text;
    }

    static UIInput createBooleanCheckbox(Application application, final SchemaAttributeBean bean, Object value) {
        ResourceAttributeDefinition attribute = bean.getAttribute();

        HtmlSelectBooleanCheckbox check = (HtmlSelectBooleanCheckbox) application.createComponent(HtmlSelectBooleanCheckbox.COMPONENT_TYPE);
        check.setReadonly(isOnlyReadable(attribute));
        check.setPartialSubmit(true);
        if (!isAttributeValueNull(value)) {
            check.setSelected((Boolean) value);
        }

        final Key key = createKey(bean, value);
        check.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void processValueChange(ValueChangeEvent event) throws AbortProcessingException {
                if (event.getNewValue() == null || !((Boolean) event.getNewValue())) {
                    bean.setValue(key, new NullAttributeValue());
                } else {
                    bean.setValue(key, event.getNewValue());
                }
            }
        });

        return check;
    }

    static Key createKey(SchemaAttributeBean bean, Object value) {
        ResourceAttributeDefinition attribute = bean.getAttribute();
        int index = bean.getValues().indexOf(value);

        return new Key(attribute.getQName(), index);
    }

    private static boolean isOnlyReadable(ResourceAttributeDefinition attribute) {
        return attribute.canRead() && !attribute.canUpdate();
    }
}
