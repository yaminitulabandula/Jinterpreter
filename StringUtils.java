public class StringUtils {

    public static String getFirstWordOfSentence(String sentence) {
        return sentence.split(" ")[0];
    }

    public static Boolean isValidVariableName(String variableName) {
        char firstLetter = variableName.charAt(0);
        Boolean isVariableFirstLetterAppropriate = ((firstLetter >= 'a' && firstLetter <= 'z') ||
                (firstLetter >= 'A' && firstLetter <= 'Z') || firstLetter == '_' || firstLetter == '$');
        Boolean areVariableLettersValid = variableName.matches("[a-zA-Z0-9_$]+");
        return isVariableFirstLetterAppropriate && areVariableLettersValid;
    }

    public static String deleteFirstWordFromSentence(String actualString) {
        int wordStartPosition = getFirstWordOfSentence(actualString).length();
        while (actualString.charAt(wordStartPosition) == ' ') { // checks for multiple spaces after first word
            wordStartPosition += 1;
        }
        return actualString.substring(wordStartPosition);
    }
}
