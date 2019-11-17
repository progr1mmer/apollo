package com.ctrip.framework.apollo.portal.listener;

import com.ctrip.framework.apollo.portal.entity.po.PortalApp;
import com.google.common.base.Preconditions;
import org.springframework.context.ApplicationEvent;

public class AppInfoChangedEvent extends ApplicationEvent{

  public AppInfoChangedEvent(Object source) {
    super(source);
  }

  public PortalApp getApp() {
    Preconditions.checkState(source != null);
    return (PortalApp) this.source;
  }
}
