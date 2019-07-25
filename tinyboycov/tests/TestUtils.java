package tinyboycov.tests;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.BitSet;

import javr.core.AVR;
import javr.core.AvrDecoder;
import javr.core.AvrInstruction;
import javr.io.HexFile;
import javrsim.peripherals.JPeripheral;
import tinyboy.core.ControlPad;
import tinyboy.core.TinyBoyEmulator;
import tinyboy.core.TinyBoyInputSequence;
import tinyboy.util.AutomatedTester;
import tinyboy.util.CoverageAnalysis;
import tinyboy.views.TinyBoyPeripheral;
import tinyboycov.core.TinyBoyInputGenerator;

/**
 * Test utilities for the assignment
 *
 * *** DO NOT CHANGE THIS FILE ***
 *
 * @author David J. Pearce
 *
 */
public class TestUtils {
  /**
   * This identifies the coverage target we are aiming for. Increasing this target
   * will improve your mark!
   */
  private static final double COVERAGE_TARGET = 95.0;
  /**
   * Identifies the directory in which the test firmwares are located.
   */
  private static final String TESTS_DIR = "tests/".replace("/", File.separator);
  /**
   * ONE second worth of cycles running at 8MHz.
   */
  private static final int ONE_SECOND = 8_000_000;

  /**
   * Check the coverage for a given firmware file using a single input sequence of
   * pulses, where each pulse has a given width.
   *
   * @param filename
   *          The name of the firmware file to test.
   * @param inputSequence
   *          The input sequence (e.g. <code>"__RL_UD_LL_R"</code>
   * @param pulseLength
   *          The width of the pulse (in cycles).
   * @throws IOException
   */
  public static void checkManualCoverage(String filename, String inputSequence, int pulseLength) throws IOException {
    TinyBoyInputSequence input = createInputSequence(inputSequence, pulseLength);
    // Construct our tiny boy emulator
    TinyBoyEmulator tinyBoy = createTinyBoy();
    // Compute the coverage info
    CoverageAnalysis coverage = computeManualCoverage(tinyBoy, filename, input);
    if (coverage.getBranchCoverage() < COVERAGE_TARGET) {
      // Indicates a fail
      System.out.println("===============================================");
      System.out.println(filename + " (" + String.format("%.2f", coverage.getInstructionCoverage()) + "% instructions, "
          + String.format("%.2f", coverage.getBranchCoverage()) + "% branches)");
      System.out.println("===============================================");
      printDisassembly(tinyBoy, coverage);
      fail("Branch coverage failed to meet target of " + COVERAGE_TARGET + "%" + Math.round(coverage.getBranchCoverage())  + "% I: " + Math.round(coverage.getInstructionCoverage())  + "%");
    } else {
      printDisassembly(tinyBoy, coverage);
      System.out.println("BRANCH COVERAGE: " + coverage.getBranchCoverage());
    }
  }

  public static void checkFuzzingCoverage(String filename) throws IOException {
    // Construct our tiny boy emulator
    TinyBoyEmulator tinyBoy = createTinyBoy();
    // Compute the coverage for the given firmware image
    CoverageAnalysis coverage = computeFuzzCoverage(tinyBoy, filename);
    // Check wether the target was reached.
    if (coverage.getBranchCoverage() < COVERAGE_TARGET) {
      // Indicates a fail
      System.out.println("===============================================");
      System.out.println(filename + " (" + String.format("%.2f", coverage.getInstructionCoverage()) + "% instructions, "
          + String.format("%.2f", coverage.getBranchCoverage()) + "% branches)");
      System.out.println("===============================================");
      printDisassembly(tinyBoy, coverage);
      fail("Branch coverage failed to meet target of " + COVERAGE_TARGET + "% B: " + Math.round(coverage.getBranchCoverage())  + "% I: " + Math.round(coverage.getInstructionCoverage())  + "%");
    } else {
      printDisassembly(tinyBoy, coverage);
      System.out.println("BRANCH COVERAGE: " + coverage.getBranchCoverage());
    }
  }

  /**
   * Create a TinyBoy emulator which has a graphical display.
   *
   * @return
   */
  private static TinyBoyEmulator createTinyBoy() {
    return new TinyBoyEmulator() {
      // This is a little ugly!!
      JPeripheral view = new TinyBoyPeripheral(this);

      @Override
      public void clock() {
        super.clock();
        view.repaint();
      }
    };
  }

  /**
   * Turn a string representation of an input sequence (e.g. "LLR_R_UUD") into an
   * instanceof TinyBoyInputSequence.
   *
   * @param input
   * @param pulseLength
   * @returncomputeFuzzCoverage
   */
  public static TinyBoyInputSequence createInputSequence(String input, int pulseLength) {
    TinyBoyInputSequence tbi = new TinyBoyInputSequence(input.length(), pulseLength);
    for (int i = 0; i != input.length(); ++i) {
      switch (input.charAt(i)) {
      case 'U':
        tbi.setPulse(i, ControlPad.Button.UP);
        break;
      case 'D':
        tbi.setPulse(i, ControlPad.Button.DOWN);
        break;
      case 'L':
        tbi.setPulse(i, ControlPad.Button.LEFT);
        break;
      case 'R':
        tbi.setPulse(i, ControlPad.Button.RIGHT);
        break;
      case '_':
        tbi.setPulse(i, null);
        break;
      default:
        throw new IllegalArgumentException("Invalid input sequence: " + input);
      }
    }
    return tbi;
  }

