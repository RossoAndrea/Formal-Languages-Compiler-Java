public class Es1_3{
    public static boolean scan(String s){
        int state=0, i=0;

        while(state>=0 && i<s.length()){
            final char ch = s.charAt(i++);

            int a= ch -'0';

            switch(state){
            case 0:
            if (Character.isDigit(ch) && (a%2==0)) state = 1;
            else if (Character.isDigit(ch) && !(a%2==0)) state = 3;
            else state = -1;
            break;
            
            case 1:
            if (Character.isDigit(ch) && (a%2==0)) state = 1;
            else if (Character.isDigit(ch) && !(a%2==0)) state = 3;
            else if (Character.isUpperCase(ch) && (a>=17 && a<=27)) state = 2;
            else state = -1;
            break;
            
            case 2:
            if (Character.isDigit(ch) || Character.isUpperCase(ch)) state = -1;
            else if (Character.isLowerCase(ch)) state = 2;
            else state = -1;

            break;
            
            case 3:
            if (Character.isDigit(ch) && (a%2==0)) state = 1;
            else if (Character.isDigit(ch) && !(a%2==0)) state = 3;
            else if (Character.isUpperCase(ch) && (a>=28 && a<=42)) state = 4;
            else state = -1;

            break;
            
            case 4:
            if (Character.isDigit(ch) || Character.isUpperCase(ch)) state = -1;
            else if (Character.isLowerCase(ch)) state = 4;
            else state = -1;

            break;
            }
        }
    return (state == 2 || state == 4);
    }
    public static void main(String[] args) {
        System.out.println(scan(args[0]) ? "OK" : "NOPE");
    }
}