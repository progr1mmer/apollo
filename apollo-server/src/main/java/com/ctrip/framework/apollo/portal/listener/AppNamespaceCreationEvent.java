package com.ctrip.framework.apollo.portal.listener;

import com.ctrip.framework.apollo.portal.entity.po.PortalAppNamespace;
import com.google.common.base.Preconditions;
import org.springframework.context.ApplicationEvent;

public class AppNamespaceCreationEvent extends ApplicationEvent {

  public AppNamespaceCreationEvent(Object source) {
    super(source);
  }

  public PortalAppNamespace getAppNamespace() {
    Preconditions.checkState(source != null);
    return (PortalAppNamespace) this.source;
  }
}
