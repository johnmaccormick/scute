import java.util.Arrays;
import java.util.Scanner;
import java.util.TreeSet;

public class TwoTrackInstruction extends Instruction {

	public static final int WRITE_A = 100;
	public static final int WRITE_B = 101;
	public static final int IFJUMP_A = 102;
	public static final int IFJUMP_B = 103;

	public static final String WRITE_A_STR = "writeA";
	public static final String WRITE_B_STR = "writeB";
	public static final String IF_A_STR = "ifA";
	public static final String IF_B_STR = "ifB";

	public static final int twoTrackOpcodesArray[] = {WRITE_A, WRITE_B, IFJUMP_A, IFJUMP_B};

	public TwoTrackInstruction(int opcode, String symbol, String target, String label) {
		super(opcode, symbol, target, label);
	}
	
	public TwoTrackInstruction(String instructionString)
			throws InstructionException {
		super(instructionString);
	}

	public TwoTrackInstruction(String instructionString, String label)
			throws InstructionException {
		super(instructionString, label);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String superVal = super.toString();
		int location = superVal.indexOf(UNEXPECTED_OPCODE);
		if (location == -1) {
			return superVal;
		}
		StringBuilder retVal = new StringBuilder(
				superVal.substring(0, location));
		switch (opcode) {
		case WRITE_A:
			retVal.append("writeA");
			appendSymbol(retVal);
			break;
		case WRITE_B:
			retVal.append("writeB");
			appendSymbol(retVal);
			break;
		case IFJUMP_A:
			retVal.append("ifA '" + symbol + "' jump " + target);
			break;
		case IFJUMP_B:
			retVal.append("ifB '" + symbol + "' jump " + target);
			break;
		case HALT:
			retVal.append("halt");
			break;
		default:
			retVal.append(UNEXPECTED_OPCODE + " " + opcode);
		}
		return retVal.toString();
	}

	protected void compileWriteA(Scanner scanner, String instructionString)
			throws InstructionException {
		opcode = WRITE_A;
		symbol = parseSymbol(instructionString, WRITE_A_STR, scanner);
	}

	protected void compileWriteB(Scanner scanner, String instructionString)
			throws InstructionException {
		opcode = WRITE_B;
		symbol = parseSymbol(instructionString, WRITE_B_STR, scanner);
	}

	protected void compileIfA(Scanner scanner, String instructionString)
			throws InstructionException {
		opcode = IFJUMP_A;
		symbol = parseSymbol(instructionString, IF_A_STR, scanner);
		checkIf(scanner, instructionString);
		parseLabel(scanner, instructionString);
	}

	protected void compileIfB(Scanner scanner, String instructionString)
			throws InstructionException {
		opcode = IFJUMP_B;
		symbol = parseSymbol(instructionString, IF_B_STR, scanner);
		checkIf(scanner, instructionString);
		parseLabel(scanner, instructionString);
	}

	protected void compileOpcode(String instructionString, Scanner scanner,
			String opcodeString) throws InstructionException {
		super.compileOpcode(instructionString, scanner, opcodeString);
		if (opcode == UNKNOWN) {
			if (opcodeString.equals("writeA")) {
				compileWriteA(scanner, instructionString);
			} else if (opcodeString.equals("writeB")) {
				compileWriteB(scanner, instructionString);
			} else if (opcodeString.equals("ifA")) {
				compileIfA(scanner, instructionString);
			} else if (opcodeString.equals("ifB")) {
				compileIfB(scanner, instructionString);
			}
		}
	}

	
	
	public static void main(String[] args) throws InstructionException {
		TwoTrackInstruction instruction1 = new TwoTrackInstruction("input");
		TwoTrackInstruction instruction2 = new TwoTrackInstruction("writeA");
		TwoTrackInstruction instruction3 = new TwoTrackInstruction("writeB 'xxx'");
		TwoTrackInstruction instruction4 = new TwoTrackInstruction("ifA 'x' jump asdf");
		TwoTrackInstruction instruction5 = new TwoTrackInstruction("ifB 'reyusfdhjk' jump dfjdkj");
		System.out.println(instruction1);
		System.out.println(instruction2);
		System.out.println(instruction3);
		System.out.println(instruction4);
		System.out.println(instruction5);
	}

}
