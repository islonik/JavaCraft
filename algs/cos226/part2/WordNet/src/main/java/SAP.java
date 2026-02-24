import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Shortest ancestral path (SAP)
 * @author Lipatov Nikita
 */
public class SAP {
    private Digraph digraph;
    private SAPCacheManager manager;

    // constructor takes a digraph (not necessarily a DAG) (directed acyclic graph)
    public SAP(Digraph g) {
        digraph = new Digraph(g);
        manager = new SAPCacheManager();
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        return getSapCache(v, w).length;
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        return getSapCache(v, w).ancestor;
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        SAPCache cache = getSapCache(v, w);
        if (cache == null) {
            return -1;
        }
        return cache.length;
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        SAPCache cache = getSapCache(v, w);
        if (cache == null) {
            return -1;
        }
        return cache.ancestor;
    }

    private SAPCache getSapCache(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null) {
            throw new NullPointerException();
        }
        TreeMap<Integer, SAPCache> map = new TreeMap<Integer, SAPCache>();
        for (Integer vI : v) {
            for (Integer wI : w) {
                SAPCache cache = getSapCache(vI, wI);
                map.put(cache.length, cache);
            }
        }
        if (map.size() > 0) {
            return map.firstEntry().getValue();
        }
        return null;
    }

    private SAPCache getSapCache(int v, int w) {
        if (manager.isExist(v, w)) {
            return manager.get(v, w);
        }

        BreadthFirstDirectedPaths bfdV = new BreadthFirstDirectedPaths(digraph, v);
        BreadthFirstDirectedPaths bfdW = new BreadthFirstDirectedPaths(digraph, w);

        List<Integer> ancestors = new ArrayList<Integer>();
        for (int i = 0; i < digraph.V(); i++) {
            if (bfdV.hasPathTo(i) && bfdW.hasPathTo(i)) {
                ancestors.add(i);
            }
        }

        Integer shortestLength = -1;
        Integer ancestor = -1;
        TreeSet<Integer> lengths = new TreeSet<Integer>();
        for (Integer tempAncestor : ancestors) {
            lengths.add(bfdV.distTo(tempAncestor) + bfdW.distTo(tempAncestor));
            if (ancestor == -1 || lengths.first() < shortestLength) {
                shortestLength = lengths.first();
                ancestor = tempAncestor;
            }
        }

        manager.add(v, w, ancestor, shortestLength);

        return manager.get(v, w);
    }

    private static class SAPCache {
        private int v;
        private int w;
        private int ancestor;
        private int length;

        @Override
        public int hashCode() {
            int result = v;
            result = 31 * result + w;
            return result;
        }

        public boolean equals(Object sapCache) {
            if (sapCache == null) {
                return false;
            }
            if (sapCache instanceof SAPCache) {
                SAPCache that = (SAPCache) sapCache;
                if (this.v == that.v && this.w == that.w) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class SAPCacheManager {
        private final HashMap<SAPCache, SAPCache> caches = new HashMap<SAPCache, SAPCache>();

        public void add(int v, int w, int ancestor, int length) {
            SAPCache sapCache = new SAPCache();
            sapCache.v = v;
            sapCache.w = w;
            sapCache.ancestor = ancestor;
            sapCache.length = length;

            caches.put(sapCache, sapCache);
        }

        public boolean isExist(int v, int w) {
            SAPCache sapCache = new SAPCache();
            sapCache.v = v;
            sapCache.w = w;
            return caches.containsKey(sapCache);
        }

        public SAPCache get(int v, int w) {
            SAPCache sapCache = new SAPCache();
            sapCache.v = v;
            sapCache.w = w;
            return caches.get(sapCache);
        }
    }

    // do unit testing of this class
    public static void main(String[] args) {
        String path = "digraph1.txt";
        if (args != null && args.length > 0) {
            path = args[0];
        }
        In in = new In(path);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length   = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }
}
