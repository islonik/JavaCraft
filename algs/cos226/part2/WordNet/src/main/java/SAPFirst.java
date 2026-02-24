import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Shortest ancestral path (SAP)
 * First attempt.
 * @author Lipatov Nikita
 */
public class SAPFirst {
    private Digraph digraph;
    private SAPCacheManager manager;

    // constructor takes a digraph (not necessarily a DAG) (directed acyclic graph)
    public SAPFirst(Digraph G) {
        digraph = G;
        manager = new SAPCacheManager();
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        if (manager.isExist(v, w)) {
            return manager.get(v, w).length;
        }

        int ancestor = ancestor(v, w);
        return length(ancestor, v, w);
    }

    private int length(int ancestor, int v, int w) {
        if (ancestor == -1) {
            return ancestor;
        } else {
            int leftLength  = bypassLength(0, ancestor, v);
            int rightLength = bypassLength(0, ancestor, w);

            manager.add(v, w, ancestor, leftLength + rightLength);

            return leftLength + rightLength;
        }
    }

    private int bypassLength(Integer length, int ancestor, int temp) {
        if (ancestor == temp) {
            return length;
        }
        Iterator<Integer> tempI = digraph.adj(temp).iterator();
        List<Integer> currentRoots = new ArrayList<Integer>();
        List<Integer> futureRoots  = new ArrayList<Integer>();
        Integer currentTemp = null;
        while (tempI.hasNext()) {
            currentTemp = tempI.next();
            if (ancestor == temp) {
                return length + 1;
            }
            currentRoots.add(currentTemp);
        }
        int result = -1;
        while(true) {
            for (Integer rootTemp : currentRoots) {
                result = length(length + 1, ancestor, futureRoots, rootTemp);
                if (result != -1) {
                    return result;
                }
            }
            if (futureRoots.size() == 0) {
                break;
            } else {
                currentRoots.clear();
                currentRoots.addAll(futureRoots);
            }
            length++;
        }
        return -1;
    }

    private int length(int length, int ancestor, List<Integer> roots, Integer temp) {
        roots.remove(temp);
        if (ancestor == temp) {
            return length;
        }
        Iterator<Integer> vI = digraph.adj(temp).iterator();
        Integer cycleTemp = null;
        while (vI.hasNext()) {
            cycleTemp = vI.next();
            roots.add(cycleTemp);
        }
        return -1;
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        if (manager.isExist(v, w)) {
            return manager.get(v, w).ancestor;
        }
        if (v == w) {
            manager.add(v, w, v, 0);

            return v;
        }
        Iterator<Integer> vI = digraph.adj(v).iterator();
        Iterator<Integer> wI = digraph.adj(w).iterator();
        List<Integer> ancestors = new ArrayList<Integer>(digraph.E() + digraph.V());

        ancestors.add(v);
        ancestors.add(w);

        List<Integer> ancestorsV = new ArrayList<Integer>();
        List<Integer> ancestorsW = new ArrayList<Integer>();

        Integer temp = null;
        while (vI.hasNext()) {
            temp = vI.next();
            if (!ancestors.contains(temp)) {
                ancestors.add(temp);
            } else {
                manager.add(v, w, temp.intValue(), length(temp.intValue(), v, w));

                return temp.intValue();
            }
            ancestorsV.add(temp);
        }

        while (wI.hasNext()) {
            temp = wI.next();
            if (!ancestors.contains(temp)) {
                ancestors.add(temp);
            } else {
                manager.add(v, w, temp.intValue(), length(temp.intValue(), v, w));

                return temp.intValue();
            }
            ancestorsW.add(temp);
        }

        boolean isLeftActive  = (ancestorsV.size() != 0) ? true : false;
        boolean isRightActive = (ancestorsW.size() != 0) ? true : false;
        int leftResult  = -1;
        int rightResult = -1;
        while (true) {
            if (isLeftActive) {
                leftResult = ancestor(ancestors, ancestorsV);
                if (ancestorsV.size() == 0) {
                    isLeftActive = false;
                }
            }
            if (isRightActive) {
                rightResult = ancestor(ancestors, ancestorsW);
                if (ancestorsW.size() == 0) {
                    isRightActive = false;
                }
            }
            if (leftResult != -1 || rightResult != -1) {
                if (leftResult != -1 && rightResult != -1) {
                    int leftLeftLength   = bypassLength(0, leftResult, v);
                    int leftRightLength  = bypassLength(0, leftResult, w);
                    int rightLeftLength  = bypassLength(0, rightResult, v);
                    int rightRightLength = bypassLength(0, rightResult, w);
                    if (leftLeftLength + leftRightLength > rightLeftLength + rightRightLength) {
                        manager.add(v, w, rightResult, rightLeftLength + rightRightLength);

                        return rightResult;
                    } else if (leftLeftLength + leftRightLength < rightLeftLength + rightRightLength) {
                        manager.add(v, w, leftResult, leftLeftLength + leftRightLength);

                        return leftResult;
                    } else {
                        if (leftResult < rightResult) {
                            return leftResult;
                        } else {
                            return rightResult;
                        }
                    }
                } else if (leftResult != -1) {
                    int leftLeftLength   = bypassLength(0, leftResult, v);
                    int leftRightLength  = bypassLength(0, leftResult, w);
                    manager.add(v, w, leftResult, leftLeftLength + leftRightLength);

                    return leftResult;
                } else if (rightResult != -1) {
                    int rightLeftLength  = bypassLength(0, rightResult, v);
                    int rightRightLength = bypassLength(0, rightResult, w);
                    manager.add(v, w, rightResult, rightLeftLength + rightRightLength);

                    return rightResult;
                } else {
                    manager.add(v, w, -1, -1);

                    return -1;
                }
            }
            if (!isLeftActive && !isRightActive) {
                break;
            }
        }
        manager.add(v, w, -1, -1);

        return -1;
    }

