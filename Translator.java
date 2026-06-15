
import java.io.*;

public class Translator{
    private Lexer3 lex;
    private BufferedReader pbr;
    private Token look;
    
    SymbolTable st = new SymbolTable();
    CodeGenerator code = new CodeGenerator();
    int count=0;

    public Translator(Lexer3 l, BufferedReader br) {
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

    public void prog() {        
        /*
        * FIRST(prog) = {assign, print, read, for, if, {}
        * GUIDA(prog -> statlist EOF) = {assign, print, read, for, if, {}
        */        
        int lnext_prog = code.newLabel();
        switch(look.tag){
            case '{', Tag.ASSIGN, Tag.PRINT, Tag.READ, Tag.FOR, Tag.IF -> {
                statlist();
                code.emitLabel(lnext_prog);
                match(Tag.EOF);
                try { code.toJasmin();}
                catch(java.io.IOException e) { System.out.println("IO error\n");}
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
        // FIRST(statlistp) = {;}

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

    public void stat() {
        /*
         * FIRST(stat} = {assign, print, read, for, if, {}
         */
        switch(look.tag){
            // GUIDA(stat -> assign [ expr to idlist ] [ expr1 to idlist1 ] ....) = {assign}
            case Tag.ASSIGN -> {
                match(Tag.ASSIGN);
                while (look.tag == '[') {
                match('[');
                expr();
                match(Tag.TO);
                idlist(0); // 0 mi dice che sto chiamando idlist tramite assign
                match(']');
                }
                break;
            }
            //GUIDA(stat -> print ( exprlist )) = {print}
            case Tag.PRINT -> {
                match(Tag.PRINT);
                match('(');
                exprlist(1);
                match(')');
                break;
            }
            // GUIDA(stat -> read ( idlist )) = {read}
            case Tag.READ -> {
                match(Tag.READ);
                match('(');
                idlist(1); // 1 mi dice che sto chiamando idlist tramite read
                match(')');
                break;
            }
            // GUIDA(stat -> for ( stat1) = {for}
            case Tag.FOR -> {
                // devo creare delle Label alle quale saltare 
                int for_true = code.newLabel();
                int for_false = code.newLabel();
                int for_start = code.newLabel();

                code.emitLabel(for_start);

                match(Tag.FOR);
                match('(');
                stat1(for_start, for_true, for_false);
                break;
            }
            // GUIDA(stat -> if ( bexpr ) stat stat2 = {if}
            case Tag.IF -> {
                // creo 3 label per saltare al codice nel caso condizione vera, falsa, fine
                int if_true = code.newLabel();
                int if_false = code.newLabel();
                int if_end = code.newLabel();

                match(Tag.IF);
                match('(');
                bexpr(if_end, if_true, if_false);
                match(')');
                // metto label condizione vera
                code.emitLabel(if_true);

                stat();
                stat2(if_end, if_true, if_false);
                break;
            }
            case '{' -> {
                match('{');
                statlist();
                match('}');
                break;
            }
            default -> error("stat Error");
        }
    }

    void stat1(int for_start, int for_true, int for_false ){
        switch(look.tag){
            //GUIDA(stat1 -> ID := expr ; bexpr ) do stat) = {ID}
            case Tag.ID -> {
                // check address se esiste
                int id_addr = st.lookupAddress(((Word)look).lexeme);
                if (id_addr == -1){
                    id_addr = count;
                    st.insert(((Word)look).lexeme, count++);
                } 
                match(Tag.ID);
                match(Tag.INIT);
                expr();
                // risultato da mettere da stack a indirizzo 
                code.emit(OpCode.istore, id_addr);

                match(Tag.SEM);
                bexpr(for_start, for_true, for_false);
                match(')');
                // mettere label condizione true
                code.emitLabel(for_true);
                match(Tag.DO);
                stat();
                // se condizione vera salto nuovamente a label for_start
                code.emit(OpCode.GOto, for_start);
                // mettere label condizione false
                code.emitLabel(for_false);
                break;
            }
            //GUIDA(stat1 -> bexpr ) do stat) = {RELOP}
            case Tag.RELOP -> {
                bexpr(for_start, for_true, for_false);
                match(')');
                // mettere label condizione true
                code.emitLabel(for_true);
                match(Tag.DO);
                stat();
                // se condizione vera salto nuovamente a label for_start
                code.emit(OpCode.GOto, for_start);
                // mettere label condizione false
                code.emitLabel(for_false);
                break;
            }
            default -> error("stat1 Error");
        }
    }
    void stat2(int if_end, int if_true, int if_false){
        switch(look.tag){
            //GUIDA(stat2 -> else stat end) = {else}
            case Tag.ELSE -> {
                // salto se serve
                code.emit(OpCode.GOto, if_end);
                
                // metto label per else cioe se if e false
                code.emitLabel(if_false);
                match(Tag.ELSE);
                stat();
                match(Tag.END);
                // metto label per fine if
                code.emitLabel(if_end);
                break;
            }
            //GUIDA(stat2 -> end) = {end}
            case Tag.END -> {
                code.emit(OpCode.GOto, if_end);
                match(Tag.END);

                // posizione label false e end
                code.emitLabel(if_false);
                code.emitLabel(if_end);
                break;
            }
            default -> error("stat2 Error");
        }
    }

    private void idlist(int ra) { // read 1, assign 0
        //FIRST(idlist) = {ID}
        //GUIDA(idlist -> ID idlistp) = {ID}
        
        if(look.tag == Tag.ID){
            //controllo se ID e in memoria, in caso non lo sia lo inserisco come nuovo ID
            int id_addr = st.lookupAddress(((Word)look).lexeme);
            if(id_addr == -1){
                id_addr = count;
                st.insert(((Word)look).lexeme, count++);
            }
            match(Tag.ID);
            if (ra == 0) {
                if(look.tag== Tag.COM){code.emit(OpCode.dup);}
                code.emit(OpCode.istore, id_addr);
            }
            if (ra == 1) {
                code.emit(OpCode.invokestatic, 0); //Output/Read()
                code.emit(OpCode.istore, id_addr);
            }
            idlistp(ra);

        }
        else error("idlist Error");

    }

    void idlistp(int ra){
        //FIRST(idlistp) = {,}
        switch (look.tag) {
            //GUIDA(idlistp -> , ID idlistp) = {,}
            case Tag.COM:
                match(Tag.COM);
                int id_addr = st.lookupAddress(((Word)look).lexeme);
                if (id_addr == -1){
                    id_addr = count;
                    st.insert(((Word)look).lexeme, count++);
                } 
                match(Tag.ID);
                if (ra == 0){
                    if(look.tag == Tag.COM){code.emit(OpCode.dup);}
                    code.emit(OpCode.istore, id_addr);
                } else {
                    code.emit(OpCode.invokestatic, 0);
                    code.emit(OpCode.istore, id_addr);
                }
                idlistp(ra);
                break;
            //GUIDA(idlistp -> ε) = {), ]}
            case ')', ']':
                break;
        
            default: error("idlistp Error"); 
        }
    }

    void bexpr(int c_se, int c_true, int c_false){
        //FIRST(bexpr) = {RELOP}        
        if(look.tag == Tag.RELOP){
            //GUIDA(bexpr -> RELOP expr expr) = {RELOP}
            // copio la stringa RELOP in relop per futuro switch
            String relop = ((Word)look).lexeme;

            match(Tag.RELOP);
            expr();
            expr();
            
            switch (relop) {
                
                case "<" -> code.emit(OpCode.if_icmplt, c_true);
                case ">" -> code.emit(OpCode.if_icmpgt, c_true);
                case "==" -> code.emit(OpCode.if_icmpeq, c_true);
                case "<=" -> code.emit(OpCode.if_icmple, c_true);
                case ">=" -> code.emit(OpCode.if_icmpge, c_true);
                case "<>" -> code.emit(OpCode.if_icmpne, c_true);

                default -> error("Word.java RELOP definition Error");
                }
        } else error("bexpr Error");
        code.emit(OpCode.GOto, c_false);
    }
    void expr(){
        //FIRST(expr) = {+, -, *, /, NUM, ID}
        switch(look.tag){
            //GUIDA(expr -> + exprlist) = {+}
            case '+':
                match(Tag.SUM);
                match('(');
                exprlist(0);
                match(')');
                break;
            //GUIDA(expr -> - exprlist) = {-}
            case '-':
                match(Tag.SUB);
                expr();
                expr();
                code.emit(OpCode.isub);
                break;
            //GUIDA(expr -> * exprlist) = {*}
            case '*':
                match(Tag.MUL);
                match('(');
                exprlist(2);
                match(')');
                break;
            //GUIDA(expr -> / exprlist) = {/}
            case '/':
                match(Tag.DIV);
                expr();
                expr();
                code.emit(OpCode.idiv);
                break;
            //GUIDA(expr -> NUM) = {NUM}
            case Tag.NUM:
                code.emit(OpCode.ldc, ((NumberTok)look).value);
                match(Tag.NUM);
                break;
            //GUIDA(expr -> ID) = {ID}
            case Tag.ID:
                int id_addr = st.lookupAddress(((Word)look).lexeme);
                if (id_addr == -1) error("expr() Error: identifier not defined");

                code.emit(OpCode.iload, id_addr);
                match(Tag.ID);
                break;
            default: error("expr Error");
        }
    }

    void exprlist(int spm){// 0 somma, 1 print, 2 moltiplicazione
        //FIRST(exprlist) = {+, -, *, /, NUM, ID}

        switch (look.tag) {
            //GUIDA(exprlist -> expr exprlistp) = {+, -, *, /, NUM, ID}
            case '+', '-', '*', '/', Tag.NUM, Tag.ID:
                expr();
                if (spm == 1) {code.emit(OpCode.invokestatic, 1);} // emette Print
                exprlistp(spm);
                break;
            default: error("exprlist Error");
        }
    }

    void exprlistp(int spm){ // 0 somma, 1 print, 2 moltiplicazione
        //FIRST(exprlistp) = {,}
        switch(look.tag){
            //GUIDA(exprlistp -> , expr exprlisp) = {,}
            case Tag.COM:
            match(Tag.COM);
            expr();

            switch (spm) {
                case 0 -> code.emit(OpCode.iadd);
                case 1 -> code.emit(OpCode.invokestatic, 1);// emette Print
                case 2 -> code.emit(OpCode.imul);
            }

            exprlistp(spm);
            break;
            
            //GUIDA(exprlistp -> ε) = {)}
            case ')': break;

            default: error("exprlistp Error");

        }
    }
    public static void main(String[] args) {
        Lexer3 lex = new Lexer3();
        String path = "C:\\Users\\39339\\Documents\\LFT_17_09\\test_Translator.lft"; //percorso file leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Translator translator = new Translator(lex, br);
            translator.prog();
            System.out.println("OK");
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
