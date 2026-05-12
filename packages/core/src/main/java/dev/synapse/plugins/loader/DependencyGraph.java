package dev.synapse.plugins.loader;

import java.util.*;

/**
 * Directed dependency graph for plugins with cycle detection.
 *
 * <p>Nodes are plugin ids. Edges represent "depends on" relationships.
 * Used to validate install order and detect circular dependencies.
 */
public class DependencyGraph {

    private final Map<String, Set<String>> edges = new HashMap<>();
    private final Map<String, Set<String>> reverseEdges = new HashMap<>();

    /**
     * Adds a directed edge: {@code from} depends on {@code to}.
     */
    public void addEdge(String from, String to) {
        edges.computeIfAbsent(from, k -> new LinkedHashSet<>()).add(to);
        reverseEdges.computeIfAbsent(to, k -> new LinkedHashSet<>()).add(from);
    }

    /**
     * Returns all direct dependencies of a plugin.
     */
    public Set<String> getDependencies(String pluginId) {
        return edges.getOrDefault(pluginId, Set.of());
    }

    /**
     * Returns all plugins that directly depend on the given plugin.
     */
    public Set<String> getDependents(String pluginId) {
        return reverseEdges.getOrDefault(pluginId, Set.of());
    }

    /**
     * Returns true if adding an edge would create a cycle.
     */
    public boolean wouldCreateCycle(String from, String to) {
        // If 'to' can already reach 'from', adding from→to creates a cycle
        return canReach(to, from, new HashSet<>());
    }

    private boolean canReach(String start, String target, Set<String> visited) {
        if (start.equals(target)) return true;
        if (!visited.add(start)) return false;
        for (String next : edges.getOrDefault(start, Set.of())) {
            if (canReach(next, target, visited)) return true;
        }
        return false;
    }

    /**
     * Detects any cycle in the graph and returns the cycle path.
     *
     * @return empty list if no cycle, otherwise the cycle path
     */
    public List<String> detectCycle() {
        Set<String> visited = new HashSet<>();
        Set<String> inStack = new HashSet<>();
        Deque<String> path = new ArrayDeque<>();

        for (String node : edges.keySet()) {
            if (!visited.contains(node)) {
                List<String> cycle = dfsCycle(node, visited, inStack, path);
                if (cycle != null) return cycle;
            }
        }
        return List.of();
    }

    private List<String> dfsCycle(String node, Set<String> visited, Set<String> inStack, Deque<String> path) {
        visited.add(node);
        inStack.add(node);
        path.push(node);

        for (String next : edges.getOrDefault(node, Set.of())) {
            if (!visited.contains(next)) {
                List<String> cycle = dfsCycle(next, visited, inStack, path);
                if (cycle != null) return cycle;
            } else if (inStack.contains(next)) {
                // Found cycle — extract it from path
                List<String> result = new ArrayList<>();
                boolean collecting = false;
                for (String s : path.reversed()) {
                    if (s.equals(next)) collecting = true;
                    if (collecting) result.add(s);
                }
                result.add(next); // close the cycle
                return result;
            }
        }

        path.pop();
        inStack.remove(node);
        return null;
    }

    /**
     * Returns a topological sort of all nodes.
     *
     * @throws IllegalStateException if the graph contains a cycle
     */
    public List<String> topologicalSort() {
        List<String> cycle = detectCycle();
        if (!cycle.isEmpty()) {
            throw new IllegalStateException(
                "Dependency cycle detected: " + String.join(" → ", cycle)
            );
        }

        Map<String, Integer> inDegree = new HashMap<>();
        Set<String> allNodes = new HashSet<>();
        allNodes.addAll(edges.keySet());
        edges.values().forEach(allNodes::addAll);

        for (String node : allNodes) {
            inDegree.putIfAbsent(node, 0);
        }
        for (Set<String> deps : edges.values()) {
            for (String dep : deps) {
                inDegree.merge(dep, 1, Integer::sum);
            }
        }

        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<String> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String node = queue.poll();
            result.add(node);
            for (String next : edges.getOrDefault(node, Set.of())) {
                int newDegree = inDegree.get(next) - 1;
                inDegree.put(next, newDegree);
                if (newDegree == 0) {
                    queue.add(next);
                }
            }
        }

        return result;
    }

    /**
     * Returns all nodes in the graph (both sources and targets).
     */
    public Set<String> getAllNodes() {
        Set<String> all = new HashSet<>();
        all.addAll(edges.keySet());
        edges.values().forEach(all::addAll);
        return all;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Set<String>> entry : edges.entrySet()) {
            sb.append(entry.getKey()).append(" → ").append(String.join(", ", entry.getValue())).append("\n");
        }
        return sb.toString();
    }
}
