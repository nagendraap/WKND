package com.adobe.aem.guides.core.workflows;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Session;
import java.util.HashMap;
import java.util.Map;

@Component(service = WorkflowProcess.class, property = {
        Constants.SERVICE_DESCRIPTION + "=Custom Workflow Process Step for WKND Project to update/add filename property",
        Constants.SERVICE_VENDOR + "=Adobe Systems",
        "process.label" + "=WKND Asset Metadata Filename Property Update Process"
})
public class WKNDCustomAssetMetadataProcess implements WorkflowProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(WKNDCustomAssetMetadataProcess.class);

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
            throws WorkflowException {
        LOGGER.info("Custom Workflow Process to Add filename Property for Asset Metadata in WKND Project");
        try {
            WorkflowData workflowData = workItem.getWorkflowData();

            // Check if the payload is an asset (JCR_PATH)
            if ("JCR_PATH".equals(workflowData.getPayloadType())) {
                String assetPath = workflowData.getPayload().toString();
                String metadataPath = getMetadataPath(assetPath);

                if (StringUtils.isNotEmpty(metadataPath)) {
                    LOGGER.info("Asset Path: {}", assetPath);
                    LOGGER.info("Metadata Node Path: {}", metadataPath);

                    // Get the filename without extension
                    String filename = getProcessedTitle(assetPath);

                    // Add the filename as properties to the metadata node
                    addFilenameToMetadata(metadataPath, workflowSession.adaptTo(Session.class), filename);
                } else {
                    LOGGER.warn("Metadata not found for Asset Path: {}", assetPath);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error processing asset metadata", e);
            throw new WorkflowException("Error processing asset metadata", e);
        }
    }

    private void addFilenameToMetadata(String metadataPath, Session session, String filename) {
        try {
            Node metadataNode = session.getNode(metadataPath);

            // Add the filename as the new dc:title property
            metadataNode.setProperty("dc:title", filename);
            LOGGER.info("DC Title property value is: {}", filename);

            // Set the filename as the jcr:title property of the metadata node
            metadataNode.setProperty("jcr:title", filename);
            LOGGER.info("JCR Title property value is: {}", filename);

            // Save the changes to the session
            session.save();
            LOGGER.info("Properties updated in metadata node: {}", metadataPath);
        } catch (Exception e) {
            LOGGER.error("Error updating dc:title property in metadata node: {}", metadataPath, e);
        }
    }

    private String getMetadataPath(String assetPath) {
        try (ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(getServiceUserMap())) {
            Resource assetResource = resourceResolver.getResource(assetPath);
            if (assetResource != null) {
                Resource metadataResource = assetResource.getChild("jcr:content/metadata");
                if (metadataResource != null) {
                    return metadataResource.getPath();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error getting metadata path for asset: {}", assetPath, e);
        }
        return null;
    }

    private String getProcessedTitle(String assetPath) {
        // Extract the filename from the assetPath and remove the extension
        return assetPath.substring(assetPath.lastIndexOf('/') + 1);
    }

    private Map<String, Object> getServiceUserMap() {
        Map<String, Object> serviceUserMap = new HashMap<>();
        // Set the service user mapping according to your AEM setup
        serviceUserMap.put(ResourceResolverFactory.SUBSERVICE, "my-subservice");
        return serviceUserMap;
    }
}
