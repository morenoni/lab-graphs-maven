package edu.grinnell.csc207.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A simple weighted, directed, graph.
 *
 * @author Samuel A. Rebelsky
 */
public class Graph {

  // +-------+-------------------------------------------------------
  // | Notes |
  // +-------+

  /*
   * We implement our graphs using adjacency lists. For each vertex, v, we
   * store a list of edges from that vertex.
   *
   * For convenience, you can refer to vertices by number or by name. However,
   * it is more efficient to refer to them by number.
   */

  // +-----------+---------------------------------------------------
  // | Constants |
  // +-----------+

  /**
   * The default initial capacity (# of nodes) of the graph.
   */
  static final int INITIAL_CAPACITY = 16;

  /**
   * The basic mark.
   */
  static final byte MARK = (byte) 1;

  /**
   * One of seven marks.
   */
  static final byte MARK01 = (byte) 1;

  /**
   * Another of seven marks.
   */
  static final byte MARK02 = (byte) 2;

  /**
   * Another of seven marks.
   */
  static final byte MARK03 = (byte) 4;

  /**
   * Another of seven marks.
   */
  static final byte MARK04 = (byte) 8;

  /**
   * Another of seven marks.
   */
  static final byte MARK05 = (byte) 16;

  /**
   * Another of seven marks.
   */
  static final byte MARK06 = (byte) 32;

  /**
   * Another of seven marks.
   */
  static final byte MARK07 = (byte) 64;

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The number of vertices in the graph.
   */
  int numVertices;

  /**
   * The number of edges in the graph.
   */
  int numEdges;

  /**
   * The vertices in the graph. The edges from vertex v are stored in
   * vertices[v].
   */
  List<Edge>[] vertices;

  /**
   * The names of the vertices. The name of vertex v is stored in
   * vertexNames[v].
   */
  String[] vertexNames;

  /**
   * Marks on the vertices.
   */
  byte[] marks;

  /**
   * The unused vertices.
   */
  Queue<Integer> unusedVertices;

  /**
   * The numbers of the vertices. The vertex with name n is given by
   * vertexNumbers.get(n).
   */
  HashMap<String, Integer> vertexNumbers;

  /**
   * The version of the graph. (Essentially, the number of times we've modified
   * the graph.)
   */
  long version;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new graph with the default capacity (number of nodes).
   */
  public Graph() {
    this(INITIAL_CAPACITY);
  } // Graph()

  /**
   * Create a new graph with a specified initial capacity (number of nodes).
   *
   * @param initialCapacity
   *   The initial capacity of the graph.
   */
  @SuppressWarnings("unchecked")
  public Graph(int initialCapacity) {
    this.vertices = (ArrayList<Edge>[]) new ArrayList[initialCapacity];
    this.vertexNames = new String[initialCapacity];
    this.marks = new byte[initialCapacity];
    this.vertexNumbers = new HashMap<String, Integer>();
    this.unusedVertices = new LinkedList<Integer>();
    this.version = 0;
    for (int i = 0; i < this.vertices.length; i++) {
      this.vertices[i] = new ArrayList<Edge>();
      this.unusedVertices.add(i);
    } // for
  } // Graph(int)

  /**
   * Create a new graph, reading the edges from a file. Edges must have the form
   * FROM TO WEIGHT, with one edge per line.
   *
   * @param fName
   *   The name of the file containing the graph.
   */
  public Graph(String fName) throws Exception {
    this();
    this.readGraph(fName);
  } // Graph

  // +----------------------+----------------------------------------
  // | Vertex names/numbers |
  // +----------------------+

  /**
   * Given a vertex number, get the corresponding vertex name.
   *
   * @param vertexNumber
   *   The number of a vertex.
   *
   * @return The corresponding vertex name. If there is no corresponding
   *   vertex name, returns null.
   */
  public String vertexName(int vertexNumber) {
    if (!validVertex(vertexNumber)) {
      return null;
    } else {
      return this.vertexNames[vertexNumber];
    } // if/else
  } // vertexName(int)

  /**
   * Given a vertex name, get the corresponding vertex number.
   *
   * @param vertexName
   *   The name of the vertext.
   *
   * @return
   *   The corresponding vertex number. If there is no corresponding vertex
   *   number, returns -1.
   */
  public int vertexNumber(String vertexName) {
    Integer result = this.vertexNumbers.get(vertexName);
    if (result == null) {
      return -1;
    } else {
      return result;
    } // if/else
  } // vertexNumber(String)

