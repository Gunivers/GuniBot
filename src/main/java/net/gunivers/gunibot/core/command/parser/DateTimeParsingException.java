package net.gunivers.gunibot.core.command.parser;

public class DateTimeParsingException extends ObjectParsingException {

	private static final long serialVersionUID = 1L;

	public DateTimeParsingException(String message) {
		super(message);
	}

	public DateTimeParsingException(Throwable cause) {
		super(cause);
	}

	public DateTimeParsingException(String message, Throwable cause) {
		super(message, cause);
	}

}
