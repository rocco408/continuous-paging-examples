# this is using DSE-bundled jar approach (same strategy used by spark/scala setup),
javac -classpath $(dse spark-classpath):. ScanDSE.java
java -classpath $(dse spark-classpath):. ScanDSE
