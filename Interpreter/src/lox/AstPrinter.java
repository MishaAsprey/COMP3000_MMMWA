package lox;

import java.util.List;

import lox.Expr.Assign;
import lox.Expr.Variable;

class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
  Void print(List<Stmt> statements) {
    for (Stmt statement : statements) {
      System.out.println(statement.accept(this));
    }
    return null;
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

    //System.out.println(new AstPrinter().print(expression));
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

  @Override
  public String visitBlockStmt(Stmt.Block stmt) {
  String a = "(";
      for (Stmt field: stmt.statements) {
        a += " " + field.accept(this);
      }
      return a + ")";
  }

  @Override
  public String visitExpressionStmt(Stmt.Expression stmt) {
    return stmt.expression.accept(this);
  }
  @Override 
  public String visitPrintStmt(Stmt.Print stmt) {
    return "(print " + stmt.expression.accept(this) + ")";
  }
  @Override
  public String visitVarStmt(Stmt.Var stmt) {
    return "(var " + stmt.name.lexeme + " " + stmt.initializer.accept(this) + ")";
  }
  @Override 
  public String visitRiverStmt(Stmt.River stmt) {
    String a = "(river " + stmt.name.lexeme;
    for (Expr field : stmt.fields) {
      a += " " + field.accept(this);
    }
    return a + ")";
  }
}