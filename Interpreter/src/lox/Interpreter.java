package lox;

class Interpreter implements Expr.Visitor<Object>{

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

    void interpret(Expr expression) { 
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }
}
