package com.forgerock.openidm.web.component.formgenerator;

import com.forgerock.openidm.provisioning.schema.ResourceAttributeDefinition;
import com.forgerock.openidm.provisioning.schema.SimpleTypeRestriction;
import com.forgerock.openidm.provisioning.schema.util.SchemaParserException;
import com.forgerock.openidm.web.model.AccountShadowDto;
import com.icesoft.faces.component.ext.HtmlPanelGrid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.faces.application.Application;
import javax.faces.component.StateHolder;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import static com.forgerock.openidm.web.component.formgenerator.SchemaFormGridFactory.*;

/**
 *
 * @author Vilo Repan
 */
public class SchemaFormBean implements Serializable {

    private static transient Logger logger = LoggerFactory.getLogger(SchemaFormBean.class);
    //data for form
    private String displayName;
    private List<SchemaAttributeBean> attributes = new ArrayList<SchemaAttributeBean>();
    //panel for use in deprecated composite component
    private transient UIComponent panel;
    private transient HtmlPanelGrid grid;
    //resource account to be shown
    private AccountShadowDto account;
    private QName defaultAccountType;
    private static final int GRID_COLUMNS_COUNT = 5;

    public List<SchemaAttributeBean> getAttributeBeanList() {
        return attributes;
    }

    @Deprecated
    public UIComponent getPanel() {
        return panel;
    }

    @Deprecated
    public void setPanel(UIComponent panel) {
        this.panel = panel;

        Application application = FacesContext.getCurrentInstance().getApplication();
        grid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
        grid.setColumns(GRID_COLUMNS_COUNT);
        panel.getChildren().add(grid);

        grid.getChildren().addAll(createAttributeForm(attributes, application));
    }

    /**
     * Only for debugging purposes only
     */
    @Deprecated
    public List<Entry<String, Object>> getFormValuesList() {
        List<Entry<String, Object>> list = new ArrayList<Entry<String, Object>>();

        for (SchemaAttributeBean bean : attributes) {
            List<Object> values = bean.getValues();
            ResourceAttributeDefinition attribute = bean.getAttribute();
            for (Object value : values) {
                Key key = new Key(attribute.getQName(), values.indexOf(value));
                list.add(new HashMap.SimpleEntry(key, value));
            }
        }

        return list;
    }

    public AccountShadowDto getAccount() {
        return account;
    }

    public QName getDefaultAccountType() {
        return defaultAccountType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public AccountShadowDto updateAccountAttributes() throws SchemaParserException {
        List<Element> attrList = new ArrayList<Element>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();

            Map<String, String> prefixMap = new HashMap<String, String>();
            for (SchemaAttributeBean bean : attributes) {
                List<Object> values = bean.getValues();
                for (Object value : values) {
                    if (value instanceof NullAttributeValue) {
                        continue;
                    }
                    if (value == null || "".equals(value)) {
                        continue;
                    }

                    ResourceAttributeDefinition attribute  = bean.getAttribute();
                    String namespace = attribute.getQName().getNamespaceURI();
                    String name = attribute.getQName().getLocalPart();

                    logger.trace("Creating element: {" + namespace + "}" + name + ": " + value.toString());

                    Element element = doc.createElementNS(namespace, name);
                    element.setPrefix(buildElementName(namespace, prefixMap));
                    element.setTextContent(value.toString());
                    attrList.add(element);
                }
            }

            if (defaultAccountType != null) {
                account.setObjectClass(defaultAccountType);
            }
        } catch (Exception ex) {
            throw new SchemaParserException("Unknown error: Can't update account attributes: " + ex.getMessage(), ex);
        }

        account.setAttributes(attrList);

        return account;
    }

    private String buildElementName(String namespace, Map<String, String> prefixMap) {
        String prefix = prefixMap.get(namespace);
        if (prefix == null) {
            prefix = "vr" + prefixMap.size();
            prefixMap.put(namespace, prefix);
        }

        return prefix;
    }

    public void generateForm(AccountShadowDto account) throws SchemaParserException {
        generateForm(account, null);
    }

