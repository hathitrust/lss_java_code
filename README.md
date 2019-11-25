# lss_java_code
Custom Java code used in HT large scale search

##Draft in progress


## Files
The file **HTPostingsFormatWrapper.java** enables HathiTrust search to use less memory for the OCR fields.  
The file **org.apache.lucene.codecs.PostingsFormat** is a one-line file (aside from the license) that tells the SPI loader to load the HTPostingsFormatWrapper.


## What is the problem we are trying to solve
This code reduces the memory use of a Solr index.  Specificly it reduces the size of the tip file, which is the in-memory index to the indexes on disk.

Because HathiTrust has volumes in over 400 languages, dirty OCR, and we use CommonGrams for efficient phrase search, the indexes tend to have over 2 billion unique terms.  There is one index file that contains an entry for every term called the "tim" file.  Lucene has an in-memory index to the "tim" file for a fraction of the terms, with pointers to the "tim" file


The "tip" file is a Lucene data structure that is read into memory and used as an in memory index to the index (tim) file. The "tim" file has one entry for every term in the index (for every field in the index) See https://lucene.apache.org/core/6_6_0/core/org/apache/lucene/codecs/lucene62/package-summary.html#package.description. 


## Explanation of code
The code instantiates a postings format with HT specific minimum and maximum block sizes (200,398) instead of the default which is (25,48)(See BlockTreeTermsWriter.java DEFAULT_MIN_BLOCK_SIZE and DEFAULT_MAX_BLOCK_SIZE

public  final class HTPostingsFormatWrapper extends PostingsFormat  {
     PostingsFormat pf = new Lucene41PostingsFormat(200,398);




## Deployment and Use
REDO:
1) Put the attached "HTPostingsFormatWrapper.jar" file in the lib directory for your Solr coresor2) Compile the attached "HTPostingsFormatWrapper.java"  and create a fileMETA-INF/services/org.apache.lucene.codecs.PostingsFormatwith the following content:	"org.apache.lucene.codecs.HTPostingsFormatWrapper"You can use the attached "org.apache.lucene.codecs.PostingsFormat" file.You then need to either put them on the classpath or compile to a jar file and place that jar in the lib directory for your solr cores.


## Recompiling for later versions of Solr/Lucene
Assuming no major changes to the API in future Solr versions the main change would be to update the following line to the appropriate newer PostingsFormat:

PostingsFormat pf = new Lucene41PostingsFormat(200,398);

Note that this is not always the latest postings format as some have special purposes.
Question about whether we ever need to change the numbers?
Check Mikes email

"The first int to Lucene41PostingsFormat is the min block size (default
25) and the second is the max (default 48) for the block tree terms
dict.

The max must be >= 2*(min-1).

Since you were using 8X the default before, maybe try min=200 and
max=398?  However, block tree should have been more RAM efficient than
3.x's terms index... if you run CheckIndex with -verbose it will print
additional details about the block structure of your terms indices...

Mike McCandless"


## Links to more background

Re: SPI loader :https://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html?is-external=true
https://lucene.apache.org/core/6_6_0/core/org/apache/lucene/codecs/lucene62/package-summary.html#package.description
