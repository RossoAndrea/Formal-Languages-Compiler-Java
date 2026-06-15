
import java.io.*;

public class Valutatore {
    private final Lexer3 lex;
    private final BufferedReader pbr;
    private Token look;

    public Valutatore(Lexer3 l, BufferedReader br) { 
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
	int expr_val;

    	// FIRST(start) = {(,NUM}
        switch(look.tag){
            // GUIDA(start) = {(,NUM}
            case '(', Tag.NUM -> {
                expr_val = expr();
                match(Tag.EOF);
                System.out.println(expr_val);
                break;
                }
            default -> error("Start Error");
            }
    }

    private int expr() { 
	// int  exprp_i, term_val, expr_val, exprp_val;

	// FIRST(expr) = {(,NUM}
        switch(look.tag){
             // GUIDA(expr -> term exprp) = {(, NUM}
            case '(' , Tag.NUM -> {
                /*  term_val = term();
                    exprp_i = term_val;
                    exprp_val = exprp(exprp_i);
                    expr_val = exprp_val;
                */
                return exprp(term());
            }
            
            default -> error("expr Error");
            }
	    return 0; //dobbiamo restituire un intero 
    }

    private int exprp(int exprp_i) {
	// int exprp1_i, exprp_val, term_val
        switch (look.tag) {
            // guida(exprp -> + term exprp) = {+}
            case '+':
                match('+');
                /* term_val = term();
                    exprp1_i = exprp_i + term_val;
                    exprp1_val = exprp(exprp1_i);
                    exprp_val = exprp1_val;
                    return exprp_val;
                */
                return exprp(exprp_i + term());
            // guida(exprp -> - term exprp) = {-}
            case '-':
                match('-');
                /* term_val = term();
                    exprp1_i = exprp_i - term_val;
                    exprp1_val = exprp(exprp1_i);
                    exprp_val = exprp1_val;
                    return exprp_val;
                */
                return exprp(exprp_i - term());
            // guida(exprp -> ε) = {),EOF}
            case ')', Tag.EOF: break;
            
            default: error("exprp Error");
	    }
        return exprp_i;
    }

    private int term() { 
    // int termp_i, fact_val, term_val, termp_val
        switch(look.tag){
	        // guida(term -> fact termp ) = {(,NUM}
            case '(', Tag.NUM:
                /*
                * fact_val = fact();
                * termp_i = fact_val;
                * termp_val = termp(termp_i);
                * term_val = termp_val;
                * return termp_val;
                */
                return termp(fact());
            
            default: error("term Error");
        }
        return 0;
    }
    
    private int termp(int termp_i) { 
	    //  int termp1_i, termp_i, fact_val, termp_val, termp1_val;
        switch(look.tag){
            // guida(termp -> * fact termp ) = {*}
            case '*':
                /*
                * fact_val = fact();
                * termp1_i = termp_i * fact_val;
                * termp1_val = ttermp(termp1_i);
                * termp_val = termp1_val;
                * return termp_val;
                */
                match(Tag.MUL);
                return termp(termp_i * fact());
            
            // guida(termp -> / fact termp ) = {/}
            case '/':
                /*
                * fact_val = fact();
                * termp1_i = termp_i / fact_val;
                * termp1_val = ttermp(termp1_i);
                * termp_val = termp1_val;
                * return termp_val;
                */
                match(Tag.DIV);
                return termp(termp_i / fact());
            
            // guida(termp -> ε) = {+,-,),EOF}
            case '+', '-', ')', Tag.EOF: break;
            default: error("termp Error");
        }
        return termp_i;
    }
    
    private int fact() { 
        int fact_val;
        //int fact_val, expr_val, NUM_val;
        switch(look.tag){
            // guida(fact -> (expr)) = {(}
            case '(':
                /*
                * expr_val = expr();
                * fact_val = expr_val;
                * return fact_val;
                */
                match(Tag.LPT);
                fact_val = expr();
                match(Tag.RPT);
                return fact_val;

            // guida(fact -> NUM) = {NUM}
            case Tag.NUM:
                fact_val = ((NumberTok) look).value;
                match(Tag.NUM);
                return fact_val;
            
            default: error("fact Error");
        }
        return 0;
    }

    public static void main(String[] args) {
        Lexer3 lex = new Lexer3();
        String path = "C:\\Users\\39339\\Documents\\LFT_17_09\\test_Valutatore.lft"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Valutatore valutatore = new Valutatore(lex, br);
            valutatore.start();
            br.close();
        } catch (IOException e) {e.printStackTrace();}
    }
}