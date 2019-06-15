package net.gunivers.gunibot.core.command.parser;

public class TimeParsingException extends ObjectParsingException {

	private static final long serialVersionUID = 1L;

	public TimeParsingException(String message, Throwable cause) {
		super(message, cause);
	}

	public TimeParsingException(String message) {
		super(message);
	}

	public TimeParsingException(Throwable cause) {
		super(cause);
	}

}
