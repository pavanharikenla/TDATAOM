package com.vz.tg.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class StanfordCoreNLPService {
    
	static StanfordCoreNLP pipeline;
	static List<String> nERList = new ArrayList<String>();
    static{
    	nERList.add("Verizon_Smartphones_iPhone");
    	nERList.add("Verizon_Products_Samsung");
    	nERList.add("Verizon_Products_LG");
    	nERList.add("Verizon_Products_Blackberry");
    	nERList.add("Verizon_Products_Motorola");
    	nERList.add("Verizon_Smartphones_iPhone");
    	nERList.add("Verizon_Smartphones_Samsung");
    	nERList.add("Verizon_Smartphones_Samsung");
    	nERList.add("Verizon_Smartphones_Samsung");
    	nERList.add("Verizon_Products_Apple");
    	nERList.add("Verizon_Tablets_Apple");
    	nERList.add("Verizon_Tablets_Samsung");
    	nERList.add("Verizon_Tablets_Samsung");
    	nERList.add("Verizon_Tablets_LG");
    	nERList.add("Verizon_FiOS");
    	nERList.add("Verizon_Smartphones_Motorola");
    	nERList.add("Verizon_Smartphones_HTC");
    	nERList.add("Verizon_Product_OS_Android");
    	nERList.add("Verizon_Product_OS_iOS");
    	nERList.add("Verizon_Product_OS_Blackberry");
    	nERList.add("Verizon_Product_OS_Windows");
    }

    public static void init() {
        //pipeline = new StanfordCoreNLP("MyPropFile.properties");
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, parse, sentiment, pos, lemma, ner, regexner");
        props.put("regexner.mapping", "verizon-regexner.txt");
        pipeline = new StanfordCoreNLP(props);
        
    }

    public static Map<String,String> findSentiment(String tweet, Map<String,String> sentMap) {

    	int mainSentiment = 0;
    	String annotationType = "NA";
    	Annotation annotation = null;
    	 
    	/** 
         * Step 1: Find out the Sentiment
         */
        if (tweet != null && tweet.length() > 0) {
            int longest = 0;
            annotation = pipeline.process(tweet);
           
            for (CoreMap sentence : annotation
                    .get(CoreAnnotations.SentencesAnnotation.class)) {
            	Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                String partText = sentence.toString();
                if (partText.length() > longest) {
                    mainSentiment = sentiment;
                    longest = partText.length();
                }
            }
        }
        //System.out.println("MainSentiment: "+mainSentiment);
    	sentMap.put("score", Integer.valueOf(mainSentiment).toString());
		

        /** 
         * Step 2: Find out the Annotation Type or Tweet Category
         */
        List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
	    boolean processSentences = true;
	    for(CoreMap sentence: sentences) {
	    	if(processSentences){
	    		 boolean processTokens = true;
	    		 for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	    		    	if(processTokens){
	    		    	    annotationType = token.get(NamedEntityTagAnnotation.class);//It's NER label of the token
	    			        if(nERList.contains(annotationType)){
	    			        	//System.out.println("NamedEntity: "+annotationType+". It breaks now.");
	    			        	processTokens = false;
	    			        	break;
	    			        }//if
	    		    	} else 
	    		    		break;
	    		  }//for
	    		 if(!processTokens)
	    			 processSentences = false;
	    	}//if
	    	else 
	    		break;
	    }//for
	    //System.out.println("NamedEntity: "+annotationType);
	    sentMap.put("category", annotationType);
	    //return mainSentiment;
	    return sentMap;
    }
}

