package com.forgerock.openidm.web.component.formgenerator;

import com.forgerock.openidm.provisioning.schema.ResourceAttributeDefinition;
import com.forgerock.openidm.provisioning.schema.SimpleTypeRestriction;
import com.icesoft.faces.component.ext.HtmlPanelGrid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.FacesComponent;
import javax.faces.component.NamingContainer;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.xml.namespace.QName;
import static javax.xml.XMLConstants.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.forgerock.openidm.web.component.formgenerator.SchemaFormGridFactory.*;

/**
 *
 * @author Vilo Repan
 */
@FacesComponent(value = "SchemaFormGrid")
public class SchemaFormGrid extends UIComponentBase implements NamingContainer {

    private static transient Logger logger = LoggerFactory.getLogger(SchemaFormGrid.class);
    public static final String BEAN_VALUE_INDEX = "beanValueIndex";
    private static final int GRID_COLUMNS_COUNT = 5;
    //UIComponent constants
    public static final String COMPONENT_TYPE = SchemaFormGrid.class.getName();
    public static final String COMPONENT_FAMILY = "SchemaFormGridFamily";
    public static final String RENDERER_TYPE = "SchemaFormGridRenderer";
    //JSF context
    private Application application;
    //grid for dynamic form and popup for help
    private List<UIComponent> children = new ArrayList<UIComponent>();
    private HtmlPanelGrid grid;
    private HelpPopup popup;
    //data for children
    private List<SchemaAttributeBean> attributes = null;
    private ValueExpression editableExpression;
    //pointer to resource schema bean    
    private ValueExpression beanExpression;
    //variables for state changes
    private boolean isRestoringState = false;
    private FormAttributeUpdate update;

    public SchemaFormGrid() {
        application = FacesContext.getCurrentInstance().getApplication();

        grid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
        grid.setColumns(GRID_COLUMNS_COUNT);
        grid.setParent(this);
        children.add(grid);

        popup = (HelpPopup) application.createComponent(HelpPopup.COMPONENT_TYPE);
        popup.setParent(this);
        children.add(popup);
    }

    void setEditable(ValueExpression expr) {
        this.editableExpression = expr;
    }

    void setBean(ValueExpression expr) {
        this.beanExpression = expr;
    }

    private boolean isEditable() {
        if (editableExpression == null) {
            return true;
        }
        return (Boolean) editableExpression.getValue(getFacesContext().getELContext());
    }

    private SchemaFormBean getBean() {
        if (beanExpression == null) {
            return null;
        }

        return (SchemaFormBean) beanExpression.getValue(getFacesContext().getELContext());
    }

    @Override
    public Object saveState(FacesContext context) {
        Object values[] = new Object[3];
        values[0] = super.saveState(context);
        values[1] = grid.saveState(context);
        values[2] = popup.saveState(context);

        return ((Object) (values));
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        setBean();

        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);
        grid.restoreState(context, values[1]);
        popup.restoreState(context, values[2]);