  // +-----------+---------------------------------------------------
  // | Observers |
  // +-----------+

  /**
   * Dump the graph in a not very useful way.
   *
   * @param pen
   *   Where to print the graph.
   */
  public void dump(PrintWriter pen) {
    pen.println("A Graph");
    pen.println("  with " + numVertices + " vertices");
    pen.println("  and " + numEdges + " edges");
    for (int vertex = 0; vertex < vertices.length; vertex++) {
      if (validVertex(vertex)) {
        pen.print(vertex + ": ");
        for (Edge e : vertices[vertex]) {
          pen.print(e + " ");
        } // for()
        pen.println();
      } // if
    } // for
    pen.println();
  } // dump(PrintWriter)

  /**
   * Dump the graph in a more useful way.
   *
   * @param pen
   *   Where to dump the graph.
   */
  public void dumpWithNames(PrintWriter pen) {
    pen.println("Vertices: ");
    pen.print(" ");
    for (int vertex = 0; vertex < vertices.length; vertex++) {
      String name = vertexName(vertex);
      if (name != null) {
        pen.print(" " + name);
      } // if
    } // for
    pen.println();
    pen.println("Edges: ");
    for (int vertex = 0; vertex < vertices.length; vertex++) {
      if (validVertex(vertex)) {
        for (Edge e : vertices[vertex]) {
          pen.println("  " + vertexName(e.source()) + " --"
              + e.weight() + "-> " + vertexName(e.target()));
        } // for()
      } // if
    } // for
    pen.println();
  } // dumpWithNames(PrintWriter)

  /**
   * Save the graph in the form expected by readGraph.
   *
   * @param fname
   *   The name of the file to use.
   */
  public void save(String fname) throws Exception {
    PrintWriter fileWriter = new PrintWriter(new File(fname));
    this.write(fileWriter);
    fileWriter.close();
  } // save(String)

  /**
   * Write the graph in the form expected by readGraph.
   *
   * @param pen
   *   Where to print the graph.
   */
  public void write(PrintWriter pen) {
    for (int vertex = 0; vertex < vertices.length; vertex++) {
      if (validVertex(vertex)) {
        for (Edge e : vertices[vertex]) {
          pen.println(vertexName(e.source()) + " " + vertexName(e.target()) + " "
              + e.weight());
        } // for()
      } // if
    } // for
  } // write(PrintWriter)

  /**
   * Get the number of edges.
   *
   * @return the number of edges.
   */
  public int numEdges() {
    return this.numEdges;
  } // numEdges()

  /**
   * Get the number of vertices.
   *
   * @return the number of vertices.
   */
  public int numVertices() {
    return this.numVertices;
  } // numVertices

  /**
   * Get an iterable for the edges.
   *
   * @return
   *   An iterable whose iterator method returns an iterator for the edges.
   */
  public Iterable<Edge> edges() {
    return () -> {
      return new Iterator<Edge>() {
        // The position of the iterator
        int pos = 0;
        // The version number of the graph when this iterator was created
        long version = Graph.this.version;
        // The current edge iterator
        Iterator<Edge> ie = Graph.this.vertices[0].iterator();
        // The current vertex
        int vertex = 0;

        /**
         * Determine if edges remain.
         */
        public boolean hasNext() {
          failFast(this.version);
          return this.pos < Graph.this.numEdges;
        } // hasNext()

        /**
         * Grab the next edge
         */
        public Edge next() {
          if (!this.hasNext()) {
            throw new NoSuchElementException();
          } // if
          while (!this.ie.hasNext()) {
            this.ie = Graph.this.vertices[++this.vertex].iterator();
          } // while
          ++this.pos;
          return ie.next();
        } // next()
      }; // new Iterator<Edge>
    };
  } // edges()

