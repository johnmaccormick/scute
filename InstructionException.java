public class InstructionException extends Exception {
	private static final long serialVersionUID = 2560147743100772504L;

	public static final int DEFAULT = 0;
	public static final int UNEXPECTED_OPCODE = 1;

	private int errorNum = DEFAULT;

	public InstructionException(String message) {
		super(message);
	}

	public InstructionException(int errorNum, String message) {
		super(message);
		this.errorNum = errorNum;
	}

	public int getErrorNum() {
		return errorNum;
	}
	
	
}
