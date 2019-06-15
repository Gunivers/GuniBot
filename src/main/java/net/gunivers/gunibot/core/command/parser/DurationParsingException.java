package net.gunivers.gunibot.core.command.parser;

public class DurationParsingException extends ObjectParsingException {

	private static final long serialVersionUID = 1L;

	public DurationParsingException(String message) {
		super(message);
	}

	public DurationParsingException(Throwable cause) {
		super(cause);
	}

	public DurationParsingException(String message, Throwable cause) {
		super(message, cause);
	}

}
