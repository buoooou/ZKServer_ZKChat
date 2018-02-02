package kafeihu.zk.server.test;

public class IntTest {

    public static int reverse(int x) {


        String original= ""+x;

        StringBuilder temp= new StringBuilder();
        for (int i=original.length()-1; i>0; i--){

            temp.append(original.charAt(i));
        }

        if(original.charAt(0)=='-'){
            temp.insert(0,'-');
        }else
        {
            temp.insert(original.length()-1,original.charAt(0));
        }
        int result;
        try{

            result=Integer.parseInt(temp.toString());
        }catch(Exception e){
            return 0;
        }
        return result;
    }

    public static int myAtoi(String str) {

        StringBuilder temp= new StringBuilder();
        for(int i=0; i<str.length(); i++){
            if(str.charAt(i)==' '){
                continue;
            }
            temp.append(str.charAt(i));
        }

        int result=0;
        try{

            result=Integer.parseInt(temp.toString());

        }catch (Exception e){
            return 0;
        }

        return result;
    }
    public static void main(String[] args) {

        String i="546";

        System.out.println(myAtoi(i));
    }
}
