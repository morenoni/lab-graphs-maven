package edu.grinnell.csc207.experiments;

import java.io.PrintWriter;
import java.util.List;

import edu.grinnell.csc207.util.Graph;

/**
 * A quick experiment with graphs.
 *
 * @author Your Name Here
 * @author Your Name Here
 * @author Samuel A. Rebelsky
 */
public class GraphExperiment {

  /**
   * Run our experiments.
   *
   * @param args
   *   Command-line arguments (ignored).
   */
  public static void main(String[] args) throws Exception {
    PrintWriter pen = new PrintWriter(System.out, true);
    // A small graph so that we can force it to expand.
    Graph g = new Graph(2);
    g.addVertex("a");
    g.addVertex("b");
    g.addVertex("c");
    g.dump(pen);

    // Add a few edges
    g.addEdge("a", "b", 1);
    g.addEdge("a", "c", 2);
    g.addEdge("b", "c", 3);
    g.addEdge("b", "a", 4);
    g.dump(pen);

    // Remove a vertex
    g.removeVertex("b");
    g.dump(pen);

    // Add another vertex
    g.addVertex("d");
    g.addEdge("a", "d", 5);
    g.addEdge("d", "a", 6);
    g.dump(pen);

    // And another (hopefully, this will replace the old b)
    g.addVertex("e");
    g.addEdge("e", "a", 7);
    g.dump(pen);

    // And another (hopefully, this will expand the graph again)
    g.addVertex("f");
    g.addEdge("c", "f", 8);
    g.addEdge("f", "c", 9);
    g.dump(pen);

    // Add an invalid edge
    try {
      g.addEdge("c",  "g", 0);
      pen.println("Surprisingly, added an edge from c to nonexistant g");
    } catch (Exception e) {
      pen.println("Correctly failed to add an edge from c to g.");
    } // try/catch

    // Add/replace a bunch of edges
    for (int i = 1; i <= 4; i++) {
      g.addEdge(0, i, i * 10);
    } // for
    g.dump(pen);

    // Remove an edge
    g.removeEdge("a", "c");
    g.dump(pen);


    Graph h = new Graph(4);
    h.addVertex("a");
    h.addVertex("b");
    h.addVertex("c");

    h.addEdge("a", "b", 1);
    h.addEdge("b", "c", 1);
    h.addEdge("c", "a", 1);
    h.dumpWithNames(pen);
    System.out.println("kdksnvjervkehjfk3bf");
    List<Integer> path = h.shortestPath(0, 3);
    for(Integer p : path) { 
      pen.println(p);
    }
    pen.println(path.size());
  } // main(String[])

} // class GraphExperiment
