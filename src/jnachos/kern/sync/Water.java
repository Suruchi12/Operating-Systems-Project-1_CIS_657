/**
 *  <MakeWater Problem>
 *  Author: Jae C. Oh
 *  
 *  Created by Patrick McSweeney on 12/17/08.
 */
package jnachos.kern.sync;

import java.io.*;
import jnachos.kern.*;

/**
 *
 */
public class Water {

	/** Semaphore H */
	static Semaphore H = new Semaphore("SemH", 0);

	/**	*/
	static Semaphore O = new Semaphore("SemO", 0);

	/**	*/
	static Semaphore wait = new Semaphore("wait", 0);

	/**	*/
	static Semaphore mutex = new Semaphore("MUTEX", 1);

	/**	*/
	static Semaphore mutex1 = new Semaphore("MUTEX1", 1);

	/**	*/
	static long count = 0;

	/**	*/
	static int Hcount, Ocount, nH, nO;

	/**	*/
	class HAtom implements VoidFunctionPtr {
		int mID;

		/**
		 *
		 */
		public HAtom(int id) {
			mID = id;
		}

		/**
		 * oAtom will call oReady. When this atom is used, do continuous
		 * "Yielding" - preserving resource
		 */
		public void call(Object pDummy) {
			mutex.P();
			if (count % 2 == 0) // first H atom
			{
				count++; // increment counter for the first H
				mutex.V(); // Critical section ended
				H.P(); // Waiting for the second H atom
			} else // second H atom
			{
				count++; // increment count for next first H
				mutex.V(); // Critical section ended
				H.V(); // wake up the first H atom
				O.V(); // wake up O atom
			}

			wait.P(); // wait for water message done

			System.out.println("H atom #" + mID + " used in making water.");
		}
	}

	/**	*/
	class OAtom implements VoidFunctionPtr {
		int mID;

		/**
		 * oAtom will call oReady. When this atom is used, do continuous
		 * "Yielding" - preserving resource
		 */
		public OAtom(int id) {
			mID = id;
		}

		/**
		 * oAtom will call oReady. When this atom is used, do continuous
		 * "Yielding" - preserving resource
		 */
		public void call(Object pDummy) {

			O.P(); // waiting for two H atoms.
			makeWater();
			wait.V(); // wake up H atoms and they will return to
			wait.V(); // resource pool
			mutex1.P();
			Hcount = Hcount - 2;
			Ocount--;
			System.out.println("Numbers Left: H Atoms: " + Hcount + ", O Atoms: " + Ocount);
			System.out.println("Numbers Used: H Atoms: " + (nH - Hcount) + ", O Atoms: " + (nO - Ocount));
			mutex1.V();
			System.out.println("O atom #" + mID + " used in making water.");
		}
	}

	/**
	 * oAtom will call oReady. When this atom is used, do continuous "Yielding"
	 * - preserving resource
	 */
	public static void makeWater() {
		System.out.println("** Water made! Splash!! **");
	}

	/**
	 * oAtom will call oReady. When this atom is used, do continuous "Yielding"
	 * - preserving resource
	 */
	public Water() {
		runWater();
	}

	/**
	 *
	 */
	public void runWater() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Number of H atoms ? ");
			nH = (new Integer(reader.readLine())).intValue();
			System.out.println("Number of O atoms ? ");
			nO = (new Integer(reader.readLine())).intValue();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Hcount = nH;
		Ocount = nO;

		for (int i = 0; i < nH; i++) {
			HAtom atom = new HAtom(i);
			(new NachosProcess(new String("hAtom" + i))).fork(atom, null);
		}

		for (int j = 0; j < nO; j++) {
			OAtom atom = new OAtom(j);
			(new NachosProcess(new String("oAtom" + j))).fork(atom, null);
		}
	}
}
