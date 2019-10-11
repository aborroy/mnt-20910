package org.alfresco.cmis.assoc;

import org.alfresco.cmis.assoc.service.CmisService;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
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

    private static Logger log = LoggerFactory.getLogger(App.class);
    
    private static final int NUM_DOCS = 900;

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
        
        log.info("--Creating documents...");
        
        Folder rootFolder = cmisService.getRootFolder();
        Folder currentFolder = null;
        
        for (int i = 0; i < NUM_DOCS; i++)
        {
            if (i % 100 == 0)
            {
                currentFolder = cmisService.createFolder(rootFolder, String.valueOf(i));
                log.info("created folder " + i + " "+ currentFolder.getId());
            }
            Document doc = cmisService.createDocumentSampleContent(currentFolder, "document-" + i + ".txt");
            log.info("\tcreated document " + i + " "+ doc.getId());
        }
        
        log.info("... documents created---");
        
        watch.stop();
        log.info(watch.prettyPrint());
        
        System.exit(0);

    }

}