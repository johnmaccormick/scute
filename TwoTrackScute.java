import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.TreeSet;

public class TwoTrackScute extends Scute {

	public static final String TRACK_SEPARATOR = ",";
	public static final String TRANSLATION_PREFIX = "2t.";

	public TwoTrackScute(String[] program, Scanner inputFile, Writer outputFile)
			throws ScuteException, InstructionException, IOException {
		this(program, inputFile, outputFile, null, null);
	}

	public TwoTrackScute(String[] program, Scanner inputFile,
			Writer outputFile, String[] alphabetA, String[] alphabetB)
			throws ScuteException, InstructionException, IOException {
		super(program, inputFile, outputFile, crossAlphabets(alphabetA,
				alphabetB));
	}

	protected Instruction compileInstruction(String instructionString,
			String label) throws InstructionException {
		Instruction instruction = new TwoTrackInstruction(instructionString,
				label);
		return instruction;
	}

	protected class twoTracks {
		public String A = null;
		public String B = null;
	}

	protected twoTracks extractTracks(String symbol) throws ScuteException {
		twoTracks tracks = new twoTracks();
		// if (symbol.equals(ATOMIC_BLANK)) {
		// tracks.A = ATOMIC_BLANK;
		// tracks.B = ATOMIC_BLANK;
		// } else {
		int boundary = symbol.indexOf(TRACK_SEPARATOR);
		if (boundary == -1) {
			throw new ScuteException("Couldn't find track separator in symbol "
					+ symbol);
		}
		tracks.A = symbol.substring(0, boundary);
		tracks.B = symbol.substring(boundary + 1);
		// }

		return tracks;
	}

	protected String getSource(Instruction instruction, char track)
			throws ScuteException {
		String source = null;
		if (instruction.getSymbol() != null) {
			source = instruction.getSymbol();
		} else {
			twoTracks tracks = extractTracks(register);
			switch (track) {
			case 'A':
				source = tracks.A;
				break;
			case 'B':
				source = tracks.B;
				break;
			default:
				throw new ScuteException("unexpected track " + track);
			}
		}
		return source;
	}

	// protected void executeOutput(Instruction instruction) throws IOException,
	// ScuteException {
	// String source = getSource(instruction, 'B');
	// outputFile.write(" " + source);
	// }

	protected void executeWriteA(Instruction instruction) throws ScuteException {
		extendMemory();
		String source = getSource(instruction, 'A');
		twoTracks tracks = extractTracks(memory.get(memoryPointer));
		String textToWrite = source + TRACK_SEPARATOR + tracks.B;
		memory.set(memoryPointer, textToWrite);
	}

	protected void executeWriteB(Instruction instruction) throws ScuteException {
		extendMemory();
		String source = getSource(instruction, 'B');
		twoTracks tracks = extractTracks(memory.get(memoryPointer));
		String textToWrite = tracks.A + TRACK_SEPARATOR + source;
		memory.set(memoryPointer, textToWrite);
	}

//	protected void executeInput() {
//		super.executeInput();
//		register = ATOMIC_BLANK + TRACK_SEPARATOR + register;
//	}

	protected twoBools executeInstruction(Instruction instruction)
			throws ScuteException, IOException {
		twoBools retVal = super.executeInstruction(instruction);
		twoTracks tracks = null;
		if (retVal.unknownOpcode) {
			retVal.unknownOpcode = false;
			int opcode = instruction.getOpcode();
			switch (opcode) {
			case TwoTrackInstruction.WRITE_A:
				executeWriteA(instruction);
				break;
			case TwoTrackInstruction.WRITE_B:
				executeWriteB(instruction);
				break;
			case TwoTrackInstruction.IFJUMP_A:
				tracks = extractTracks(register);
				if (tracks.A.equals(instruction.getSymbol())) {
					doJump(instruction);
					retVal.jumped = true;
				}
				break;
			case TwoTrackInstruction.IFJUMP_B:
				tracks = extractTracks(register);
				if (tracks.B.equals(instruction.getSymbol())) {
					doJump(instruction);
					retVal.jumped = true;
				}
				break;
			default:
				retVal.unknownOpcode = true;
			}
		}
		return retVal;
	}

	protected static String[] crossAlphabets(String[] alphabet1,
			String[] alphabet2) {
		if (alphabet1 == null || alphabet2 == null) {
			return null;
		}
		alphabet1 = addBlank(alphabet1);
		alphabet2 = addBlank(alphabet2);
		String[] retVal = new String[alphabet1.length * alphabet2.length];
		int i = 0;
		for (String symbol1 : alphabet1) {
			for (String symbol2 : alphabet2) {
				retVal[i] = symbol1 + TRACK_SEPARATOR + symbol2;
				i++;
			}
		}
		return retVal;
	}

	public String[] translateToStrings() throws ScuteException {
		Instruction[] instructions = translate();
		String[] program = new String[instructions.length];
		for (int i = 0; i < instructions.length; i++) {
			program[i] = instructions[i].toString();
		}
		return program;
	}

