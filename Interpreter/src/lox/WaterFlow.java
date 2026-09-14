package lox;

public class WaterFlow {
    char sign;
    double value;

    public WaterFlow(double v){
        sign = '#';
        value = v;
    }

    public String toString(){
        return "#" + String.valueOf(value);
    }
}
