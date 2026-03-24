public class EmailValidator {
    public static boolean isValid(String email) {
        int dotNumber=0;
        int atNumber=0;
        for (int i = 0; i < email.length(); i++) {
            //Check for spaces
            if(email.charAt(i)==32){
                return false;
            }
            //Check for more than one @
            if(email.charAt(i)=='@'){
                dotNumber++;
                if(dotNumber>1){
                    return false;
                }
            }
            //Check for more than one dot
            if(email.charAt(i)=='.'){
                atNumber++;
                if(atNumber>1){
                    return false;
                }
            }
            //Check for any other forbidden characters
            if((email.charAt(i)<48&&email.charAt(i)!=46) || (email.charAt(i)>57&&email.charAt(i)<64) || (email.charAt(i)>90&&email.charAt(i)<97) || email.charAt(i)>122){
                return false;
            }
        }
        //Check if there is a dot and @
        if(dotNumber!=1||atNumber!=1){
            return false;
        }
        //Split the mail
        String[] splitMail=email.split("[@.]");
        //Check for position of @
        if (email.charAt(splitMail[0].length()) !='@') {
            return false;
        }
        //Check for position of dot
        if (email.charAt(splitMail[1].length()+splitMail[0].length()+1) !='.') {
            return false;
        }
        return true;
    }
}
