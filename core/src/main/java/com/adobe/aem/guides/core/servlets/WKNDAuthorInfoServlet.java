package com.adobe.aem.guides.core.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = Servlet.class, property = { "sling.servlet.paths=/bin/author-info",
        "sling.servlet.methods=GET" })
public class WKNDAuthorInfoServlet extends SlingAllMethodsServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(WKNDAuthorInfoServlet.class);

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/xml"); // Set content type as XML initially, we'll change to JSON if needed

        String extension = request.getRequestPathInfo().getExtension();
        if ("json".equalsIgnoreCase(extension)) {
            response.setContentType("application/json");
        }

        PrintWriter out = response.getWriter();

        String path = "/content/wknd";
        ResourceResolver resourceResolver = request.getResourceResolver();
        Resource resource = resourceResolver.getResource(path);

        if (resource == null) {
            response.setStatus(SlingHttpServletResponse.SC_NOT_FOUND);
            out.println("Resource not found");
            log.error("Resource not found at path: {}", path);
            return;
        }

        String createdBy = resource.getValueMap().get("jcr:createdBy", String.class);
        if (createdBy == null) {
            log.warn("cq:createdBy property is null for path: {}", path);
        } else {
            log.info("Author info requested for path: {}, createdBy: {}", path, createdBy);
        }

        List<String> modifiedPages = getModifiedPagesByAuthor(createdBy, resourceResolver, path);

        if ("json".equalsIgnoreCase(extension)) {
            out.println("{");
            out.println("  \"author\": \"" + createdBy + "\",");
            out.println("  \"modifiedPages\": [");
            for (int i = 0; i < modifiedPages.size(); i++) {
                out.println("    \"" + modifiedPages.get(i) + "\"" + (i < modifiedPages.size() - 1 ? "," : ""));
            }
            out.println("  ]");
            out.println("}");
        } else { // Default to XML format
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<author-info>");
            out.println("  <author>" + createdBy + "</author>");
            out.println("  <modifiedPages>");
            for (String page : modifiedPages) {
                out.println("    <page>" + page + "</page>");
            }
            out.println("  </modifiedPages>");
            out.println("</author-info>");
        }
    }

    private List<String> getModifiedPagesByAuthor(String author, ResourceResolver resourceResolver, String path) {
        List<String> modifiedPages = new ArrayList<>();
        Resource resource = resourceResolver.getResource(path);
        if (resource != null) {
            collectModifiedPagesByAuthor(resource, author, modifiedPages);
        }
        return modifiedPages;
    }

    private void collectModifiedPagesByAuthor(Resource resource, String author, List<String> modifiedPages) {
        String lastModifiedBy = resource.getValueMap().get("jcr:createdBy", String.class);
        if (author.equals(lastModifiedBy)) {
            modifiedPages.add(resource.getPath());
        }

        Iterator<Resource> children = resource.listChildren();
        while (children.hasNext()) {
            Resource childResource = children.next();
            collectModifiedPagesByAuthor(childResource, author, modifiedPages);
        }
    }
}
