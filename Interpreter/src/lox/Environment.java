package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
        return values.get(name.lexeme);
        }

        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name,
            "Undefined variable '" + name.lexeme + "'.");
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name,
            "Undefined variable '" + name.lexeme + "'.");
    }

    void define(String name, Object value) {
        values.put(name, value);
    }

    void change(String command, ArrayList<Object> objects){

        switch(command){
            case "river addition":
                for(int i = 0; i < objects.size(); i++){
                    System.out.println(objects.get(i));
                }
                Token river1 = (Token) objects.get(0);
                String river1Name = river1.lexeme;
                Token river2 = (Token) objects.get(1);
                String river2Name = river2.lexeme;
                Object river1ValuesCheck = values.get(river1Name);
                if(river1ValuesCheck instanceof List<?>){
                    List<Object> river1Values = new ArrayList<>();
                    river1Values.addAll((List<?>) river1ValuesCheck);
                    if(river1Values.size() == 2){
                        river1Values.add(river2Name);
                    }
                }
                break;
            default:
                break;
        }
    }
}