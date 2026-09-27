package lox;

import lox.Expr.Assign;
import lox.Expr.Variable;

class AstPrinter implements Expr.Visitor<String>,
                            Stmt.Visitor<String> {
  String print(Expr expr) {
    return expr.accept(this);
  }

  String print(Stmt stmt) {
    return stmt.accept(this);
  }

  @Override
  public String visitBlockStmt(Stmt.Block stmt) {
    StringBuilder builder = new StringBuilder();
    builder.append("(block");
    for (Stmt statement : stmt.statements) {
      builder.append("\n  ").append(indent(statement.accept(this)));
    }
    builder.append(")");
    return builder.toString();
  }

  @Override
  public String visitExpressionStmt(Stmt.Expression stmt) {
    return parenthesize(";", stmt.expression);
  }

  @Override
  public String visitPrintStmt(Stmt.Print stmt) {
    return parenthesize("print", stmt.expression);
  }

  @Override
  public String visitVarStmt(Stmt.Var stmt) {
    if (stmt.initializer == null) {
      return "(var " + stmt.name.lexeme + ")";
    }
    return parenthesize("var " + stmt.name.lexeme + " =", stmt.initializer);
  }

  @Override
  public String visitRiverStmt(Stmt.River stmt) {
    return "(river " + stmt.name.lexeme + " " + fieldList(stmt.fields) + ")";
  }

  @Override
  public String visitDamStmt(Stmt.Dam stmt) {
    StringBuilder builder = new StringBuilder();
    builder.append("(dam ").append(stmt.name.lexeme);
    builder.append(" [").append(stmt.capacity.accept(this)).append("]");
    for (int i = 0; i < stmt.conditions.size(); i++) {
      builder.append("\n  (when ").append(stmt.conditions.get(i).accept(this));
      builder.append(" ").append(parenthesize("release", stmt.releases.get(i))).append(")");
    }
    builder.append("\n  (default ").append(parenthesize("release", stmt.defaultRelease)).append("))");
    return builder.toString();
  }

  @Override
  public String visitMapStmt(Stmt.Map stmt) {
    return "(map)";
  }

  private String fieldList(java.util.List<Expr> fields) {
    StringBuilder builder = new StringBuilder("[");
    for (int i = 0; i < fields.size(); i++) {
      if (i > 0) builder.append(", ");
      builder.append(fields.get(i).accept(this));
    }
    return builder.append("]").toString();
  }

  // Pushes the lines of a nested statement one level to the right.
  private String indent(String text) {
    return text.replace("\n", "\n  ");
  }

  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    return parenthesize(expr.operator.lexeme,
                        expr.left, expr.right);
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expr) {
    return parenthesize("group", expr.expression);
  }

  @Override
  public String visitLiteralExpr(Expr.Literal expr) {
    if (expr.value == null) return "nil";
    // Show flows the way they are written in the source, e.g. #1.2
    if (expr.value instanceof WaterFlow) {
      String text = String.valueOf(((WaterFlow)expr.value).value);
      if (text.endsWith(".0")) text = text.substring(0, text.length() - 2);
      return "#" + text;
    }
    return expr.value.toString();
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expr) {
    return parenthesize(expr.operator.lexeme, expr.right);
  }

  private String parenthesize(String name, Expr... exprs) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    for (Expr expr : exprs) {
      builder.append(" ");
      builder.append(expr.accept(this));
    }
    builder.append(")");

    return builder.toString();
  }

  public static void main(String[] args) {
    Expr expression = new Expr.Binary(
        new Expr.Unary(
            new Token(TokenType.MINUS, "-", null, 1),
            new Expr.Literal(123)),
        new Token(TokenType.STAR, "*", null, 1),
        new Expr.Grouping(
            new Expr.Literal(45.67)));

    System.out.println(new AstPrinter().print(expression));
}

  @Override
  public String visitAssignExpr(Assign expr) {
    StringBuilder builder = new StringBuilder();
      builder.append("(= ").append(expr.name.lexeme).append(" ");
      builder.append(expr.value.accept(this));
      builder.append(")");
      return builder.toString();
  }

  @Override
  public String visitVariableExpr(Variable expr) {
      return expr.name.lexeme;
  }
}