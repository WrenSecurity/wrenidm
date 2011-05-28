package com.forgerock.openidm.web.component.formgenerator;

import com.icesoft.faces.component.ext.HtmlCommandButton;
import com.icesoft.faces.component.ext.HtmlOutputText;
import com.icesoft.faces.component.ext.HtmlPanelGrid;
import com.icesoft.faces.component.panelpopup.PanelPopup;
import javax.faces.application.Application;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import static com.forgerock.openidm.web.component.formgenerator.SchemaFormGridFactory.createGrid;

/**
 *
 * @author Vilo Repan
 */
@FacesComponent(value = "HelpPopup")
public class HelpPopup extends PanelPopup {

    public static final String COMPONENT_TYPE = "HelpPopup";
    private transient HtmlOutputText helpText;
    private String value;

    public HelpPopup() {
        setAutoCentre(true);
        setRendered(false);
        setStyle("z-index: 1000; top: 30%; left: 10%; position: absolute;");
        setDraggable("true");
        setResizable(false);

        Application application = FacesContext.getCurrentInstance().getApplication();
        HtmlOutputText text = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
        text.setValue("Information");
        text.setStyle("width: 100%;");
        getFacets().put("header", text);

        HtmlPanelGrid body = (HtmlPanelGrid) createGrid(application, 1);
        body.setWidth("100%");
        helpText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
        helpText.setEscape(true);
        body.getChildren().add(helpText);

        HtmlCommandButton b = (HtmlCommandButton) application.createComponent(HtmlCommandButton.COMPONENT_TYPE);
        b.setType("submit");
        b.setValue("Close");
        b.setId("closePopupButton");
        b.addActionListener(new CloseButtonListener());
        body.getChildren().add(b);

        getFacets().put("body", body);
    }

    public void setHelp(String help) {
        value = help;
        if (helpText != null) {
            helpText.setValue(value);
        }
    }

    @Override
    public Object saveState(FacesContext context) {
        Object[] state = new Object[2];
        state[0] = super.saveState(context);
        state[1] = value;

        return state;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        value = (String) values[1];

        setHelp(value);
    }

    public static class CloseButtonListener extends AbstractStateHolder implements ActionListener {

        @Override
        public void processAction(ActionEvent evt) throws AbortProcessingException {
            if (evt.getComponent() != null && evt.getComponent().getParent() != null
                    && evt.getComponent().getParent().getParent() != null) {
                UIComponent comp = evt.getComponent().getParent().getParent();
                //comp is instance of HelpPopup
                comp.setRendered(false);
            }
//            setRendered(false);
        }
    }
}
