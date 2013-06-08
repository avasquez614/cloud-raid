# ! /bin/sh

echo "Cloud RAID Server starting, enter 'x' to shutdown server, 'r' to restart server ..."
#java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 -Djava.library.path=./jni -cp "./jars/cloud-raid-server.jar:./libs/*:./service/wrapper.jar:./hazelcast/hazelcast-1.9.2.1.jar" org.alfresco.jlan.app.JLANServer jlanConfig.xml
java -Djava.library.path=./jni -cp "./jars/cloud-raid-server.jar:./libs/*:./service/wrapper.jar:./hazelcast/hazelcast-1.9.2.1.jar" org.alfresco.jlan.app.JLANServer jlanConfig.xml
