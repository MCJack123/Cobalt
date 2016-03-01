/**
 * ****************************************************************************
 * Copyright (c) 2009 Luaj.org. All rights reserved.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ****************************************************************************
 */
package org.squiddev.cobalt.lib;


import org.squiddev.cobalt.lib.jse.JsePlatform;
import org.squiddev.cobalt.*;

import java.util.Random;

import static org.squiddev.cobalt.Factory.valueOf;
import static org.squiddev.cobalt.Factory.varargsOf;

/**
 * Subclass of {@link LibFunction} which implements the lua standard {@code math}
 * library.
 * This has been implemented to match as closely as possible the behavior in the corresponding library in C.
 *
 * @see LibFunction
 * @see JsePlatform
 * @see <a href="http://www.lua.org/manual/5.1/manual.html#5.6">http://www.lua.org/manual/5.1/manual.html#5.6</a>
 */
public class MathLib extends OneArgFunction {
	private Random random;

	@Override
	public LuaValue call(LuaState state, LuaValue arg) {
		LuaTable t = new LuaTable(0, 30);
		t.set(state, "pi", Factory.valueOf(Math.PI));
		t.set(state, "huge", LuaDouble.POSINF);
		bind(state, t, MathLib1.class, new String[]{
			"abs", "ceil", "cos", "deg",
			"exp", "floor", "rad", "sin",
			"sqrt", "tan",
			"acos", "asin", "atan", "cosh",
			"exp", "log", "log10", "sinh",
			"tanh"
		});
		bind(state, t, MathLib2.class, new String[]{
			"fmod", "ldexp", "pow", "atan2"
		});
		bind(state, t, MathLibV.class, new String[]{
			"frexp", "max", "min", "modf",
			"randomseed", "random",});
		((MathLibV) t.get(state, "randomseed")).mathlib = this;
		((MathLibV) t.get(state, "random")).mathlib = this;
		env.set(state, "math", t);
		state.loadedPackages.set(state, "math", t);
		return t;
	}

	private static final class MathLib1 extends OneArgFunction {
		@Override
		public LuaValue call(LuaState state, LuaValue arg) {
			switch (opcode) {
				case 0:
					return valueOf(Math.abs(arg.checkdouble()));
				case 1:
					return Factory.valueOf(Math.ceil(arg.checkdouble()));
				case 2:
					return Factory.valueOf(Math.cos(arg.checkdouble()));
				case 3:
					return Factory.valueOf(Math.toDegrees(arg.checkdouble()));
				case 4:
					return Factory.valueOf(Math.exp(arg.checkdouble()));
				case 5:
					return Factory.valueOf(Math.floor(arg.checkdouble()));
				case 6:
					return Factory.valueOf(Math.toRadians(arg.checkdouble()));
				case 7:
					return Factory.valueOf(Math.sin(arg.checkdouble()));
				case 8:
					return Factory.valueOf(Math.sqrt(arg.checkdouble()));
				case 9:
					return Factory.valueOf(Math.tan(arg.checkdouble()));
				case 10:
					return Factory.valueOf(Math.acos(arg.checkdouble()));
				case 11:
					return Factory.valueOf(Math.asin(arg.checkdouble()));
				case 12:
					return Factory.valueOf(Math.atan(arg.checkdouble()));
				case 13:
					return Factory.valueOf(Math.cosh(arg.checkdouble()));
				case 14:
					return Factory.valueOf(Math.exp(arg.checkdouble()));
				case 15:
					return Factory.valueOf(Math.log(arg.checkdouble()));
				case 16:
					return Factory.valueOf(Math.log10(arg.checkdouble()));
				case 17:
					return Factory.valueOf(Math.sinh(arg.checkdouble()));
				case 18:
					return Factory.valueOf(Math.tanh(arg.checkdouble()));
			}
			return Constants.NIL;
		}
	}

	private static final class MathLib2 extends TwoArgFunction {
		@Override
		public LuaValue call(LuaState state, LuaValue arg1, LuaValue arg2) {
			switch (opcode) {
				case 0: { // fmod
					double x = arg1.checkdouble();
					double y = arg2.checkdouble();
					double q = x / y;
					double f = x - y * (q >= 0 ? Math.floor(q) : Math.ceil(q));
					return Factory.valueOf(f);
				}
				case 1: { // ldexp
					double x = arg1.checkdouble();
					double y = arg2.checkdouble() + 1023.5;
					long e = (long) ((0 != (1 & ((int) y))) ? Math.floor(y) : Math.ceil(y - 1));
					return Factory.valueOf(x * Double.longBitsToDouble(e << 52));
				}
				case 2:
					return Factory.valueOf(Math.pow(arg1.checkdouble(), arg2.checkdouble()));
				case 3:
					return Factory.valueOf(Math.atan2(arg1.checkdouble(), arg2.checkdouble()));
			}
			return Constants.NIL;
		}
	}

	private static final class MathLibV extends VarArgFunction {
		private MathLib mathlib;

		@Override
		public Varargs invoke(LuaState state, Varargs args) {
			switch (opcode) {
				case 0: { // frexp
					double x = args.checkdouble(1);
					if (x == 0) return varargsOf(Constants.ZERO, Constants.ZERO);
					long bits = Double.doubleToLongBits(x);
					double m = ((bits & (~(-1L << 52))) + (1L << 52)) * ((bits >= 0) ? (.5 / (1L << 52)) : (-.5 / (1L << 52)));
					double e = (((int) (bits >> 52)) & 0x7ff) - 1022;
					return varargsOf(Factory.valueOf(m), Factory.valueOf(e));
				}
				case 1: { // max
					double m = args.checkdouble(1);
					for (int i = 2, n = args.narg(); i <= n; ++i) {
						m = Math.max(m, args.checkdouble(i));
					}
					return Factory.valueOf(m);
				}
				case 2: { // min
					double m = args.checkdouble(1);
					for (int i = 2, n = args.narg(); i <= n; ++i) {
						m = Math.min(m, args.checkdouble(i));
					}
					return Factory.valueOf(m);
				}
				case 3: { // modf
					double x = args.checkdouble(1);
					double intPart = (x > 0) ? Math.floor(x) : Math.ceil(x);
					double fracPart = x - intPart;
					return varargsOf(Factory.valueOf(intPart), Factory.valueOf(fracPart));
				}
				case 4: { // randomseed
					long seed = args.checklong(1);
					mathlib.random = new Random(seed);
					return Constants.NONE;
				}
				case 5: { // random
					if (mathlib.random == null) {
						mathlib.random = new Random();
					}

					switch (args.narg()) {
						case 0:
							return Factory.valueOf(mathlib.random.nextDouble());
						case 1: {
							int m = args.checkint(1);
							if (m < 1) argError(1, "interval is empty");
							return Factory.valueOf(1 + mathlib.random.nextInt(m));
						}
						default: {
							int m = args.checkint(1);
							int n = args.checkint(2);
							if (n < m) argError(2, "interval is empty");
							return Factory.valueOf(m + mathlib.random.nextInt(n + 1 - m));
						}
					}
				}
			}
			return Constants.NONE;
		}
	}
}