    public void generateForm(AccountShadowDto account, QName accountType) throws SchemaParserException {
        if (account == null) {
            throw new IllegalArgumentException("Account object can't be null.");
        }
        this.account = account;

        attributes.clear();
        try {
            SchemaFormParser parser = new SchemaFormParser();

            List<ResourceAttributeDefinition> list = parser.parseSchemaForAccount(account, accountType);
            Map<SimpleKey, List<Object>> formValues = parser.getAttributeValueMap();
            for (ResourceAttributeDefinition attribute : list) {
                List<Object> values = formValues.get(createKey(attribute));
                attributes.add(new SchemaAttributeBean(attribute, values));
            }
            displayName = parser.getDisplayName();
            defaultAccountType = parser.getDefaultAccountType();
        } catch (SchemaParserException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SchemaParserException("Unknown error: " + ex.getMessage(), ex);
        }
    }

    private SimpleKey createKey(ResourceAttributeDefinition attribute) {
        return new SimpleKey(attribute.getQName());
    }

    private List<UIComponent> createAttributeForm(List<SchemaAttributeBean> list, Application application) {
        List<UIComponent> formList = new ArrayList<UIComponent>();
        int absoluteIndex = 0;
        for (SchemaAttributeBean bean : list) {
            ResourceAttributeDefinition attribute = bean.getAttribute();
            List<Object> values = bean.getValues();
            if (values.isEmpty()) {
                values.add(new NullAttributeValue());
            }

            while (values.size() < attribute.getMinOccurs()) {
                values.add(new NullAttributeValue());
            }

            List<UIComponent> attributeRows = createAttributeRows(application, bean, values, list.indexOf(bean), absoluteIndex);
            formList.addAll(attributeRows);
            absoluteIndex += values.size();
        }

        return formList;
    }

    private List<UIComponent> createAttributeRows(Application application, SchemaAttributeBean bean, List<Object> values, int attributeIndex, int absoluteIndex) {
        List<UIComponent> attributeRows = new ArrayList<UIComponent>();
        ResourceAttributeDefinition attribute = bean.getAttribute();

        for (int index = 0; index < values.size(); index++) {
            Object value = values.get(index);

            UIComponent help;
            UIComponent label;
            if (index == 0) {
                if (attribute.getHelp() != null && !attribute.getHelp().isEmpty()) {
                    UICommand helpButton = createHelpImage(application, attribute.getHelp());
                    helpButton.setId("help_" + absoluteIndex + "_" + index);
                    helpButton.addActionListener(new FormButtonListener(FormButtonType.HELP, absoluteIndex, attributeIndex, index));
                    help = helpButton;
                } else {
                    help = createBlankImage(application);
                }
                String colon = ":";
                if (attribute.getMinOccurs() > 0) {
                    colon += "<em>*</em>";
                }
                label = createLabel(application, attribute.getAttributeDisplayName() + colon);
            } else {
                help = createBlankComponent(application);
                label = createBlankComponent(application);
            }
            attributeRows.add(help);
            attributeRows.add(label);

            UIComponent input = createInput(application, bean, value, index);
            attributeRows.add(input);

            UIComponent column = createButtonPanel(application, absoluteIndex, attributeIndex, index, bean);
            attributeRows.add(column);

            UIComponent errorLabel = createErrorMessage(application, input);
            attributeRows.add(errorLabel);

            absoluteIndex++;
        }

        return attributeRows;
    }

    private UIComponent createButtonPanel(Application application, int absoluteIndex, int attributeIndex,
            int valueIndex, SchemaAttributeBean bean) {
        UIComponent buttonPanel = createGrid(application, 2);

        if (bean.canRemoveValue() && bean.getValueListSize() > 1) {
            UICommand button = createDeleteImage(application);
            button.setId("delete_" + absoluteIndex + "_" + valueIndex);
            button.addActionListener(new FormButtonListener(FormButtonType.DELETE, absoluteIndex, attributeIndex, valueIndex));
            buttonPanel.getChildren().add(button);
        } else {
            buttonPanel.getChildren().add(createBlankImage(application));
        }

        if (bean.canAddValue() && (valueIndex + 1 == bean.getValueListSize())) {
            UICommand button = createAddImage(application);
            button.setId("add_" + absoluteIndex + "_" + valueIndex);
            button.addActionListener(new FormButtonListener(FormButtonType.ADD, absoluteIndex, attributeIndex, valueIndex));
            buttonPanel.getChildren().add(button);
        } else {
            buttonPanel.getChildren().add(createBlankImage(application));
        }

        return buttonPanel;
    }

