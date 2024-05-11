import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * This class is a singleton which is used to store the content and properties of input instructions file.
 * Class stores code lines, last line number, current line under execution
 */
public class SILFile {

    private static SILFile sInstance;
    private final LinkedHashMap<Integer, String> codeLines = new LinkedHashMap<>();
    private Integer currentLineUnderExecution;
    private Integer firstLineNumber;
    private Integer lastLineNumber;

    // constructor made private because this is going to be a Singleton
    private SILFile() { }

    // returns the InputFile Singleton
    public static SILFile getInstance() {
        if (sInstance == null) {
            sInstance = new SILFile();
        }
        return sInstance;
    }

    public void initializeSILFileState(List<String> rawFileLines) {
        // beautify the code
        SILFileBeautifier.beautify(rawFileLines, codeLines);
        // Retrieve the line numbers of the file
        Object[] lineNumbers = codeLines.keySet().toArray();
        setFirstLineNumber((Integer) lineNumbers[0]);
        setLastLineNumber((Integer) lineNumbers[lineNumbers.length - 1]);
    }

    public LinkedHashMap<Integer, String> getCodeLines() {
        return codeLines;
    }

    public void setCurrentLineUnderExecution(Integer lineNumber) {
        currentLineUnderExecution = lineNumber;
    }

    public Integer getCurrentLineUnderExecution() {
        return currentLineUnderExecution;
    }

    private void setFirstLineNumber(Integer firstLineNumber) {
        this.firstLineNumber = firstLineNumber;
    }

    public Integer getFirstLineNumber() {
        return firstLineNumber;
    }

    private void setLastLineNumber(Integer lineNumber) {
        lastLineNumber = lineNumber;
    }

    public Integer getLastLineNumber() {
        return lastLineNumber;
    }
}