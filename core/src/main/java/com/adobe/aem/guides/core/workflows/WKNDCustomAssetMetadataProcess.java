package com.adobe.aem.guides.core.workflows;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import java.util.HashMap;
import java.util.Map;


@Component(property = {
        Constants.SERVICE_DESCRIPTION + "=Custom Workflow Process Step for WKND Project to update/add filename property",
        Constants.SERVICE_VENDOR + "=Adobe Systems",
        "process.label" + "=WKND Asset Metadata Filename Property Update Process"
})
public class WKNDCustomAssetMetadataProcess implements WorkflowProcess {

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private static final Logger LOGGER = LoggerFactory.getLogger(WKNDCustomAssetMetadataProcess.class);

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

                    // Get the filename without extension and with proper formatting
                    String formattedFilename = getProcessedTitle(assetPath);

                    // Add the formattedFilename as a property to the metadata node
                    addFormattedFilenamePropertyToMetadata(metadataPath, workflowSession.adaptTo(Session.class), formattedFilename);
                } else {
                    LOGGER.warn("Metadata not found for Asset Path: {}", assetPath);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error processing asset metadata", e);
            throw new WorkflowException("Error processing asset metadata", e);
        }
    }


    private void addFormattedFilenamePropertyToMetadata(String metadataPath, Session session, String formattedFilename) {
        try {
            Node metadataNode = session.getNode(metadataPath);

            // Add the formattedFilename as a custom property to the metadata node
            metadataNode.setProperty("formattedFilename", formattedFilename);

            // Save the changes to the session
            session.save();
            LOGGER.info("Formatted Filename property added to metadata node: {}", metadataPath);
            LOGGER.info("Formatted Filename property added is: {}", formattedFilename);

        } catch (Exception e) {
            LOGGER.error("Error adding Formatted Filename property to metadata node: {}", metadataPath, e);
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
        String filename = assetPath.substring(assetPath.lastIndexOf('/') + 1);
        String filenameWithoutExtension = filename.substring(0, filename.lastIndexOf('.'));

        // Split the filename by hyphens and capitalize each word
        String[] parts = filenameWithoutExtension.split("-");
        StringBuilder formattedFilename = new StringBuilder();
        for (String part : parts) {
            formattedFilename.append(StringUtils.capitalize(part)).append(" ");
        }
        formattedFilename.deleteCharAt(formattedFilename.length() - 1); // Remove the last space

        return formattedFilename.toString();
    }

    private Map<String, Object> getServiceUserMap() {
        Map<String, Object> serviceUserMap = new HashMap<>();
        // Set the service user mapping according to your AEM setup
        serviceUserMap.put(ResourceResolverFactory.SUBSERVICE, "my-subservice");
        return serviceUserMap;
    }
}