    private UIComponent createInput(Application application, SchemaAttributeBean bean, Object value, int index) {
        UIComponent component = null;

        ResourceAttributeDefinition attribute = bean.getAttribute();
//        if (attribute.isSimple()) {
            if (new QName(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI, "date").equals(attribute.getType())) {
                component = createSelectInputDate(application, bean, value);
            } else {
                SimpleTypeRestriction restriction = attribute.getRestriction();
                if (restriction.getEnumeration() != null) {
                    component = createSelectOneMenu(application, bean, value);
                }
            }
//        } else {
//            component = createComplexInput(application, bean, value);
//        }

        if (component == null) {
            component = createInputText(application, bean, value);
        }

        component.setId(attribute.getQName().getLocalPart() + Integer.toString(index));

        return component;
    }

    private void updateAttributeRows(int absoluteIndex, int attributeIndex, int valueIndex) {
        logger.trace("updateAttributeRows");
        SchemaAttributeBean bean = this.getAttributeBeanList().get(attributeIndex);

        List<UIComponent> row = createAttributeRows(FacesContext.getCurrentInstance().getApplication(), bean, bean.getValues(), attributeIndex, absoluteIndex);
        List<UIComponent> gridChildren = grid.getChildren();

        int start = (absoluteIndex - valueIndex) * GRID_COLUMNS_COUNT;
        int end = (absoluteIndex + 1) * GRID_COLUMNS_COUNT;
        for (int index = start; index < end; index++) {
            gridChildren.remove(start);
        }

        for (int i = 0; i < row.size(); i++) {
            gridChildren.add(start + i, row.get(i));
        }
    }

    private enum FormButtonType {

        ADD, DELETE, HELP;
    }

    class FormButtonListener implements ActionListener, StateHolder {

        public static final String VALUE_INDEX = "valueIndex";
        private boolean isTransient;
        private FormButtonType type;
        private int absoluteIndex;
        private int attributeIndex;
        private int valueIndex;

        FormButtonListener(FormButtonType type, int absoluteIndex, int attributeIndex, int valueIndex) {
            this.absoluteIndex = absoluteIndex;
            this.attributeIndex = attributeIndex;
            this.valueIndex = valueIndex;
            this.type = type;
        }

        @Override
        public void processAction(ActionEvent event) throws AbortProcessingException {
            logger.trace("processAction: " + type);

            SchemaAttributeBean attrBean = getAttributeBeanList().get(attributeIndex);

            switch (type) {
                case ADD:
                    attrBean.addValue(event);
                    updateAttributeRows(absoluteIndex, attributeIndex, valueIndex);
                    break;
                case DELETE:
                    event.getComponent().getAttributes().put(VALUE_INDEX, valueIndex);
                    attrBean.removeValue(event);
                    updateAttributeRows(absoluteIndex, attributeIndex, valueIndex);
                    break;
                case HELP:
                    System.out.println("HEEEEEEEEEEEEEEEEEEEEEEEELLLLLLLLLLLPPPPPPPPPPPPPPPPPPPPPP");
                    break;
            }
        }

        @Override
        public Object saveState(FacesContext context) {
            Object[] object = new Object[5];
            object[0] = isTransient;
            object[1] = attributeIndex;
            object[2] = valueIndex;
            object[3] = type;
            object[4] = absoluteIndex;

            return object;
        }

        @Override
        public void restoreState(FacesContext context, Object state) {
            Object[] object = (Object[]) state;
            isTransient = (Boolean) object[0];
            attributeIndex = (Integer) object[1];
            valueIndex = (Integer) object[2];
            type = (FormButtonType) object[3];
            absoluteIndex = (Integer) object[4];
        }

        @Override
        public boolean isTransient() {
            return isTransient;
        }

        @Override
        public void setTransient(boolean newTransientValue) {
            isTransient = newTransientValue;
        }
    }
}
