import java.io.*;
import java.util.*;

public class TFIDFSearch 
{

    public static void main(String[] args) 
    {
        TFIDFSearch search = new TFIDFSearch();
        Trie trie = null;
        String name = args[0].replaceAll("[^0-9]", "");
        String place = "corpus6412203850.ser";
        String totalTextCountPlace = "totalTextCount.ser";
        ArrayList<String> searchName = new ArrayList<>();
        HashMap<String,Double> resultMap2 = new HashMap<>();   
        HashMap<String,Double> word_size = new HashMap<>();       
        Map<String, Map<String, Double>> tfidfMap = new HashMap<>();
        int totalTextCount = 0;

        try (FileInputStream fileIn = new FileInputStream(place);
             ObjectInputStream in = new ObjectInputStream(fileIn)) 
        {
            trie = (Trie) in.readObject();
            System.out.println("Trie object deserialized successfully.");
        } 
        catch (IOException i) 
        {
            System.out.println("Failed to deserialize objects: " + i.getMessage());
            i.printStackTrace();
        } 
        catch (ClassNotFoundException c) 
        {
            System.out.println("Trie class not found.");
            c.printStackTrace();
        }
        try 
        {
            FileInputStream fileIn = new FileInputStream(totalTextCountPlace);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            TotalTextCount totalTextCountObject = (TotalTextCount) objectIn.readObject();
            objectIn.close();

            totalTextCount = totalTextCountObject.getTotalTextCount();
            System.out.println("Deserialized TotalTextCount: " + totalTextCount);
        } 
        catch (IOException | ClassNotFoundException e) 
        {
            e.printStackTrace();
        }
        Hashtable<Integer, Integer> wordsCount = TFIDFSearch.deserializeWordsCount("wordsCount.ser");
        
        try 
        {
            String line;
            BufferedReader br = new BufferedReader(new FileReader(args[1]));
            FileWriter fr = new FileWriter("output.txt");
            int lineCount = 1 ;
            int number = 0 ;
            while ((line = br.readLine()) != null) 
            {
                String[] parts = line.split("\\s+");
                
                if(lineCount == 1 )
                {
                    number = Integer.parseInt(parts[0]);
                }
                else if(line.contains("AND"))
                {
                    for(int i = 0 ; i< parts.length ; i++)
                    {
                        if(!parts[i].equals("AND")&& !searchName.contains(parts[i]))
                        {
                            searchName.add(parts[i]);
                            word_size.put(parts[i],1.0);
                        }
                        else if(searchName.contains(parts[i]))
                        {
                            double count = word_size.get(parts[i])+1.0;
                            word_size.put(parts[i],count);
                        }
                    }
                    for(int i = 0 ; i< searchName.size() ; i++)
                    {
                        
                            HashMap<String , Double> innerMap = new HashMap<>();
                            for(int j = 0 ; j < totalTextCount ; j++)
                            {
                                double tf_idf = trie.calculateTFIDF(searchName.get(i), Integer.toString(j), totalTextCount , wordsCount);
                                innerMap.put(Integer.toString(j),tf_idf);
                            }
                            tfidfMap.put(searchName.get(i),innerMap);
                        
                            
                           
                    }
                    for(int i = 0 ; i < searchName.size() ; i++)
                    {
                        for(int j = 0 ; j < totalTextCount ; j++)
                        {
                            if( i == 0 && tfidfMap.get(searchName.get(i)).get(Integer.toString(j)) >= 0)
                            {
                                double times = word_size.get(searchName.get(i));
                                double total = tfidfMap.get(searchName.get(i)).get(Integer.toString(j))*times;
                                resultMap2.put(Integer.toString(j),total);
                            }
                            else if(resultMap2.containsKey(Integer.toString(j))&&tfidfMap.get(searchName.get(i)).get(Integer.toString(j)) >= 0)
                            {
                                double sum = resultMap2.get(Integer.toString(j));
                                double times = word_size.get(searchName.get(i));
                                double total = sum + (tfidfMap.get(searchName.get(i)).get(Integer.toString(j))*times);
                                resultMap2.put(Integer.toString(j),total);
                            }
                            else if(resultMap2.containsKey(Integer.toString(j))&&tfidfMap.get(searchName.get(i)).get(Integer.toString(j))<0)
                            {
                                resultMap2.remove(Integer.toString(j));
                            }
                        }
                    }
                    if (resultMap2 != null && resultMap2.size()>1 && resultMap2.size() > number) 
                    {
                        PriorityQueue<Map.Entry<String, Double>> maxQueue = new PriorityQueue<>((entry1, entry2) -> {
                            int compare = entry2.getValue().compareTo(entry1.getValue());
                            if (compare == 0) {
                                return Integer.compare(Integer.parseInt(entry1.getKey()), Integer.parseInt(entry2.getKey()));
                            }
                                return compare;
                            });
                            maxQueue.addAll(resultMap2.entrySet());
                            for (int i = 0; i < number; i++) {
                                Map.Entry<String, Double> entry = maxQueue.poll();
                                if (!maxQueue.isEmpty()) {
                                    fr.write(entry.getKey() + " ");
                                } else {
                                    fr.write("-1 ");
                                }
                            }
                            maxQueue.clear();
                    }
                    else if(resultMap2.size() >1 &&  resultMap2.size()<= number )
                    {
                        PriorityQueue<Map.Entry<String, Double>> maxQueue = new PriorityQueue<>((entry1, entry2) -> {
                            int compare = entry2.getValue().compareTo(entry1.getValue());
                            if (compare == 0) {
                                return Integer.compare(Integer.parseInt(entry1.getKey()), Integer.parseInt(entry2.getKey()));
                            }
                                return compare;
                            });
                            maxQueue.addAll(resultMap2.entrySet());
                        int count = 0; 
                        while (!maxQueue.isEmpty() && count < number - 1) {
                            Map.Entry<String, Double> entry = maxQueue.poll();
                            fr.write(entry.getKey() + " ");
                            count++;
                        }

                        if (!maxQueue.isEmpty()) {
                            Map.Entry<String, Double> lastEntry = maxQueue.poll();
                            fr.write(lastEntry.getKey() + " ");
                        } else {
                            fr.write("-1 "); 
                        }

                        while (count < number - 1) {
                            fr.write("-1 "); 
                            count++;
                        }

                    }
                    else if(resultMap2.size()==1)
                    {
                        for (String key : resultMap2.keySet()) 
                        {
                            fr.write(key+" ");
                            break;
                        }
                        for(int i = 1 ; i <number ; i++)
                        {
                            fr.write("-1 ");
                        }
                    }
                    
                    else
                    {
                        for(int i = 0 ; i < number ; i++)
                        {
                            fr.write("-1 ");
                        }
                    }
                    fr.write("\n");
                    

                }
                else if(line.contains("OR"))
                {
                    for(int i = 0 ; i< parts.length ; i++)
                    {
                        if(!parts[i].equals("OR")&& !searchName.contains(parts[i]))
                        {
                            searchName.add(parts[i]);
                            word_size.put(parts[i],1.0);
                        }
                        else if(searchName.contains(parts[i]))
                        {
                            double count = word_size.get(parts[i])+1.0;
                            word_size.put(parts[i],count);
                        }
                    }
                    for(int i = 0 ; i< searchName.size() ; i++)
                    {
                         
                            HashMap<String , Double> innerMap = new HashMap<>();
                            for(int j = 0 ; j < totalTextCount ; j++)
                            {
                                double tf_idf = trie.calculateTFIDF(searchName.get(i), Integer.toString(j), totalTextCount , wordsCount);
                                innerMap.put(Integer.toString(j),tf_idf);
                            }
                            tfidfMap.put(searchName.get(i),innerMap);
                         
                    }

                        
                    for(int i = 0 ; i < searchName.size() ; i++)
                    {
                        for(int j = 0 ; j < totalTextCount ; j++)
                        {
                            
                            double tmp = tfidfMap.get(searchName.get(i)).get(Integer.toString(j));
                            if((!resultMap2.containsKey(Integer.toString(j)))&&(tmp>=0.0))
                            {
                                double times = word_size.get(searchName.get(i));
                                double total = tfidfMap.get(searchName.get(i)).get(Integer.toString(j))*times;
                                resultMap2.put(Integer.toString(j),total);
                            }
                            else if((resultMap2.containsKey(Integer.toString(j)))&&(tmp>=0.0))
                            {
                                double sum = resultMap2.get(Integer.toString(j));
                                double times = word_size.get(searchName.get(i));
                                double total = sum + (tfidfMap.get(searchName.get(i)).get(Integer.toString(j))*times);
                                resultMap2.put(Integer.toString(j),total);
                            }
                        }
                    }
                    if (resultMap2 != null && resultMap2.size()>1 && resultMap2.size() > number) 
                    {
                        PriorityQueue<Map.Entry<String, Double>> maxQueue = new PriorityQueue<>((entry1, entry2) -> 
                        {
                            int compare = entry2.getValue().compareTo(entry1.getValue());
                            if (compare == 0) {
                                return Integer.compare(Integer.parseInt(entry1.getKey()), Integer.parseInt(entry2.getKey()));
                            }
                                return compare;
                        });
                        maxQueue.addAll(resultMap2.entrySet());
                        for (int i = 0; i < number; i++) {
                            Map.Entry<String, Double> entry = maxQueue.poll();
                            if (entry != null) {
                                fr.write(entry.getKey() + " ");
                            } else {
                                fr.write("-1 ");
                                break; 
                            }
                        }

                        maxQueue.clear();
                    }
                    else if(resultMap2.size() >1 &&  resultMap2.size()<= number )
                    {
                        PriorityQueue<Map.Entry<String, Double>> maxQueue = new PriorityQueue<>((entry1, entry2) -> {
                            int compare = entry2.getValue().compareTo(entry1.getValue());
                            if (compare == 0) {
                                return Integer.compare(Integer.parseInt(entry1.getKey()), Integer.parseInt(entry2.getKey()));
                            }
                                return compare;
                            });
                            maxQueue.addAll(resultMap2.entrySet());
                        int count = 0; 
                        while (!maxQueue.isEmpty() && count < number - 1) {
                            Map.Entry<String, Double> entry = maxQueue.poll();
                            fr.write(entry.getKey() + " ");
                            count++;
                        }

                        if (!maxQueue.isEmpty()) {
                            Map.Entry<String, Double> lastEntry = maxQueue.poll();
                            fr.write(lastEntry.getKey() + " ");
                        } else {
                            fr.write("-1 "); 
                        }

                        while (count < number - 1) {
                            fr.write("-1 ");
                            count++;
                        }

                    }
                    else if(resultMap2.size()==1)
                    {
                        for (String key : resultMap2.keySet()) 
                        {
                            fr.write(key+" ");
                            break;
                        }
                        for(int i = 1 ; i <number ; i++)
                        {
                            fr.write("-1 ");
                        }
                    }
                    
                    else
                    {
                        for(int i = 0 ; i < number ; i++)
                        {
                            fr.write("-1 ");
                        }
                    }
                    fr.write("\n");
                    

                }
                else
                {
                    searchName.add(parts[0]);
                        HashMap<String , Double> innerMap = new HashMap<>();
                        for(int j = 0 ; j < totalTextCount ; j++)
                        {
                            double tf_idf = trie.calculateTFIDF(searchName.get(0), Integer.toString(j), totalTextCount , wordsCount);
                            innerMap.put(Integer.toString(j),tf_idf);
                        }
                        tfidfMap.put(searchName.get(0),innerMap);
                    
                    
                    
                    if(tfidfMap.get(searchName.get(0)) != null)
                    {
                        for(int  j = 0 ; j <tfidfMap.get(searchName.get(0)).size() ; j++ )
                        {
                            if(tfidfMap.get(searchName.get(0)).get(Integer.toString(j))>=0.0)
                            {
                                resultMap2.put(Integer.toString(j), tfidfMap.get(searchName.get(0)).get(Integer.toString(j)));
                            }
                        }
                    }
                    if (resultMap2 != null && resultMap2.size()>1 && resultMap2.size() > number) 
                    {
                        PriorityQueue<Map.Entry<String, Double>> maxQueue = new PriorityQueue<>((entry1, entry2) -> {
                            int compare = entry2.getValue().compareTo(entry1.getValue());
                            if (compare == 0) {
                                return Integer.compare(Integer.parseInt(entry1.getKey()), Integer.parseInt(entry2.getKey()));
                            }
                                return compare;
                            });
                            maxQueue.addAll(resultMap2.entrySet());
                            for (int i = 0; i < number; i++) {
                                Map.Entry<String, Double> entry = maxQueue.poll();
                                if (!maxQueue.isEmpty()) {
                                    fr.write(entry.getKey() + " ");
                                } else {
                                    fr.write("-1 ");
                                }
                            }
                            maxQueue.clear();
                    }
                    else if(resultMap2.size() >1 &&  resultMap2.size() <= number )
                    {
                        PriorityQueue<Map.Entry<String, Double>> maxQueue = new PriorityQueue<>((entry1, entry2) -> {
                            int compare = entry2.getValue().compareTo(entry1.getValue());
                            if (compare == 0) {
                                return Integer.compare(Integer.parseInt(entry1.getKey()), Integer.parseInt(entry2.getKey()));
                            }
                                return compare;
                            });
                            maxQueue.addAll(resultMap2.entrySet());
                        int count = 0; 
                        while (!maxQueue.isEmpty() && count < number - 1) 
                        {
                            Map.Entry<String, Double> entry = maxQueue.poll();
                            fr.write(entry.getKey() + " ");
                            count++;
                        }

                        if (!maxQueue.isEmpty()) {
                            Map.Entry<String, Double> lastEntry = maxQueue.poll();
                            fr.write(lastEntry.getKey() + " ");
                        } else {
                            fr.write("-1 "); 
                        }

                        while (count < number - 1) {
                            fr.write("-1 "); 
                            count++;
                        }

                    }
                    else if(resultMap2.size()==1)
                    {
                        for (String key : resultMap2.keySet()) 
                        {
                            fr.write(key+" ");
                            break;
                        }
                        for(int i = 1 ; i <number ; i++)
                        {
                            fr.write("-1 ");
                        }
                    }
                    
                    else
                    {
                        for(int i = 0 ; i < number ; i++)
                        {
                            fr.write("-1 ");
                        }
                    }
                    fr.write("\n");
                    
                }
                lineCount++;
                word_size.clear();
                searchName.clear();
                resultMap2.clear();
                tfidfMap.clear();
            }
            fr.close();
            br.close();
        }
        
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
    @SuppressWarnings("unchecked")
    private static Hashtable<Integer, Integer> deserializeWordsCount(String filename) {
        Hashtable<Integer, Integer> deserializedWordsCount = null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Object obj = ois.readObject();
            deserializedWordsCount = (Hashtable<Integer, Integer>) obj;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return deserializedWordsCount;
    }

}


