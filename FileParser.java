import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class FileParser {

    private final Scanner consoleInputReader;
    private SILFile silFile;

    // Used to retrieve line number for execution after control is returned from a sub routine
    private final Stack<Integer> subRoutineStack = new Stack<>();
    // Simple integer stack to perform PUSH & POP integer operations to be supported by SIL Parser
    private final Stack<Integer> operationalStack = new Stack<>();

    private final List<String> rawFileLines = new ArrayList<>();
    private final List<Integer> lineNumbers = new ArrayList<>();
    private final HashMap<String, Variable> variables = new HashMap<>();

    FileParser() {
        consoleInputReader = new Scanner(System.in);
    }

    /**
     * Main parse function
     * @param file the SIL file that needs to be parsed
     */
    public void parse(File file) {
        try  {
            silFile = SILFile.getInstance();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            fetchRawFileLines(bufferedReader);
            silFile.initializeSILFileState(rawFileLines);
            lineNumbers.addAll(silFile.getCodeLines().keySet());
            executeInstructions(silFile.getFirstLineNumber(), silFile.getLastLineNumber());
        } catch (IOException e) {
            System.out.println("Error while reading the file #file-error");
        }
    }

    /**
     * The function splits the whole file into lines and stores those lines as list of strings
     * @param bufferedReader is the Buffered reader that has the file stored in it as String Buffer
     */
    private void fetchRawFileLines(BufferedReader bufferedReader) {
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                rawFileLines.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error while reading the lines.");
        }
    }

    /**
     * Execute few lines of code of a specific code snippet
     * @param startLineNumber is the line number of first instruction to be executed
     * @param endLineNumber is the line number of last instruction to be executed
     */
    private void executeInstructions(Integer startLineNumber, Integer endLineNumber) {
        silFile.getCodeLines().keySet().forEach(lineNumber -> {
            if (lineNumber >= startLineNumber && lineNumber <= endLineNumber)
                examineCodeLine(lineNumber);
        });
    }

    /**
     * The function examines each line of code to parse
     */
    private void examineCodeLine(Integer lineNumber) {
        String code = silFile.getCodeLines().get(lineNumber);
        String instruction = StringUtils.getFirstWordOfSentence(code);
        switch (instruction) {
            case "INTEGER" -> examineDeclarationInstruction(lineNumber, code); // DONE
            case "INPUT" -> examineInputInstruction(lineNumber, code); // DONE
            case "LET" -> examineInitializationInstruction(lineNumber, code); // DONE
            case "PUSH" -> examinePushInstruction(lineNumber, code); // DONE
            case "POP" -> examinePopInstruction(lineNumber, code); // DONE
            case "IF" -> examineConditionalInstruction(lineNumber, code); // DONE
            case "GOTO" -> examineGotoInstruction(lineNumber, code); // DONE
            case "GOSUB" -> examineGoSubInstruction(lineNumber, code); //
            case "PRINT", "PRINTLN" -> examinePrintInstruction(lineNumber, code); // ALMOST DONE
            case "RET" -> examineReturnInstruction(lineNumber, code); //
            case "END" -> System.exit(0); // DONE
            default -> System.out.println("Syntax error occurred while parsing");
        }
    }

    /**
     * This function examines declaration instructions and creates variables for the encountered variable declarations
     * @param lineNumber line number of declaration instruction
     * @param codeLine instruction code line
     */
    private void examineDeclarationInstruction(Integer lineNumber, String codeLine) {
        String instruction = StringUtils.deleteFirstWordFromSentence(codeLine);
        String[] instructionVariables = instruction.split(",");
        for (String variable: instructionVariables) {
            if (StringUtils.isValidVariableName(variable))
                variables.put(variable, new Variable(variable));
            else {
                System.out.println("Not a valid variable name at "+lineNumber);
                System.exit(0);
            }
        }
    }

    /**
     * This function helps to retrieve console input from the user.
     * @param lineNumber line number of input instruction
     * @param codeLine instruction code line
     */
    private void examineInputInstruction(Integer lineNumber, String codeLine) {
        String instruction = StringUtils.deleteFirstWordFromSentence(codeLine);
        String[] instructionVariables = instruction.split(",");
        for (String variable: instructionVariables) {
            if (!variables.containsKey(variable)) {
                System.out.println(variable+" variable not declared at "+lineNumber);
                System.exit(0);
            }
        }
        // the input variables were successfully declared by this time
        String consoleInput = consoleInputReader.nextLine();
        if (!Objects.equals(consoleInput, "")) {
            String[] inputs = consoleInput.split(" ");
            if (inputs.length == instructionVariables.length) {
                for (int i = 0; i < instructionVariables.length; i++) {
                    try {
                        Variable requiredVariable = variables.get(instructionVariables[i]);
                        requiredVariable.value = Integer.parseInt(inputs[i]);
                        requiredVariable.state = VariableState.INITIALIZED;
                    } catch (NumberFormatException e) {
                        System.out.println(e.getMessage());
                        System.exit(0);
                    }
                }
            } else {
                System.out.println("Line "+lineNumber+" missing input value");
                System.exit(0);
            }
        } else {
            System.out.println("Line "+lineNumber+" missing input value");
            System.exit(0);
        }
    }

    /**
     * This part of code handles the lines of code that deal with initialization i.e: which start with LET
     * @param lineNumber is the line number of the initialization instruction
     * @param code is a particular line of code
     */
    private void examineInitializationInstruction(Integer lineNumber, String code) {
        String instruction = StringUtils.deleteFirstWordFromSentence(code);
        String[] operands = instruction.split("=");
        String variableBeingAssigned = operands[0];
        if (!variables.containsKey(variableBeingAssigned)) {
            System.out.println(variableBeingAssigned+" variable not declared at "+lineNumber);
            System.exit(0);
        } else if (operands.length > 2) {
            System.out.println("Invalid initialization at "+lineNumber);
            System.exit(0);
        }
        String expressionString = operands[1];
        boolean hasOnlyDigits = expressionString.matches("[0-9]+");
        if (hasOnlyDigits)
            variables.get(variableBeingAssigned).value = Integer.parseInt(expressionString);
        else
            variables.get(variableBeingAssigned).value = evaluate(buildMathematicalExpressionForEvaluation(expressionString));
        variables.get(variableBeingAssigned).state = VariableState.INITIALIZED;
    }

    /**
     * Push the expression result on to the top of the operational stack
     * @param lineNumber is the line number of the push instruction
     * @param code is a particular line of code
     */
    private void examinePushInstruction(Integer lineNumber, String code) {
        String instruction = StringUtils.deleteFirstWordFromSentence(code);
        int instructionResult = evaluate(buildMathematicalExpressionForEvaluation(instruction));
        operationalStack.push(instructionResult);
    }

    /**
     * Pop the top of the stack and store it into the variable
     * @param lineNumber is the line number of the push instruction
     * @param code is a particular line of code
     */
    private void examinePopInstruction(Integer lineNumber, String code) {
        String variableName = StringUtils.deleteFirstWordFromSentence(code);
        if (variables.containsKey(variableName)) {
            try {
                variables.get(variableName).value = operationalStack.pop();
                variables.get(variableName).state = VariableState.INITIALIZED;
            } catch (EmptyStackException e) {
                System.out.println(e.getMessage());
                System.exit(0);
            }
        } else {
            System.out.println(variableName+" not declared at line "+lineNumber);
            System.exit(0);
        }
    }

    private void examineConditionalInstruction(Integer lineNumber, String code) {
        String codeWithoutIf = StringUtils.deleteFirstWordFromSentence(code).trim();
        String[] conditionalClauses = codeWithoutIf.split("THEN");
        String ifClause = conditionalClauses[0];
        String thenClause = conditionalClauses[1];
        String relationalOperator = ifClause.contains("<") ? "<" : (ifClause.contains(">") ? ">"
                : (ifClause.contains("=") ? "=" : (ifClause.contains("!") ? "!" : "" )));
        if (relationalOperator.equals("")) {
            System.out.println("If clause doesn't have a valid relational operator at "+lineNumber);
            System.exit(0);
        }
        String[] relationalOperands = ifClause.split(relationalOperator);
        if (relationalOperands.length > 2) {
            System.out.println("Invalid conditional statement at "+lineNumber);
            System.exit(0);
        }
        if (computeExpressionResult(
                relationalOperator,
                evaluate(buildMathematicalExpressionForEvaluation(relationalOperands[0])),
                evaluate(buildMathematicalExpressionForEvaluation(relationalOperands[1]))
        )) {
            thenClause = thenClause.trim();
            String decisionWord = StringUtils.getFirstWordOfSentence(thenClause);
            switch (decisionWord) {
                case "PRINT", "PRINTLN" -> examinePrintInstruction(lineNumber, thenClause);
                case "GOTO" -> examineGotoInstruction(lineNumber, thenClause);
            }
        }
    }

    private void examineGotoInstruction(Integer lineNumberOfGotoInstruction, String code) {
        String lineNumber = StringUtils.deleteFirstWordFromSentence(code);
        Integer gotoLineNumber = Integer.parseInt(lineNumber);
        executeInstructions(gotoLineNumber, silFile.getLastLineNumber());
    }

    private void examineGoSubInstruction(Integer lineNumber, String code) {
        Integer goSubLineNumber = Integer.parseInt(StringUtils.deleteFirstWordFromSentence(code));
        int indexOfGoSubLine = lineNumbers.indexOf(lineNumber);
        Integer nextLineToBeExecuted = lineNumbers.get(indexOfGoSubLine + 1);
        subRoutineStack.push(nextLineToBeExecuted);
        executeInstructions(goSubLineNumber, silFile.getLastLineNumber());
    }

    private void examineReturnInstruction(Integer lineNumber, String code) {
        Integer lineNumberToBeResumed = subRoutineStack.pop();
        executeInstructions(lineNumberToBeResumed, silFile.getLastLineNumber());
    }

    /**
     * The function examines the print statements and parses them
     * @param lineNumber is the instruction line to be printed
     * @param code is the line to print
     */
    private void examinePrintInstruction(Integer lineNumber, String code) {
        String decisionWord = StringUtils.getFirstWordOfSentence(code);
        String instruction = StringUtils.deleteFirstWordFromSentence(code);
        if (variables.containsKey(instruction)) {
            Integer value = variables.get(instruction).value;
            if (Objects.equals(decisionWord, "PRINT"))
                System.out.print(value);
            else
                System.out.println(value);
        } else {
            if (!instruction.startsWith("\"")) {
                int expressionResult = evaluate(buildMathematicalExpressionForEvaluation(instruction));
                if (Objects.equals(decisionWord, "PRINT"))
                    System.out.print(expressionResult);
                else
                    System.out.println(expressionResult);
            } else {
                String printableResult = instruction.replaceAll("\"", "");
                if (Objects.equals(decisionWord, "PRINT"))
                    System.out.print(printableResult);
                else
                    System.out.println(printableResult);
            }
        }
    }

    /**
     * This function builds a numerical expression from a variable expression by retrieving the values of
     * variables from the hashMap which we stored during initialization
     * @param expression is the expression that has variables instead of numbers
     * @return the numerical expression
     */
    private String buildMathematicalExpressionForEvaluation(String expression) {
        StringBuilder mathExpressionBuilder = new StringBuilder();
        StringBuilder variableNameBuilder = new StringBuilder();
        for (int i = 0; i < expression.length(); i++) {
            if (expression.charAt(i) == '+' || expression.charAt(i) == '-'
                    || expression.charAt(i) == '*' || expression.charAt(i) == '/'
                    || (expression.charAt(i) >= '0' && expression.charAt(i) <= '9')) {
                retrieveVariableValueAndAppendToMathExpression(mathExpressionBuilder, variableNameBuilder);
                mathExpressionBuilder.append(expression.charAt(i));
            } else {
                variableNameBuilder.append(expression.charAt(i));
            }
        }
        retrieveVariableValueAndAppendToMathExpression(mathExpressionBuilder, variableNameBuilder);
        return mathExpressionBuilder.toString();
    }

    private void retrieveVariableValueAndAppendToMathExpression(StringBuilder mathExpressionBuilder, StringBuilder variableNameBuilder) {
        String variableName = variableNameBuilder.toString();
        if (!(variableName.isBlank() || variableName.isEmpty())) {
            if (variables.containsKey(variableName)) {
                mathExpressionBuilder.append(variables.get(variableName).value.toString());
                variableNameBuilder.delete(0, variableNameBuilder.length());
            } else {
                System.out.println(variableName+" variable not declared");
                System.exit(0);
            }
        }
    }

    private Boolean computeExpressionResult(String relationalOperator, Integer leftOperand, Integer rightOperand) {
        return switch (relationalOperator) {
            case "<" -> leftOperand < rightOperand;
            case ">" -> leftOperand > rightOperand;
            case "=" -> Objects.equals(leftOperand, rightOperand);
            default -> !Objects.equals(leftOperand, rightOperand);
        };
    }

    /**
     * This code for expression evaluation has been taken from Geeks for geeks website.
     * The function is intended to evaluate an expression containing numbers and mathematical operators
     * Link to the article: <a href="https://www.geeksforgeeks.org/expression-evaluation/">...</a>
     * @param expression : It is the expression that needs to be evaluated by using a stack
     * @return : The function returns an integer i.e: the result of the evaluated expression
     */
    public int evaluate(String expression)
    {
        char[] tokens = expression.toCharArray();
        Stack<Integer> values = new Stack<>();
        Stack<Character> ops = new Stack<>();
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i] == ' ')
                continue;
            if (tokens[i] >= '0' && tokens[i] <= '9') {
                StringBuffer sBuffer = new StringBuffer();
                while (i < tokens.length && tokens[i] >= '0' && tokens[i] <= '9')
                    sBuffer.append(tokens[i++]);
                values.push(Integer.parseInt(sBuffer.toString()));
                i--;
            }
            else if (tokens[i] == '(')
                ops.push(tokens[i]);
            else if (tokens[i] == ')') {
                while (ops.peek() != '(')
                    values.push(applyOp(ops.pop(),
                            values.pop(),
                            values.pop()));
                ops.pop();
            } else if (tokens[i] == '+' || tokens[i] == '-' || tokens[i] == '*' || tokens[i] == '/') {
                while (!ops.empty() && hasPrecedence(tokens[i], ops.peek()))
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                ops.push(tokens[i]);
            }
        }
        while (!ops.empty())
            values.push(applyOp(ops.pop(), values.pop(), values.pop()));
        return values.pop();
    }
    public boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')')
            return false;
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-'))
            return false;
        else
            return true;
    }

    public int applyOp(char op, int b, int a) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0)
                    throw new UnsupportedOperationException("Cannot divide by zero");
                return a / b;
        }
        return 0;
    }
}

class Variable {
    String name;
    Integer value;
    VariableState state;

    Variable(String name) {
        this.name = name;
        value = Integer.MIN_VALUE;
        state = VariableState.DECLARED;
    }
}

enum VariableState {
    DECLARED,
    INITIALIZED
}
