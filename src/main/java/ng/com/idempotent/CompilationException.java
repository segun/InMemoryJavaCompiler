package ng.com.idempotent;

public final class CompilationException extends RuntimeException {
	private static final long serialVersionUID = 5272588827551900536L;

	public CompilationException(String msg) {
		super(msg);
	}

}