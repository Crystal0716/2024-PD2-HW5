import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Hashtable;

public class Indexer {
    private Trie trie;
    private Hashtable<Integer, Integer> wordsCount;

    @SuppressWarnings("unchecked")
    public void deserialize(String filePath) {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filePath))) {
            trie = (Trie) inputStream.readObject();
            System.out.println("Trie object deserialized successfully.");

            Object obj = inputStream.readObject();
            if (obj instanceof Hashtable) {
                wordsCount = (Hashtable<Integer, Integer>) obj;
                System.out.println("wordsCount object deserialized successfully.");
            } else {
                System.out.println("Failed to deserialize wordsCount object: Unexpected object type.");
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Failed to deserialize objects: " + e.getMessage());
        }
    }

}

