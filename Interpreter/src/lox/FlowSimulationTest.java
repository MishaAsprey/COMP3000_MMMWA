
package lox;

public class FlowSimulationTest {

    private static final double TOLERANCE = 0.000001;

    public static void main(String[] args) {

        testInitialWaterFlow();
        testInitialFlowAddedOnce();
        testRainfallDistribution();
        testOverlappingRainfall();
        testInitialFlowAndRainfall();
        testMultipleRivers();
        testInvalidRiver();
        testInvalidDay();

        System.out.println(
                "\nAll flow simulation tests passed!"
        );
    }

    // Checks that #5m is stored as a WaterFlow value.
    private static void testInitialWaterFlow() {

        FlowSimulation simulation = new FlowSimulation(10);

        simulation.addRiver(
                "RiverA",
                10,
                new WaterFlow(5.0)
        );

        check(
                "Initial WaterFlow on Day 1",
                simulation.getDailyFlow("RiverA", 1).value,
                5.0
        );
    }

    // Initial flow must not be added again on later days.
    private static void testInitialFlowAddedOnce() {

        FlowSimulation simulation = new FlowSimulation(10);

        simulation.addRiver(
                "RiverA",
                10,
                new WaterFlow(5.0)
        );

        check(
                "Initial flow on Day 2",
                simulation.getDailyFlow("RiverA", 2).value,
                0.0
        );

        check(
                "Initial flow on Day 3",
                simulation.getDailyFlow("RiverA", 3).value,
                0.0
        );
    }

    // Checks the ten-day rainfall distribution.
    private static void testRainfallDistribution() {

        FlowSimulation simulation = new FlowSimulation(10);

        simulation.addRiver(
                "RiverA",
                10,
                new WaterFlow(0.0)
        );

        simulation.addRainfallEvent(
                "RiverA",
                0,
                10.0
        );

        check(
                "Rainfall on Day 1",
                simulation.getDailyFlow("RiverA", 1).value,
                0.5
        );

        check(
                "Rainfall on Day 2",
                simulation.getDailyFlow("RiverA", 2).value,
                0.25
        );

        check(
                "Rainfall on Day 3",
                simulation.getDailyFlow("RiverA", 3).value,
                0.125
        );

        double total = 0;

        for (double flow : simulation.getDailyFlows("RiverA")) {
            total += flow;
        }

        check(
                "Total rainfall over ten days",
                total,
                1.0
        );
    }

    // Checks that overlapping rainfall events accumulate.
    private static void testOverlappingRainfall() {

        FlowSimulation simulation = new FlowSimulation(10);

        simulation.addRiver(
                "RiverA",
                10,
                new WaterFlow(0.0)
        );

        simulation.addRainfallEvent("RiverA", 0, 10.0);
        simulation.addRainfallEvent("RiverA", 1, 10.0);

        // Day 2:
        // First event contributes 0.25.
        // Second event contributes 0.5.
        // Total = 0.75.
        check(
                "Overlapping rainfall on Day 2",
                simulation.getDailyFlow("RiverA", 2).value,
                0.75
        );
    }

    // Checks that initial flow and rainfall are combined.
    private static void testInitialFlowAndRainfall() {

        FlowSimulation simulation = new FlowSimulation(10);

        simulation.addRiver(
                "RiverA",
                10,
                new WaterFlow(5.0)
        );

        simulation.addRainfallEvent("RiverA", 0, 10.0);

        check(
                "Initial flow plus rainfall on Day 1",
                simulation.getDailyFlow("RiverA", 1).value,
                5.5
        );

        check(
                "Rainfall on Day 2",
                simulation.getDailyFlow("RiverA", 2).value,
                0.25
        );
    }

    // Checks that separate rivers have separate schedules.
    private static void testMultipleRivers() {

        FlowSimulation simulation = new FlowSimulation(10);

        simulation.addRiver(
                "RiverA",
                10,
                new WaterFlow(5.0)
        );

        simulation.addRiver(
                "RiverB",
                8,
                new WaterFlow(2.7)
        );

        check(
                "RiverA initial flow",
                simulation.getDailyFlow("RiverA", 1).value,
                5.0
        );

        check(
                "RiverB initial flow",
                simulation.getDailyFlow("RiverB", 1).value,
                2.7
        );
    }

    // Checks that an unknown river is rejected.
    private static void testInvalidRiver() {

        FlowSimulation simulation = new FlowSimulation(10);

        try {
            simulation.getDailyFlow("UnknownRiver", 1);

            throw new AssertionError(
                    "Unknown river should cause an error."
            );

        } catch (IllegalArgumentException expected) {
            System.out.println("Unknown river: PASS");
        }
    }

    // Checks that invalid day numbers are rejected.
    private static void testInvalidDay() {

        FlowSimulation simulation = new FlowSimulation(10);

        simulation.addRiver(
                "RiverA",
                10,
                new WaterFlow(5.0)
        );

        try {
            simulation.getDailyFlow("RiverA", 11);

            throw new AssertionError(
                    "Invalid day should cause an error."
            );

        } catch (IllegalArgumentException expected) {
            System.out.println("Invalid day: PASS");
        }
    }

    private static void check(
            String testName,
            double actual,
            double expected) {

        if (Math.abs(actual - expected) > TOLERANCE) {
            throw new AssertionError(
                    testName
                    + " failed. Expected: " + expected
                    + ", got: " + actual
            );
        }

        System.out.println(testName + ": PASS");
    }
}
