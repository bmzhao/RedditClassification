import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.TreeMap;

//try length normalization
//cosine similarity instead possibly
//try not using idf on test data
public class RedditClassification 
{
	public static void main(String[] args) throws IOException
	{
		
		final double NUM_TRAINING_DATA = .90;
		final int NUM_SUBREDDITS = 2; //cannot exceed 996
		final boolean randomizedSubreddits = true;
		final int totalIterations = 100;
		double average = 0;
		double min = 100;
		double max = 0;
		for (int ultimateCount = 0; ultimateCount < totalIterations; ultimateCount++)
		{
			if (randomizedSubreddits)
			{
				Scanner URLReader = new Scanner(new File("Subreddits.txt"));
				ArrayList<String> websites = new ArrayList<>();
				while (URLReader.hasNextLine())
				{
					websites.add(URLReader.nextLine());
				}
				Collections.shuffle(websites);
				PrintWriter URLCreator = new PrintWriter(new File("URL.txt"));
				String baseURL = "https://raw.githubusercontent.com/umbrae/reddit-top-2.5-million/master/data/";
				System.out.println("Randomly choosing " + NUM_SUBREDDITS + " subreddits");
				for (int i = 0; i < NUM_SUBREDDITS; i++)
				{
					String sub = websites.get(i);
					System.out.println("Using subreddit: " + sub);
					URLCreator.println(baseURL + sub + ".csv");
				}
				websites = null;
				URLReader.close();
				URLReader = null;
				URLCreator.close();
				URLCreator = null;
				
			}
			
			
			//use GetRedditFromInternet to obtain an arraylist of all filenames
			//of all csvs downloaded, provided a given URL list of subreddit databases
			GetRedditFromInternet files = new GetRedditFromInternet(new File("URL.txt"));
			ArrayList<String> filenames = files.getPosts();
			files = null;
			
			//we will extract just the Title of each post from each of the .REDDIT file databases,
			//and write them to corresponding .TITLE files
			for (int i = 0; i < filenames.size(); i++)
			{
				ExtractPosts.convertToTitles(filenames.get(i));
				filenames.set(i,filenames.get(i).replaceAll(".REDDIT", ".TITLE"));
	
			}
			
			//load all titles into memory, in a mapping with whatever subreddit it came from
			TreeMap<String, ArrayList<String>> allTitles = new TreeMap<String, ArrayList<String>>();
			for (int i = 0; i < filenames.size(); i++)
			{
				ArrayList<String> documents = new ArrayList<String>();
				String category = filenames.get(i).replace(".TITLE", "");
				Scanner s = new Scanner(new File(filenames.get(i)));
		
				while (s.hasNextLine())
				{
					String current = s.nextLine();
					if (!current.equals(""))
						documents.add(current);
	
				}
				s.close();
				s = null;
				
				Collections.shuffle(documents);
				allTitles.put(category,documents);
			}
			filenames = null;
			
			
			System.out.println("ADDING TRAINING DATA TO CLASSIFIER");
			//use NUM_TRAINING_DATA percentage of each class' titles as training data for the classifier
			RocchioClassifier rc = new RocchioClassifier();
			for (String s: allTitles.keySet())
			{
				int totalDocs = allTitles.get(s).size();
				for (int i = 0; i < NUM_TRAINING_DATA * totalDocs; i++)
					rc.addTrainingDocument(allTitles.get(s).remove(0), s);
			}
			
			System.out.println("Generating vectors and centroids...");
			rc.finish();
			System.out.println("CLASSIFYING");
			int success = 0;
			int fail = 0;
			for (String s: allTitles.keySet())
			{
				for (int i = 0; i < allTitles.get(s).size();i++)
				{
					String answer = rc.testData(allTitles.get(s).get(i));
					if (s.equals(answer))
					{
						System.out.println("Success!");
						success++;
					}
					else
					{
						System.out.println("fail...");
						fail++;
					}
						
				}
			}
			double toDisplay = success*1.0 / (success + fail);
			System.out.println(toDisplay);
			if (toDisplay > max)
				max = toDisplay;
			if (toDisplay < min)
				min = toDisplay;
			average+=toDisplay;
			
		}
		System.out.println("The average accuracy of predictions on " + NUM_SUBREDDITS + " subreddits is " + average/totalIterations);
		System.out.println("The min accuracy of predictions is " + min);
		System.out.println("The max accuracy of predictions is " + max);
		
	}
	
}
