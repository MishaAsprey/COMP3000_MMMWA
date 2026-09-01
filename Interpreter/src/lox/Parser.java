package lox;

import java.util.List;

class Parser {
    private final List<Token> tokens;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
}