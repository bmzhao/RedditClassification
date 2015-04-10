import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.TreeMap;

public class RocchioClassifier extends Classifier
{
	public RocchioClassifier()
	{
		super();
	}
	
	public String testData(String doc)
	{
		//clean, stem, and vectorize the input test data document
		String[] newDoc = super.clean(doc);
		HashMap<String, Integer> testTF = new HashMap<String,Integer>();
		for (int i = 0; i < newDoc.length; i++)
		{
			if (!testTF.containsKey(newDoc[i]))
				testTF.put(newDoc[i], 1);
			else
				testTF.put(newDoc[i], testTF.get(newDoc[i])+1);
		}
		ArrayList<Double> vectorModel = super.vectorize(testTF);
		
		/*
		 * TESTING OPTIMZATIONs
		 */
		testTF=null;
		newDoc=null;
		
		
		//create HashMap of distance from each centroid vector as a key, 
		//with the string of the category name as the value
		TreeMap<Double,String> distances = new TreeMap<Double,String>();
		
		HashMap<String,Category> temp = super.getData();
		Iterator<String> it = temp.keySet().iterator();
		while (it.hasNext())
		{
			String catName = it.next();
			Category currentCat = temp.get(catName);
			
			double distance = calculateDistance(vectorModel,currentCat.centroid);
			distances.put(distance, catName);
		}
		//return the name of the class with the smallest associated distance 
		return distances.get(distances.firstKey());		
		
		
	}
	
	private static double calculateDistance(ArrayList<Double> a, ArrayList<Double> b)
	{
		double sum = 0;
		for (int i = 0; i < a.size(); i++)
		{
			sum += (a.get(i) - b.get(i)) * ( a.get(i) - b.get(i)); 
		}
		return Math.sqrt(sum);
	}
}