package tinyboycov.core;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import tinyboy.core.ControlPad;
import tinyboy.core.TinyBoyInputSequence;
import tinyboy.util.AutomatedTester;
import tinyboycov.tests.TestUtils;

/**
 * The TinyBoy Input Generator is responsible for generating and refining inputs
 * to try and ensure that sufficient branch coverage is obtained.
 *
 * @author David J. Pearce
 *
 */
public class TinyBoyInputGenerator implements AutomatedTester.InputGenerator<TinyBoyInputSequence> {

	/**
	 * Use random number generation with fixed seed for deterministic behaviour. You
	 * can use a proper seed if you prefer. This may work better.
	 */
	private Random random = new Random(854269); // try different seeds

	boolean init = true;
	int size = 0;

	private ConcurrentHashMap<TinyBoyInputSequence, ArrayList<TinyBoyInputSequence>> parents = new ConcurrentHashMap<TinyBoyInputSequence, ArrayList<TinyBoyInputSequence>>();
	private ConcurrentHashMap<TinyBoyInputSequence, Pair> pairs = new ConcurrentHashMap<TinyBoyInputSequence, Pair>();

	int m = 4;
	int noStartingParents = 2; // (2)

	private static final int PULSE_LENGTH = (20 * 8_000); // (20)
	private static final int PULSE_COUNT = 100; // (100)
	private int n = PULSE_COUNT/3; // 2 (3)
	private int noChildren = 4; // 3 (4)
	private int noBest = 2; // (1) (2)

	private TinyBoyInputSequence input = new TinyBoyInputSequence(PULSE_COUNT, PULSE_LENGTH);
	int i = 0;

	// URDDLLLDL_LRLULRLURRDRL_UURRLU_RDLDLLLLLDLULR_ULL_RUULDURLULUUDURRD_ULULR_R_RLDU_L_R_R_DRRDLDRUURRUR
	private String soku = "LRD_LDLDUDUD_LLLRLLLRRDRUUD_DRULL__L_DUURLRULRRU_RLRLLRL_LLRRLUUURURR_RLLD_D_LDDRULRULLLUUUURDRLDUU_";

	// DUUD_UD_R_DDRRDLRLDURD_LUDLRLUUUULRU_DDULD_UULLRRDUUUDURLUL_UULURDULRLURRU_DULLRRLDUUD_L_RLDD_UURRRU
	private String snake = "DUUD_UD_R_DDRRDLRLDURD_LUDLRLUUUULRU_DDULD_UULLRRDUUUDURLUL_UULURDULRLURRU_DULLRRLDUUD_L_RLDD_UURRRU";

	// ..
	private String tetris = "RDUUDUURDRDRRDUDLRRRDRLLRDDLRLRLLRLLDDRRUDULULUDDULURDULLRUULUDURUDRDLUDULDDRDDURRURLRURDLDDDUUURRLU";

	private String snake2 = "_URLUURDRRLR_UUURULRLU___LRRLUDRURD_URLUURDRRLR_UUURULRLU___LRRLUDRURDL_L_LRLLDLLUDLLDUDLRUDDDDRUD_URDLRURDR__DDUU__RRU_LD___DULDDURULD_URLUURDRRLR_UUURULRLU___LRRLUDRURDL_L_LRLLDLLUDLLDUDLRUDDDDRUD_URDLRURDR__DDUU__RRU_LD___DULDDURULDL_L_LRLLDLLUDLLDUDLRUDDDDRUD_URDLRURDR__DDUU__RRU_LD___DULDDURULD_URLUURDRRLR_UUURULRLU___LRRLUDRURDL_L_LRLLDLLUDLLDUDLRUDDDDRUD_URDLRURDR__DDUU__RRU_LD___DULDDURULD";
	private String tetris2 = "RLDUDLDDUURRUDURLRUULLLLLULULDLLUDDRURLDLLDLLULDLRURUDRURRRUULUDDDDULLLRLLDDULUDURRULRDRRUUDURLLULDU";

