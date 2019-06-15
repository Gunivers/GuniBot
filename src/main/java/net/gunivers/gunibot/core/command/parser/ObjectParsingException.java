package net.gunivers.gunibot.core.command.parser;

public class ObjectParsingException extends Exception {

	private static final long serialVersionUID = 1L;

	public ObjectParsingException(String message) {
		super(message);
	}

	public ObjectParsingException(Throwable cause) {
		super(cause);
	}

	public ObjectParsingException(String message, Throwable cause) {
		super(message, cause);
	}

}