  /**
   * Get all of the edges from a particular vertex.
   *
   * @param vertex
   *   The vertex whose edges we seek.
   *
   * @return
   *   An iterable whose iterator method returns an iterator for the edges.
   */
  public Iterable<Edge> edgesFrom(int vertex) {
    if (!validVertex(vertex)) {
      // Special case for invalid vertices
      return () -> {
        return new Iterator<Edge>() {
          long version = Graph.this.version;

          public boolean hasNext() {
            failFast(this.version);
            return false;
          } // hasNext()

          public Edge next() {
            failFast(this.version);
            throw new NoSuchElementException();
          } // next()
        }; // new Iterator<Edge>
      };
    } else {
      return () -> {
        return new Iterator<Edge>() {
          // The version number of the graph when this iterator was created
          long version = Graph.this.version;
          // The underlying iterator. We wrap it so that the client
          // cannot call the remove method.
          Iterator<Edge> edges = Graph.this.vertices[vertex].iterator();

          public boolean hasNext() {
            failFast(this.version);
            return edges.hasNext();
          } // hasNext()

          public Edge next() {
            failFast(this.version);
            return edges.next();
          } // next()
        }; // new Iterator<Edge>
      };
    } // if else
  } // edgesFrom(int)

  /**
   * Get all of the edges from a particular vertex.
   *
   * @param vertex
   *   The vertex whose edges we seek.
   *
   * @return
   *   An iterable whose iterator method returns an iterator for the edges.
   */
  public Iterable<Edge> edgesFrom(String vertex) {
    return this.edgesFrom(vertexNumber(vertex));
  } // edgesFrom(String)

  /**
   * Get a path from start to finish.
   *
   * @param start
   *   The start of the path.
   * @param finish
   *   The end of the path.
   *
   * @return A path from start to finish. If no such path exists, returns null.
   */
  public List<Edge> path(int start, int finish) {
    // An array of the edges that lead to vertices. incoming[i]
    // is an edge that leads to vertex i. This approach is derived
    // from one by GM and GT.
    Edge[] incoming = new Edge[vertices.length];

    // Vertices left to process. (We use BFS.)
    Queue<Integer> remaining = new LinkedList<Integer>();
    remaining.add(start);

    // Keep going until we reach finish or run out of edges
    while ((incoming[finish] == null) && (!remaining.isEmpty())) {
      Integer v = remaining.remove();
      for (Edge e : this.edgesFrom(v)) {
        int to = e.target();
        if (incoming[to] == null) {
          remaining.add(to);
          incoming[to] = e;
        } // if
      } // while
    } // while

    // Return the appropriate list
    if (incoming[finish] == null) {
      return null;
    } else {
      LinkedList<Edge> path = new LinkedList<Edge>();
      int current = finish;
      do {
        Edge e = incoming[current];
        path.addFirst(e);
        current = e.source();
      } while (current != start);
      return path;
    } // if/else
  } // path(int, int)

  /**
   * Get a path from start to finish. If no such path exists, returns null.
   *
   * @param start
   *   The start of the path.
   * @param finish
   *   The end of the path.
   *
   * @return A path from start to finish. If no such path exists, returns null.
   */
  public List<Edge> path(String start, String finish) {
    return path(this.vertexNumber(start), this.vertexNumber(finish));
  } // path(String, String)

  /**
   * Get an iterable for the vertices.
   *
   * @return an iterable whose iterator method returns an iterator
   *   for all the vertices.
   */
  public Iterable<Integer> vertices() {
    return () -> {
      return new Iterator<Integer>() {
        // The position of the iterator
        int pos = 0;
        // The version number of the graph when this iterator was created
        long version = Graph.this.version;
        // The current vertex number
        int vertex = 0;

        /**
         * Determine if vertices remain.
         */
        public boolean hasNext() {
          failFast(this.version);
          return this.pos < Graph.this.numVertices;
        } // hasNext()

        /**
         * Grab the next vertex.
         */
        public Integer next() {
          if (!this.hasNext()) {
            throw new NoSuchElementException();
          } // if
          while (Graph.this.vertexNames[this.vertex] == null) {
            ++this.vertex;
          } // while
          return this.vertex++;
        } // next()
      }; // new Iterator<Integer>
    };
  } // vertices()

  // +----------+----------------------------------------------------
  // | Mutators |
  // +----------+

  /**
   * Add an edge between two vertices. If the edge already exists, replace it.
   *
   * @param source
   *   The source of the edge.
   * @param target
   *   The target of the edge.
   * @param weight
   *   The weight of the edge.
   *
   * @throws Exception
   *   If either or both vertices are invalid.
   */
  public void addEdge(int source, int target, int weight) throws Exception {
    if (!validVertex(source) || !validVertex(target)) {
      throw new Exception("Invalid ends");
    } // if
    if (source == target) {
      throw new Exception("Cannot add an edge from a vertex to itself");
    } // if
    ++this.version;
    Edge newEdge = new Edge(source, target, weight);
    ListIterator<Edge> edges = this.vertices[source].listIterator();
    while (edges.hasNext()) {
      if (edges.next().target() == target) {
        edges.set(newEdge);
        return;
      } // if
    } // while
    edges.add(newEdge);
    ++this.numEdges;
  } // addEdge(int, int, int)

