package com.forgerock.openidm.web.component.formgenerator;

import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;

/**
 *
 * @author Vilo Repan
 */
public abstract class AbstractStateHolder implements StateHolder {

    private boolean isTransient;

    @Override
    public Object saveState(FacesContext context) {
        Object[] object = new Object[1];
        object[0] = isTransient;

        return object;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        Object[] object = (Object[]) state;
        isTransient = (Boolean) object[0];
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