	public Instruction[] translate() throws ScuteException {
		ArrayList<Instruction> newProgram = new ArrayList<Instruction>();
		TreeSet<Integer> twoTrackOpcodes = new TreeSet<Integer>();
		for (int opcode : TwoTrackInstruction.twoTrackOpcodesArray) {
			twoTrackOpcodes.add(opcode);
		}

		for (Instruction instruction : instructions) {
			int opcode = instruction.getOpcode();
			if (!twoTrackOpcodes.contains(opcode)) {
				newProgram.add(instruction);
			} else {
				switch (opcode) {
				case TwoTrackInstruction.WRITE_A:
					translateWriteA(instruction, newProgram);
					break;
				case TwoTrackInstruction.WRITE_B:
					translateWriteB(instruction, newProgram);
					break;
				case TwoTrackInstruction.IFJUMP_A:
					translateIfA(instruction, newProgram);
					break;
				case TwoTrackInstruction.IFJUMP_B:
					translateIfB(instruction, newProgram);
					break;
				default:

				}
			}
		}
		Instruction[] newProgramArray = newProgram
				.toArray(new Instruction[] {});
		return newProgramArray;
	}

	private void translateIfB(Instruction instruction,
			ArrayList<Instruction> newProgram) {
		// TODO Auto-generated method stub
		newProgram.add(instruction);
	}

	private void translateIfA(Instruction instruction,
			ArrayList<Instruction> newProgram) {
		// TODO Auto-generated method stub
		newProgram.add(instruction);
	}

	private void translateWriteA(Instruction instruction,
			ArrayList<Instruction> newProgram) throws ScuteException {
		translateWriteAorB(instruction, newProgram, 'A');

//		ArrayList<Instruction> registerContentJumps = new ArrayList<Instruction>();
//		ArrayList<Instruction> reads = new ArrayList<Instruction>();
//		ArrayList<Instruction> writes = new ArrayList<Instruction>();
//		int lineNum = newProgram.size();
//		String prefix = TRANSLATION_PREFIX + lineNum + ".";
//		String finalTarget = prefix + "done";
//		for (String symbol : alphabet) {
//			// instruction for jumping based on symbol
//			String target = prefix + symbol;
//			Instruction if1 = new Instruction(Instruction.IFJUMP, symbol,
//					target, null);
//			registerContentJumps.add(if1);
//
//			// instructions for reading and jumping based on value read
//			Instruction read = new Instruction(Instruction.READ, null, null,
//					target);
//			reads.add(read);
//			for (String symbol2 : alphabet) {
//				String target2 = target + "." + symbol2;
//				Instruction if2 = new Instruction(Instruction.IFJUMP, symbol2,
//						target2, null);
//				reads.add(if2);
//				// instructions for writing then reloading original symbol
//				String writeSymbol = instruction.symbol + TRACK_SEPARATOR
//						+ symbol;
//				Instruction write = new Instruction(Instruction.WRITE,
//						writeSymbol, null, target2);
//				writes.add(write);
//				Instruction load = new Instruction(Instruction.LOAD, symbol,
//						null, null);
//				writes.add(load);
//				Instruction jump = new Instruction(Instruction.JUMP, null,
//						finalTarget, null);
//				writes.add(jump);
//			}
//		}
//		// copy any label from the original instruction to the new first
//		// instruction
//		registerContentJumps.get(0).label = instruction.label;
//		// add a final no-op instruction where everyone jumps at the end
//		writes.add(new Instruction(Instruction.NOOP, null, null, finalTarget));
//
//		// add the three lists of instructions to the existing program
//		newProgram.addAll(registerContentJumps);
//		newProgram.addAll(reads);
//		newProgram.addAll(writes);
	}

	private void translateWriteB(Instruction instruction,
			ArrayList<Instruction> newProgram) throws ScuteException {
		translateWriteAorB(instruction, newProgram, 'B');
		
		
//		ArrayList<Instruction> registerContentJumps = new ArrayList<Instruction>();
//		ArrayList<Instruction> reads = new ArrayList<Instruction>();
//		ArrayList<Instruction> writes = new ArrayList<Instruction>();
//		int lineNum = newProgram.size();
//		String prefix = TRANSLATION_PREFIX + lineNum + ".";
//		String finalTarget = prefix + "done";
//		for (String symbol : alphabet) {
//			twoTracks tracks = extractTracks(symbol);
//
//			// instruction for jumping based on symbol
//			String target = prefix + symbol;
//			Instruction if1 = new Instruction(Instruction.IFJUMP, symbol,
//					target, null);
//			registerContentJumps.add(if1);
//
//			// instructions for reading and jumping based on value read
//			Instruction read = new Instruction(Instruction.READ, null, null,
//					target);
//			reads.add(read);
//			for (String symbol2 : alphabet) {
//				String target2 = target + "." + symbol2;
//				Instruction if2 = new Instruction(Instruction.IFJUMP, symbol2,
//						target2, null);
//				reads.add(if2);
//				// instructions for writing then reloading original symbol
//				String writeSymbol = tracks.A + TRACK_SEPARATOR
//						+ instruction.symbol;
//				Instruction write = new Instruction(Instruction.WRITE,
//						writeSymbol, null, target2);
//				writes.add(write);
//				Instruction load = new Instruction(Instruction.LOAD, symbol,
//						null, null);
//				writes.add(load);
//				Instruction jump = new Instruction(Instruction.JUMP, null,
//						finalTarget, null);
//				writes.add(jump);
//			}
//		}
//		// copy any label from the original instruction to the new first
//		// instruction
//		registerContentJumps.get(0).label = instruction.label;
//		// add a final no-op instruction where everyone jumps at the end
//		writes.add(new Instruction(Instruction.NOOP, null, null, finalTarget));
//
//		// add the three lists of instructions to the existing program
//		newProgram.addAll(registerContentJumps);
//		newProgram.addAll(reads);
//		newProgram.addAll(writes);
	}

