package net.gunivers.gunibot.datas.serialize;

public class SerializationIdConflictException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String conflictedId;

	public SerializationIdConflictException(Serializer serializer) {
		conflictedId = serializer.systemId;
	}

	public SerializationIdConflictException(String message, Serializer serializer) {
		super(message);
		conflictedId = serializer.systemId;
	}

	public String getConflictedId() {
		return conflictedId;
	}

}
