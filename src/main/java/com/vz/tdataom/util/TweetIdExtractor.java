package com.vz.tdataom.util;

import com.google.common.primitives.Longs;

public class TweetIdExtractor {
	
	public static Long extractTweetId(String tweetIdString){
		
		int lastColIndex = tweetIdString.lastIndexOf(":");
		String tweet = tweetIdString.substring(lastColIndex+1, tweetIdString.length());
		Long tweetId = Longs.tryParse(tweet);
		return tweetId;
		
	}
	//Testing for travis push....
}
