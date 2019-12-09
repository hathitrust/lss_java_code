# lss_java_code
Custom Java code used in HT large scale search

## Draft in progress


## Files
The file **HTPostingsFormatWrapper.java** enables HathiTrust search to use less memory for the OCR fields.  
The file **org.apache.lucene.codecs.PostingsFormat** is a one-line file (aside from the license) that tells the SPI loader to load the HTPostingsFormatWrapper.


## What is the problem we are trying to solve
This code reduces the memory use of a Solr index.  Specificly it reduces the size of the tip file, which is the in-memory index to the indexes on disk.

Because HathiTrust has volumes in over 400 languages, dirty OCR, and we use CommonGrams for efficient phrase search, the indexes tend to have over 2 billion unique terms. There is an index file which contains one entry for each unique term in an index.  In order to speed up access to this file there is a second file which is read into memory and contains pointers to the file on disk for every Nth term.

Prior to Solr 4 there were settings in solrconfig.xml that could be used to reduce the memory impact of large numbers of terms by changing N ( See https://www.hathitrust.org/blogs/large-scale-search/too-many-words  and https://www.hathitrust.org/blogs/large-scale-search/too-many-words-again for background and how we solved this problem prior to Solr 4)

In Solr 4 a much more efficient index struture was adopted using FST's and the ability to change settings in solrconfig.xml to deal with a very large number of unique terms was removed (Because no one but us seems to have this order of magnitude of unique terms).  

As a replacement for making a change in the solrconfig.xml file we  need this Solr "plugin".

 In Solr 4 and above there is one index file that contains an entry for every term called the "tim" file.  Lucene has an in-memory index to the "tim" file called the "tip" file.  The "tip" file holds pointers to the blocks in the "tim" file.  

With the default block size, the "tip" file holds too many pointers and results in very large memory use. This code reduces the size of the "tip" file by increasing the block size to about 8 times the default block size.  This trades larger sequential disk reads for less memory use. 

See https://lucene.apache.org/core/6_6_0/core/org/apache/lucene/codecs/lucene62/package-summary.html#package.description. 


## Explanation of code

The code instantiates a postings format with HT specific minimum and maximum block sizes (200,398) instead of the default which is (25,48)(See BlockTreeTermsWriter.java DEFAULT_MIN_BLOCK_SIZE and DEFAULT_MAX_BLOCK_SIZE

These numbers should produce a "tip" file with about 1/8th of the memory footprint of a "tip" file created with the default (25 * 8 = 200, the new minimum block size). The max block size  is set to  >= 2*(min-1).

public  final class HTPostingsFormatWrapper extends PostingsFormat  {
     PostingsFormat pf = new Lucene41PostingsFormat(200,398);



## Deployment and Use

### Creating a special jar file.

1.   Compile  "HTPostingsFormatWrapper.java"
2.   Copy the HTPostingsFormatWrapper.class file to a new empty directory

3.    In that directory create the following subdirectories

   * META-INF
   * META-INF/services
   * org/apache/lucene/codecs

4.   Put the **HTPostingsFormatWrapper.class** file in **org/apache/lucene/codecs**
5.   Put the *org.apache.lucene.codecs.PostingsFormat** file in **META-INF/services**

6.   Create a jar:

```
jar -cf HTPostingsFormat.jar META-INF META-INF/services/ META-INF/services/org.apache.lucene.codecs.PostingsFormat  org org/apache/lucene/codecs/HTPostingsFormatWrapper.class
```

The resulting file should look like this:

```
jar -tvf HTPostingsFormatWrapper.jar
     0 Thu Aug 10 12:12:42 EDT 2017 META-INF/
    69 Thu Aug 10 12:12:42 EDT 2017 META-INF/MANIFEST.MF
     0 Thu Aug 10 12:10:48 EDT 2017 META-INF/services/
   842 Thu Aug 10 12:10:48 EDT 2017 META-INF/services/org.apache.lucene.codecs.PostingsFormat
     0 Thu Aug 10 12:09:14 EDT 2017 org/
     0 Thu Aug 10 12:09:14 EDT 2017 org/apache/
     0 Thu Aug 10 12:09:14 EDT 2017 org/apache/lucene/
     0 Thu Aug 10 12:09:20 EDT 2017 org/apache/lucene/codecs/
  1132 Thu Aug 10 12:00:42 EDT 2017 org/apache/lucene/codecs/HTPostingsFormatWrapper.class
```
### Deployment

Put the resulting jar file in the SOLR_HOME/lib directory.  In production we put this in a shared directory and put symlinks in the directory for each core.  
For example 
```
/htsolr/lss/shared/lib/HTPostingsFormat.jar
/htsolr/lss/cores/1/core-1x/lib -> /htsolr/serve/lss-shared/lib
```

## Recompiling for later versions of Solr/Lucene

I aways check the release notes for changes between the version we are running on and any new version we want to upgrade to try to determine if there are any significant changes.  
Assuming no major changes to the API in future Solr versions the main change would be to update the following line to the appropriate newer PostingsFormat:

PostingsFormat pf = new Lucene50PostingsFormat(200,398);

After the move from Solr/Lucene 4.1 to Solr/Lucene 5, the PostingsFormat has remained stable.  However the best thing to check besides release notes is the JavaDoc for the base PostingsFormat class.  For example for Solr 8.3 (http://lucene.apache.org/core/8_3_0/core/org/apache/lucene/codecs/PostingsFormat.html) you can see in the JavaDoc that the two "Direct Known Subclasses" are Lucene50PostingsFormat and PerFieldPostingsFormat.  If there were a Lucene83PostingsFormat listed, it would be the one to use.


## Links to more background

Re: SPI loader :https://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html?is-external=true
https://lucene.apache.org/core/6_6_0/core/org/apache/lucene/codecs/lucene62/package-summary.html#package.description
https://lucene.472066.n3.nabble.com/Details-on-setting-block-parameters-for-Lucene41PostingsFormat-td4178472.html
https://lucene.472066.n3.nabble.com/How-to-configure-Solr-PostingsFormat-block-size-td4179029.html

From Mike McCandless:

"The first int to Lucene41PostingsFormat is the min block size (default
25) and the second is the max (default 48) for the block tree terms
dict.

The max must be >= 2*(min-1).

Since you were using 8X the default before, maybe try min=200 and
max=398?  However, block tree should have been more RAM efficient than
3.x's terms index... if you run CheckIndex with -verbose it will print
additional details about the block structure of your terms indices...

Mike McCandless"

##TODO: run checkindex --verbose on a recent snap (so as not to affect production)
See if it gives any clues about block structure.

