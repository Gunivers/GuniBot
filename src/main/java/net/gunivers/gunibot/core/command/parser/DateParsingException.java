package net.gunivers.gunibot.core.command.parser;

public class DateParsingException extends ObjectParsingException {

	private static final long serialVersionUID = 1L;

	public DateParsingException(String message, Throwable cause) {
		super(message, cause);
	}

	public DateParsingException(String message) {
		super(message);
	}

	public DateParsingException(Throwable cause) {
		super(cause);
	}

}
