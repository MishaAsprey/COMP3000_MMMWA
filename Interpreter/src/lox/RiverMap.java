package lox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Records the river network as '->' connections run, and draws it with 'map;'.
class RiverMap {
  // Every river and dam in declaration order, mapped to "river" or "dam".
  private final Map<String, String> kinds = new LinkedHashMap<>();
  // For each river or dam, the names that drain into it.
  private final Map<String, List<String>> upstream = new LinkedHashMap<>();
  // Names that already drain into something (so they are not outputs).
  private final Set<String> drains = new HashSet<>();

  void add(String name, String kind) {
    kinds.put(name, kind);
  }

  void connect(String from, String to) {
    List<String> sources = upstream.computeIfAbsent(to, k -> new ArrayList<>());
    if (!sources.contains(from)) sources.add(from);
    drains.add(from);
  }

  void print() {
    Set<String> shown = new HashSet<>();

    for (String name : kinds.keySet()) {
      // Output rivers don't drain anywhere, so each one is the top of a tree.
      if (!drains.contains(name)) {
        System.out.println(label(name));
        printUpstream(name, "", new HashSet<>(), shown);
      }
    }

    // Rivers stuck in a loop never reach an output, so show them separately.
    for (String name : kinds.keySet()) {
      if (!shown.contains(name)) {
        System.out.println(label(name) + " (never reaches an output)");
        printUpstream(name, "", new HashSet<>(), shown);
      }
    }
  }

  private void printUpstream(String name, String indent, Set<String> path, Set<String> shown) {
    path.add(name);
    shown.add(name);
    List<String> sources = upstream.getOrDefault(name, new ArrayList<>());

    for (int i = 0; i < sources.size(); i++) {
      String source = sources.get(i);
      boolean last = i == sources.size() - 1;
      String branch = last ? "└── " : "├── ";

      // A loop (a -> b; b -> a;) would otherwise print forever.
      if (path.contains(source)) {
        System.out.println(indent + branch + source + " (loop!)");
        continue;
      }

      System.out.println(indent + branch + label(source));
      printUpstream(source, indent + (last ? "    " : "│   "), path, shown);
    }

    path.remove(name);
  }

  private String label(String name) {
    return "dam".equals(kinds.get(name)) ? name + " (dam)" : name;
  }
}
