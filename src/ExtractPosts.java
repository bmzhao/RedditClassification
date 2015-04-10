import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;




public class ExtractPosts 
{
	public static final int TITLE_COMMACOUNT = 4;
	public static final int ENTIRE_POST_COMMACOUNT = 21;
	
	//writes the titles of all the posts of a .reddit file
	public static void convertToTitles(String filename) throws IOException
	{
		File existenceChecker = new File(filename.replace(".REDDIT", ".TITLE"));
		if (existenceChecker.exists())
		{
			System.out.println("Using locally found title posts file of subreddit " + filename.replace(".REDDIT",""));
			return;
		}
		
		BufferedReader br = new BufferedReader(new FileReader(filename));
		PrintWriter pw = new PrintWriter(new FileWriter(filename.replace(".REDDIT", ".TITLE"))); 
				
		String title;
			
		//convert file contents to a string (for some reason my csv file is interpreted as one single line)
		String fileContents = "";
		String currentLine = br.readLine();
		while (currentLine!=null)
		{
			fileContents+= currentLine;
			currentLine = br.readLine();
		}
		br.close();
		
		
		//extract all titles from all posts in the filecontents string
		String[] substrings = fileContents.split(",");
		

		
		
		int commaCount;
		int currentElement = 0;
		
		while(currentElement+TITLE_COMMACOUNT < substrings.length) //while you haven't reached the end of the string
		{
			//go through each post
			commaCount = TITLE_COMMACOUNT;
			currentElement += TITLE_COMMACOUNT;
			title = substrings[currentElement];
			if (title.startsWith("\"") )
			{
				int quoteCount = countOccurrences(substrings[currentElement],"\"");
				while ((!substrings[currentElement].endsWith("\"")) || (quoteCount%2==1))
				{
					currentElement++;
					title+=substrings[currentElement];
					quoteCount+=countOccurrences(substrings[currentElement], "\"");
				}
				title=title.substring(1, title.length()-1);
			}
			

			
			
			pw.println(title);
		
			commaCount++;
			currentElement++;
			while ((commaCount != ENTIRE_POST_COMMACOUNT))
			{
	
				if (!substrings[currentElement].startsWith("\""))
				{
					currentElement++;
					commaCount++;
				}
				else
				{
					int quoteCount = countOccurrences(substrings[currentElement],"\"");
					while ((!substrings[currentElement].endsWith("\"")) || (quoteCount%2==1))
					{	
						currentElement++;
						quoteCount+=countOccurrences(substrings[currentElement], "\"");
					}
					currentElement++;
					commaCount++;
				}
			}
		}
		pw.close();
		
		
	}
	
	private static int countOccurrences(String a, String b)
	{
		return a.length() - a.replace(b, "").length();
	}
}
