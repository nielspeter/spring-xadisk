/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.strandberg.xadisk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.xadisk.bridge.proxies.interfaces.XAFileSystem;
import org.xadisk.bridge.proxies.interfaces.XAFileSystemProxy;
import org.xadisk.bridge.proxies.interfaces.XASession;
import org.xadisk.filesystem.standalone.StandaloneFileSystemConfiguration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.transaction.*;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Inspired by org.hibernate.context.JTASessionContext
 *
 * @author Niels Peter Strandberg
 */
@Component
public class XADiskSessionFactory {

    private TransactionManager transactionManager;
    private StandaloneFileSystemConfiguration standaloneFileSystemConfiguration;
    private XAFileSystem xaFileSystem;

    private Map<Transaction, XASession> xaSessionMap;

    @Autowired
    public XADiskSessionFactory(JtaTransactionManager jtaTransactionManager, StandaloneFileSystemConfiguration standaloneFileSystemConfiguration) throws InterruptedException {
        this.transactionManager = jtaTransactionManager.getTransactionManager();
        this.standaloneFileSystemConfiguration = standaloneFileSystemConfiguration;
        xaSessionMap = Collections.synchronizedMap(new WeakHashMap<Transaction, XASession>());
    }

    public XASession getXASession() throws SystemException, RollbackException {

        final Transaction transaction = transactionManager.getTransaction();

        synchronized (transaction) {

            // get the xaSession associated with the current transaction
            XASession xaSession = xaSessionMap.get(transaction);

            if (xaSession == null) {
                xaSession = xaFileSystem.createSessionForXATransaction();
                transaction.enlistResource(xaSession.getXAResource());
                transaction.registerSynchronization(new Synchronization() {
                    public void beforeCompletion() {
                    }

                    public void afterCompletion(int status) {
                        xaSessionMap.remove(transaction);
                    }
                });

                xaSessionMap.put(transaction, xaSession);
            }

            return xaSession;
        }
    }

    @PostConstruct
    public void init() throws InterruptedException {
        xaFileSystem = XAFileSystemProxy.bootNativeXAFileSystem(standaloneFileSystemConfiguration);
        xaFileSystem.waitForBootup(-1);
    }

    @PreDestroy
    public void destroy() throws IOException {
        xaFileSystem.shutdown();
    }

}