    private int ancestor(List<Integer> ancestors, List<Integer> certainSideAncestors) {
        List<Integer> newCertainSideAncestors = new ArrayList<Integer>();
        for (Integer root : certainSideAncestors) {
            Iterator<Integer> digraphI = digraph.adj(root.intValue()).iterator();
            while (digraphI.hasNext()) {
                Integer temp = digraphI.next();
                if (ancestors.contains(temp)) {
                    return temp.intValue();
                }
                ancestors.add(temp);
                newCertainSideAncestors.add(temp);
            }
        }
        certainSideAncestors.clear();
        certainSideAncestors.addAll(newCertainSideAncestors);
        return -1;
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        Iterator<Integer> vI = v.iterator();
        Iterator<Integer> wI = w.iterator();
        List<Integer> vL = new ArrayList<Integer>();
        List<Integer> wL = new ArrayList<Integer>();
        Integer temp = null;
        while (vI.hasNext()) {
            temp = vI.next();
            vL.add(temp);
        }

        while (wI.hasNext()) {
            temp = wI.next();
            wL.add(temp);
        }

        TreeSet<Integer> lengths = new TreeSet<Integer>();
        for (Integer vLI : vL) {
            for (Integer wLI : wL) {
                lengths.add(length(vLI, wLI));
            }
        }
        if (lengths.size() > 0) {
            return lengths.first();
        }
        return -1;
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        Iterator<Integer> vI = v.iterator();
        Iterator<Integer> wI = w.iterator();
        List<Integer> vL = new ArrayList<Integer>();
        List<Integer> wL = new ArrayList<Integer>();
        Integer temp = null;
        while (vI.hasNext()) {
            temp = vI.next();
            vL.add(temp);
        }

        while (wI.hasNext()) {
            temp = wI.next();
            wL.add(temp);
        }

        TreeSet<Integer> ancestors = new TreeSet<Integer>();
        for (Integer vLI : vL) {
            for (Integer wLI : wL) {
                ancestors.add(ancestor(vLI, wLI));
            }
        }
        if (ancestors.size() > 0) {
            return ancestors.first();
        }
        return -1;
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

        public void clear() {
            caches.clear();
        }

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
