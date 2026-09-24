package lox;

import java.util.List;

class Interpreter implements Expr.Visitor<Object>,
                             Stmt.Visitor<Void> {

    private Environment environment = new Environment();

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value;
    }
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    void executeBlock(List<Stmt> statements,
                    Environment environment) {
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
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
        value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitRiverStmt(Stmt.River stmt) {
        // TODO:evaluate river fields once we have real semantics for them
        //POST? I don't really know if I'm "doing that" but I want to implement my arrow "->"
        //which means I need to be able to save rivers and shit
        Object flow = new WaterFlow(0);
        for (int i = 0; i < stmt.fields.size(); i++){
            Object value = evaluate(stmt.fields.get(i));
            if (i == 1) flow = value;
        }

        environment.define(stmt.name.lexeme, flow);
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
         

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
            case ARROW:
                if (!checkWaterFlowOperands(expr.operator, left, right)){
                    throw new RuntimeError(expr.operator, "Only flows can drain into a river!");
                }
                Token river = ((Expr.Variable)expr.right).name;
                WaterFlow combined = new WaterFlow(((WaterFlow)left).value + ((WaterFlow)right).value);
                environment.assign(river, combined);
                return combined;
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
        return environment.get(expr.name);
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