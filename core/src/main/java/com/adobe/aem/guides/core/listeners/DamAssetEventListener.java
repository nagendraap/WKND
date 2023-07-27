package com.adobe.aem.guides.core.listeners;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = EventHandler.class,
        immediate = true,
        property = {
                "event.topics=org/apache/sling/api/resource/Resource/ADDED",
                "event.topics=org/apache/sling/api/resource/Resource/CHANGED",
                "service.description=Simple DAM Asset Event Listener",
                "service.vendor=WKND"
        })
public class DamAssetEventListener implements EventHandler {
    private static final Logger log = LoggerFactory.getLogger(DamAssetEventListener.class);

    @Override
    public void handleEvent(Event event) {
        String resourceType = (String) event.getProperty("resourceType");
        String path = (String) event.getProperty("path");

        if (resourceType != null && resourceType.equals("dam:Asset")) {
            String jcrContentPath = path + "/jcr:content";
            String metadataPath = jcrContentPath + "/metadata";

            if (event.getTopic().equals("org/apache/sling/api/resource/Resource/ADDED")) {
                log.info("New asset added to DAM: {}", path);
                log.info("jcr:content path: {}", jcrContentPath);
                log.info("Metadata node path: {}", metadataPath);
                // You can add your custom logic here for newly added assets.
            } else if (event.getTopic().equals("org/apache/sling/api/resource/Resource/CHANGED")) {
                log.info("Asset modified in DAM: {}", path);
                log.info("jcr:content path: {}", jcrContentPath);
                log.info("Metadata node path: {}", metadataPath);
                // You can add your custom logic here for modified assets.
            }
        }
    }
}