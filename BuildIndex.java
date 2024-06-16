import java.io.*;
import java.util.*;

public class BuildIndex 
{
    public static void main(String[] args) 
    {
        String name = args[0].replaceAll("[^0-9]", "");
        String place = "corpus"+name+".ser";
        String fileName = "wordsCount.ser";
        Trie trie = new Trie();
        Hashtable<Integer, Integer> wordsCount = new Hashtable<>();
        int totalTextCount = 0 ;
        try
        {
            String line ;
            int lineCount = 0 ;
            int textCount = 0 ;
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            while((line = br.readLine())!= null)
            {
                
                String[] parts = line.split("\\s+");
                lineCount = Integer.parseInt(parts[0]);
                textCount = (lineCount-1)/5 ;
                line = line.toLowerCase().replaceAll("[^a-zA-Z]+", " ").trim();
                String[] parts2 = line.trim().split("\\s+");
                if (!wordsCount.containsKey(textCount))
                {
                    wordsCount.put(textCount, 0);
                }
                for(int i = 0 ; i < parts2.length ; i++)
                {
                    trie.insert(parts2[i], Integer.toString(textCount));
                    wordsCount.put(textCount, wordsCount.get(textCount) + 1);
                }
                if((lineCount % 5 )== 0)
                {
                    totalTextCount ++ ;
                }
               
            }
           
            br.close();
        }
       
        catch (IOException e)
        {
            e.printStackTrace();
        }
        try {
            FileOutputStream fileOut = new FileOutputStream(place);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(trie);
            objectOut.close();
            System.out.println("Trie has been written to " + place);

            TotalTextCount totalTextCountObject = new TotalTextCount(totalTextCount);
            String totalTextCountPlace = "totalTextCount.ser";
            FileOutputStream fileOutTotal = new FileOutputStream(totalTextCountPlace);
            ObjectOutputStream objectOutTotal = new ObjectOutputStream(fileOutTotal);
            objectOutTotal.writeObject(totalTextCountObject);
            objectOutTotal.close();
            System.out.println("TotalTextCount has been written to " + totalTextCountPlace);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
            outputStream.writeObject(wordsCount);
            System.out.println("wordsCount can  " + fileName);
        } catch (IOException e) {
            System.out.println(" wordsCount cannot " + e.getMessage());
        }
    }

}

class TrieNode implements Serializable
{
    private static final long serialVersionUID = 1L;
    TrieNode[] children = new TrieNode[26];
    boolean isEndOfWord = false;
    Hashtable<String, Integer> additionalInfo = new Hashtable<>();
    HashMap<String,Double> tf_idf = new HashMap<>();
    int keyWordsCount ;
}




class Trie implements Serializable
{
    private static final long serialVersionUID = 1L;
    TrieNode root = new TrieNode();


    public void insert(String word, String searchNumber)
    {
        TrieNode node = root;
        for (char c : word.toCharArray())
        {
            if (node.children[c - 'a'] == null)
            {
                node.children[c - 'a'] = new TrieNode();
            }
            node = node.children[c - 'a'];
        }
        if (!node.additionalInfo.containsKey(searchNumber))
        {
            node.keyWordsCount++;
        }
        node.isEndOfWord = true;
        int count = node.additionalInfo.getOrDefault(searchNumber, 0);
        node.additionalInfo.put(searchNumber, count + 1);    
    }

    

    public double calculateTFIDF(String word, String textId, int totalTextCount , Hashtable<Integer, Integer> wordsCount) 
    {
        double tf = tf(word, textId , wordsCount);
        double idf = idf(word, totalTextCount);
        double count = 0.0 ;
        count = tf*idf ;
        return count;
    }

    public double tf(String word, String textId, Hashtable<Integer, Integer> wordsCount) 
    {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (node == null) {
                return -1.0;
            }
            node = node.children[c - 'a'];
        }
        if (node == null || !node.additionalInfo.containsKey(textId)) {
            return -1.0;
        }
        
        Integer wordCount = node.additionalInfo.get(textId);
        if (wordCount == null || wordCount == 0) {
            return -1.0; 
        }
        
        Integer totalCount = wordsCount.get(Integer.parseInt(textId));

        if (totalCount == null || totalCount == 0) 
        {
            return -1.0; 
        }
        return (double) wordCount / totalCount;
    }



    public double idf(String word, int totalTextCount) 
    {
        TrieNode node = root;
        for (char c : word.toCharArray()) 
        {
            if (node == null) 
            {
                return 1;
            }
            node = node.children[c - 'a'];
        }
        if (node == null || node.keyWordsCount == 0) 
        {
            return 1;
        }
        return (Math.log((double) totalTextCount / node.keyWordsCount));
    }
    
    

}


class TotalTextCount implements Serializable 
{
    private static final long serialVersionUID = 1L;
    private int totalTextCount;

    public TotalTextCount(int totalTextCount) {
        this.totalTextCount = totalTextCount;
    }

    public int getTotalTextCount() {
        return totalTextCount;
    }

    public void setTotalTextCount(int totalTextCount) {
        this.totalTextCount = totalTextCount;
    }
}