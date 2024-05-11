import java.io.File;

public class Main {
    public static void main(String[] args) {
        // Parse each file given in the Command Line Arguments
        for (String fileName: args) {
            // retrieve the file from the device based on the file name given in the command line arguments
            File silFile = new File(fileName);
            // Create an instance of File Parser that supports SIL file parsing
            FileParser silFileParser = new FileParser();
            // Start SIL file parsing
            silFileParser.parse(silFile);
        }
    }
}