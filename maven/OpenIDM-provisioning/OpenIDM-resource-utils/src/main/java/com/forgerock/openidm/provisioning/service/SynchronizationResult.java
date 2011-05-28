package com.forgerock.openidm.provisioning.service;

import com.forgerock.openidm.provisioning.objects.ResourceObject;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectChangeType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceStateType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceStateType.SynchronizationState;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author elek
 */
public class SynchronizationResult {

    private List<Change> changes = new ArrayList();

    public List<SynchronizationResult.Change> getChanges() {
        return changes;
    }

    public void addChange(SynchronizationResult.Change change) {
        changes.add(change);
    }

    public static class Change {

        private ResourceObject identifier;

        private ObjectChangeType change;

        private ResourceStateType.SynchronizationState token;

        public Change(ResourceObject identifier, ObjectChangeType change, SynchronizationState token) {
            this.identifier = identifier;
            this.change = change;
            this.token = token;
        }

        public ObjectChangeType getChange() {
            return change;
        }

        public void setChange(ObjectChangeType change) {
            this.change = change;
        }

        // Is ResourceObject really the right type here?
        // TODO: If yes then explain why
        public ResourceObject getIdentifier() {
            return identifier;
        }

        public void setIdentifier(ResourceObject identifier) {
            this.identifier = identifier;
        }

        public SynchronizationState getToken() {
            return token;
        }

        public void setToken(SynchronizationState token) {
            this.token = token;
        }
    }
}
