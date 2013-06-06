import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ScuteTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testScute() throws ScuteException, InstructionException,
			IOException {
		String[] program = { "I: input", "if 'a' jump A", "output", "noop",
				"jump Z", "A: output", "jump I", "Z: write 'g'", "move right",
				"write 't'", "move left", "read", "move right", "move right",
				"write", "read", "output", "output 'c'", "load 'g'", "output",
				"halt", };

		String inputString = "a a a a a a c g";

		StringWriter outputFile = new StringWriter();
		Scanner inputFile = new Scanner(inputString);
		// inputFile.useDelimiter("");
		// outputFile.println("test");

		Scute scute = new Scute(program, inputFile, outputFile,
				Scute.geneLetters);
		String output = outputFile.toString();
		assertTrue(output.equals(" a a a a a a c g c g"));
		// System.out.println(output);
		// System.out.println("********************");
		// scute.printLabels();
		// scute.printProgram();
	}

	@Test
	public void testTwoTrackScute() throws ScuteException,
			InstructionException, IOException {
		String[] program = { "I: input", "ifB 'a' jump A", "output", "jump Z",
				"A: output", "jump I", "Z: writeB 'g'", "move right", "noop",
				"writeB 't'", "move left", "read", "move right", "move right",
				"writeA", "read", "output", "move right", "move right",
				"writeB 't'", "read", "output", "move right", "writeB 't'",
				"writeA 'a'", "read", "ifA 'b' jump I", "ifB 't' jump ZZ",
				"halt", "ZZ: output", "output 'c'", "load '1,g'", "output",
				"halt", };

		String inputString = "a a a a a a c g";

		StringWriter outputFile = new StringWriter();
		Scanner inputFile = new Scanner(inputString);
		// inputFile.useDelimiter("");
		// outputFile.println("test");

		TwoTrackScute scute = new TwoTrackScute(program, inputFile, outputFile,
				Scute.geneLetters, Scute.geneLetters);
		String output = outputFile.toString();
		System.out.println(output);
		assertTrue(output.equals(" a a a a a a c blank t t c g"));
		// System.out.println("********************");
		// scute.printLabels();
		// scute.printProgram();
	}

	@Test
	public void testTwoTrackTranslate() throws ScuteException,
			InstructionException, IOException {
		String[] program = { "input", "write", "writeB 'b'", "read", "output",
				"halt", };

		String inputString = "a";

		StringWriter outputFile = new StringWriter();
		Scanner inputFile = new Scanner(inputString);
		// inputFile.useDelimiter("");
		// outputFile.println("test");

		TwoTrackScute scute = new TwoTrackScute(program, inputFile, outputFile,
				Scute.geneLetters, Scute.geneLetters);
		String output = outputFile.toString();
		System.out.println(output);

		String[] translatedProgram = scute.translateToStrings();

		Scanner inputFile2 = new Scanner(inputString);
		StringWriter outputFile2 = new StringWriter();
		Scute translatedScute = new Scute(translatedProgram, inputFile2,
				outputFile2);
		String output2 = outputFile2.toString();
		System.out.println(output2);

		 assertTrue(output.equals(output2));
		// System.out.println("********************");
		// scute.printLabels();
		// scute.printProgram();
	}

}
