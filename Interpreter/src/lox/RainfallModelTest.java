
package lox;

public class RainfallModelTest {

    public static void main(String[] args) {

        // Test the catchment area.
        double area = RainfallModel.calculateCatchmentArea(10);
        check("Catchment area", area, 100.0);

        // Test rainfall conversion.
        double total = RainfallModel.calculateTotalRainwater(20, 10);
        check("Total rainwater", total, 2.0);

        // Test the 10-day rainfall distribution.
        double[] dailyWater = new double[10];

        RainfallModel.addRainfallEvent(
            dailyWater, 0, 20, 10
        );

        check("Day 1", dailyWater[0], 1.0);
        check("Day 2", dailyWater[1], 0.5);
        check("Day 3", dailyWater[2], 0.25);

        double sum = 0;

        for (double water : dailyWater) {
            sum += water;
        }

        check("Total over 10 days", sum, 2.0);

        // Test two rainfall events happening at different times.
        double[] overlappingWater = new double[12];

        RainfallModel.addRainfallEvent(
            overlappingWater, 0, 20, 10
        );

        RainfallModel.addRainfallEvent(
            overlappingWater, 2, 10, 10
        );

        check(
            "Two events on Day 3",
            overlappingWater[2],
            0.75
        );

        // Test zero rainfall.
        double noRain = RainfallModel.calculateTotalRainwater(0, 10);
        check("Zero rainfall", noRain, 0.0);

        System.out.println("All rainfall tests passed!");
    }

    private static void check(
            String name, double actual, double expected) {

        double difference = Math.abs(actual - expected);

        if (difference > 0.000000001) {
            throw new AssertionError(
                name + " failed. Expected " + expected
                + " but got " + actual
            );
        }

        System.out.println(name + ": PASS");
    }
}
