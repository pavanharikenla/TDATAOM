package com.vz.tg.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.vz.tdataom.model.Tweet;
import com.vz.tg.util.TweetIdExtractor;

public class SolrIndexer {

	public static String line;
	public static final String url = "http://113.128.164.246:8983/solr/tg_core"; //Solr Instance
    public static SolrServer server ;
    private static Map<Integer, String> sentimentMap = new HashMap<Integer, String>();

	/** 
	 * Static Initializer block to perform a look-up of the Sentiment based on the score returned by Core NLP
	 */
    static {
		/** Reference:
		 *  0: very negative
	    	1: slightly negative
	    	2: neutral
	    	3: slightly positive
	    	4: very positive
		 */
		
		sentimentMap.put((Integer) 0, "Very Negative");
		sentimentMap.put((Integer) 1, "Slightly Negative");
		sentimentMap.put((Integer) 2, "Neutral");
		sentimentMap.put((Integer) 3, "Slightly Positive");
		sentimentMap.put((Integer) 4, "Very Positive");
	}
   /**
    * Method to index the Tweets from a flat file
    * @throws FileNotFoundException
    * @throws IOException
    * @throws SolrServerException
    */
	public static void indexJsonTweets() throws FileNotFoundException,
			IOException, SolrServerException {

		String[] toppings = new String[1];
		toppings[0] = "GNIP_twitterDumponFile.json";
		SolrServer server = new HttpSolrServer(url);
		
		StanfordCoreNLPService.init();
		Map<String,String> sentMap = new HashMap<String,String>();
		
		int counter = 0;
		String tweetDate = "";
		for(String smallFile : toppings){
			try (InputStream fis = new FileInputStream(smallFile);
					InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
					BufferedReader br = new BufferedReader(isr);) {
		
				while ((line = br.readLine()) != null) {
					if (line.length() > 0) {
						try {
							if(counter < 3233)
								tweetDate = "2015-07-31T16:00:31.000Z";
							else if (counter >= 3233 && counter < 5999)
								tweetDate = "2015-07-30T12:10:31.000Z";
							else if (counter >= 5999 && counter < 9134)
								tweetDate = "2015-07-29T10:22:31.000Z";
							else if (counter >= 9134 && counter < 14474)
								tweetDate = "2015-07-28T13:34:31.000Z";
							else if (counter >= 14474 && counter < 19934)
								tweetDate = "2015-07-27T18:00:31.000Z";
							else if (counter >= 19934 && counter < 24000)
								tweetDate = "2015-07-26T21:55:31.000Z";
							else if (counter >= 24000)
								tweetDate = "2015-07-25T15:12:31.000Z";
							
							Tweet p = (Tweet) new Gson().fromJson(line, Tweet.class);
							Long tweetId = TweetIdExtractor.extractTweetId(p.getId());
							DateTimeZone zoneUTC = DateTimeZone.UTC;
							DateTime jodaDate = new DateTime(tweetDate, zoneUTC);
							Date dateNew = jodaDate.toDate();
							System.out.println("Date: "+dateNew);
							
							sentMap = StanfordCoreNLPService.findSentiment(p.getBody(), sentMap);
							String sentiment =  sentimentMap.get(Ints.tryParse(sentMap.get("score")));
							
							SolrInputDocument tweet = new SolrInputDocument();
							
							tweet.addField("id", tweetId);
							tweet.addField("tweet_content", p.getBody());
							tweet.addField("tweetPostedTime", dateNew);
							
							tweet.addField("display_name", p.getActor().getDisplayName());
							tweet.addField("followers", p.getActor().getFollowersCount());
							tweet.addField("fiends", p.getActor().getFriendsCount());
							tweet.addField("actorLink", p.getActor().getLink());
							tweet.addField("actorObjectType", p.getActor().getObjectType());
							tweet.addField("actorKlout", p.getGnip().getKloutScore());
							
							tweet.addField("sentimentScore", Ints.tryParse(sentMap.get("score")));
							
							tweet.addField("sentiment", sentiment);
							tweet.addField("tweet_category", sentMap.get("category"));
							int age = generateAge();
							tweet.addField("age", age);
							tweet.addField("agegroup", getAgeGroup(age));
							
							server.add(tweet);
							server.commit();
							
							counter++;
						} catch (JsonSyntaxException e) {
							System.out.println(e);
	
						}
					}
				}
			}
		}	

	}
	
	/**
	 * Method to search the indexed data from Solr
	 * @throws SolrServerException
	 */
    public static void query_data() throws SolrServerException {

		server = new HttpSolrServer(url);
		SolrQuery query = new SolrQuery();
		query.setQuery("*:*");
		//query.addFilterQuery("tweet_content:*love*");
		/*query.addFilterQuery("body:sprint");
		query.setFields("id", "wireless_service_provider", "body");
		query.set("dataType", "text_general");*/
		
		query.setStart(0);
		query.setRows(1000);
		QueryResponse response = server.query(query);
		SolrDocumentList results = response.getResults();
		System.out.println("Number of indexed records: " + results.getNumFound());
		/*for (int i = 0; i < results.size(); i++) {
			System.out.println(results.get(i));
		}*/
		
	}

	/** 
	 * Method to delete the indexed data from Solr
	 * @throws SolrServerException
	 * @throws IOException
	 */
	public static void deleteAllIndexData() throws SolrServerException, IOException {
		SolrServer server = new HttpSolrServer(url);
		server.deleteByQuery("*:*");//
		server.commit();
		System.out.println("all the data are deleted");
	}

	/**
	 * Generate a random age that can be assigned to a Twitter user
	 * @return
	 */
	public static int generateAge() {
		int min = 13;
		int max = 70;
	    Random rand = new Random();
	    int randomAge = rand.nextInt((max - min) + 1) + min;
	    return randomAge;
	}
	
	/**
	 * Get the age groups based on the age
	 * @param age
	 * @return
	 */
	public static String getAgeGroup(int age){
		
		String generation = "";
		final String GEN_Z = "Generation Z";
		final String MILLENNIALS = "Millennials";
		final String GEN_X = "Generation X";
		final String BOOMERS = "Baby Boomers";
		
		/**
		 * Generation Information
		 * 1 - Generation Z = 2000 to present (Up to 16 years)
		 * 2 - Millennials = 1981 to 1999(16 to 35 years)
		 * 3 - Generation X 1965 - 80 (35 years to 50 years)
		 * 4 - Baby Boomers 1946 - 64 (51 years to 69)
		 */
		 
		if(age < 16)
			generation = GEN_Z;
		else if(age >=16 && age <35)
			generation = MILLENNIALS;
		else if(age >=35 && age <51)
			generation = GEN_X;
		else if(age >=51 && age <70)
			generation = BOOMERS;
		
		return generation;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException, SolrServerException {
		
		indexJsonTweets();
		//deleteAllIndexData();
		//query_data();
	}

}