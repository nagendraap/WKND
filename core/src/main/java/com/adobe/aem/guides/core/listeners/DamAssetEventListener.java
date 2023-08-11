package com.adobe.aem.guides.core.listeners;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import java.util.HashMap;
import java.util.Map;

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

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

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
                setFilenameAsTitle(metadataPath, path);
            } else if (event.getTopic().equals("org/apache/sling/api/resource/Resource/CHANGED")) {
                log.info("Asset modified in DAM: {}", path);
                log.info("jcr:content path: {}", jcrContentPath);
                log.info("Metadata node path: {}", metadataPath);
                setFilenameAsTitle(metadataPath, path);
            }
        }
    }

    private void setFilenameAsTitle(String metadataPath, String assetPath) {
        try (ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(getServiceUserMap())) {
            Resource metadataResource = resourceResolver.getResource(metadataPath);
            if (metadataResource != null) {
                Node metadataNode = metadataResource.adaptTo(Node.class);
                if (metadataNode != null) {
                    String processedTitle = getProcessedTitle(assetPath);
                    metadataNode.setProperty("title", processedTitle);
                    metadataNode.getSession().save();
                    log.info("Title property updated with the processed filename.");
                } else {
                    log.warn("Unable to adapt metadataResource to a Node.");
                }
            } else {
                log.warn("Metadata resource not found at path: {}", metadataPath);
            }
        } catch (Exception e) {
            log.error("Error while updating title property with the processed filename: {}", e.getMessage(), e);
        }
    }

    private String getProcessedTitle(String assetPath) {
        String filenameWithExtension = assetPath.substring(assetPath.lastIndexOf("/") + 1);
        int extensionIndex = filenameWithExtension.lastIndexOf(".");
        if (extensionIndex > 0) {
            filenameWithExtension = filenameWithExtension.substring(0, extensionIndex);
        }

        String[] parts = filenameWithExtension.split("-");
        StringBuilder processedTitle = new StringBuilder();
        for (String part : parts) {
            processedTitle.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                processedTitle.append(part.substring(1));
            }
            processedTitle.append(" ");
        }
        return processedTitle.toString().trim();
    }

    private Map<String, Object> getServiceUserMap() {
        Map<String, Object> serviceUserMap = new HashMap<>();
        // Set the service user mapping according to your AEM setup
        serviceUserMap.put(ResourceResolverFactory.SUBSERVICE, "my-subservice");
        return serviceUserMap;
    }
}
