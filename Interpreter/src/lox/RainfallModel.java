
package lox;

public class RainfallModel {

    // How much of the rainfall arrives on each day.
    private static final double[] DAILY_WEIGHTS = {
        0.5,
        0.25,
        0.125,
        0.0625,
        0.03125,
        0.015625,
        0.0078125,
        0.00390625,
        0.001953125,
        0.001953125
    };

    // Each river has a square catchment area.
    public static double calculateCatchmentArea(double length) {
        if (length < 0) {
            throw new IllegalArgumentException(
                "River length cannot be negative."
            );
        }

        return length * length;
    }

    // Converts rainfall into million cubic metres of water.
    public static double calculateTotalRainwater(
            double rainfall, double length) {

        if (rainfall < 0) {
            throw new IllegalArgumentException(
                "Rainfall cannot be negative."
            );
        }

        double area = calculateCatchmentArea(length);

        return rainfall * area / 1000.0;
    }

    // Adds rainfall water to the days it will arrive.
    public static void addRainfallEvent(
            double[] dailyWater,
            int startDay,
            double rainfall,
            double length) {

        if (dailyWater == null || dailyWater.length == 0) {
            throw new IllegalArgumentException(
                "Daily water schedule cannot be empty."
            );
        }

        if (startDay < 0 || startDay >= dailyWater.length) {
            throw new IllegalArgumentException(
                "Start day is outside the simulation."
            );
        }

        double totalWater = calculateTotalRainwater(
            rainfall, length
        );

        for (int i = 0; i < DAILY_WEIGHTS.length; i++) {
            int day = startDay + i;

            // Stop if there are no more days in the simulation.
            if (day >= dailyWater.length) {
                break;
            }

            // Add to existing water instead of replacing it.
            dailyWater[day] += totalWater * DAILY_WEIGHTS[i];
        }
    }

    // Returns the percentages used by the rainfall model.
    public static double[] getDailyWeights() {
        return DAILY_WEIGHTS.clone();
    }
}
