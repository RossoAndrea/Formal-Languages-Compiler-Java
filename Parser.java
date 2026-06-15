
import java.io.*;
import java.util.*;

public class Parser{
    private Lexer3 lex;
    private BufferedReader pbr;
    private Token look;

    public Parser(Lexer3 l, BufferedReader br) {
        lex = l;
        pbr = br;
        move();
    }

    void move() {
        look = lex.lexical_scan(pbr);
        System.out.println("token = " + look);
    }

    void error(String s) {
	throw new Error("near line " + lex.line + ": " + s);
    }

    void match(int t) {
	if (look.tag == t) {
	    if (look.tag != Tag.EOF) move();
	} else error("syntax error");
    }

    public void start() {
        // FIRST(start) = {(,NUM}
        // GUIDA(start) = {(,NUM}
        switch(look.tag){
        case '(', Tag.NUM -> {
            expr();
	        match(Tag.EOF);
            break;
            }
        default -> error("Start Error");
        }
    }

    private void expr() {
	    // FIRST(expr) = {(,NUM}
        // GUIDA(expr -> term exprp) = {(, NUM}
        switch(look.tag){
            case '(' , Tag.NUM -> {
                term();
                exprp();
                break;
            }
            default -> error("expr Error");
        }
    }

    private void exprp() {
	switch (look.tag) {
    // guida(exprp -> + term exprp) = {+}
	case '+':
	    match(Tag.SUM);
        term();
        exprp();
        break;
    // guida(exprp -> - term exprp) = {-}
    case '-':
        match(Tag.SUB);
        term();
        exprp();
        break;
    // guida(exprp -> ε) = {),EOF
         case ')', Tag.EOF: break;
    default:
    error("exprp Error");
    break;
    
	}
    }

    private void term() {
    switch(look.tag){
    // guida(term -> fact termp ) = {(,NUM}
    case '(', Tag.NUM -> {
            fact();
            termp();
            break;
        }
    
    default -> error("term Error");
    }
    }

    private void termp() {
    switch(look.tag){
        // guida(termp -> * fact termp ) = {*}
        case '*':
            match(Tag.MUL);
            fact();
            termp();
            break;
        // guida(termp -> / fact termp ) = {/}
        case '/':
            match(Tag.DIV);
            fact();
            termp();
            break;
        // guida(termp -> ε) = {+,-,),EOF}
        case '+', '-', ')', Tag.EOF: break;
        default: error("termp Error");
    }
    }

    private void fact() {
        switch(look.tag){
        // guida(fact -> (expr)) = {(}
        case '(' -> {
            match(Tag.LPT);
            expr();
            match(Tag.RPT);
            break;
        }
        // guida(fact -> NUM) = {NUM}
        case Tag.NUM -> match(Tag.NUM);
        default -> error("fact Error");
        }
    }
		
    public static void main(String[] args) {
        Lexer3 lex = new Lexer3();
        String path = "C:\\Users\\39339\\Documents\\LFT_17_09\\test_Parser1.lft"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Parser parser = new Parser(lex, br);
            parser.start();
            System.out.println("Input OK");
            br.close();
        } catch (IOException e) {e.printStackTrace();}
    }
}