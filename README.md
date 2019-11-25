# lss_java_code
Custom Java code used in HT large scale search

##Draft in progress


## Files
The file **HTPostingsFormatWrapper.java** enables HathiTrust search to use less memory for the OCR fields.  
The file **org.apache.lucene.codecs.PostingsFormat** is a one-line file (aside from the license) that tells the SPI loader to load the HTPostingsFormatWrapper.

## Explanation of code

## What is the problem we are trying to solve
This code reduces the memory use of a Solr index.  Specificly it reduces the size of the xxx file, which is the in-memory index to the indexes on disk.
Because HathiTrust has volumes in over 400 languages, dirty OCR, and we use CommonGrams for efficient phrase search, the indexes tend to have over 2 billion unique terms.  The in-memory 
## Deployment and Use
REDO:
1) Put the attached "HTPostingsFormatWrapper.jar" file in the lib directory for your Solr coresor2) Compile the attached "HTPostingsFormatWrapper.java"  and create a fileMETA-INF/services/org.apache.lucene.codecs.PostingsFormatwith the following content:	"org.apache.lucene.codecs.HTPostingsFormatWrapper"You can use the attached "org.apache.lucene.codecs.PostingsFormat" file.You then need to either put them on the classpath or compile to a jar file and place that jar in the lib directory for your solr cores.


## Recompiling for later versions of Solr/Lucene
Assuming no major changes to the API in future Solr versions the main change would be to update the

## Links to more background

Re: SPI loader :https://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html?is-external=true
https://lucene.apache.org/core/6_6_0/core/org/apache/lucene/codecs/lucene62/package-summary.html#package.description
