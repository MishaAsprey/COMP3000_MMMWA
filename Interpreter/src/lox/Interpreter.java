package lox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class Interpreter implements Expr.Visitor<Object>,
                             Stmt.Visitor<Void> {

    private Environment environment = new Environment();

    private final FlowSimulation flowSimulation =
        new FlowSimulation(10);

    private int currentDay = 1;
    
    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        System.out.println("visitLiteralExpr");
    return expr.value;
    }
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        System.out.println("visitGroupingExpr");
        return evaluate(expr.expression);
    }
    private Object evaluate(Expr expr) {
        System.out.println("evaluate");
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        System.out.println("execute");
        stmt.accept(this);
    }

    void executeBlock(List<Stmt> statements,
                    Environment environment) {
                        System.out.println("executeBlock");
        Environment previous = this.environment;
        try {
            this.environment = environment;

        for (Stmt statement : statements) {
            execute(statement);
        }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        System.out.println("visitBlockStmt");
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitSetupStmt(Stmt.Setup stmt) {
        executeBlock(stmt.statements, environment);
        return null;
    }

    @Override
    public Void visitDaysStmt(Stmt.Days stmt) {

    // Executes the Days block once for each simulation day.
        try {
         for (int day = 1; day <= 10; day++) {
                currentDay = day;

             executeBlock(
                     stmt.statements,
                       new Environment(environment)
              );
          }
     } finally {
         currentDay = 1;
      }

        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        System.out.println("visitExpressionStmt");
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        System.out.println("visitPrintStmt");
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        System.out.println("visitVarStmt");
        Object value = null;
        if (stmt.initializer != null) {
        value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitRiverStmt(Stmt.River stmt) {
        System.out.println("visitRiverStmt");
        // TODO: evaluate river fields once we have real semantics for them
        List<Object> values = new ArrayList<>();
        for (Object field : stmt.fields) {
            values.add(evaluate((Expr) field));
        }
        environment.define(stmt.name.lexeme, values);
        return null;
    }

    @Override
    public Void visitDamStmt(Stmt.Dam stmt) {
        System.out.println("visitDamStmt");
        // TODO: evaluate river fields once we have real semantics for them
        List<Object> values = new ArrayList<>();
        for (Expr field : stmt.fields) {
            values.add(evaluate(field));
        }

        if (values.size() != 2 ||
                !(values.get(0) instanceof Double) ||
                !(values.get(1) instanceof WaterFlow)) {

            throw new RuntimeError(
                    stmt.name,
                    "River requires a length and an initial WaterFlow value."
                );
        }

        double length = (double) values.get(0);
        WaterFlow initialFlow = (WaterFlow) values.get(1);

        // Registers the river with the simulation.
        flowSimulation.addRiver(
                    stmt.name.lexeme,
                    length,
                initialFlow
        );

        // Stores the river declaration in the environment.
        environment.define(stmt.name.lexeme, values);
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        System.out.println("visitAssignExpr");
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        System.out.println("visitBinaryExpr");
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        System.out.println(expr.left);
        System.out.println(expr.right);
         

        switch (expr.operator.type) {
            case GREATER:
                if(checkNumberOperands(expr.operator, left, right)){ return (double)left > (double)right;}
                else if(checkWaterFlowOperands(expr.operator, left, right)){
                    WaterFlow leftWaterFlow = (WaterFlow)left;
                    WaterFlow rightWaterFlow = (WaterFlow)right;
                    return leftWaterFlow.value > rightWaterFlow.value;
                }
                throw new RuntimeError(expr.operator, "Operand must be a number.");
            case GREATER_EQUAL:
                if(checkNumberOperands(expr.operator, left, right)){
                    return (double)left >= (double)right;
                }
                else if(checkWaterFlowOperands(expr.operator, left, right)){
                    WaterFlow leftWaterFlow = (WaterFlow)left;
                    WaterFlow rightWaterFlow = (WaterFlow)right;
                    return leftWaterFlow.value >= rightWaterFlow.value;
                }
                throw new RuntimeError(expr.operator, "Operand must be a number.");
            case LESS:
                if(checkNumberOperands(expr.operator, left, right)){
                    return (double)left < (double)right;
                }
                else if(checkWaterFlowOperands(expr.operator, left, right)){
                    WaterFlow leftWaterFlow = (WaterFlow)left;
                    WaterFlow rightWaterFlow = (WaterFlow)right;
                    return leftWaterFlow.value < rightWaterFlow.value;
                }
                throw new RuntimeError(expr.operator, "Operand must be a number.");
            case LESS_EQUAL:
                if(checkNumberOperands(expr.operator, left, right)){
                    return (double)left <= (double)right;
                }
                else if(checkWaterFlowOperands(expr.operator, left, right)){
                    WaterFlow leftWaterFlow = (WaterFlow)left;
                    WaterFlow rightWaterFlow = (WaterFlow)right;
                    return leftWaterFlow.value <= rightWaterFlow.value;
                }
                throw new RuntimeError(expr.operator, "Operand must be a number.");
            case MINUS:
                if(checkNumberOperands(expr.operator, left, right)){
                    return (double)left - (double)right;
                }
                else if(checkWaterFlowOperands(expr.operator, left, right)){
                    WaterFlow leftWaterFlow = (WaterFlow)left;
                    WaterFlow rightWaterFlow = (WaterFlow)right;
                    WaterFlow newWaterFlow = new WaterFlow(leftWaterFlow.value - rightWaterFlow.value);
                    return newWaterFlow;
                }
                throw new RuntimeError(expr.operator, "Operand must be a number.");
            case PLUS:
                
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                } 

                else if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }

                else if(checkWaterFlowOperands(expr.operator, left, right)){
                    WaterFlow leftWaterFlow = (WaterFlow)left;
                    WaterFlow rightWaterFlow = (WaterFlow)right;
                    WaterFlow newWaterFlow = new WaterFlow(leftWaterFlow.value + rightWaterFlow.value);
                    return newWaterFlow;
                }
                else if(checkRiverOperands(left, right)){
                    Expr.Variable left2 = (Expr.Variable) expr.left;
                    Expr.Variable right2 = (Expr.Variable) expr.right;
                    Object leftName = left2.name;
                    Object rightName = right2.name;

                    ArrayList<Object> objects = new ArrayList<>();
                    objects.add(leftName);
                    objects.add(rightName);
                    
                    environment.change("river addition", objects);
                    return null;
                }
                throw new RuntimeError(expr.operator,
            "Operands must be two numbers or two strings.");
            
            case SLASH:
                if(checkNumberOperands(expr.operator, left, right)){
                    return (double)left / (double)right;
                }
                else if(checkWaterFlowOperands(expr.operator, left, right)){
                    WaterFlow leftWaterFlow = (WaterFlow)left;
                    WaterFlow rightWaterFlow = (WaterFlow)right;
                    WaterFlow newWaterFlow = new WaterFlow(leftWaterFlow.value / rightWaterFlow.value);
                    return newWaterFlow;
                }
                throw new RuntimeError(expr.operator, "Operand must be a number.");
            case STAR:
                if(checkNumberOperands(expr.operator, left, right)){
                    return (double)left * (double)right;
                }
                else if(checkWaterFlowOperands(expr.operator, left, right)){
                    WaterFlow leftWaterFlow = (WaterFlow)left;
                    WaterFlow rightWaterFlow = (WaterFlow)right;
                    WaterFlow newWaterFlow = new WaterFlow(leftWaterFlow.value * rightWaterFlow.value);
                    return newWaterFlow;
                }
                throw new RuntimeError(expr.operator, "Operand must be a number.");
            case BANG_EQUAL: 
                if(checkWaterFlowOperands(expr.operator, left, right)){
                    WaterFlow leftWaterFlow = (WaterFlow)left;
                    WaterFlow rightWaterFlow = (WaterFlow)right;
                    return !isEqual(leftWaterFlow.value, rightWaterFlow.value);
                }
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                if(checkWaterFlowOperands(expr.operator, left, right)){
                    WaterFlow leftWaterFlow = (WaterFlow)left;
                    WaterFlow rightWaterFlow = (WaterFlow)right;
                    return isEqual(leftWaterFlow.value, rightWaterFlow.value);
                }
                return isEqual(left, right);
            }
            

            // Unreachable.
            return null;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        System.out.println("visitUnaryExpr");
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
            }

        // Unreachable.
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        System.out.println("visitVariableExpr");
        return environment.get(expr.name);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {

     Object object = evaluate(expr.object);

     // Checks whether the property belongs to a river.
      if (object instanceof List<?> &&
             expr.name.lexeme.equals("flow") &&
             expr.object instanceof Expr.Variable) {

         String riverName =
                 ((Expr.Variable) expr.object).name.lexeme;

            // Retrieves the river's flow for the current day.
         try {
               return flowSimulation.getDailyFlow(
                      riverName,
                      currentDay
              );

         } catch (IllegalArgumentException error) {
                throw new RuntimeError(expr.name, error.getMessage());
         }
       }

      throw new RuntimeError(
              expr.name,
              "Only rivers have a 'flow' property."
        );
    }

    private boolean checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return true;
        return false;
    }

    private boolean checkNumberOperands(Token operator,
                                    Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return true;

        return false;
    }

    private boolean checkWaterFlowOperand(Token operator, Object operand) {
        if (operand instanceof WaterFlow) return true;
        return false;
    }

    private boolean checkWaterFlowOperands(Token operator,
                                    Object left, Object right) {
        if (left instanceof WaterFlow && right instanceof WaterFlow) return true;
        return false;
    }

    private boolean checkRiverOperands(Object left, Object right) {
        if (left instanceof ArrayList && right instanceof ArrayList){
            ArrayList<?> leftCheck = (ArrayList<?>) left;
            ArrayList<?> rightCheck = (ArrayList<?>) right;
            if(((leftCheck.size() == 2 && leftCheck.get(0) instanceof Double && leftCheck.get(1) instanceof WaterFlow) || (leftCheck.size() == 3 && leftCheck.get(0) instanceof Double && leftCheck.get(1) instanceof WaterFlow && leftCheck.get(2) instanceof String)) && ((rightCheck.size() == 2 && rightCheck.get(0) instanceof Double && rightCheck.get(1) instanceof WaterFlow) || (rightCheck.size() == 2 && rightCheck.get(0) instanceof Double && rightCheck.get(1) instanceof WaterFlow && rightCheck.get(2) instanceof String))){
                return true;
            }
        };
        return false;
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
        String text = object.toString();
        if (text.endsWith(".0")) {
            text = text.substring(0, text.length() - 2);
        }
        return text;
        }

        return object.toString();
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }
}