package eaiti;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import redis.clients.jedis.Jedis; 


public class eaiti {
	
	/**
	* This is the main method which will query http://topsites.eaiti.com/ and output a user specified number of top sites
	* @param N - a command line input specifying the number of sites to be outputted
	* @return Nothing.
	* @exception IllegalArgumentException On input error.
	*/
	
	public static void main(String[] args){
	
		Jedis jedis = new Jedis("localhost");
	
		
		int N = tryParse(args[0]);
		if(args.length!=1){
			System.err.print("Incorrect number of arguments provided");
			throw new IllegalArgumentException();
		}
		URL url;
		int siteCount = 0; 
        int page = 0; 
        int skipTop5 = 0;
        
        //N lines of output 
		while(N > 0){
            String a;
			try {
				if(page == 0){
		            a="http://topsites.eaiti.com/";
				}
				else{
		            a="http://topsites.eaiti.com/?page=" + page;
				}
	            url = new URL(a);
	            URLConnection conn = url.openConnection();
	
	            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	
	            String inputLine;
	            int pageLimitCount = 0; 
	            //while there is another line AND we haven't maxed out output for current page AND user requested more lines
	            while ((inputLine = br.readLine()) != null && pageLimitCount < 25 && N > 0) {
	            	//if we've displayed 500, there are no more results
	            	if(siteCount >= 500){
	            		System.exit(1);
	            	}
	            	//parse the line 
	            	if(inputLine.contains("href") && !inputLine.contains("?page=") && !inputLine.contains("rel=")){
	            		//if we are on a page with top 5, skip it 
	            		if(page > 0 && skipTop5 < 5){
	            			skipTop5++;
	            			continue;
	            		}
	            		//output 
	            		siteCount++;
	            		if(jedis.get(Integer.toString(siteCount)) != null){
	            			System.out.println(siteCount + "" +jedis.get(Integer.toString(siteCount)));  
	            		}
	            		else{
		            		jedis.set(Integer.toString(siteCount), inputLine.replaceAll("\\<[^>]*>",""));
		            		System.out.println(siteCount + "" +jedis.get(Integer.toString(siteCount))); 
	            		}
	            			            		            		               
		            	N--;
		            	pageLimitCount++; 
	            	}
	            	if(pageLimitCount == 25){
	            		page++; 
	            		skipTop5 = 0; 
	            	}
	            }//end while
	            br.close();
	
	        } catch (MalformedURLException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}//end while
	}
	
	/**
	* This method is used to try to parse an Integer from a String
	* @param String text- a stringed digit 
	* @return An integer should the input be a valid string

	*/
	
	public static Integer tryParse(String text) {
		  try {
		    return Integer.parseInt(text);
		  } catch (NumberFormatException e) {
		    return null;
		  }
		}
}