  /**
   * Perform coverage analysis on a single input sequence.
   *
   * @param tinyBoy
   * @param filename
   * @param sequence
   * @return
   * @throws IOException
   */
  public static CoverageAnalysis computeManualCoverage(TinyBoyEmulator tinyBoy, String filename,
      TinyBoyInputSequence sequence) throws IOException {
    // Construct an input generator which returns only the supplied input.
    AutomatedTester.InputGenerator<?> generator = constructManualGenerator(sequence);
    // Run the tests
    return computeCoverage(tinyBoy, filename, sequence.size(), generator);
  }

  /**
   * Perform coverage testing via fuzzing the given firmware and, when this fails
   * to reach the required target, produce some useful output.
   *
   * @param tinyBoy
   *          the TinyBoyEmulator to be used for testing.
   * @param filename
   *          The filename of the firmware image to test.
   * @return
   * @throws IOException
   */
  public static CoverageAnalysis computeFuzzCoverage(TinyBoyEmulator tinyBoy, String filename) throws IOException {
    // Construct the input generator
    AutomatedTester.InputGenerator<?> generator = new TinyBoyInputGenerator();
    // Run the tests
    return computeCoverage(tinyBoy, filename, ONE_SECOND, generator);
  }

  /**
   * Construct an input generator for the automated tester which simply returns
   * the given input sequence, and that's it.
   *
   * @param sequence
   * @return
   */
  public static AutomatedTester.InputGenerator<?> constructManualGenerator(TinyBoyInputSequence sequence) {
    return new AutomatedTester.InputGenerator<TinyBoyInputSequence>() {
      private TinyBoyInputSequence seq = sequence;

      @Override
      public TinyBoyInputSequence generate() {
        TinyBoyInputSequence r = seq;
        seq = null; // prevent it from being used again
        return r;
      }

      @Override
      public void record(TinyBoyInputSequence input, BitSet output) {
        // Do nothing here as this is only for a single input sequence
      }

    };
  }

  /**
   * Perform automated coverage analysis of a given firmware on a given TinyBoy
   * instance using a given input generator.
   *
   * @param tinyBoy
   *          The TinyBoy emulator being used.
   * @param filename
   *          The firmware image to test.
   * @param cycles
   *          The maximum number of cycles to execute the test for.
   * @param generator
   *          The generator used to generate inputs for testing.
   * @return
   * @throws IOException
   */
  public static CoverageAnalysis computeCoverage(TinyBoyEmulator tinyBoy, String filename, int cycles,
      AutomatedTester.InputGenerator<?> generator) throws IOException {
    // Read the firmware image
    HexFile.Reader hfr = new HexFile.Reader(new FileReader(TESTS_DIR + filename));
    HexFile firmware = hfr.readAll();
    // Construct the fuzz tester
    AutomatedTester tester = new AutomatedTester(tinyBoy, firmware, generator);
    // Run the fuzz tester for 10 inputs.
    return tester.run(50, cycles, COVERAGE_TARGET); // NOTE: CHANGED 
  }

  /**
   * Disassemble the firmware image in order to provide useful feedback.
   *
   * @param tinyBoy
   * @param The
   *          set of reachable instructions. This is critical to determining what
   *          is a valid statement, versus what is not.
   * @return
   */
  public static void printDisassembly(TinyBoyEmulator tinyBoy, CoverageAnalysis coverage) {
    AvrDecoder decoder = new AvrDecoder();
    AVR.Memory code = tinyBoy.getAVR().getCode();
    int size = code.size() / 2;
    boolean ignoring = false;
    int instructions = 0;
    int coveredInstructions = 0;
    int branches = 0;
    int coveredBranches = 0;
    for (int i = 0; i != size;) {
      if (coverage.isReachableInstruction(i)) {
        AvrInstruction insn = decoder.decode(code, i);
        System.out.print(String.format("%04X", i));
        instructions++;
        if (coverage.wasCovered(i)) {
          System.out.print(" [*] ");
          coveredInstructions++;
        } else {
          System.out.print(" [ ] ");
        }
        System.out.print(insn.toString());
        if (coverage.isConditionalBranchCovered(i)) {
          System.out.println("\t<<<<<<<<<<<<<<<<<<<< (" + branches++ + ")");
          coveredBranches++;
        } else if (coverage.isConditionalBranch(i)) {
          System.out.println("\t<<<<<<<<<<<<<<<<<<<< UNCOVERED (" + branches++ + ")");
        } else {
          System.out.println();
        }
        i = i + insn.getWidth();
        ignoring = false;
      } else {
        if (!ignoring) {
          System.out.println(" ... ");
          ignoring = true;
        }
        i = i + 1;
      }
    }
    System.out.println("Instruction Coverage = " + coveredInstructions + " / " + instructions + "(" + code.size() + ")"
        + "(" + ((double) coveredInstructions / (double) instructions) + ")");
    System.out.println("Branch Coverage = " + coveredBranches + " / " + branches + "("
        + ((double) coveredBranches / (double) branches) + ")");
  }

}