	private void translateWriteAorB(Instruction instruction,
			ArrayList<Instruction> newProgram, char track)
			throws ScuteException {

		ArrayList<Instruction> registerContentJumps = new ArrayList<Instruction>();
		ArrayList<Instruction> reads = new ArrayList<Instruction>();
		ArrayList<Instruction> writes = new ArrayList<Instruction>();
		int lineNum = newProgram.size();
		String prefix = TRANSLATION_PREFIX + lineNum + ".";
		String finalTarget = prefix + "done";
		for (String symbol : alphabet) {

			// instruction for jumping based on symbol
			String target = prefix + symbol;
			Instruction if1 = new Instruction(Instruction.IFJUMP, symbol,
					target, null);
			registerContentJumps.add(if1);

			// instructions for reading and jumping based on value read
			Instruction read = new Instruction(Instruction.READ, null, null,
					target);
			reads.add(read);
			for (String symbol2 : alphabet) {
				twoTracks tracks2 = extractTracks(symbol2);
				String target2 = target + "." + symbol2;
				Instruction if2 = new Instruction(Instruction.IFJUMP, symbol2,
						target2, null);
				reads.add(if2);
				// instructions for writing then reloading original symbol
				String writeSymbol = null;
				switch (track) {
				case 'A':
					writeSymbol = instruction.symbol + TRACK_SEPARATOR
							+ tracks2.B;
					break;
				case 'B':
					writeSymbol = tracks2.A + TRACK_SEPARATOR
							+ instruction.symbol;
					break;
				default:
					throw new ScuteException("unexpected track name '" + track
							+ "'");
				}
				Instruction write = new Instruction(Instruction.WRITE,
						writeSymbol, null, target2);
				writes.add(write);

				Instruction load = new Instruction(Instruction.LOAD, symbol,
						null, null);
				writes.add(load);
				Instruction jump = new Instruction(Instruction.JUMP, null,
						finalTarget, null);
				writes.add(jump);
			}
		}
		// copy any label from the original instruction to the new first
		// instruction
		registerContentJumps.get(0).label = instruction.label;
		// add a final no-op instruction where everyone jumps at the end
		writes.add(new Instruction(Instruction.NOOP, null, null, finalTarget));

		// add the three lists of instructions to the existing program
		newProgram.addAll(registerContentJumps);
		newProgram.addAll(reads);
		newProgram.addAll(writes);
	}

	public static void main(String[] args) throws ScuteException,
			InstructionException, IOException {
		String[] program = { "input", "writeA '1'", "writeB 'b'", "read", "output",
				"move right",
				"writeA '0'", 
				"move right",
				"writeB 'a'", 
				"read", "output",
				"move left",
				"read", "output",
				"writeB 'b'", 
				"read", "output",
				"halt", };

		String inputString = ATOMIC_BLANK + TRACK_SEPARATOR + "a";

		StringWriter outputFile = new StringWriter();
		Scanner inputFile = new Scanner(inputString);
		// inputFile.useDelimiter("");
		// outputFile.println("test");

		TwoTrackScute scute = new TwoTrackScute(program, inputFile, outputFile,
				Scute.binary, Scute.twoLetters);
		String output = outputFile.toString();
		System.out.println(output);

		String[] translatedProgram = scute.translateToStrings();

		PrintWriter resultFile = new PrintWriter(new FileWriter("result.txt"));
		for (String line : translatedProgram) {
			resultFile.println(line);
		}
		resultFile.close();

		System.out.println("******************************************");
		System.out.println("******************************************");
		System.out.println("******************************************");
		System.out.println("******************************************");
		
		Scanner inputFile2 = new Scanner(inputString);
		StringWriter outputFile2 = new StringWriter();
		Scute translatedScute = new Scute(translatedProgram, inputFile2,
				outputFile2, scute.getAlphabet());
		String output2 = outputFile2.toString();
		System.out.println(output2);
	}

}