  /**
   * Add an edge between two vertices. If the edge already exists, replace it.
   *
   * @param source
   *   The source of the edge.
   * @param target
   *   The target of the edge.
   * @param weight
   *   The weight of the edge.
   *
   * @throws Exception
   *   If either or both vertices are invalid.
   */
  public void addEdge(String source, String target, int weight)
      throws Exception {
    addEdge(this.vertexNumber(source), this.vertexNumber(target), weight);
  } // addEdge(String, String, int)

  /**
   * Add a vertex with a particular name.
   *
   * @param name
   *   The name of the vertex.
   *
   * @return v the number of the vertex
   *
   * @exception Exception if there is already a vertex with that name.
   */
  public int addVertex(String name) throws Exception {
    if (this.vertexNumber(name) != -1) {
      throw new Exception("Already have a node named " + name);
    } // if
    return addVertex(name, this.newVertexNumber());
  } // addVertex(String)

  /**
   * Add an unnamed vertex.
   *
   * @return v the number of the vertex
   */
  public int addVertex() {
    int v = this.newVertexNumber();
    String name = "v" + v;
    // On the off chance there is already a vertex with that name,
    // we try some other names.
    while (this.vertexNumber(name) != -1) {
      name = "v" + name;
    } // while
    return addVertex(name, v);
  } // addVertex()

  /**
   * Read a graph from a file. If there are edges in the current graph,
   * may overwrite them with a new weight.
   *
   * @param fname
   *   THe name of the file to read from.
   *
   * @throws Exception
   *   If any of the lines * have the wrong form.
   */
  public void readGraph(String fname) throws Exception {
    BufferedReader lines = new BufferedReader(new FileReader(fname));
    // Since the only way to determine if no lines are left in a
    // BufferedReader is to see if readLine() throws an exception,
    // we put our loop in a try/catch clause.
    try {
      while (true) {
        String line = lines.readLine();
        String[] parts = line.split("[\\s*]");
        if (parts.length == 3) {
          int from = this.safeVertexNumber(parts[0]);
          int to = this.safeVertexNumber(parts[1]);
          int weight = Integer.parseInt(parts[2]);
          this.addEdge(from, to, weight);
        } // if
      } // while
    } catch (Exception e) {
    } // try/catch
    lines.close();
  } // readGraph()

  /**
   * Remove an edge. If the edge does not exist, does nothing.
   *
   * @param source
   *   The source of the edge.
   * @param target
   *   The target of the edge.
   */
  public void removeEdge(int source, int target) {
    Iterator<Edge> ie = this.vertices[source].iterator();
    while (ie.hasNext()) {
      if (ie.next().target() == target) {
        ie.remove();
        --this.numEdges;
        ++this.version;
        // We could probably break out of the loop at this point,
        // but it's safer to go through the whole list.
      } // if
    } // while
  } // removeEdge(int, int)

  /**
   * Remove an edge. If the edge does not exist, does nothing.
   *
   * @param source
   *   The source of the edge.
   * @param target
   *   The target of the edge.
   */
  public void removeEdge(String source, String target) {
    removeEdge(this.vertexNumber(source), this.vertexNumber(target));
  } // removeEdge(String, String)

  /**
   * Remove a vertex. If the vertex does not exist, does nothing.
   *
   * @param vertex
   *   The vertex to remove
   */
  public void removeVertex(int vertex) {
    // Ignore pointless vertex numbers
    if (!validVertex(vertex)) {
      return;
    } // if

    // Note the change to the graph
    ++this.version;
    --this.numVertices;
    this.numEdges -= this.vertices[vertex].size();

    // Clear out the entries associated with the vertex
    this.vertices[vertex].clear();
    this.vertexNames[vertex] = null;

    // Clear out edges to that vertex
    for (int i = 0; i < this.vertices.length; i++) {
      Iterator<Edge> ie = this.vertices[i].iterator();
      while (ie.hasNext()) {
        if (ie.next().target() == vertex) {
          ie.remove();
          --this.numEdges;
        } // if
      } // while
    } // for

    // Note that the vertex is once again available to use.
    this.unusedVertices.add(vertex);
  } // removeVertex(int)

