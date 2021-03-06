/*******************************************************************************
 * Copyright (c) 2019, 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.concurrent.persistent.fat.multiple;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.testcontainers.containers.JdbcDatabaseContainer;

import componenttest.containers.ExternalTestServiceDockerClientStrategy;
import componenttest.topology.database.container.DatabaseContainerFactory;
import componenttest.topology.impl.LibertyServer;
import componenttest.topology.impl.LibertyServerFactory;

@RunWith(Suite.class)
@SuiteClasses({
    MultiplePersistentExecutorsTest.class,
    MultiplePersistentExecutorsWithFailoverEnabledTest.class
    })
public class FATSuite {
	
	static LibertyServer server = LibertyServerFactory.getLibertyServer("com.ibm.ws.concurrent.persistent.fat.multiple");
	static final JdbcDatabaseContainer<?> testContainer = DatabaseContainerFactory.create();

    @BeforeClass
    public static void beforeSuite() throws Exception {        
        //Allows local tests to switch between using a local docker client, to using a remote docker client. 
        ExternalTestServiceDockerClientStrategy.setupTestcontainers();
        
        testContainer.start();
    }
    
    @AfterClass
    public static void afterSuite() {
    	testContainer.stop();
    }
}