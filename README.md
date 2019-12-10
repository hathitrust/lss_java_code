# lss_java_code
Custom Java code used in HT large scale search

## Draft in progress


## Files
The file **HTPostingsFormatWrapper.java** enables HathiTrust search to use less memory for the OCR fields.  
The file **org.apache.lucene.codecs.PostingsFormat** is a one-line file (aside from the license) that tells the SPI loader to load the HTPostingsFormatWrapper.


## What is the problem we are trying to solve
This code reduces the memory use of a Solr index.  Specificly it reduces the size of the tip file, which is the in-memory index to the indexes on disk.


## Explanation of code

```
public  final class HTPostingsFormatWrapper extends PostingsFormat  {
  PostingsFormat pf = new Lucene50PostingsFormat(200,398);
  
  public HTPostingsFormatWrapper() {
    super("HTPostingsFormatWrapper");
  }
 }
```


The code instantiates a postings format with HT specific minimum and maximum block sizes (200,398) instead of the default which is (25,48)(See BlockTreeTermsWriter.java DEFAULT_MIN_BLOCK_SIZE and DEFAULT_MAX_BLOCK_SIZE

In Solr 4 and above there is one index file that contains an entry for every term called the "tim" file.  Lucene has an in-memory index to the "tim" file called the "tip" file.  The "tip" file holds pointers to the blocks in the "tim" file.  

With the default block size, the "tip" file holds too many pointers and results in very large memory use. This code reduces the size of the "tip" file by increasing the block size to about 8 times the default block size.  Larger block size means smaller number of total blocks needed, which means a smaller number of pointers.

See *Background details* (below) for more details



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

## Background details

Because HathiTrust has volumes in over 400 languages, dirty OCR, and we bigrams and unigrams for CJK and we use CommonGrams for efficient phrase search, the indexes tend to have over 2 billion unique terms. There is an index file which contains one entry for each unique term in an index.  In order to speed up access to this file there is a second file which is read into memory and contains pointers to the file on disk for every Nth term.

Prior to Solr 4 there were settings in solrconfig.xml that could be used to reduce the memory impact of large numbers of terms by changing N ( See https://www.hathitrust.org/blogs/large-scale-search/too-many-words  and https://www.hathitrust.org/blogs/large-scale-search/too-many-words-again for background and how we solved this problem prior to Solr 4)

In Solr 4 a much more efficient index struture was adopted using FST's and the ability to change settings in solrconfig.xml to deal with a very large number of unique terms was removed (Because no one but us seems to have this order of magnitude of unique terms).  

As a replacement for making a change in the solrconfig.xml file we  need this Solr "plugin" which uses the JAVA Service Provider Interface (SPI) to tell Solr to use the provided code. See https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html

 In Solr 4 and above there is one index file that contains an entry for every term called the "tim" file.  Lucene has an in-memory index to the "tim" file called the "tip" file.  The "tip" file holds pointers to the blocks in the "tim" file.  

With the default block size, the "tip" file holds too many pointers and results in very large memory use. This code reduces the size of the "tip" file by increasing the block size to about 8 times the default block size.  This trades larger sequential disk reads for less memory use. 

See https://lucene.apache.org/core/6_6_0/core/org/apache/lucene/codecs/lucene62/package-summary.html#package.description. 



## Background details old

Because HathiTrust has volumes in over 400 languages, dirty OCR, and we use CommonGrams for efficient phrase search, the indexes tend to have over 2 billion unique terms. There is an index file which contains one entry for each unique term in an index.  In order to speed up access to this file there is a second file which is read into memory and contains pointers to the file on disk for every Nth term.

Prior to Solr 4 there were settings in solrconfig.xml that could be used to reduce the memory impact of large numbers of terms by changing N ( See https://www.hathitrust.org/blogs/large-scale-search/too-many-words  and https://www.hathitrust.org/blogs/large-scale-search/too-many-words-again for background and how we solved this problem prior to Solr 4)

In Solr 4 a much more efficient index struture was adopted using FST's and the ability to change settings in solrconfig.xml to deal with a very large number of unique terms was removed (Because no one but us seems to have this order of magnitude of unique terms).  

As a replacement for making a change in the solrconfig.xml file we  need this Solr "plugin".

 In Solr 4 and above there is one index file that contains an entry for every term called the "tim" file.  Lucene has an in-memory index to the "tim" file called the "tip" file.  The "tip" file holds pointers to the blocks in the "tim" file.  

With the default block size, the "tip" file holds too many pointers and results in very large memory use. This code reduces the size of the "tip" file by increasing the block size to about 8 times the default block size.  This trades larger sequential disk reads for less memory use. 

See https://lucene.apache.org/core/6_6_0/core/org/apache/lucene/codecs/lucene62/package-summary.html#package.description. 

## Considerations for future modification

### Move to AWS

Part of the reason we have a very large number of unique terms per index (shard), is that we currently spread the index over 12 shards.  So each index holds over a million very large documents.  Our indexes are now close to a terabyte in size.  Very few Solr users have indexes over about 100GB.

With the move to AWS, we have the possiblity of testing a deployment that is more like most of the users of Solr.  We might want to experiment with spreading the index over 60 or 100 shards.  If we do that, we could test to see if we still have an issue with memory and too many unique terms per shard.

### Tuning the block size settings
It should be possible to fine-tune the block size.  There is a trade-off between memory use and the number of disk accesses. Testing with the previous (Solr before 4.0) version showed that the increase in number of disk accesses was insignificant in terms of overall query response time.  If a similar test with Solr 6 or above shows a similar lack of significant performance penalty, and memory use does not need to be further reduced, further tuning would be unnecessary.

### Testing

To test we would build two 1 shard size indexes on buzz.  
We then configure a Solr with 1 core pointing to one of the indexes and run test queries while monitoring memory use.
Taking down Solr, emptying the OS cache and then configuring the Solr to point to the other index we repeat the test.
We can then compare both memory use and query response time.

This can be done with  the Solr default block size settings  and one with our modification to get a baseline.
If there is a significant difference in query response time, further tuning could be done.
The tests could be repeated with changes to the block size, although each test of block size settings requires building a new index.

## Links to more background

* Re: SPI loader :https://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html?is-external=true
* https://lucene.apache.org/core/6_6_0/core/org/apache/lucene/codecs/lucene62/package-summary.html#package.description
* https://lucene.472066.n3.nabble.com/Details-on-setting-block-parameters-for-Lucene41PostingsFormat-td4178472.html
* https://lucene.472066.n3.nabble.com/How-to-configure-Solr-PostingsFormat-block-size-td4179029.html

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