  /**
   * Remove a vertex. If the vertex does not exist, does nothing.
   *
   * @param vertex
   *   The name of the vertex to remove.
   */
  public void removeVertex(String vertex) {
    this.removeVertex(this.vertexNumber(vertex));
  } // removeVertex(String)

  // +------------------+--------------------------------------------
  // | Marking vertices |
  // +------------------+

  /**
   * Remove all of the marks.
   */
  public void clearMarks() {
    this.marks = new byte[this.marks.length];
  } // clearMarks

  /**
   * Determine if a vertex is marked with a particular mark.
   *
   * @param vertex
   *   The number of the vertex to check.
   * @param mark
   *   The mark to check for.
   *
   * @return true if the vertex has been marked with the specified mark
   *    and false otherwise.
   */
  boolean isMarked(int vertex, byte mark) {
    return (this.validVertex(vertex) && ((this.marks[vertex] & mark) != 0));
  } // isMarked(int, byte)

  /**
   * Determine if a vertex is marked at all.
   *
   * @param vertex
   *   The number of the vertex to check.
   *
   * @return true if the vertex has been marked and false otherwise.
   */
  boolean isMarked(int vertex) {
    return (this.validVertex(vertex) && (this.marks[vertex] != 0));
  } // isMarked(int)

  /**
   * Determine if a vertex is marked with a particular mark.
   *
   * @param vertex
   *   The name of the vertex to check.
   * @param mark
   *   The mark to check for.
   *
   * @return true if the vertex has been marked with the specified mark
   *    and false otherwise.
   */
  boolean isMarked(String vertex, byte mark) {
    return this.isMarked(this.vertexNumber(vertex), mark);
  } // isMarked(String, byte)

  /**
   * Determine if a vertex is marked at all.
   *
   * @param vertex
   *   The name of the vertex to check.
   *
   * @return true if the vertex has been marked and false otherwise.
   */
  boolean isMarked(String vertex) {
    return this.isMarked(this.vertexNumber(vertex));
  } // isMarked(String)

  /**
   * Mark a vertex with one of the possible marks.
   *
   * @param vertex
   *   The number of the vertex to mark.
   * @param mark
   *   The mark to add.
   */
  void mark(int vertex, byte mark) {
    if (validVertex(vertex)) {
      this.marks[vertex] |= mark;
    } // if validVertex
  } // mark(int, byte)

  /**
   * Mark a vertex using the default mark.
   *
   * @param vertex
   *   The number of the vertex to mark.
   */
  void mark(int vertex) {
    this.mark(vertex, Graph.MARK);
  } // mark(int)

  /**
   * Mark a vertex with one of the possible marks.
   *
   * @param vertex
   *   The name of the vertex to mark.
   * @param mark
   *   The mark to add.
   */
  void mark(String vertex, byte mark) {
    this.mark(this.vertexNumber(vertex), mark);
  } // mark(String, byte)

  /**
   * Mark a vertex using the default mark.
   *
   * @param vertex
   *   The name of the vertex to mark.
   */
  void mark(String vertex) {
    this.mark(this.vertexNumber(vertex));
  } // mark(String)

  /**
   * Unmark a vertex.
   *
   * @param vertex
   *   The number of the vertex to unmark.
   * @param mark
   *   The mark to remove.
   */
  void unmark(int vertex, byte mark) {
    if (validVertex(vertex)) {
      // This approach makes Charlie uncomfortable. However, the more sensible
      // thing (commented out below) does not seem to work.
      this.marks[vertex] = (byte) ((this.marks[vertex] | mark) - mark);
      // this.marks[vertex] |= (byte) ~mark;
    } // if validVertex
  } // unmark(int, byte)

  /**
   * Unmark a vertex (removes all marks).
   *
   * @param vertex
   *   The number of the vertex to unmark.
   */
  void unmark(int vertex) {
    if (validVertex(vertex)) {
      this.marks[vertex] = 0;
    } // validVertex
  } // unmark(int)

  /**
   * Unmark a vertex.
   *
   * @param vertex
   *   The name of the vertex to unmark.
   * @param mark
   *   The mark to remove.
   */
  void unmark(String vertex, byte mark) {
    this.unmark(this.vertexNumber(vertex), mark);
  } // unmark(String, byte)

