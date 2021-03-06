package org.alfresco.cmis.assoc;

import org.alfresco.cmis.assoc.service.CmisService;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Item;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StopWatch;

@SpringBootApplication
public class App implements CommandLineRunner
{

    private static Logger LOG = LoggerFactory.getLogger(App.class);
    
    private static final int NUM_DOCS = 1000;
    private static final boolean ADD_RELATIONSHIPS = false;

    @Autowired
    CmisService cmisService;

    public static void main(String[] args)
    {
        SpringApplication.run(App.class, args);
    }

    public void run(String... args) throws Exception
    {

        StopWatch watch = new StopWatch("CMIS Operations");
        watch.start("Document creation");
        
        LOG.info("--Getting admin user ObjectId...");
        CmisObject adminUser = cmisService.getAdminUser();
        LOG.info("... " + adminUser.getId());
        
        LOG.info("--Creating documents...");
        
        Folder rootFolder = cmisService.getRootFolder();
        Folder currentFolder = null;
        
        for (int i = 0; i < NUM_DOCS; i++)
        {
            if (i % 100 == 0)
            {
                currentFolder = cmisService.createFolder(rootFolder, String.valueOf(i));
                LOG.info("created folder " + i + " " + currentFolder.getId());
            }
            
            Document doc = cmisService.createDocumentSampleContent(currentFolder, "document-" + i, ".txt");
            LOG.info("\tcreated document " + i + " "+ doc.getId());
            
            if (ADD_RELATIONSHIPS)
            {
                Item item = cmisService.createSampleItem(currentFolder, "document-" + i);
                LOG.info("\t\tcreated item " + i + " "+ item.getId());
                
                ObjectId relationship = cmisService.createRelationship(doc, item, "R:otp:documentBackOfficeDocumentForm");
                LOG.info("\t\tcreated doc relationship " + relationship);
                
                cmisService.createRelationship(doc, adminUser, "R:otp:documentNew");
                cmisService.createRelationship(doc, adminUser, "R:otp:documentApproval");
                cmisService.createRelationship(doc, adminUser, "R:otp:documentAgreed");
                cmisService.createRelationship(doc, adminUser, "R:otp:documentExecution");
                cmisService.createRelationship(doc, adminUser, "R:otp:documentStarter");
                LOG.info("\t\tcreated user relationships with " + adminUser.getId());
            }

        }
        
        LOG.info("... documents created---");
        
        watch.stop();
        LOG.info(watch.prettyPrint());
        
        System.exit(0);

    }

}