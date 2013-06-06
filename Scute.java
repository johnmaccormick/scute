import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Scute {

	public static final String[] letters = { "a", "b", "c", "d", "e", "f", "g",
			"h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
			"u", "v", "w", "x", "y", "z" };

	public static final String[] digits = { "0", "1", "2", "3", "4", "5", "6",
			"7", "8", "9" };

	public static final String[] binary = { "0", "1" };

	public static final String[] twoLetters = { "a", "b" };
	public static final String[] threeLetters = { "a", "b", "c" };
	public static final String[] fourLetters = { "a", "b", "c", "d", "z" };

	public static final String[] geneLetters = { "c", "a", "g", "t", "z" };

	public static final String BLANK = "blank";
	public static final String END_OF_FILE = "endOfFile";

	protected static final Pattern nonAlphaNumeric = Pattern
			.compile("[^a-zA-Z0-9]");

	protected static final Pattern nonWhiteSpace = Pattern.compile("[^\\s]");

	protected String register;
	protected String[] alphabet;

	protected ArrayList<String> memory;
	protected Scanner inputFile;
	protected/* PrintStream,OutputStream */Writer outputFile;
	protected String[] program;
	protected Instruction[] instructions;
	protected int memoryPointer;
	protected int programPointer;
	protected HashMap<String, Integer> labels;

	public Scute(String[] program, Scanner inputFile, Writer outputFile)
			throws ScuteException, InstructionException, IOException {
		this(program, inputFile, outputFile, null);
	}

	public Scute(String[] program, Scanner inputFile,
	/* PrintStream,OutputStream */Writer outputFile, String[] alphabet)
			throws ScuteException, InstructionException, IOException {
		register = null;
		this.alphabet = addBlank(alphabet);
		memory = new ArrayList<String>();
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.program = program;
		memoryPointer = 0;
		programPointer = 0;
		compile();
		execute();
	}

	protected static String[] addBlank(String[] alphabet) {
		if (alphabet == null) {
			return null;
		}
		ArrayList<String> alphabetList = new ArrayList<String>(
				Arrays.asList(alphabet));
		if (alphabetList.contains(BLANK)) {
			return alphabet;
		} else {
			alphabetList.add(BLANK);
			return alphabetList.toArray(new String[] {});
		}
	}

	protected String getInstructionLabel(String instructionString) {
		String[] components = instructionString.split(":");
		if (components.length > 1) {
			String label = components[0];
			// change of policy: don't allow colons in symbols
			// // at this stage, it may not actually be a label,
			// // so check to see if it is alphanumeric.
			// if (!nonAlphaNumeric.matcher(label).find()) {
			return label;
			// }
		}
		return null;
	}

	protected String stripLabel(String instructionString) {
		int colonLocation = instructionString.indexOf(':');
		assert (colonLocation > 0);
		return instructionString.substring(colonLocation + 1);
	}

	protected void compile() throws ScuteException, InstructionException {
		instructions = new Instruction[program.length];
		makeLabels();
		for (int i = 0; i < program.length; i++) {
			String instructionString = program[i];
			String label = getInstructionLabel(instructionString);
			if (label != null) {
				instructionString = stripLabel(instructionString);
			}
			Instruction instruction = compileInstruction(instructionString,
					label);
			if (instruction.getLabel() != null) {
				if (!labels.containsKey(instruction.getLabel())) {
					throw new ScuteException("unknown label '"
							+ instruction.getLabel()
							+ "' found in instruction " + i + ", '"
							+ instructionString + "'");

				}
			}
			instructions[i] = instruction;
		}
	}

	protected Instruction compileInstruction(String instructionString,
			String label) throws InstructionException {
		Instruction instruction = new Instruction(instructionString, label);
		return instruction;
	}

	protected void makeLabels() throws ScuteException {
		labels = new HashMap<String, Integer>();
		for (int i = 0; i < program.length; i++) {
			String instruction = program[i];
			String label = getInstructionLabel(instruction);
			if (label != null) {
				if (labels.containsKey(label)) {
					throw new ScuteException("duplicate label '" + label
							+ "' found in instruction '" + instruction + "'");
				}
				labels.put(label, i);
			}
		}
	}

	protected void execute() throws ScuteException, IOException {
		boolean done = false;
		while (!done) {
			Instruction instruction = instructions[programPointer];
//			 System.out.println("Executing " + instruction);
//			 System.out.println("Before: " + this);

			twoBools result = executeInstruction(instruction);

			if (result.unknownOpcode) {
				throw new ScuteException("Unexpected opcode in instruction '"
						+ instruction + "'");
			}

			if (!result.jumped) {
				programPointer++;
			}
			if (instruction.getOpcode() == Instruction.HALT) {
				done = true;
			}
//			 System.out.println("After: " + this);
		}
	}

	protected class twoBools {
		public boolean jumped;
		public boolean unknownOpcode;
	}

	protected twoBools executeInstruction(Instruction instruction)
			throws ScuteException, IOException {
		twoBools retVal = new twoBools();
		retVal.jumped = false;
		retVal.unknownOpcode = false;
		int opcode = instruction.getOpcode();
		switch (opcode) {
		case Instruction.INPUT:
			executeInput();
			break;
		case Instruction.OUTPUT:
			executeOutput(instruction);
			break;
		case Instruction.WRITE:
			executeWrite(instruction);
			break;
		case Instruction.MOVE_LEFT:
			if (memoryPointer > 0) {
				memoryPointer--;
			}
			break;
		case Instruction.MOVE_RIGHT:
			memoryPointer++;
			break;
		case Instruction.READ:
			executeRead();
			break;
		case Instruction.LOAD:
			register = instruction.symbol;
			break;
		case Instruction.JUMP:
			doJump(instruction);
			retVal.jumped = true;
			break;
		case Instruction.IFJUMP:
			if (register.equals(instruction.getSymbol())) {
				doJump(instruction);
				retVal.jumped = true;
			}
			break;
		case Instruction.HALT:
			break;
		case Instruction.NOOP:
			break;
		default:
			retVal.unknownOpcode = true;
		}
		return retVal;
	}

	protected void executeOutput(Instruction instruction) throws IOException,
			ScuteException {
		String source = getSource(instruction);
		outputFile.write(" " + source);
	}

	protected void executeInput() {
		if (inputFile.hasNext()) {
			register = inputFile.next();
		} else {
			register = END_OF_FILE;
		}
	}

	protected void doJump(Instruction instruction) {
		programPointer = labels.get(instruction.getTarget());
	}

	protected void executeWrite(Instruction instruction) {
		extendMemory();
		String source = getSource(instruction);
		memory.set(memoryPointer, source);
	}

	protected String getSource(Instruction instruction) {
		String source = null;
		if (instruction.getSymbol() != null) {
			source = instruction.getSymbol();
		} else {
			source = register;
		}
		return source;
	}

	protected void executeRead() {
		extendMemory();
		register = memory.get(memoryPointer);
	}

	protected void extendMemory() {
		if (memoryPointer >= memory.size()) {
			int extraCells = memoryPointer - memory.size() + 1;
			for (int i = 0; i < extraCells; i++) {
				memory.add(BLANK);
			}
		}
	}

	// for debugging
	public void printLabels() {
		for (String label : labels.keySet()) {
			System.out.println(label + ": " + labels.get(label));
		}
	}

	public void printProgram() {
		for (Instruction instruction : instructions) {
			System.out.println(instruction);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(register + "|" + programPointer + "||");
		for (int i = 0; i < memory.size(); i++) {
			if (i == memoryPointer)
				builder.append("*");
			builder.append(memory.get(i));
			if (i == memoryPointer)
				builder.append("*");
			builder.append("|");
		}
		return builder.toString();
	}

	/**
	 * @param args
	 * @throws InstructionException
	 * @throws ScuteException
	 * @throws IOException
	 */
	public static void main(String[] args) throws ScuteException,
			InstructionException, IOException {
		String[] program = { "I: input", "if 'a' jump A", "output", "jump Z",
				"A: output", "jump I", "Z: write 'g'", "move right",
				"write 't'", "move left", "read", "move right", "move right",
				"write", "read", "output", "output 'c'", "halt", };

		String inputString = "aaaaaacg";

		StringWriter outputFile = new StringWriter();
		Scanner inputFile = new Scanner(inputString);
		inputFile.useDelimiter("");
		// outputFile.println("test");

		Scute scute = new Scute(program, inputFile, outputFile,
				Scute.geneLetters);
		System.out.println(outputFile);
		System.out.println("********************");
		// scute.printLabels();
		// scute.printProgram();
	}

}