  /**
   * Unmark a vertex.
   *
   * @param vertex
   *   The name of the vertex to unmark.
   */
  void unmark(String vertex) {
    this.unmark(this.vertexNumber(vertex));
  } // unmark(String)

  // +-----------+---------------------------------------------------
  // | Utilities |
  // +-----------+

  /**
   * Add a vertex name / vertex number pair.  Assumes neither the name
   * or number have been used.
   *
   * @param name
   *   The name of the vertex.
   * @param v
   *   The number of the vertex.
   *
   * @return v (mostly for convenience)
   */
  private int addVertex(String name, int v) {
    ++this.version;
    ++this.numVertices;
    this.vertexNumbers.put(name, v);
    this.vertexNames[v] = name;
    return v;
  } // addVertex(String, int)

  /**
   * Expand the necessary arrays.
   */
  private void expand() {
    int oldSize = this.vertices.length;
    int newSize = oldSize * 2;
    this.vertexNames = Arrays.copyOf(this.vertexNames, newSize);
    this.marks = Arrays.copyOf(this.marks, newSize);
    this.vertices = Arrays.copyOf(this.vertices, newSize);
    for (int i = oldSize; i < newSize; i++) {
      this.vertices[i] = new ArrayList<Edge>();
      this.unusedVertices.add(i);
    } // for
  } // expand()

  /**
   * Compare an expected version to the current version. Die if they do not
   * match. (Used to implement the traditional "fail fast" policy for
   * iterators.)
   *
   * @param expectedVersion
   *   The expected version of the graph.
   */
  private void failFast(long expectedVersion) {
    if (this.version != expectedVersion) {
      throw new ConcurrentModificationException();
    } // if
  } // failFast(int)

  /**
   * Determine if a vertex is valid.
   *
   * @param vertex
   *   The number of the vertex.
   *
   * @return true if the vertex if valid and false otherwise.
   */
  private boolean validVertex(int vertex) {
    return ((vertex >= 0) && (vertex < this.vertices.length)
        && (this.vertexNames[vertex] != null));
  } // validVertex

  /**
   * Get the next unused vertex number.
   *
   * @return an unused vertex number.
   */
  private int newVertexNumber() {
    if (this.unusedVertices.isEmpty()) {
      this.expand();
    } // if
    return this.unusedVertices.remove();
  } // newVertexNumber()

  /**
   * Get a vertex number for a vertex name, even if the name is not already in
   * the graph.
   *
   * @param vertex
   *   The name of the vertex.
   *
   * @return the corresponding vertex number.
   */
  private int safeVertexNumber(String vertex) throws Exception {

    int num = this.vertexNumber(vertex);
    if (num == -1) {
      num = this.addVertex(vertex);
    } // if
    return num;
  } // safeVertexNumber(String)

  public List<Integer> shortestPath(int source, int sink) { 
    int[] nodeDistances = new int[this.numVertices];
    int[] prevNodes = new int[this.numVertices];

    for (int i = 0; i < this.numVertices; ++i) { 
      nodeDistances[i] = Integer.MAX_VALUE;
      prevNodes[i] = -1; 
      unmark(i);        
    }
    nodeDistances[source] = 0;
    int nearest = source;
    while (!isMarked(sink)) {  
      int nearestDistance = Integer.MAX_VALUE;
      for (int i = 0; i < this.numVertices; ++i) { 
        if (!isMarked(i) && nodeDistances[i] < nearestDistance) { 
          nearest = i;
          nearestDistance = nodeDistances[i];
        }
      }
      if (nearestDistance == Integer.MAX_VALUE) {
        break; 
      }

      mark(nearest);

      for (Edge edge : this.vertices[nearest]) { 
        int newDistance = edge.weight() + nodeDistances[nearest];
        if (nodeDistances[edge.target()] > newDistance) { 
          nodeDistances[edge.target()] = newDistance;
          prevNodes[edge.target()] = nearest; 
        }
      }
    }

    if (prevNodes[sink] == -1) {
      return null;
    }

    List<Integer> path = new ArrayList<>();
    for (int prev = sink; prev != -1; prev = prevNodes[prev]) { 
      path.add(0, prev); 
    }
    return path;
  }



} // class Graph
