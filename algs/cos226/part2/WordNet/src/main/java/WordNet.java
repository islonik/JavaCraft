import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * @author Lipatov Nikita
 */
public class WordNet {
    private Digraph digraph;
    private SAP sap;
    private final Map<Integer, String> idToSynset = new HashMap<Integer, String>();
    private final Map<String, Set<Integer>> nouns = new HashMap<String, Set<Integer>>(); // The number of nouns in synsets.txt is 119,188.

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        parseSynsets(synsets);
        parseHypernyms(hypernyms);

        checkCycles();
        checkRoot();

        this.sap = new SAP(digraph);
    }

    private void parseSynsets(String synsets) {
        In synsetIn = new In(synsets);
        String oneLine = null;
        while (synsetIn.hasNextLine()) {
            oneLine = synsetIn.readLine();
            StringTokenizer oneSynsetTokenizer = new StringTokenizer(oneLine, ",");
            int synsetId = 0;
            if (oneSynsetTokenizer.hasMoreTokens()) {
                synsetId = Integer.parseInt(oneSynsetTokenizer.nextToken());
            }

            if (oneSynsetTokenizer.hasMoreTokens()) {
                String synset = oneSynsetTokenizer.nextToken();
                idToSynset.put(synsetId, synset);
                StringTokenizer synonymTokenizer = new StringTokenizer(synset, " ");
                while (synonymTokenizer.hasMoreTokens()) {
                    String synonym = synonymTokenizer.nextToken();
                    Set<Integer> ids = nouns.get(synonym);
                    if (null == ids) {
                        ids = new HashSet<Integer>();
                    }
                    ids.add(synsetId);
                    nouns.put(synonym, ids);
                }
            }
            /*
            String description = null;
            if (oneSynsetTokenizer.hasMoreTokens()) {
                description = oneSynsetTokenizer.nextToken();
            }
            */
        }
    }

    private void parseHypernyms(String hypernyms) {
        In hypernymIn = new In(hypernyms);
        digraph = new Digraph(idToSynset.size());

        String oneLine = null;
        while (hypernymIn.hasNextLine()) {
            oneLine = hypernymIn.readLine();
            StringTokenizer oneHypernymTokenizer = new StringTokenizer(oneLine, ",");
            boolean isFirst = true;
            String hypernymFirst = null;
            while (oneHypernymTokenizer.hasMoreTokens()) {
                String hypernymId = oneHypernymTokenizer.nextToken();
                if (isFirst) {
                    hypernymFirst = hypernymId;
                    isFirst = false;
                } else {
                    digraph.addEdge(Integer.parseInt(hypernymFirst), Integer.parseInt(hypernymId));
                }
            }
        }
    }

    // Check for cycles
    private void checkCycles() {
        DirectedCycle cycle = new DirectedCycle(digraph);
        if (cycle.hasCycle()) {
            throw new IllegalArgumentException("Not a valid DAG");
        }
    }

    // Check if not rooted
    private void checkRoot() {
        int rooted = 0;
        for (int i = 0; i < digraph.V(); i++) {
            if (!this.digraph.adj(i).iterator().hasNext()) {
                rooted++;
            }
        }

        if (rooted != 1) {
            throw new IllegalArgumentException("Not a rooted DAG");
        }
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return nouns.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) {
            throw new NullPointerException();
        }
        if (nouns.containsKey(word)) {
            return true;
        }
        return false;
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB)) {
            throw new IllegalArgumentException("Both words must be nouns!");
        }
        Set<Integer> setA = nouns.get(nounA);
        Set<Integer> setB = nouns.get(nounB);
        return sap.length(setA, setB);
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB)) {
            throw new IllegalArgumentException("Both words must be nouns!");
        }
        Set<Integer> setA = nouns.get(nounA);
        Set<Integer> setB = nouns.get(nounB);
        int ancestor = sap.ancestor(setA, setB);
        return idToSynset.get(ancestor);
    }

    // do unit testing of this class
    public static void main(String[] args) {

    }
}
