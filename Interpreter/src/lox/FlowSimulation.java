package lox;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class FlowSimulation {

    private final int numberOfDays;

    // Stores each river and its daily flow schedule.
    private final Map<String, double[]> riverFlows =
            new LinkedHashMap<>();

    // Stores the length of each river.
    private final Map<String, Double> riverLengths =
            new LinkedHashMap<>();

    // Stores the river connections
    private final Map<String, String> riverConnections = 
            new LinkedHashMap<>();

    public FlowSimulation(int numberOfDays) {
        if (numberOfDays <= 0) {
            throw new IllegalArgumentException(
                    "Simulation must contain at least one day."
            );
        }

        this.numberOfDays = numberOfDays;
    }

    // Registers a river in the simulation.
    // Example: river RiverA [10, #5m];
    // Length is measured in kilometres.
    // Initial flow is added once on Day 1.
    public void addRiver(
            String name,
            double length,
            WaterFlow initialFlow) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "River must have a name."
            );
        }

        if (!Double.isFinite(length) || length < 0) {
            throw new IllegalArgumentException(
                    "River length must be non-negative."
            );
        }

        if (initialFlow == null ||
                !Double.isFinite(initialFlow.value) ||
                initialFlow.value < 0) {

            throw new IllegalArgumentException(
                    "Initial flow must be non-negative."
            );
        }

        if (riverFlows.containsKey(name)) {
            throw new IllegalArgumentException(
                    "River already exists: " + name
            );
        }

        double[] dailyFlow = new double[numberOfDays];

        // Initial flow is included only on Day 1.
        dailyFlow[0] = initialFlow.value;

        riverFlows.put(name, dailyFlow);
        riverLengths.put(name, length);
    }

    public void addRiverConnection(String exitRiver, String enterRiver){
        riverConnections.put(exitRiver, enterRiver);
    }

    // Adds a rainfall event to a river.
    // startDay is zero-based: 0 = Day 1, 1 = Day 2.
    // RainfallModel distributes the rainfall across ten days.
    public void addRainfallEvent(
            String riverName,
            int startDay,
            double rainfallMm) {

        double[] dailyFlow = getRiverSchedule(riverName);
        double length = riverLengths.get(riverName);

        RainfallModel.addRainfallEvent(
                dailyFlow,
                startDay,
                rainfallMm,
                length
        );
    }

    // Gets the flow of a river on a particular day.
    // Day numbers are one-based: 1 = Day 1, 2 = Day 2.
    public WaterFlow getDailyFlow(
            String riverName,
            int day) {

        double[] dailyFlow = getRiverSchedule(riverName);

        if (day < 1 || day > numberOfDays) {
            throw new IllegalArgumentException(
                    "Day must be between 1 and "
                    + numberOfDays + "."
            );
        }

        return new WaterFlow(dailyFlow[day - 1]);
    }

    // Returns a copy of all daily flows for a river.
    public double[] getDailyFlows(String riverName) {
        return getRiverSchedule(riverName).clone();
    }

    // Returns all registered river names.
    public Set<String> getRiverNames() {
        return Collections.unmodifiableSet(
                riverFlows.keySet()
        );
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }

    // Gets the daily schedule for a river.
    private double[] getRiverSchedule(String riverName) {
        double[] schedule = riverFlows.get(riverName);

        if (schedule == null) {
            throw new IllegalArgumentException(
                    "River does not exist: " + riverName
            );
        }

        return schedule;
    }
}