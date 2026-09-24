package lox;

public class WaterFlow {
    char sign;
    double value;

    public WaterFlow(double v){
        sign = '#';
        value = v;
    }

    public String toString(){
        String text = String.valueOf(value);
        if (text.endsWith(".0")) { //We don't need to express integers as decimals
            text = text.substring(0, text.length() - 2);
        }
        return text + " ML/day";
    }

}
