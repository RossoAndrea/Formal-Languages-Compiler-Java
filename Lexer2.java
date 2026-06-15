
import java.io.*; 

public class Lexer2 extends Lexer {

    public static int line = 1;
    private char peek = ' ';
    
    private void readch(BufferedReader br) {
        try {
            peek = (char) br.read();
        } catch (IOException exc) {
            peek = (char) -1; // ERROR
        }
    }

    public Token lexical_scan(BufferedReader br) {
        while (peek == ' ' || peek == '\t' || peek == '\n'  || peek == '\r') {
            if (peek == '\n') line++;
            readch(br);
        }
        
        switch (peek) {
            case '!':
                peek = ' ';
                return Token.not;

	// ... gestire i casi di ( ) [ ] { } + - * / ; , ... //
            case '(':
                peek = ' ';
                return Token.lpt;

            case ')':
                peek = ' ';
                return Token.rpt;
                
            case '[':
                peek = ' ';
                return Token.lpq;
            
            case ']':
                peek = ' ';
                return Token.rpq;
            
            case '{':
                peek = ' ';
                return Token.lpg;
            
            case '}':
                peek = ' ';
                return Token.rpg;
            
            case '+':
                peek = ' ';
                return Token.plus;
            
            case '-':
                peek = ' ';
                return Token.minus;
            
            case '*':
                peek = ' ';
                return Token.mult;
            
            case '/':
                peek = ' ';
                return Token.div;

            case ';':
                peek = ' ';
                return Token.semicolon;

            case ',':
                peek = ' ';
                return Token.comma;

            case '&':
                readch(br);
                if (peek == '&') {
                    peek = ' ';
                    return Word.and;
                } else {
                    System.err.println("Erroneous character"
                            + " after & : "  + peek );
                    return null;
                }

	// ... gestire i casi di || < > <= >= == <> ... //
            case '|':
                readch(br);
                if (peek == '|') {//    ||
                    peek = ' ';
                    return Word.or;
                } else {
                    System.err.println("Erroneous character"
                            + " after | : "  + peek );
                    return null;
                }
            
            case ':':
                readch(br);
                if (peek == '=') { //   := 
                    peek = ' ';
                    return Word.init;
                } else {
                    System.err.println("Erroneous character"
                            + " after : : "  + peek );
                    return null;
                }

            case '=':
                readch(br);
                if (peek == '=') { //   ==
                    peek = ' ';
                    return Word.eq;
                } else {
                    System.err.println("Erroneous character"
                            + " after = : "  + peek );
                    return null;
                }
            
            case '<':
                readch(br);
                if (peek == '=') { //   <=
                    peek = ' ';
                    return Word.le;
                } else if(peek == '>'){//   <>
                    peek = ' ';
                    return Word.ne;
                }
                else {//    <
                    return Word.lt;
                }

            case '>':
                readch(br);
                if (peek == '=') { //   >=
                    peek = ' ';
                    return Word.ge;
                } 
                else {//    >
                    return Word.gt;
                }
    
            case (char)-1:
                return new Token(Tag.EOF);

            default:
            if (Character.isLetter(peek)|| peek == '_') {
                    
                // ... gestire il caso degli identificatori e delle parole chiave //
                // identificatori == [_a-zA-Z][_a-zA-Z0-9]*

                StringBuilder identifier = new StringBuilder();
                while (Character.isLetterOrDigit(peek) || peek == '_') {
                    identifier.append(peek);
                    readch(br);
                }

                String identifier_RE = "[a-zA-Z_*a-zA-Z0-9][a-zA-Z0-9_]*";
                switch (identifier.toString()) {

                    case "to":      return Word.to;

                    case "if":      return Word.iftok;

                    case "do":      return Word.dotok;

                    case "for":   return Word.fortok;
                    
                    case "end":     return Word.end;

                    case "read":    return Word.read;      

                    case "else":    return Word.elsetok;

                    case "assign":  return Word.assign;

                    case "begin":   return Word.begin;

                    case "print":   return Word.print;


                    default:
                        if (identifier.toString().matches(identifier_RE)) {
                            return new Word(Tag.ID, identifier.toString());
                        }
                        System.err.println("Syntax error in: " + identifier);
                        return null;

                }

            }  else if (Character.isDigit(peek)) {

                // ... gestire il caso dei numeri ... //
                // creo una stringa sulla quale inserisco il carattere numerico letto,
                // quando non leggo un numero la costante numerica e finita, quindi faccio return
                // il carattere letto inizialmente e 0 allora faccio subito il return
                StringBuilder number = new StringBuilder();
            while (Character.isDigit(peek)) {
                number.append(peek);
                readch(br);
            }
            
            if(number.charAt(0) != '0')
                return new NumberTok(Tag.NUM, number.toString());
            else
                return new NumberTok(Tag.NUM, "0");

            } else {
            System.err.println("Erroneous character: " + peek);
            return null;
            }
         }
    }
		
    public static void main(String[] args) {
        Lexer2 lex = new Lexer2();
        String path = "C:\\Users\\39339\\Documents\\LFT_17_09\\test_Lexer2.lft"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Token tok;
            do {
                tok = lex.lexical_scan(br);
                System.out.println("Scan: " + tok);
            } while (tok.tag != Tag.EOF);
            br.close();
        } catch (IOException e) {e.printStackTrace();}    
    }

}
