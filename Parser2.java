
import java.io.*;

public class Parser2 {
    private Lexer3 lex;
    private BufferedReader pbr;
    private Token look;

    public Parser2(Lexer3 l, BufferedReader br) {
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

    void prog(){
        /*
         * FIRST(prog) = {assign, print, read, for, if, {}
         * GUIDA(prog -> statlist EOF) = {assign, print, read, for, if, {}
         */
        switch(look.tag){
        case '{', Tag.ASSIGN, Tag.PRINT, Tag.READ, Tag.FOR, Tag.IF -> {
            statlist();
            match(Tag.EOF);
            break;
            }
        default -> error("prog Error");
        }

    }
    void statlist(){
        /*
         * FIRST(statlist) = {assign, print, read, for, if, {}
         * GUIDA(statlist -> stat statlistp) = {assign, print, read, for, if, {}
         */
        switch(look.tag){
            case '{', Tag.ASSIGN, Tag.PRINT, Tag.READ, Tag.FOR, Tag.IF -> {
                stat();
                statlistp();
                break;
                }
            default -> error("statlist Error");
            }
    } 
    void statlistp(){
        /*
         * FIRST(statlistp) = {;}
         */
        switch(look.tag){
            //GUIDA(statlisp -> ; stat stalistp) = {;}
            case ';': 
                match(Tag.SEM);
                stat();
                statlistp();
                break;
            //GUIDA(statlistp -> ε) = {EOF, }}
            case '}', Tag.EOF: break;
            default: error("statlisp Error");

            }
    }

    void stat(){
        /*
         * FIRST(stat} = {assign, print, read, for, if, {}
         */
        switch(look.tag){
            // GUIDA(stat -> assign expr to idlist) = {assign}
            case Tag.ASSIGN -> {
                match(Tag.ASSIGN);
                assignlist();
                break;
            }
            //GUIDA(stat -> print { exprlist }) = {print}
            case Tag.PRINT -> {
                match(Tag.PRINT);
                match(Tag.LPT);
                exprlist();
                match(Tag.RPT);
                break;
            }
            // GUIDA(stat -> read { idlist }) = {read}
            case Tag.READ -> {
                match(Tag.READ);
                match(Tag.LPT);
                idlist();
                match(Tag.RPT);
                break;
            }
            // GUIDA(stat -> for ( stat1) = {for}
            case Tag.FOR -> {
                match(Tag.FOR);
                match(Tag.LPT);
                stat1();
                break;
            }
            // GUIDA(stat -> if ( bexpr ) stat stat2 = {if}
            case Tag.IF -> {
                match(Tag.IF);
                match(Tag.LPT);
                bexpr();
                match(Tag.RPT);
                stat();
                stat2();
                break;
            }
            case '{' -> {
                match(Tag.LPG);
                statlist();
                match(Tag.RPG);
                break;
            }
            default -> error("stat Error");
            }
    }
    void stat1(){
        switch(look.tag){
            //GUIDA(stat1 -> ID := expr ; bexpr ) do stat) = {ID}
            case Tag.ID -> {
                match(Tag.ID);
                match(Tag.INIT);
                expr();
                match(Tag.SEM);
                bexpr();
                match(Tag.RPT);
                match(Tag.DO);
                stat();
                break;
            }
            //GUIDA(stat1 -> bexpr ) do stat) = {RELOP}
            case Tag.RELOP -> {
                bexpr();
                match(Tag.RPT);
                match(Tag.DO);
                stat();
                break;
            }
            default -> error("stat1 Error");
        }
    }
    void stat2(){
        switch(look.tag){
            //GUIDA(stat2 -> else stat end) = {else}
            case Tag.ELSE -> {
                match(Tag.ELSE);
                stat();
                match(Tag.END);
                break;
            }
            //GUIDA(stat2 -> end) = {end}
            case Tag.END -> {
                match(Tag.END);
                break;
            }
            default -> error("stat2 Error");
        }
    }
    void assignlist(){
        //FIRST(assignlist) = {[}
        switch(look.tag){
            //GUIDA(assignlist -> [ expr to idlist ] assignlistp) = {[}
            case '[' -> {
                match(Tag.LPS);
                expr();
                match(Tag.TO);
                idlist();
                match(Tag.RPS);
                assignlistp();
                break;
            }
            default -> error("assignlist Error");
        }
    }
    void assignlistp(){
        //FIRST(assignlistp) = {[}
        switch(look.tag){
            //GUIDA(assignlistp -> [ expr to idlist ] assignlistp) = {[}
            case '[':
                match(Tag.LPS);
                expr();
                match(Tag.TO);
                idlist();
                match(Tag.RPS);
                assignlistp();
                break;
            
            //GUIDA(assignlistp -> ε) = {;,else,end,EOF}
            case Tag.SEM, Tag.ELSE, Tag.END, Tag.EOF:
                break;
            
            default: error("assignlistp Error");
        }
    }
    void idlist(){
        //FIRST(idlist) = {ID}
        switch(look.tag){
            //GUIDA(idlist -> ID idlistp) = {ID}
            case Tag.ID -> {
                match(Tag.ID);
                idlistp();
                break;
            }
            default -> error("idlist Error");
        }
    }
    void idlistp(){
        //FIRST(idlistp) = {,}
        switch (look.tag) {
            //GUIDA(idlistp -> , ID idlistp) = {,}
            case Tag.COM:
                match(Tag.COM);
                match(Tag.ID);
                idlistp();
                break;
            
            //GUIDA(idlistp -> ε) = {), ]}
            case ')', ']':
                break;
        
            default: error("idlistp Error"); 
        }
    }
    void bexpr(){
        //FIRST(bexpr) = {RELOP}
        switch (look.tag) {
            //GUIDA(bexpr -> RELOP expr expr) = {RELOP}
            case Tag.RELOP:
                match(Tag.RELOP);
                expr();
                expr();
                break;
            default: error("bexpr Error");
        }
    }
    void expr(){
        //FIRST(expr) = {+, -, *, /, NUM, ID}
        switch(look.tag){
            //GUIDA(expr -> + exprlist) = {+}
            case '+':
                match(Tag.SUM);
                match(Tag.LPT);
                exprlist();
                match(Tag.RPT);
                break;
            //GUIDA(expr -> - exprlist) = {-}
            case '-':
                match(Tag.SUB);
                expr();
                expr();
                break;
            //GUIDA(expr -> * exprlist) = {*}
            case '*':
                match(Tag.MUL);
                match(Tag.LPT);
                exprlist();
                match(Tag.RPT);
                break;
            //GUIDA(expr -> / exprlist) = {/}
            case '/':
                match(Tag.DIV);
                expr();
                expr();
                break;
            //GUIDA(expr -> NUM) = {NUM}
            case Tag.NUM:
                match(Tag.NUM);
                break;
            //GUIDA(expr -> ID) = {ID}
            case Tag.ID:
                match(Tag.ID);
                break;
            default: error("expr Error");
        }
    }
    void exprlist(){
        //FIRST(exprlist) = {+, -, *, /, NUM, ID}
        switch (look.tag) {
            //GUIDA(exprlist -> expr exprlistp) = {+, -, *, /, NUM, ID}
            case '+', '-', '*', '/', Tag.NUM, Tag.ID:
            expr();
            exprlistp();
            break;
            default: error("exprlist Error");
        }
    }
    void exprlistp(){
        //FIRST(exprlistp) = {,}
        switch(look.tag){
            //GUIDA(exprlistp -> , expr exprlisp) = {,}
            case Tag.COM:
            match(Tag.COM);
            expr();
            exprlistp();
            break;
            //GUIDA(exprlistp -> ε) = {)}
            case ')': break;
            default: error("exprlistp Error");

        }
    }
    public static void main(String[] args) {
        Lexer3 lex = new Lexer3();
        String path = "C:\\Users\\39339\\Documents\\LFT_17_09\\test_Parser2.lft"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Parser2 parser = new Parser2(lex, br);
            parser.prog();
            System.out.println("Input OK");
            br.close();
        } catch (IOException e) {e.printStackTrace();}
    }
}