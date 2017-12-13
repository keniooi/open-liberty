package com.ibm.ws.microprofile.faulttolerance.test;

import java.io.File;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * We weave in the hamcrest jar that is used by some of the tck tests
 */
public class ArchiveProcessor implements ApplicationArchiveProcessor {
//TODO Check that some of the FaultTolerance tests actually use hamcrest!
    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor#process(org.jboss.shrinkwrap.api.Archive, org.jboss.arquillian.test.spi.TestClass)
     */
    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        if (applicationArchive instanceof WebArchive) {
            File hamcrest = new File("../lib/hamcrest-all-1.3.jar");
            System.out.println("WLP: Adding Jar:" + hamcrest.getAbsolutePath());
            ((WebArchive) applicationArchive).addAsLibraries(hamcrest);
        }
    }
}