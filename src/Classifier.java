import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;


abstract class Classifier 
{
	//helper class represents the data for each classification category
	public static class Category
	{
		//total number of documents in this category
		public int documentsInCategory;                //accounted for XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		
		//HashMap for the term frequencies of each string in the category
		public HashMap<String, Integer> categoryTF;     //accounted for XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		
		//each document (within this category's) term frequencies 
		public ArrayList<HashMap<String,Integer>> documentTF;    //accounted forXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		
		//each document's ordered tf-idf vector representation
		//order is with respect to a sorted iteration through the overall
		//classifier's docFrequency HashMap
		public ArrayList<ArrayList<Double>> documentVectorModel;  //accounted forXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		
		//centroid vector of the category
		public ArrayList<Double> centroid;						//accounted forXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		
		public Category()
		{
			documentsInCategory = 0;
			categoryTF = new HashMap<String, Integer>();
			documentTF = new ArrayList<HashMap<String,Integer>>();
			documentVectorModel = new ArrayList<ArrayList<Double>>();
			centroid = new ArrayList<Double>();
		}
	}
	//dictionary maps each unique string to its document frequency
	//doc frequency is the number of documents it has appeared in
	private HashMap<String, Integer> docFrequency;				//accounted forXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	
	//data maps each string to a category, eg: "AskReddit" string
	//will be mapped to AskReddit category's documents info 
	private HashMap<String, Category> data;						//accounted forXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	
	//total number of documents added to the classifier
	private int totalDocumentCount;								//accounted forXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	
	//words to ignore 
	public Classifier()
	{
		docFrequency = new HashMap<String,Integer>();
		data = new HashMap<String, Category>();
		totalDocumentCount = 0;
	}
	

		
	//given an input training doc, this method
	//converts string document to lowercase, removes punctuations, stems the words
	//and iterates through each individual string of the doc (delimited by whitespace)
	//and updates the fields of the classifier accordingly
	public void addTrainingDocument(String doc, String cat)
	{
		//increment the total document count
		totalDocumentCount++;
		
		//add this category to the data field if it doesn't exist 
		if (!data.containsKey(cat))
			data.put(cat, new Category());
		
		Category currentCat = data.get(cat);
		
		//increase the number of documents in this category
		currentCat.documentsInCategory++;
		
		HashMap<String,Integer> docTF = new HashMap<String, Integer>(); // will be added to category's documentTF arraylist
		//use clean to remove punctuation, switch to lowercase, stem, and split
		//the document on whitespace
		String[] documents = clean(doc);
		for (int i = 0; i < documents.length; i++)
		{
			String current = documents[i];
			//increment the TF count of this string in the document's HashMap representation
			//and also, if appropriate, increment the doc frequency counter of string
			if (!docTF.containsKey(current))
			{
				if (!docFrequency.containsKey(current))
					docFrequency.put(current, 1);
				else
					docFrequency.put(current, docFrequency.get(current)+1);
				docTF.put(current, 1);
			}
				
			else
				docTF.put(current, docTF.get(current)+1);
			
			//increment the TF count of this string in the category's HashMap representation
			if (!data.get(cat).categoryTF.containsKey(current))
				currentCat.categoryTF.put(current,1);
			else
				currentCat.categoryTF.put(current, currentCat.categoryTF.get(current)+1);
		
			
		}
		documents = null;
		//add this document's HashMap TF representation to the current category's documentTF arraylist
		currentCat.documentTF.add(docTF);
		
	}
	
