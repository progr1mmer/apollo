package com.ctrip.framework.apollo.portal.listener;

import com.ctrip.framework.apollo.portal.entity.po.PortalAppNamespace;
import com.google.common.base.Preconditions;
import org.springframework.context.ApplicationEvent;

public class AppNamespaceDeletionEvent extends ApplicationEvent {

  public AppNamespaceDeletionEvent(Object source) {
    super(source);
  }

  public PortalAppNamespace getAppNamespace() {
    Preconditions.checkState(source != null);
    return (PortalAppNamespace) this.source;
  }
}
