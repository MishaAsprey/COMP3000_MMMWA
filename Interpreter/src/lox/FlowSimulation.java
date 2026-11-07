package lox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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

    // Stores the connections
    private final Map<String, String> connections = 
            new LinkedHashMap<>();

    // Stores each dam and its daily flow schedule.
    private final Map<String, double[]> damAmounts =
            new LinkedHashMap<>();

    // Stores the size of each dam.
    private final Map<String, Double> damSizes =
            new LinkedHashMap<>();

    private final Map<String, WaterFlow> damMaxes =
            new LinkedHashMap<>();

    private final Map<String, List<Object>> damCases =
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

    public void addDam(
            String name,
            double size,
            WaterFlow currentVol,
            WaterFlow maxVol,
            List<Object> cases) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Dam must have a name."
            );
        }

        if (!Double.isFinite(size) || size < 0) {
            throw new IllegalArgumentException(
                    "Dam size must be non-negative."
            );
        }

        if (currentVol == null ||
                !Double.isFinite(currentVol.value) ||
                currentVol.value < 0) {

            throw new IllegalArgumentException(
                    "Current volume must be non-negative."
            );
        }

        if (maxVol == null ||
                !Double.isFinite(maxVol.value) ||
                maxVol.value < 0) {

            throw new IllegalArgumentException(
                    "Max volume must be non-negative."
            );
        }

        if (damAmounts.containsKey(name)) {
            throw new IllegalArgumentException(
                    "Dam already exists: " + name
            );
        }

        if(cases.size() != 3){
            throw new IllegalArgumentException(
                    "Dam does not have correct cases: " + name
            );
        }

        double[] dailyFlow = new double[numberOfDays];

        dailyFlow[0] = currentVol.value;

        damAmounts.put(name, dailyFlow);
        damSizes.put(name, size);
        damMaxes.put(name, maxVol);

        Object pCheck = cases.get(0);
        List<Object> percentages = new ArrayList<>();
        if(pCheck instanceof List<?>){
            List<?> pCheck2 = (List<?>) pCheck;
            for(Object element : pCheck2){
                percentages.add((Expr) element);
            }
        }
        else{
            throw new IllegalArgumentException(
                    "Dam does not have correct cases: " + name
            );
        }

        Object cCheck = cases.get(1);
        List<String> conditions = new ArrayList<>();
        if(cCheck instanceof List<?>){
            List<?> cCheck2 = (List<?>) cCheck;
            for(Object element : cCheck2){
                if(element instanceof String){
                    conditions.add((String) element);
                }
            }
        }
        else{
            throw new IllegalArgumentException(
                    "Dam does not have correct cases: " + name
            );
        }

        Object fCheck = cases.get(2);
        List<Object> flows = new ArrayList<>();
        if(fCheck instanceof List<?>){
            List<?> fCheck2 = (List<?>) fCheck;
            for(Object element : fCheck2){
                flows.add(element);
            }
        }
        else{
            throw new IllegalArgumentException(
                    "Dam does not have correct cases: " + name
            );
        }

        List<Double> percentagesActual = new ArrayList<>();
        List<String> conditionsActual = new ArrayList<>();
        List<WaterFlow> flowsActual = new ArrayList<>();

        int i = 0;
        while(i < percentages.size()){
            if(percentages.get(i) instanceof Double && flows.get(i) instanceof WaterFlow){
                percentagesActual.add((Double) percentages.get(i));
                conditionsActual.add(conditions.get(i));
                flowsActual.add((WaterFlow) flows.get(i));
            }
            i++;
        }
        flowsActual.add((WaterFlow) flows.get(i));

        List<Object> casesActual = new ArrayList<>();
        casesActual.add(percentagesActual);
        casesActual.add(conditionsActual);
        casesActual.add(flowsActual);

        damCases.put(name, casesActual);
    }

    public void addConnection(String exitRiver, String enterRiver){
        connections.put(exitRiver, enterRiver);
    }

    public void addRainfall(int startDay, double rainfallMm){
        //System.out.println("ran");
        Set<String> rivers = getRiverNames();
        Set<String> dams = getDamNames();
        //System.out.println(rivers);
        for(String river: rivers){
            addRiverRainfallEvent(river, startDay, rainfallMm);
            //System.out.println("ran river " + river);
        }
        for(String dam: dams){
            addDamRainfallEvent(dam, startDay, rainfallMm);
        }
        addConnectionFlows(rivers, dams);
    }

    // Adds a rainfall event to a river.
    // startDay is zero-based: 0 = Day 1, 1 = Day 2.
    // RainfallModel distributes the rainfall across ten days.
    public void addRiverRainfallEvent(
            String riverName,
            int startDay,
            double rainfallMm) {
        //System.out.println("ran");
        double[] dailyFlow = getRiverSchedule(riverName);
        double length = riverLengths.get(riverName);

        RainfallModel.addRainfallEvent(
                dailyFlow,
                startDay,
                rainfallMm,
                length
        );
    }

    public void addDamRainfallEvent(
            String damName,
            int startDay,
            double rainfallMm) {
        //System.out.println("ran");
        double[] dailyFlow = getDamSchedule(damName);
        double size = damSizes.get(damName);

        RainfallModel.addRainfallEvent(
                dailyFlow,
                startDay,
                rainfallMm,
                size
        );
    }

    // Gets the flow of a river on a particular day.
    // Day numbers are one-based: 1 = Day 1, 2 = Day 2.
    public WaterFlow getDailyFlow(
            String riverName,
            int day) {

        double[] dailyFlow = getRiverSchedule(riverName);
        if(dailyFlow == null){
            dailyFlow = getDamSchedule(riverName);
        }

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
        if(getRiverSchedule(riverName) != null){
            return getRiverSchedule(riverName).clone();
        }
        else{
            return getDamSchedule(riverName).clone();
        }
    }

    // Returns all registered river names.
    public Set<String> getRiverNames() {
        return Collections.unmodifiableSet(
                riverFlows.keySet()
        );
    }

    public Set<String> getDamNames() {
        return Collections.unmodifiableSet(
                damAmounts.keySet()
        );
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }

    // Gets the daily schedule for a river.
    private double[] getRiverSchedule(String riverName) {
        double[] schedule = riverFlows.get(riverName);

        if (schedule == null) {
            // throw new IllegalArgumentException(
            //         "River does not exist: " + riverName
            // );
            return null;
        }

        return schedule;
    }

    private double[] getDamSchedule(String damName) {
        double[] schedule = damAmounts.get(damName);

        if (schedule == null) {
            // throw new IllegalArgumentException(
            //         "River does not exist: " + damName
            // );
            return null;
        }

        return schedule;
    }

    public void addConnectionFlows(Set<String> rivers, Set<String> dams){
        for(int i = 1; i < 10; i++){
            for(String river: rivers){
                String connection = connections.get(river);
                if(connection != null){
                    boolean isRiver = true;
                    double[] exitFlows = riverFlows.get(river);
                    double[] entryFlows = riverFlows.get(connection);
                    if(entryFlows == null){
                        entryFlows = damAmounts.get(connection);
                        isRiver = false;
                    }
                    entryFlows[i] += exitFlows[i-1];
                    if(isRiver){
                        riverFlows.put(connection, entryFlows);
                    }
                    else{
                        damAmounts.put(connection, entryFlows);
                    }
                }
            }
            for(String dam : dams){
                String connection = connections.get(dam);
                if(connection != null){
                    boolean isRiver = true;
                    double[] exitFlows = damAmounts.get(dam);
                    double[] entryFlows = riverFlows.get(connection);
                    double max = damMaxes.get(dam).value;
                    if(entryFlows == null){
                        isRiver = false;
                        entryFlows = damAmounts.get(connection);
                    }
                    WaterFlow exitAmount = new WaterFlow(0);
                    int j = 0;
                    double percent = exitFlows[i-1] / max * 100;
                    List<Object> cases = damCases.get(dam);

                    Object pCheck = cases.get(0);
                    List<Double> percentages = new ArrayList<>();
                    if(pCheck instanceof List<?>){
                        List<?> pCheck2 = (List<?>) pCheck;
                        for(Object element : pCheck2){
                            if(element instanceof Double){
                                percentages.add((Double) element);
                            }
                        }
                    }
                    else{
                        throw new IllegalArgumentException(
                            "Incorrect case"
                        );
                    }

                    Object cCheck = cases.get(1);
                    List<String> conditions = new ArrayList<>();
                    if(cCheck instanceof List<?>){
                        List<?> cCheck2 = (List<?>) cCheck;
                        for(Object element : cCheck2){
                            if(element instanceof String){
                                conditions.add((String) element);
                            }
                        }
                    }
                    else{
                        throw new IllegalArgumentException(
                            "Incorrect case"
                        );
                    }

                    Object fCheck = cases.get(2);
                    List<WaterFlow> flows = new ArrayList<>();
                    if(fCheck instanceof List<?>){
                        List<?> fCheck2 = (List<?>) fCheck;
                        for(Object element : fCheck2){
                            if(element instanceof WaterFlow){
                                flows.add((WaterFlow) element);
                            }
                        }
                    }
                    else{
                        throw new IllegalArgumentException(
                            "Incorrect case"
                        );
                    }
                    //System.out.println("percentages size " + percentages.size());
                    while(j < percentages.size()){
                        //System.out.println("ran 2");
                        if(conditions.get(j).equals("=")){
                            //System.out.println("condition 1 '='");
                            if(percent == percentages.get(j)){
                                if(flows.get(j).value > exitFlows[i-1]){
                                    exitAmount.value = exitFlows[i-1];
                                }
                                else{
                                    exitAmount.value = flows.get(j).value;
                                }
                                break;
                            }
                        }
                        else if(conditions.get(j).equals("<")){
                            //System.out.println("condition 2 '<'");
                            if(percent < percentages.get(j)){
                                if(flows.get(j).value > exitFlows[i-1]){
                                    exitAmount.value = exitFlows[i-1];
                                }
                                else{
                                    exitAmount.value = flows.get(j).value;
                                }
                                break;
                            }
                        }
                        else if(conditions.get(j).equals("<=")){
                        // System.out.println("condition 3 '<='");
                            if(percent <= percentages.get(j)){
                                if(flows.get(j).value > exitFlows[i-1]){
                                    exitAmount.value = exitFlows[i-1];
                                }
                                else{
                                    exitAmount.value = flows.get(j).value;
                                }
                                break;
                            }
                        }
                        else if(conditions.get(j).equals(">")){
                            //System.out.println("condition 4 '>'");
                            if(percent > percentages.get(j)){
                                if(flows.get(j).value > exitFlows[i-1]){
                                    exitAmount.value = exitFlows[i-1];
                                }
                                else{
                                    exitAmount.value = flows.get(j).value;
                                }
                                break;
                            }
                            
                        }
                        else if(conditions.get(j).equals(">=")){
                            //System.out.println("condition 5 '>='");
                            if(percent >= percentages.get(j)){
                                if(flows.get(j).value > exitFlows[i-1]){
                                    exitAmount.value = exitFlows[i-1];
                                }
                                else{
                                    exitAmount.value = flows.get(j).value;
                                }
                                break;
                            }
                            
                        }
                        //System.out.println("completed while");
                        j++;
                    }
                    if(j == percentages.size()){
                        if(flows.get(j).value > exitFlows[i-1]){
                            exitAmount.value = exitFlows[i-1];
                        }
                        else{
                            exitAmount.value = flows.get(j).value;
                        }
                    }
                    if(exitFlows[i-1] - exitAmount.value > max){
                        exitAmount.value += (exitFlows[i-1] - exitAmount.value) - max;
                    }
                    //System.out.println("j " + j);
                    //System.out.println(exitAmount.value);
                    entryFlows[i] += exitAmount.value;
                    exitFlows[i] += exitFlows[i-1] - exitAmount.value;
                    if(isRiver){
                        riverFlows.put(connection, entryFlows);
                    }
                    else{
                        damAmounts.put(connection, entryFlows);
                    }
                }
            }
        }
    }
}