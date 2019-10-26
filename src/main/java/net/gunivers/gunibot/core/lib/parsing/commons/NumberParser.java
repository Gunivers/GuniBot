package net.gunivers.gunibot.core.lib.parsing.commons;

import net.gunivers.gunibot.core.lib.parsing.Parser;
import net.gunivers.gunibot.core.lib.parsing.ParsingException;

public abstract class NumberParser<N extends Number> implements Parser<N>
{
	protected N min;
	protected N max;

	public NumberParser() {}
	public NumberParser(N min) { this.min = min; }
	public NumberParser(N min, N max)
	{
		this.min = min;
		this.max = max;
	}

	protected abstract N rawParse(String input) throws NumberFormatException;

	@Override
	public N parse(String input) throws ParsingException
	{
		try
		{
			N number = this.rawParse(input);

			if (this.min != null && number.doubleValue() < this.min.doubleValue())
				throw new ParsingException("The number "+ input +" should be greater than "+ this.min);

			if (this.max != null && number.doubleValue() > this.max.doubleValue())
				throw new ParsingException("The number "+ input +" should be lesser than "+ this.max);

			return number;
		}
		catch (NumberFormatException e) { throw new ParsingException(e.getMessage()); }
	}

	public N getMin() { return this.min; }
	public N getMax() { return this.max; }

	public static class ByteParser extends NumberParser<Byte>
	{
		public ByteParser() { super(); }
		public ByteParser(byte min) { super(min); }
		public ByteParser(byte min, byte max) { super(min, max); }

		@Override protected Byte rawParse(String input) throws NumberFormatException { return Byte.parseByte(input); }
	}

	public static class ShortParser extends NumberParser<Short>
	{
		public ShortParser() { super(); }
		public ShortParser(short min) { super(min); }
		public ShortParser(short min, short max) { super(min, max); }

		@Override protected Short rawParse(String input) throws NumberFormatException { return Short.parseShort(input); }
	}

	public static class IntegerParser extends NumberParser<Integer>
	{
		public IntegerParser() { super(); }
		public IntegerParser(int min) { super(min); }
		public IntegerParser(int min, int max) { super(min, max); }

		@Override protected Integer rawParse(String input) throws NumberFormatException { return Integer.parseInt(input); }
	}

	public static class LongParser extends NumberParser<Long>
	{
		public LongParser() { super(); }
		public LongParser(long min) { super(min); }
		public LongParser(long min, long max) { super(min, max); }

		@Override protected Long rawParse(String input) throws NumberFormatException { return Long.parseLong(input); }
	}

	public static class FloatParser extends NumberParser<Float>
	{
		public FloatParser() { super(); }
		public FloatParser(float min) { super(min); }
		public FloatParser(float min, float max) { super(min, max); }

		@Override protected Float rawParse(String input) throws NumberFormatException { return Float.parseFloat(input); }
	}

	public static class DoubleParser extends NumberParser<Double>
	{
		public DoubleParser() { super(); }
		public DoubleParser(double min) { super(min); }
		public DoubleParser(double min, double max) { super(min, max); }

		@Override protected Double rawParse(String input) throws NumberFormatException { return Double.parseDouble(input); }
	}
}
