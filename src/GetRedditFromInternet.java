import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


//class which handles retrieving .csv files of given subreddits top posts
//and can copy them to the current working directory
public class GetRedditFromInternet 
{
	//these are the subreddits from which we will get posts from
	//we store both the subreddits names, as well as the URLS of the subreddit csv 
	public ArrayList<String> subreddits;
	public ArrayList<String> URLs;
	
	
	//input: file of urls of the reddit post databases separated by newlines
	public GetRedditFromInternet(File f)throws IOException
	{
		//initialize the URL arraylist 
		URLs = new ArrayList<String>();
		
		//check if the file exists
		if (!f.exists())
			throw new FileNotFoundException();
		
		//read all of the urls, and add them to the URL arraylist
		BufferedReader br = new BufferedReader(new FileReader(f));
		String current = br.readLine();
		while(current!=null && isValid(current))
		{
			URLs.add(current);
			current = br.readLine();
		}
		br.close();
		
	}
	
	//generates a subreddit for each URL
	private void generateSubreddits()
	{
		subreddits = new ArrayList<String>();
		for (int i = 0; i < URLs.size(); i++)
		{
			String topic = URLs.get(i);
			topic = topic.substring(topic.lastIndexOf('/')+1, topic.length()-4);
			subreddits.add(topic);
		}
	}
	
	private static boolean isValid(String URL)
	{
		if (URL.startsWith("http") && URL.endsWith(".csv"))
			return true;
		else
			return false;
	}
	
	//will download all csvs for given subreddits, writes them as .REDDIT files,  
	//and returns an arraylist of all such filenames
	public ArrayList<String> getPosts() throws IOException
	{
		generateSubreddits();
		ArrayList<String> filenames = new ArrayList<String>();
		boolean needToUseJsoup = false;
		for (int i = 0; i < subreddits.size(); i++)
		{
			if (new File(subreddits.get(i)+".REDDIT").exists())
			{
				filenames.add(subreddits.get(i)+".REDDIT");
			}
			else
			{
				needToUseJsoup = true;
				filenames.clear();
				break;
			}
		}
		if (needToUseJsoup == false)
		{
			System.out.println("Using local .REDDIT CSVs");
			return filenames;
		}
			
		for (int i = 0; i < URLs.size(); i++)
		{
			int maxCount = 100;
			int currentCount = 0;
			while (currentCount < maxCount)
			{
				try
				{
					Document doc = Jsoup.connect(URLs.get(i)).maxBodySize(0).get();
					String toParse = doc.body().toString();
					toParse = toParse.substring(6+213,toParse.length()-7).trim();
					String name = subreddits.get(i) + ".REDDIT";
					File file = new File(name);
					if (!file.exists())
					{
						BufferedWriter bw = new BufferedWriter(new FileWriter(name));
						bw.write(toParse);
						bw.close();
					}
					filenames.add(name);
					break;
					
				}
				catch (java.net.SocketTimeoutException e)
				{
					System.out.println("Timeout Exception caught...Continuing");
					currentCount++;
				}
			}
					
		}
		return filenames;
	}
}
