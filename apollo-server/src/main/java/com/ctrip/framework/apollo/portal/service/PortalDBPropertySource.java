package com.ctrip.framework.apollo.portal.service;

import com.ctrip.framework.apollo.common.config.RefreshablePropertySource;
import com.ctrip.framework.apollo.portal.entity.po.PortalServerConfig;
import com.ctrip.framework.apollo.portal.repository.PortalServerConfigRepository;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;


/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Component
public class PortalDBPropertySource extends RefreshablePropertySource {
  private static final Logger logger = LoggerFactory.getLogger(PortalDBPropertySource.class);

  @Autowired
  private PortalServerConfigRepository serverConfigRepository;

  public PortalDBPropertySource(String name, Map<String, Object> source) {
    super(name, source);
  }

  public PortalDBPropertySource() {
    super("PortalDBConfig", Maps.newConcurrentMap());
  }

  @Override
  protected void refresh() {
    Iterable<PortalServerConfig> dbConfigs = serverConfigRepository.findAll();

    for (PortalServerConfig config: dbConfigs) {
      String key = config.getKey();
      Object value = config.getValue();

      if (this.source.isEmpty()) {
        logger.info("Load config from DB : {} = {}", key, value);
      } else if (!Objects.equals(this.source.get(key), value)) {
        logger.info("Load config from DB : {} = {}. Old value = {}", key,
                    value, this.source.get(key));
      }

      this.source.put(key, value);
    }
  }


}
