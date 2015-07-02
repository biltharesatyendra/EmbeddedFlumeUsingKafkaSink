package EmbeddedFlume.EmbeddedFlume;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.flume.Event;
//import org.apache.flume.agent.embedded.EmbeddedAgent;
import org.apache.flume.event.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import flume.EmbeddedAgent;

public class TestFlumeEmbeddedAgentnew {

  private static final Logger LOGGER = LoggerFactory.getLogger
    (TestFlumeEmbeddedAgentnew.class);
  private final EmbeddedAgent agent = new EmbeddedAgent(
    "UsingFlume");
  private int batchSize = 1;

  public static void main(String args[]) throws Exception {
    TestFlumeEmbeddedAgentnew usingFlumeEmbeddedAgent = new
      TestFlumeEmbeddedAgentnew();
    usingFlumeEmbeddedAgent.run(args);
    int i = 0;
    while (i++ < 10) {
      usingFlumeEmbeddedAgent.generateAndSend();
    }
  }

  public void run(String args[]) throws Exception {
   
 
    Map<String, String> config = new HashMap<String, String>();
    parseHostsAndPort( config);
    
    File dcDir = Files.createTempDir();
    dcDir.deleteOnExit();
    config.put("source.type", "embedded");
    config.put("channel.type", "memory");
    config.put("channel.capacity", "100000");
    config.put("channel.dataDirs", dcDir.toString() + "/data");
    config.put("channel.checkpointDir", dcDir.toString() + "/checkpoint");
    agent.configure(config);
    agent.start();
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      public void run() {
        agent.stop();
      }
    }));
  }

  private void generateAndSend() {
	  Map<String,String> data = new HashMap<String, String>();
	
    List<Event> events = new ArrayList<Event>(100);
    for (int i = 0; i < batchSize; i++) {
     
      Event current = EventBuilder.withBody(
    	        RandomStringUtils.randomAlphanumeric(124).getBytes());
      current.setHeaders(data);
      byte[] myms ;
      String message = "sample message";
      myms = message.getBytes();
	current.setBody(myms );
      events.add(current);
      
    }
    try {
      agent.putAll(events);
    } catch (Throwable e) {
      LOGGER.error(
        "Error while attempting to write data to remote host at " +
          "%s:%s. Events will be dropped!");
      // The client cannot be reused, since we don't know why the
      // connection
      // failed. Destroy this client and create a new one.
    }
  }

  private void parseHostsAndPort(
    Map<String, String> config) {
    String host = "localhost";
    Preconditions.checkNotNull(host, "Remote host cannot be null.");
   
    String port ="41434";
    Preconditions.checkNotNull(port, "Port cannot be null.");

    String[] hostnames = host.split(",");
    int hostCount = hostnames.length;
    final String sinkStr = "sink";
    StringBuilder stringNamesBuilder = new StringBuilder("");
    for (int i = 0; i < hostCount; i++) {
      stringNamesBuilder.append(sinkStr).append(i).append(" ");
    }
    // this puts sinks = sink0 sink1 sink2 sink 3 etc...
    config.put("sinks", stringNamesBuilder.toString());
    final String parameters[] = {"type", "hostname", "port",
                                 "batch-size","kafka.metadata.broker.list"};
     String sink_name =  "sink.KafkaSink";
     System.out.println("using " + hostCount + "sinks ");
    for (int i = 0; i < hostCount; i++) {
      final String currentSinkPrefix = sinkStr + String.valueOf(i) +
        ".";
      config.put(currentSinkPrefix + parameters[0], sink_name);
      config.put(currentSinkPrefix + parameters[1], hostnames[i]);
      config.put(currentSinkPrefix + parameters[2], port);
      config.put(currentSinkPrefix + parameters[3],
        String.valueOf(batchSize));
      config.put(currentSinkPrefix + parameters[4],
    	        "localhost:9092");
    }

    if (hostnames.length > 1) {
      config.put("processor.type", "load_balance");
      config.put("processor.backoff", "true");
      config.put("processor.selector", "round_robin");
      config.put("processor.selector.maxTimeout", "30000");
    } else {
      config.put("processor.type", "default");
    }
  }
}
