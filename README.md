# spring-xadisk

Make it easy to use [XADisk](https://xadisk.java.net/) in a Spring project.

With component-scan

    <context:component-scan base-package="io.strandberg.xadisk"/>

    <bean id="standaloneFileSystemConfiguration" class="org.xadisk.filesystem.standalone.StandaloneFileSystemConfiguration">
        <constructor-arg index="0" value="build/tmp/xadisk"/>
        <constructor-arg index="1" value="instance-1"/>
    </bean>

Without component-scan

    <bean id="standaloneFileSystemConfiguration" class="org.xadisk.filesystem.standalone.StandaloneFileSystemConfiguration">
        <constructor-arg index="0" value="build/tmp/xadisk"/>
        <constructor-arg index="1" value="instance-1"/>
    </bean>

    <bean id="xaDiskSessionFactory" class="io.strandberg.xadisk.XADiskSessionFactory" destroy-method="destroy" init-method="init">
        <constructor-arg ref="transactionManager"/>
        <constructor-arg ref="standaloneFileSystemConfiguration"/>
    </bean>

    <bean id="fileService" class="io.strandberg.xadisk.XADiskFileServiceImpl">
        <constructor-arg ref="xaDiskSessionFactory"/>
    </bean>

For an example, se [atomikos-jta-xadisk-jpa-jms-example](https://github.com/nielspeter/atomikos-jta-xadisk-jpa-jms-example) 
