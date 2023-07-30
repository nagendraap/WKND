package com.adobe.aem.guides.core.workflow;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.dam.api.Asset;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

@Component(property = {
        Constants.SERVICE_DESCRIPTION + "=Custom Workflow Process Step for WKND Project to update/add title",
        Constants.SERVICE_VENDOR + "=Adobe Systems",
        "process.label" + "=WKND Asset Metadata Title Update Process"
})
public class WKNDAssetMetadataTitleUpdate implements WorkflowProcess {

    private final Logger logger = LoggerFactory.getLogger(WKNDAssetMetadataTitleUpdate.class);

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        logger.info("Custom Workflow Process to Update Title for Asset Metadata in WKND Project");

        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        logger.info("Asset Workflow Path: " + payloadPath);

        // Initialize resourceResolver and session
        ResourceResolver resourceResolver = null;
        Session session = null;

        try {
            // Get the ResourceResolver from the workflow session
            resourceResolver = workflowSession.adaptTo(ResourceResolver.class);
            session = resourceResolver.adaptTo(Session.class);

            // Get the Resource object representing the asset
            Resource resource = resourceResolver.getResource(payloadPath);

            if (resource != null && resource.adaptTo(Asset.class) != null) {
                Asset asset = resource.adaptTo(Asset.class);

                // Get the metadata node of the asset
                Node metadataNode = asset.adaptTo(Node.class).getNode("jcr:content/metadata");

                // Read the dc:title property from the metadata node
                if (metadataNode.hasProperty("dc:title")) {
                    Property titleProperty = metadataNode.getProperty("dc:title");
                    String title = titleProperty.getString();
                    logger.info("Asset dc:title property: " + title);
                    // You can now use the 'title' variable as needed in your workflow process
                    metadataNode.setProperty("title", title);

                    // Save the changes made to the Node
                    session.save();
                    logger.info("dc:title property added/updated with value: " + title);

                } else {
                    logger.warn("dc:title property not found in metadata node.");
                }
            } else {
                logger.warn("Invalid payload resource or not an asset.");
            }
        } catch (RepositoryException e) {
            logger.error("Error while getting dc:title property: " + e.getMessage(), e);
        } finally {
            // Make sure to close the resourceResolver and session properly to avoid resource leaks
            if (resourceResolver != null) {
                resourceResolver.close();
            }
            if (session != null) {
                session.logout();
            }
        }
    }
}
