import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SILFileBeautifier {

    private static final List<String> rejectionArray = new ArrayList<>();
    private static final String end  = "END";
    private static final String ret = "RET";
    private static final String print = "PRINT";
    private static final String println = "PRINTLN";
    private static final String iF = "IF";

    private static void initializeRejectionArray() {
        rejectionArray.add("PRINT");
        rejectionArray.add("PRINTLN");
        rejectionArray.add("IF");
    }

    public static void beautify(List<String> rawFileLines,
                                LinkedHashMap<Integer, String> beautifiedFileLines) throws NumberFormatException {
        initializeRejectionArray();
        for (String rawFileLine: rawFileLines) {
            Integer lineNumber = fetchLineNumber(rawFileLine);
            String beautifiedLine = beautifyCodeLine(StringUtils.deleteFirstWordFromSentence(rawFileLine));
            beautifiedFileLines.put(lineNumber, beautifiedLine);
        }
    }

    public static Integer fetchLineNumber(String rawFileLine) throws NumberFormatException {
        String lineNumberString = StringUtils.getFirstWordOfSentence(rawFileLine);
        return Integer.parseInt(lineNumberString);
    }

    public static String beautifyCodeLine(String codeWithoutLineNumber) {
        String instruction = StringUtils.getFirstWordOfSentence(codeWithoutLineNumber).toUpperCase();
        if (instruction.equals(end) || instruction.equals(ret)) {
            return instruction.toUpperCase();
        }
        String codeWithoutInstruction = StringUtils.deleteFirstWordFromSentence(codeWithoutLineNumber);
        StringBuilder enhancedCodeBuilder = new StringBuilder();
        enhancedCodeBuilder.append(instruction);
        enhancedCodeBuilder.append(" ");
        char[] codeCharacters = codeWithoutInstruction.toCharArray();
        if (!rejectionArray.contains(instruction)) {
            for (char codeCharacter: codeCharacters) {
                if (codeCharacter != ' ') {
                    enhancedCodeBuilder.append(codeCharacter);
                }
            }
        } else {
            if (instruction.equals(iF)) {
                String[] conditionalClauses = codeWithoutInstruction.split("THEN");
                for (char codeCharacter: conditionalClauses[0].toCharArray()) {
                    if (codeCharacter != ' ') {
                        enhancedCodeBuilder.append(codeCharacter);
                    }
                }
                enhancedCodeBuilder.append("THEN");
                enhancedCodeBuilder.append(StringUtils.getFirstWordOfSentence(conditionalClauses[1].trim()).toUpperCase());
                enhancedCodeBuilder.append(' ');
                enhancedCodeBuilder.append(StringUtils.deleteFirstWordFromSentence(conditionalClauses[1].trim()));
            } else if (instruction.equals(print) || instruction.equals(println)) {
                boolean isPrintable = false;
                for (char codeCharacter: codeCharacters) {
                    if (codeCharacter == '\"')
                        isPrintable = !isPrintable;
                    if (isPrintable) {
                        enhancedCodeBuilder.append(codeCharacter);
                    } else {
                        if (codeCharacter != ' ')
                            enhancedCodeBuilder.append(codeCharacter);
                    }
                }
            }
        }
        return enhancedCodeBuilder.toString();
    }
}
