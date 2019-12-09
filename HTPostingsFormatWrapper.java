package org.apache.lucene.codecs;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.lucene.codecs.perfield.PerFieldPostingsFormat; // javadocs
import org.apache.lucene.index.SegmentWriteState;
import org.apache.lucene.index.SegmentReadState;
import org.apache.lucene.util.NamedSPILoader;
import org.apache.lucene.codecs.lucene50.Lucene50PostingsFormat;
/** 
 *  Wrapper class suggested by Hoss to facilitate loading a Lucene41PostingsFormat 
 *  with non-default min and max Block size params
 *  Hoss email Jan 13 2014
 *  See:  http://lucene.472066.n3.nabble.com/How-to-configure-Solr-PostingsFormat-block-size-tt4179029.html
 *  
 *  */
public  final class HTPostingsFormatWrapper extends PostingsFormat  {
  
  
  //values suggested by McCandless email of Jan 10 2015
  //http://lucene.472066.n3.nabble.com/Details-on-setting-block-parameters-for-Lucene41PostingsFormat-tt4178472.html
   PostingsFormat pf = new Lucene50PostingsFormat(200,398);
  
  public HTPostingsFormatWrapper() {
    super("HTPostingsFormatWrapper");
  }   
 
    
  /** Writes a new segment */
  public  FieldsConsumer fieldsConsumer(SegmentWriteState state) throws IOException
  {
      return pf.fieldsConsumer(state);
  }
  public  FieldsProducer fieldsProducer(SegmentReadState state) throws IOException
  {
      return pf.fieldsProducer(state);
  }

}
