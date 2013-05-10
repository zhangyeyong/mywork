#!/bin/sh

java -cp plugins/com.zte.jbundle.home.jar com.zte.jbundle.home.utils.ClearConfiguration
java -XX:+UseParallelGC -XX:PermSize=128M -Dapp=jbundle -Xms512M -Xmx512M -jar plugins/org.eclipse.osgi_3.8.0.v20120529-1548.jar -configuration configuration -console
