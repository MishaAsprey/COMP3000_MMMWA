package lox;

import java.util.List;

abstract class Stmt {
  interface Visitor<R> {
    R visitBlockStmt(Block stmt);
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
    River(Token name, List<Expr> fields) {
        this.name = name;
        this.fields = fields;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitRiverStmt(this);
    }

    final Token name;
    final List<Expr> fields;
  }

  static class Dam extends Stmt {
    Dam(Token name, Expr capacity, List<Expr> conditions, List<Expr> releases, Expr defaultRelease) {
      this.name = name;
      this.capacity = capacity;
      this.conditions = conditions;
      this.releases = releases;
      this.defaultRelease = defaultRelease;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitDamStmt(this);
    }

    final Token name;
    final Expr capacity;
    final List<Expr> conditions;
    final List<Expr> releases;
    final Expr defaultRelease;
  }

  abstract <R> R accept(Visitor<R> visitor);
}
