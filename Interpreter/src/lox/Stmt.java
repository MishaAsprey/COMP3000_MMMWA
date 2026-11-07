package lox;

import java.util.List;
import java.util.Map;

abstract class Stmt {
  interface Visitor<R> {
    R visitBlockStmt(Block stmt);
    R visitSetupStmt(Setup stmt);
    R visitDaysStmt(Days stmt);
    R visitExpressionStmt(Expression stmt);
    R visitPrintStmt(Print stmt);
    R visitVarStmt(Var stmt);
    R visitRiverStmt(River stmt);
    R visitDamStmt(Dam stmt);

  }
  static class Block extends Stmt {
    Block(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }

    final List<Stmt> statements;
  }
  static class Setup extends Stmt {
    Setup(Token keyword, List<Stmt> statements) {
      this.keyword = keyword;
      this.statements = statements;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSetupStmt(this);
    }

    final Token keyword;
    final List<Stmt> statements;
  }
  static class Days extends Stmt {
    Days(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitDaysStmt(this);
    }

    final List<Stmt> statements;
  }
  static class Expression extends Stmt {
    Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }

    final Expr expression;
  }
  static class Print extends Stmt {
    Print(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }

    final Expr expression;
  }
  static class Var extends Stmt {
      Var(Token name, Expr initializer) {
        this.name = name;
        this.initializer = initializer;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }

      final Token name;
      final Expr initializer;
  }

static class River extends Stmt {
    River(Token name, List<Object> fields) {
        this.name = name;
        this.fields = fields;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitRiverStmt(this);
    }

    final Token name;
    final List<Object> fields;
  }

  static class Dam extends Stmt {
    Dam(Token name, List<Expr> fields, List<Object> cases) {
        this.name = name;
        this.fields = fields;
        this.cases = cases;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitDamStmt(this);
    }

    final Token name;
    final List<Expr> fields;
    final List<Object> cases;
  }

  abstract <R> R accept(Visitor<R> visitor);
}