	//must call this method after finishing entering the last training data (through the addTrainingDoc method)
	//and before using the attemptClassify method
	//will update each Document's vector model, and calculate category's centroid vectors
	//note: vectors are un-normalized
	public void finish()
	{
		//create an alphabetical ordering of all of the unique strings 
		ArrayList<String> ordering = createOrdering();
		//create the documentVectorModel for each category's documents
		//for each of the categories...
		Iterator<String> categoryIt = data.keySet().iterator();
		while (categoryIt.hasNext())
		{
			Category currentCat = data.get(categoryIt.next());
			
			//for each document in each category
			for (int i = 0; i < currentCat.documentTF.size(); i++)
			{
				//singleDoc will be each document's tf*idf vector
				ArrayList<Double> singleDoc = new ArrayList<Double>();
				HashMap<String,Integer> currentDoc = currentCat.documentTF.get(i);
				//for each string in the overall unique string ordering 
				for (int j = 0; j < ordering.size(); j++)
				{
					//add the tf*idf value for the string in the document to the singleDoc arraylist
					String key = ordering.get(j);
					if (currentDoc.containsKey(key))
						singleDoc.add((double)(1+Math.log10(currentDoc.get(key)))*Math.log10(totalDocumentCount * 1.0 / docFrequency.get(key)));
					else
						singleDoc.add(0.0);
									
				}
				/*
				 * TESTING OPTIMIZATIONs
				 */
				currentDoc= null;
				
				
				//add the singleDoc tf*idf vector to the documentVectorModel for this category
				currentCat.documentVectorModel.add(singleDoc);
			}	
			
			
			/*
			 * TETSING OPTIMIZATIONs
			 */
			currentCat.categoryTF = null;
			currentCat.documentTF = null;
		}
		System.out.println("Now generating centroids...");		
		//create the centroid vector for each category 
		categoryIt = data.keySet().iterator();
		while(categoryIt.hasNext())
		{
			Category currentCat = data.get(categoryIt.next());
			
			//for each unique word
			for (int i = 0; i < ordering.size(); i++)
			{
				double sum = 0;
				for (int j = 0; j < currentCat.documentVectorModel.size(); j++)
					sum+=currentCat.documentVectorModel.get(j).get(i);
				double average = (sum*1.0)/currentCat.documentVectorModel.size();
				currentCat.centroid.add(average);
			}
			/*
			 * TESTING OPTIMIZATION
			 */
			currentCat.documentVectorModel= null;
		}
		/*
		 * TESTING OPTIMIZATION
		 */
		ordering = null;
		
	}
	
	//each different classifier's implementation of classifying test data
	//returns the String corresponding to whichever class it thinks it belongs to
	abstract public String testData(String doc);
	
	protected ArrayList<String> createOrdering()
	{
		ArrayList<String> ordering = new ArrayList<String>();
		Iterator<String> it= docFrequency.keySet().iterator();
		while (it.hasNext())
		{
			ordering.add(it.next());
		}
		Collections.sort(ordering);
		return ordering;
	}
	
	//intended to be called by subclass to convert a HashMap of test data
	//into vector form
	//can only be called after inputting all training data, and running "finish"
	protected ArrayList<Double> vectorize(HashMap<String, Integer> test)
	{
		ArrayList<Double> answer = new ArrayList<Double>();
		ArrayList<String> ordering = createOrdering(); 
		for (int i = 0; i < ordering.size(); i++)
		{
			String word = ordering.get(i);
			if (test.containsKey(word))
				answer.add((1+Math.log10(test.get(word)))*1.0 *Math.log10(totalDocumentCount * 1.0 / docFrequency.get(word)) );
			else
				answer.add(0.0);
		}
		return answer;
	}
	
	//cleans and stems each string, and gets rid of stop words
	protected String[] clean(String doc)
	{
		String[] titles = doc.toLowerCase().replaceAll("[^a-z ]", "").split("\\s+");
		StopWords stop = new StopWords();
		ArrayList<String> holder = new ArrayList<>();
		for (int i = 0; i < titles.length; i++)
		{			
			if (stop.isStopWord(titles[i]))
			{
				titles[i] = null;
				continue;
			}	
			Stemmer s = new Stemmer();
			char[] word = titles[i].toCharArray();
			s.add(word, word.length);
			s.stem();
			holder.add(s.toString());
		}
		
					
		titles = holder.toArray(new String[holder.size()]);
		stop = null;
		holder = null;
		return titles;
				
	}
	
	protected HashMap<String,Integer> getDocFrequency()
	{
		return docFrequency;
	}
	
	protected HashMap<String, Category> getData()
	{
		return data;
	}
	
	protected int getTotalDocumentCount()
	{
		return totalDocumentCount;
	}
	
}
