package com.adobe.aem.guides.core.models;

import com.adobe.aem.guides.core.beans.DateUtil;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = {Resource.class, SlingHttpServletRequest.class}, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL, resourceType = "wknd/components/content/jsonComponent")
@Exporter(name = "jackson", selector = "exporter", extensions = "json")
public class JSONExporterModel {

    @ValueMapValue
    private String name;

    @ValueMapValue
    private int age;

    @ValueMapValue
    private String about;

    @ValueMapValue
    private String dateOfBirth;

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getAbout() {
        return about;
    }

    public String getDateOfBirth() {
        return DateUtil.formatDate(dateOfBirth);
    }
}