package org.alfresco.cmis.assoc.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

/**
 * CMIS Service to handle operations within the session.
 * 
 * @author aborroy
 *
 */
@Service
public class CmisService
{

    // Set values from "application.properties" file
    @Value("${alfresco.repository.url}")
    String alfrescoUrl;
    @Value("${alfresco.repository.user}")
    String alfrescoUser;
    @Value("${alfresco.repository.pass}")
    String alfrescoPass;

    // CMIS living session
    private Session session;

    @PostConstruct
    public void init()
    {

        String alfrescoBrowserUrl = alfrescoUrl + "/api/-default-/public/cmis/versions/1.1/browser";

        Map<String, String> parameter = new HashMap<String, String>();

        parameter.put(SessionParameter.USER, alfrescoUser);
        parameter.put(SessionParameter.PASSWORD, alfrescoPass);

        parameter.put(SessionParameter.BROWSER_URL, alfrescoBrowserUrl);
        parameter.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());

        SessionFactory factory = SessionFactoryImpl.newInstance();
        session = factory.getRepositories(parameter).get(0).createSession();

    }
    
    public Folder getRootFolder()
    {
        return session.getRootFolder();
    }
    
    public Folder createFolder(Folder parentFolder, String name)
    {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        properties.put(PropertyIds.NAME, name);
        return parentFolder.createFolder(properties);
    }

    public Document createDocument(Folder folder, String documentName)
    {

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        properties.put(PropertyIds.NAME, documentName);

        byte[] content = "Hello World!".getBytes();
        InputStream stream = new ByteArrayInputStream(content);
        ContentStream contentStream = new ContentStreamImpl(documentName, BigInteger.valueOf(content.length),
                "text/plain", stream);

        return folder.createDocument(properties, contentStream, VersioningState.MAJOR);
    }

    public Document createDocumentSampleContent(Folder folder, String documentName) throws Exception
    {

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        properties.put(PropertyIds.NAME, documentName);

        File contentFile = ResourceUtils.getFile("classpath:cryptonomicon.txt");
        ContentStream contentStream = new ContentStreamImpl(documentName, BigInteger.valueOf(contentFile.length()),
                "text/plain", new FileInputStream(contentFile));

        Document doc = folder.createDocument(properties, contentStream, VersioningState.MAJOR);
        
        addAspect(doc, "P:cm:titled", 
                "P:cm:projectsummary", 
                "P:cm:dublincore",
                "P:cm:summarizable",
                "P:cm:geographic",
                "P:exif:exif",
                "P:audio:audio");
        
        properties = new HashMap<>();
        properties.put("cm:title", "Title");
        properties.put("cm:description", "Description");
        properties.put("cm:summaryWebscript", "Summary WebScript");
        properties.put("cm:publisher", "Publisher");
        properties.put("cm:contributor", "Contributor");
        properties.put("cm:type", "Type");
        properties.put("cm:identifier", "Identifier");
        properties.put("cm:dcsource", "DC Source");
        properties.put("cm:coverage", "Coverage");
        properties.put("cm:rights", "Rights");
        properties.put("cm:subject", "Subject");
        properties.put("cm:summary", "Summary");
        properties.put("cm:latitude", 0.0);
        properties.put("cm:longitude", 0.0);
        properties.put("exif:pixelXDimension", 0);
        properties.put("exif:pixelYDimension", 0);
        properties.put("exif:exposureTime", 0.0);
        properties.put("exif:fNumber", 0.0);
        properties.put("exif:focalLength", 0.0);
        properties.put("exif:isoSpeedRatings", "1000");
        properties.put("exif:manufacturer", "Manufacturer");
        properties.put("exif:model", "Model");
        properties.put("exif:software", "Software");
        properties.put("exif:orientation", 1);
        properties.put("exif:xResolution", 0.0);
        properties.put("exif:yResolution", 0.0);
        properties.put("exif:resolutionUnit", "Resolution Unit");
        properties.put("audio:album", "Album");
        properties.put("audio:artist", "Artist");
        properties.put("audio:composer", "Composer");
        properties.put("audio:engineer", "Engineer");
        properties.put("audio:genre", "Genre");
        properties.put("audio:trackNumber", 0);
        properties.put("audio:sampleRate", 0);
        properties.put("audio:sampleType", "Sample Type");
        properties.put("audio:channelType", "Channel Type");
        properties.put("audio:compressor", "Compressor");
        updateProperties(doc, properties);
        
        return doc;
    }
    
    public ObjectId createRelationship(CmisObject sourceObject, CmisObject targetObject, String relationshipName)
    {

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, "a new relationship");
        properties.put(PropertyIds.OBJECT_TYPE_ID, relationshipName);
        properties.put(PropertyIds.SOURCE_ID, sourceObject.getId());
        properties.put(PropertyIds.TARGET_ID, targetObject.getId());

        return session.createRelationship(properties);

    }

    public void addAspect(CmisObject cmisObject, String... aspects)
    {

        List<Object> currentAspects = cmisObject.getProperty("cmis:secondaryObjectTypeIds").getValues();
        Map<String, Object> aspectListProps = new HashMap<String, Object>();
        for (String aspect : aspects) {
            if (!currentAspects.contains(aspect))
            {
                currentAspects.add(aspect);
            }
        }
        aspectListProps.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, currentAspects);
        cmisObject.updateProperties(aspectListProps);

    }

    public void updateProperties(CmisObject cmisObject, Map<String, Object> properties)
    {
        cmisObject.updateProperties(properties);
    }

    public ItemIterable<Relationship> getRelationships(ObjectId objectId, String relationshipName)
    {

        ObjectType typeDefinition = session.getTypeDefinition(relationshipName);
        OperationContext operationContext = session.createOperationContext();
        return session.getRelationships(objectId, true, RelationshipDirection.EITHER, typeDefinition, operationContext);

    }

    public ItemIterable<QueryResult> query(String query)
    {
        return session.query(query, false);
    }

    public void remove(CmisObject object)
    {

        if (BaseTypeId.CMIS_FOLDER.equals(object.getBaseTypeId()))
        {
            Folder folder = (Folder) object;
            ItemIterable<CmisObject> children = folder.getChildren();
            for (CmisObject child : children)
            {
                remove(child);
            }
        }
        session.delete(object);
    }

}
