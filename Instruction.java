import java.util.Scanner;
import java.util.regex.Pattern;

public class Instruction {
	public static final int UNKNOWN = -1;
	public static final int INPUT = 0;
	public static final int OUTPUT = 1;
	public static final int WRITE = 2;
	public static final int READ = 3;
	public static final int MOVE_LEFT = 4;
	public static final int MOVE_RIGHT = 5;
	public static final int JUMP = 6;
	public static final int IFJUMP = 7;
	public static final int LOAD = 8;
	public static final int HALT = 9;
	public static final int NOOP = 10;

	public static final String OUTPUT_STR = "output";
	public static final String WRITE_STR = "write";
	public static final String JUMP_STR = "jump";
	public static final String IF_STR = "if";
	public static final String LOAD_STR = "load";
	public static final String COMMENT_PREFIX = "#";
	public static final String UNEXPECTED_OPCODE = "unexpected opcode";

	public static final Pattern whitespace = Pattern.compile("\\s+");

	public int opcode;
	protected String symbol = null;
	protected String target = null;
	protected String label = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder retVal = new StringBuilder();
		if (label != null) {
			retVal.append(label + ": ");
		}
		switch (opcode) {
		case INPUT:
			retVal.append("input");
			break;
		case OUTPUT:
			retVal.append("output");
			appendSymbol(retVal);
			break;
		case WRITE:
			retVal.append("write");
			appendSymbol(retVal);
			break;
		case MOVE_LEFT:
			retVal.append("move left");
			break;
		case MOVE_RIGHT:
			retVal.append("move right");
			break;
		case READ:
			retVal.append("read");
			break;
		case JUMP:
			retVal.append("jump " + target);
			break;
		case IFJUMP:
			retVal.append("if '" + symbol + "' jump " + target);
			break;
		case LOAD:
			retVal.append("load '" + symbol + "'");
			break;
		case HALT:
			retVal.append("halt");
			break;
		case NOOP:
			retVal.append("noop");
			break;
		default:
			retVal.append(UNEXPECTED_OPCODE + " " + opcode);
		}
		return retVal.toString();
	}

	protected void appendSymbol(StringBuilder retVal) {
		if (symbol != null) {
			retVal.append(" '" + symbol + "'");
		}
	}

	protected void compileWrite(Scanner scanner, String instructionString)
			throws InstructionException {
		opcode = WRITE;
		symbol = parseSymbol(instructionString, WRITE_STR, scanner);
	}

	protected void compileLoad(Scanner scanner, String instructionString)
			throws InstructionException {
		opcode = LOAD;
		symbol = parseSymbol(instructionString, LOAD_STR, scanner);
	}

	protected void compileOutput(Scanner scanner, String instructionString)
			throws InstructionException {
		opcode = OUTPUT;
		symbol = parseSymbol(instructionString, OUTPUT_STR, scanner);
	}

	protected String parseSymbol(String instructionString, String opcodeStr,
			Scanner scanner) throws InstructionException {
		int writePos = instructionString.indexOf(opcodeStr);
		int i = writePos + opcodeStr.length();
		if (i >= instructionString.length()) {
			return null;
		}
		while (Character.isWhitespace(instructionString.charAt(i))) {
			i++;
			if (i >= instructionString.length()) {
				return null;
			}
		}
		if (instructionString.substring(i).startsWith(COMMENT_PREFIX)) {
			return null;
		}

		char symbolDelimiter = 'x'; // dummy value
		if (instructionString.charAt(i) == '\'') {
			symbolDelimiter = '\'';
		} else if (instructionString.charAt(i) == '"') {
			symbolDelimiter = '"';
		} else {
			throw new InstructionException(
					"write symbol in instruction string '" + instructionString
							+ "' must be delimited by single or double quotes");
		}
		int symbolStart = i + 1;
		int symbolEnd = instructionString.indexOf(symbolDelimiter, symbolStart);
		if (symbolEnd == -1) {
			throw new InstructionException(
					"write symbol in instruction string '" + instructionString
							+ "' started with delimiter " + symbolDelimiter
							+ ", but no closing delimiter " + symbolDelimiter
							+ "was found");

		}
		String theSymbol = instructionString.substring(symbolStart, symbolEnd);
		String symbolDelimiterStr = Character.toString(symbolDelimiter);
		scanner.findInLine(symbolDelimiterStr);
		scanner.findInLine(symbolDelimiterStr);
		return theSymbol;
	}

	protected void compileIf(Scanner scanner, String instructionString)
			throws InstructionException {
		opcode = IFJUMP;
		symbol = parseSymbol(instructionString, IF_STR, scanner);
		checkIf(scanner, instructionString);
		parseLabel(scanner, instructionString);
	}

	protected void checkIf(Scanner scanner, String instructionString)
			throws InstructionException {
		if (symbol == null) {
			throw new InstructionException(
					"no symbol found after the 'if' in instruction string '"
							+ instructionString + "'");
		}

		if (!scanner.hasNext() || !scanner.next().equals("jump")) {
			throw new InstructionException("expected 'jump' after symbol '"
					+ symbol + "' in instruction string '" + instructionString
					+ "'");
		}
	}

	protected void compileJump(Scanner scanner, String instructionString)
			throws InstructionException {
		opcode = JUMP;
		parseLabel(scanner, instructionString);
	}

	protected void parseLabel(Scanner scanner, String instructionString)
			throws InstructionException {
		if (scanner.hasNext()) {
			target = scanner.next();
		} else {
			target = null;
		}
		if (target == null || target.equals("")) {
			throw new InstructionException(
					"no jump label found in instruction string '"
							+ instructionString + "'");
		}
	}

	protected void compileMove(Scanner scanner, String instructionString)
			throws InstructionException {
		String direction = scanner.next();
		if (direction.equals("left")) {
			opcode = MOVE_LEFT;
		} else if (direction.equals("right")) {
			opcode = MOVE_RIGHT;
		} else {
			throw new InstructionException("unexpected direction string '"
					+ direction + "' found in instruction string '"
					+ instructionString + "'");
		}
	}

	public Instruction(int opcode, String symbol, String target, String label) {
		this.opcode = opcode;
		this.symbol = symbol;
		this.target = target;
		this.label = label;
	}

	public Instruction(String instructionString) throws InstructionException {
		this(instructionString, null);
	}

	public Instruction(String instructionString, String label)
			throws InstructionException {
		this.label = label;
		Scanner scanner = new Scanner(instructionString);
		// String[] components = instructionString.split("\\s");
		// int i = 0;
		// String opcodeString = components[i];
		// while (opcodeString.length() == 0) {
		// i++;
		// if (i >= components.length) {
		// throw new InstructionException(
		// "no opcode found in instruction string '"
		// + instructionString + "'");
		// }
		// opcodeString = components[i];
		// }
		String opcodeString = null;
		if (scanner.hasNext()) {
			opcodeString = scanner.next();
		}
		if (opcodeString == null || opcodeString.equals("")) {
			throw new InstructionException(
					"no opcode found in instruction string '"
							+ instructionString + "'");
		}

		compileOpcode(instructionString, scanner, opcodeString);

		if (opcode == UNKNOWN) {
			throw new InstructionException(
					InstructionException.UNEXPECTED_OPCODE, UNEXPECTED_OPCODE
							+ " string '" + opcodeString
							+ "' found in instruction string '"
							+ instructionString + "'");
		}

		if (scanner.hasNext()) {
			String nextPart = scanner.next();
			if (nextPart.length() > 0) {
				if (!nextPart.startsWith(COMMENT_PREFIX)) {
					System.out.println(nextPart);
					while (scanner.hasNext()) {
						System.out.println(scanner.next());
					}
					throw new InstructionException("unexpected component '"
							+ nextPart + "' found in instruction string '"
							+ instructionString + "'");
				}
			}
		}

		// for (String component : components) {
		// System.out.println("*" + component + "*");
		// if (component.length() == 0)
		// continue;
		//
		// }
	}

	protected void compileOpcode(String instructionString, Scanner scanner,
			String opcodeString) throws InstructionException {
		if (opcodeString.equals("input")) {
			opcode = INPUT;
		} else if (opcodeString.equals("output")) {
			compileOutput(scanner, instructionString);
		} else if (opcodeString.equals("write")) {
			compileWrite(scanner, instructionString);
		} else if (opcodeString.equals("move")) {
			compileMove(scanner, instructionString);
		} else if (opcodeString.equals("read")) {
			opcode = READ;
		} else if (opcodeString.equals("jump")) {
			compileJump(scanner, instructionString);
		} else if (opcodeString.equals("if")) {
			compileIf(scanner, instructionString);
		} else if (opcodeString.equals("load")) {
			compileLoad(scanner, instructionString);
		} else if (opcodeString.equals("halt")) {
			opcode = HALT;
		} else if (opcodeString.equals("noop")) {
			opcode = NOOP;
		} else {
			opcode = UNKNOWN;
		}
	}

	/**
	 * @return the opcode
	 */
	public int getOpcode() {
		return opcode;
	}

	/**
	 * @return the symbol
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	public static void main(String[] args) throws InstructionException {
		Instruction instruction1 = new Instruction("input");
		Instruction instruction2 = new Instruction("output");
		Instruction instruction2b = new Instruction("output 'www' #asdf");
		Instruction instruction3 = new Instruction("write 'a'  # ahem");
		Instruction instruction3b = new Instruction("write # ahem");
		Instruction instruction3c = new Instruction("write ");
		Instruction instruction3d = new Instruction("write");
		Instruction instruction4 = new Instruction("write \"abc12 s'adf\"");
		Instruction instruction5 = new Instruction("move left");
		Instruction instruction6 = new Instruction("move right");
		Instruction instruction7 = new Instruction("read");
		Instruction instruction7b = new Instruction("load 'sdf' # hmm");
		Instruction instruction8 = new Instruction("jump ABC34");
		Instruction instruction9 = new Instruction("if 'asdf' jump ABC34");
		Instruction instruction9b = new Instruction("noop");
		Instruction instruction10 = new Instruction("  halt  ", "XVY");
		Instruction instruction11 = new Instruction("  halt  #  asdf ##");
		System.out.println(instruction1);
		System.out.println(instruction2);
		System.out.println(instruction2b);
		System.out.println(instruction3);
		System.out.println(instruction3b);
		System.out.println(instruction3c);
		System.out.println(instruction3d);
		System.out.println(instruction4);
		System.out.println(instruction5);
		System.out.println(instruction6);
		System.out.println(instruction7);
		System.out.println(instruction7b);
		System.out.println(instruction8);
		System.out.println(instruction9);
		System.out.println(instruction9b);
		System.out.println(instruction10);
		System.out.println(instruction11);
	}
}