        isRestoringState = true;
    }

    private void setBean() {
        SchemaFormBean bean = getBean();
        if (bean == null) {
            return;
        }

        attributes = bean.getAttributeBeanList();

        grid.getChildren().clear();
        grid.getChildren().addAll(createAttributeForm(attributes));
    }

    @Override
    public void encodeChildren(FacesContext context) throws IOException {
        if (!isRestoringState) {
            setBean();
        }

        if (update != null) {
            List<UIComponent> gridChildren = grid.getChildren();
            for (int index = update.start; index < update.end; index++) {
                gridChildren.remove(update.start);
            }

            for (int i = 0; i < update.row.size(); i++) {
                gridChildren.add(update.start + i, update.row.get(i));
            }

            update = null;
        }

        grid.encodeAll(context);
        popup.encodeAll(context);
    }

    @Override
    public void decode(FacesContext context) {
        grid.decode(context);
        popup.decode(context);

        super.decode(context);
    }

    @Override
    public void processRestoreState(FacesContext context, Object state) {
        Object[] object = (Object[]) state;
        grid.processRestoreState(context, object[0]);
        popup.processRestoreState(context, object[1]);
        super.processRestoreState(context, object[2]);
    }

    @Override
    public void processDecodes(FacesContext context) {
        grid.processDecodes(context);
        popup.processDecodes(context);
        super.processDecodes(context);
    }

    @Override
    public Object processSaveState(FacesContext context) {
        Object[] state = new Object[3];
        state[0] = grid.processSaveState(context);
        state[1] = popup.processSaveState(context);
        state[2] = super.processSaveState(context);

        return state;
    }

    @Override
    public void processUpdates(FacesContext context) {
        grid.processUpdates(context);
        popup.processUpdates(context);
        super.processUpdates(context);
    }

    @Override
    public void processValidators(FacesContext context) {
        grid.processValidators(context);
        popup.processValidators(context);
        super.processValidators(context);
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public int getFacetCount() {
        return 0;
    }

    @Override
    public Iterator<UIComponent> getFacetsAndChildren() {
        return new ArrayList<UIComponent>().iterator();
    }

    @Override
    public Map<String, UIComponent> getFacets() {
        return new HashMap<String, UIComponent>();
    }

    @Override
    public List<UIComponent> getChildren() {
        return new ArrayList<UIComponent>();
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    @Override
    public UIComponent findComponent(String expr) {
        if (getId() != null && getId().equals(expr)) {
            return this;
        }

        UIComponent comp = super.findComponent(expr);
        if (comp != null) {
            return comp;
        }

        UIComponent component = null;
        for (UIComponent child : children) {
            component = findComponentRecursive(expr, child);
            if (component != null) {
                return component;
            }
        }

        return null;
    }

    private UIComponent findComponentRecursive(String id, UIComponent comp) {
        if (comp.getId() != null && comp.getId().equals(id)) {
            return comp;
        }

        List<UIComponent> childList = comp.getChildren();
        UIComponent component;
        for (UIComponent child : childList) {
            component = findComponentRecursive(id, child);
            if (component != null) {
                return component;
            }
        }

        return null;
    }

    private List<UIComponent> createAttributeForm(List<SchemaAttributeBean> list) {
        List<UIComponent> formList = new ArrayList<UIComponent>();
        for (SchemaAttributeBean bean : list) {
            ResourceAttributeDefinition attribute = bean.getAttribute();
            List<Object> values = bean.getValues();
            if (values.isEmpty()) {
                values.add(new NullAttributeValue());
            }

            while (values.size() < attribute.getMinOccurs()) {
                values.add(new NullAttributeValue());
            }

            List<UIComponent> attributeRows = createAttributeRows(bean, values, list.indexOf(bean));
            formList.addAll(attributeRows);
        }

        return formList;
    }

    private int getAbsoluteIndex(int attrIndex, int valueIndex) {
        SchemaAttributeBean bean = attributes.get(attrIndex);

        return getAbsoluteIndex(bean, bean.getValues().get(valueIndex));
    }

    private int getAbsoluteIndex(SchemaAttributeBean bean, Object value) {
        int index = 0;

        outer:
        for (SchemaAttributeBean attrBean : attributes) {
            for (Object attrValue : attrBean.getValues()) {
                if (attrBean.equals(bean) && attrValue.equals(value)) {
                    break outer;
                }

                index++;
            }
        }

        return index;
    }

    private List<UIComponent> createAttributeRows(SchemaAttributeBean bean, List<Object> values, int attributeIndex) {
        List<UIComponent> attributeRows = new ArrayList<UIComponent>();
        ResourceAttributeDefinition attribute = bean.getAttribute();

        for (int index = 0; index < values.size(); index++) {
            Object value = values.get(index);

            UIComponent help;
            UIComponent label;
            if (index == 0) {
                if (attribute.getHelp() != null && !attribute.getHelp().isEmpty()) {
                    UICommand helpButton = createHelpImage(application, attribute.getHelp());
                    helpButton.setId("help_" + attributeIndex + "_" + index);
                    helpButton.addActionListener(new FormButtonListener(FormButtonType.HELP, attributeIndex, index));
                    help = helpButton;
                } else {
                    help = createBlankImage(application);
                }

                label = createLabel(application, attribute.getAttributeDisplayName() + ":");
                if (attribute.getMinOccurs() > 0) {
                    UIComponent required = createRequiredLabel(application, "*");
                    label.getChildren().add(required);
                }
            } else {
                help = createBlankComponent(application);
                label = createBlankComponent(application);
            }
            attributeRows.add(help);
            attributeRows.add(label);

            if (isEditable()) {
                UIInput input = createInput(bean, value, attributeIndex);
//                if (index == 0) {
//                    input.setConverter(new SchemaAttributeValidator(beanExpression, bean.getIdentifier(), input.getConverter()));
//                }
                attributeRows.add(input);

                UIComponent column = createButtonPanel(attributeIndex, index, bean);
                attributeRows.add(column);

                UIComponent errorLabel = createErrorMessage(application, input);
                attributeRows.add(errorLabel);
            } else {
                UIComponent output = createOutputText(application, value);
                attributeRows.add(output);
                attributeRows.add(createBlankComponent(application));
                attributeRows.add(createBlankComponent(application));
            }
        }

        return attributeRows;
    }

    private UIComponent createButtonPanel(int attributeIndex, int valueIndex, SchemaAttributeBean bean) {
        UIComponent panel = createGrid(application, 2);

        if (bean.canRemoveValue() && bean.getValueListSize() > 1) {
            UICommand button = createDeleteImage(application);
            button.setId("delete_" + attributeIndex + "_" + valueIndex);
            button.addActionListener(new FormButtonListener(FormButtonType.DELETE, attributeIndex, valueIndex));
            panel.getChildren().add(button);
        } else {
            panel.getChildren().add(createBlankImage(application));
        }

        if (bean.canAddValue() && (valueIndex + 1 == bean.getValueListSize())) {
            UICommand button = createAddImage(application);
            button.setId("add_" + attributeIndex + "_" + valueIndex);
            button.addActionListener(new FormButtonListener(FormButtonType.ADD, attributeIndex, valueIndex));
            panel.getChildren().add(button);
        } else {
            panel.getChildren().add(createBlankImage(application));
        }

        return panel;
    }

    private UIInput createInput(SchemaAttributeBean bean, Object value, int attributeIndex) {
        UIInput component = null;

        ResourceAttributeDefinition attribute = bean.getAttribute();
//        if (attribute.isSimple()) {
        if (new QName(W3C_XML_SCHEMA_NS_URI, "date").equals(attribute.getType())) {
            component = createSelectInputDate(application, bean, value);
        } else if (new QName(W3C_XML_SCHEMA_NS_URI, "boolean").equals(attribute.getType())) {
            component = createBooleanCheckbox(application, bean, value);
        } else {
            SimpleTypeRestriction restriction = attribute.getRestriction();
            if (restriction != null && restriction.getEnumeration() != null) {
                component = createSelectOneMenu(application, bean, value);
            }
        }
//        } else {
//            component = createComplexInput(application, bean, value);
//        }

        if (component == null) {
            component = createInputText(application, bean, value);
        }

        component.setId(attribute.getQName().getLocalPart() + "_" + Integer.toString(attributeIndex) + "_" + bean.getValues().indexOf(value));
        component.setConverter(getInputConverter(application, attribute));

        return component;
    }

    private void updateAttributeRows(FormButtonType updateType, int attributeIndex) {
        logger.trace("updateAttributeRows");
        SchemaFormBean formBean = getBean();
        SchemaAttributeBean bean = formBean.getAttributeBeanList().get(attributeIndex);

        List<UIComponent> row = createAttributeRows(bean, bean.getValues(), attributeIndex);

        int absoluteIndex = getAbsoluteIndex(attributeIndex, 0);
        int start = absoluteIndex * GRID_COLUMNS_COUNT;
        int end = (absoluteIndex + bean.getValueListSize() - 1) * GRID_COLUMNS_COUNT;
        if (updateType == FormButtonType.DELETE) {
            end += 2 * GRID_COLUMNS_COUNT;
        }

        update = new FormAttributeUpdate(start, end, row);
    }

    private void showHelpPopup(String help) {
        popup.setHelp(help);
        popup.setRendered(true);
    }

    private enum FormButtonType {

        ADD, DELETE, HELP;
    }

    private class FormAttributeUpdate {

        private int start = 0;
        private int end = 0;
        private List<UIComponent> row;

        FormAttributeUpdate(int start, int end, List<UIComponent> row) {
            this.start = start;
            this.end = end;
            this.row = row;
        }
    }

    private class FormButtonListener extends AbstractStateHolder implements ActionListener {

        private FormButtonType type;
        private int attributeIndex;
        private int valueIndex;

        FormButtonListener(FormButtonType type, int attributeIndex, int valueIndex) {
            this.attributeIndex = attributeIndex;
            this.valueIndex = valueIndex;
            this.type = type;
        }

        @Override
        public void processAction(ActionEvent event) throws AbortProcessingException {
            logger.trace("processAction: " + type);
            SchemaFormBean bean = getBean();

            SchemaAttributeBean attrBean = bean.getAttributeBeanList().get(attributeIndex);

            switch (type) {
                case ADD:
                    attrBean.addValue(event);
                    updateAttributeRows(FormButtonType.ADD, attributeIndex);
                    break;
                case DELETE:
                    event.getComponent().getAttributes().put(BEAN_VALUE_INDEX, valueIndex);
                    attrBean.removeValue(event);
                    updateAttributeRows(FormButtonType.DELETE, attributeIndex);
                    break;
                case HELP:
                    showHelpPopup(attrBean.getAttribute().getHelp());
                    break;
            }
        }

        @Override
        public Object saveState(FacesContext context) {
            Object[] object = new Object[4];
            object[0] = super.saveState(context);
            object[1] = attributeIndex;
            object[2] = valueIndex;
            object[3] = type;

            return object;
        }

        @Override
        public void restoreState(FacesContext context, Object state) {
            Object[] object = (Object[]) state;
            super.restoreState(context, object[0]);
            attributeIndex = (Integer) object[1];
            valueIndex = (Integer) object[2];
            type = (FormButtonType) object[3];
        }
    }
}
