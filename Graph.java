import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Graph {
    /**
     * HashMap of <Vertex, VertexAdjacencyList> pairs
     */
    private final Map<Integer, List<Integer>> graphMap = new HashMap<>();

    /**
     * Adds a new vertex to the graph.
     * @param vertex to be added
     */
    public void addVertex(final Integer vertex) {
        graphMap.put(vertex, new LinkedList<>());
    }

    /**
     * Adds an edge between src and dest vertices.
     * @param src vertex
     * @param dest vertex
     */
    public void addEdge(final Integer src, final Integer dest) {
        if (!graphMap.containsKey(src)) {
            addVertex(src);
        }
        if (!graphMap.containsKey(dest)) {
            addVertex(dest);
        }
        graphMap.get(src).add(dest);
        graphMap.get(dest).add(src);
    }

    /**
     * Checks if there's an edge between src and dest vertices.
     * @param src vertex
     * @param dest vertex
     * @return true or false
     */
    public boolean hasEdge(final Integer src, final Integer dest)
    {
        if (graphMap.get(src) == null) {
            return false;
        }
        return graphMap.get(src).contains(dest);
    }

    // Graph map getter
    public Map<Integer, List<Integer>> getMap() {
        return graphMap;
    }
}