	private ArrayList<TinyBoyInputSequence> possible = new ArrayList<TinyBoyInputSequence>() {
		{
			add(TestUtils.createInputSequence(snake2, (int) (125 * 8_000)));
			add(TestUtils.createInputSequence(tetris2, (int) (125 * 8_000)));

			add(TestUtils.createInputSequence(soku, PULSE_LENGTH));
			add(TestUtils.createInputSequence(snake, PULSE_LENGTH));
			add(TestUtils.createInputSequence(tetris, PULSE_LENGTH));
		}
	};

	@Override
	public TinyBoyInputSequence generate() {

		if (i < possible.size()) {
			return possible.get(i++);

		}

		if (parents.isEmpty() || init) {

			input = randomlyMutate(input, n, m);

			if (!parents.isEmpty())
				input = randomlyMutate(getBestInputSeq(), PULSE_COUNT / 2, m);

			while (parents.size() < noStartingParents + 2) {

				parents.put(input, selectAndMutate(input));
				return input;
			}

			ArrayList<TinyBoyInputSequence> bestStartingParents = new ArrayList<TinyBoyInputSequence>();
			List<TinyBoyInputSequence> currentPairs = new ArrayList<>(pairs.keySet());

			while (bestStartingParents.size() < noStartingParents) {
				TinyBoyInputSequence best = getBestInputSeq(currentPairs);

				bestStartingParents.add(best);
				currentPairs.remove(best);
			}

			parents.clear();
			for (TinyBoyInputSequence seq : bestStartingParents) {
				parents.put(seq, selectAndMutate(seq));
			}

			for (Map.Entry<TinyBoyInputSequence, Pair> p : pairs.entrySet()) {
				if (!bestStartingParents.contains(p.getKey()))
					pairs.remove(p.getKey());
			}
			init = false;

		}

		for (Map.Entry<TinyBoyInputSequence, ArrayList<TinyBoyInputSequence>> p : parents.entrySet()) {
			if (pairs.get(p.getKey()) == null) {
				return p.getKey();
			}
			if (p.getValue().isEmpty()) {
				p.getValue().addAll(selectAndMutate(p.getKey()));
			}

			for (TinyBoyInputSequence seq : p.getValue()) {
				if (pairs.get(seq) == null) {
					return seq;
				}
			}
		}

		ArrayList<TinyBoyInputSequence> nps = new ArrayList<TinyBoyInputSequence>();
		List<TinyBoyInputSequence> currentPairs = new ArrayList<>(pairs.keySet());

		while (nps.size() < noBest) {
			TinyBoyInputSequence best = getBestInputSeq(currentPairs);

			nps.add(best);
			currentPairs.remove(best);
		}

		parents.clear();

		for (TinyBoyInputSequence seq : nps) {
			parents.put(seq, selectAndMutate(seq));
		}

		for (Map.Entry<TinyBoyInputSequence, Pair> p : pairs.entrySet()) {
			if (!nps.contains(p.getKey()))
				pairs.remove(p.getKey());
		}

		for (Map.Entry<TinyBoyInputSequence, ArrayList<TinyBoyInputSequence>> p : parents.entrySet()) {
			if (pairs.get(p.getKey()) == null) {
				return p.getKey();
			}
			if (p.getValue().isEmpty()) {
				p.getValue().addAll(selectAndMutate(p.getKey()));
			}

			for (TinyBoyInputSequence seq : p.getValue()) {
				if (pairs.get(seq) == null) {
					return seq;
				}
			}
		}

		return getBestInputSeq();
	}

	private TinyBoyInputSequence getBestInputSeq() {
		Object[] crunchifyKeys = pairs.keySet().toArray();
		Object key = crunchifyKeys[new Random().nextInt(crunchifyKeys.length)];

		Pair best = pairs.get(key);

		for (Map.Entry<TinyBoyInputSequence, Pair> p1 : pairs.entrySet()) {
			for (Map.Entry<TinyBoyInputSequence, Pair> p2 : pairs.entrySet()) {

				if (subsumedBy(best.output, p1.getValue().output))
					best = p1.getValue();
			}
		}

		return best.input;
	}

