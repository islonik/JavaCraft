/**
 * @author Lipatov Nikita
 */
public class Outcast {
    private WordNet wordNet;

    // constructor takes a WordNet object
    public Outcast(WordNet wordnet) {
        this.wordNet = wordnet;
    }

    // given an array of WordNet nouns, return an outcast
    // Given a list of wordnet nouns A1, A2, ..., An, which noun is the least related to the others?
    // To identify an outcast, compute the sum of the distances between each noun and every other one:
    // di = dist(Ai, A1) + dist(Ai, A2) + ... + dist(Ai, An)
    // and return a noun At for which dt is maximum.
    public String outcast(String[] nouns)  {
        String outcast = null;
        int max = 0;

        for (String nounA : nouns) {
            int dist = 0;
            for (String nounB : nouns) {
                if (!nounA.equals(nounB)) {
                    dist += wordNet.distance(nounA, nounB);
                }
            }
            if (dist > max) {
                max = dist;
                outcast = nounA;
            }
        }
        return outcast;
    }

    // see test client below
    // % java Outcast synsets.txt hypernyms.txt outcast5.txt outcast8.txt outcast11.txt
    // outcast5.txt: table
    // outcast8.txt: bed
    // outcast11.txt: potato
    public static void main(String[] args)  {
        if (args == null || args.length == 0) {
            args = new String[]{"synsets.txt", "hypernyms.txt", "outcast5.txt", "outcast8.txt", "outcast11.txt"};
        }
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }
    }
}
