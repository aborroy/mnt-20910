package org.alfresco.cmis.assoc.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.alfresco.cmis.assoc.util.WordGenerator;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Item;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

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
    
    @Autowired
    ResourceLoader resourceLoader;
    
    @Autowired
    WordGenerator wordGenerator;

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

    public Document createDocumentSampleContent(Folder folder, String documentName, String documentExt) throws Exception
    {

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "D:otp:document");
        properties.put(PropertyIds.NAME, documentName + documentExt);

        Resource contentFile = resourceLoader.getResource("classpath:in-the-beggining-command.txt");
        ContentStream contentStream = new ContentStreamImpl(documentName + documentExt, null,
                "text/plain", contentFile.getInputStream());

        Document doc = folder.createDocument(properties, contentStream, VersioningState.MAJOR);
        
        addAspect(doc, "P:cm:titled");
        
        properties = new HashMap<>();
        properties.put("cm:title", wordGenerator.getSentence());
        properties.put("cm:description", wordGenerator.getSentence());
        properties.put("otp:processId", wordGenerator.getWord());
        properties.put("otp:originalPresent", wordGenerator.getWord());
        properties.put("otp:sourceDocId", wordGenerator.getWord());
        properties.put("otp:docType", wordGenerator.getWord());
        properties.put("otp:docBarcode", wordGenerator.getWord());
        properties.put("otp:fileName", wordGenerator.getWord());
        properties.put("otp:fileType", wordGenerator.getWord());
        properties.put("otp:employeeFullName", wordGenerator.getSentence());
        properties.put("otp:employeeLoginSs", wordGenerator.getSentence());
        properties.put("otp:departmentIdSs", wordGenerator.getSentence());
        properties.put("otp:departmentIdSap", wordGenerator.getSentence());
        properties.put("otp:departmentIdPath", wordGenerator.getSentence());
        properties.put("otp:deleted", wordGenerator.getWord());
        properties.put("otp:docInboxEmployeeId", wordGenerator.getWord());
        properties.put("otp:docInboxEmployeeNm", wordGenerator.getSentence());
        properties.put("otp:docMessageId", wordGenerator.getSentence());
        
        updateProperties(doc, properties);
        
        return doc;
        
    }
    
    public Item createSampleItem(Folder folder, String itemName) throws Exception 
    {
        
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "I:otp:backOfficeDocumentForm");
        properties.put(PropertyIds.NAME, itemName);
        
        properties.put("otp:backOfficeDocument", "backOfficeDocument");
        properties.put("otp:backOfficeFormParentProcessName", "backOfficeFormParentProcessName");
        properties.put("otp:backOfficeProcess", "backOfficeProcess");
        properties.put("otp:backOfficeOperation", "backOfficeOperation");
        properties.put("otp:backOfficeWorkflow", "backOfficeWorkflow");
        
        return folder.createItem(properties);
        
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
    
    public CmisObject getAdminUser()
    {
    
        ItemIterable<QueryResult> persons = query("SELECT cmis:objectId FROM cm:person WHERE cm:userName = 'admin'");
        for (QueryResult person : persons)
        {
            return session.getObject(person.getPropertyValueById("cmis:objectId").toString());
        }
        
        return null;
        
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