	private TinyBoyInputSequence getBestInputSeq(List<TinyBoyInputSequence> currentPairs) {

		Pair best = pairs.get(currentPairs.get(0));

		for (int a = 0; a < currentPairs.size(); a++) {
			for (int b = 0; b < currentPairs.size(); b++) {

				if (subsumedBy(best.output, pairs.get(currentPairs.get(a)).output))
					best = pairs.get(currentPairs.get(a));
			}
		}

		return best.input;
	}

	@Override
	public void record(TinyBoyInputSequence seq, BitSet output) {
		if (seq == null || output == null) {
			System.out.println("	 null");
		}

		if (i < possible.size())
			return;

		if (pairs.get(seq) == null) {
			pairs.put(seq, new Pair(seq, output));
		}

		System.out.println(getBestInputSeq());
	}

	private TinyBoyInputSequence getParent(TinyBoyInputSequence input) {
		for (Map.Entry<TinyBoyInputSequence, ArrayList<TinyBoyInputSequence>> p : parents.entrySet()) {
			if (p.getValue().contains(input)) {
				return p.getKey();
			}

		}

		return null;
	}

	/**
	 * Check whether a given input sequence is completely subsumed by another.
	 *
	 * @param lhs
	 *            The one which may be subsumed.
	 * @param rhs
	 *            The one which may be subsuming.
	 * @return
	 */
	public boolean subsumedBy(BitSet lhs, BitSet rhs) {
		for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
			if (!rhs.get(i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * This selects a root to mutate and does so.
	 *
	 * @return
	 */
	private ArrayList<TinyBoyInputSequence> selectAndMutate(TinyBoyInputSequence parent) {
		ArrayList<TinyBoyInputSequence> seqs = new ArrayList<TinyBoyInputSequence>();
		ArrayList<TinyBoyInputSequence> children = new ArrayList<TinyBoyInputSequence>();
		TinyBoyInputSequence seq = randomlyMutate(parent, n, m);

		for (Map.Entry<TinyBoyInputSequence, ArrayList<TinyBoyInputSequence>> p : parents.entrySet()) {
			seqs.add(p.getKey());
			seqs.addAll(p.getValue());
		}

		while (children.size() < noChildren) {
			seq = randomlyMutate(parent, n, m);
			if (!seqs.contains(seq))
				children.add(seq);

		}

		return children;
	}

	/**
	 * Randomly mutate a given input sequence. This will mutate exactly n input
	 * values randomly.
	 *
	 * Varying the value of n will change how aggressive the mutation is.
	 *
	 * The incoming list will not be affected and a completely new (mutated)
	 * sequence will be created.
	 *
	 * @param root
	 *            The input sequence to mutate.
	 * @param n
	 *            The number of pulses to mutate.
	 * @param m
	 *            The sides of the dice to roll for each mutation. This determines
	 *            the likelihood of a button versus null.
	 * @return
	 */
	private TinyBoyInputSequence randomlyMutate(TinyBoyInputSequence root, int n, int m) {
		TinyBoyInputSequence nRoot = new TinyBoyInputSequence(root);
		final int size = PULSE_COUNT;
		for (int i = 0; i != size; ++i) {
			int index = random.nextInt(size - i);
			if (index < n) {
				// Perform a mutation
				ControlPad.Button b = getRandomButton(m);
				nRoot.setPulse(i, b);
				n = n - 1;
			}
		}
		return nRoot;
	}

	/**
	 * Get a random control pad button, or null (to indicate no button should be
	 * pushed). The <code>m</code> parameter indicates the total size of the dice.
	 * That is, we have number of buttons + m gives the sides of the dice. If a
	 * number comes up which is not a control pad button, then just return null.
	 * Hence, we can control the probability of a button versus null.
	 *
	 * @param m
	 * @return
	 */
	private ControlPad.Button getRandomButton(int m) {
		int numButtons = ControlPad.Button.values().length;
		int roll = random.nextInt(m);
		if (roll >= numButtons) {
			// We've rolled into a gap.
			return null;
		} else {
			// We've rolled into a button.
			return ControlPad.Button.values()[roll];
		}

	}

	private final static class Pair {
		public final TinyBoyInputSequence input;
		public final BitSet output;

		public Pair(TinyBoyInputSequence input, BitSet output) {
			this.input = input;
			this.output = output;
		}
	}
}
