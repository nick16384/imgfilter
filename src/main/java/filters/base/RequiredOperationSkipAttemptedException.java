package filters.base;

import java.util.concurrent.ExecutionException;

public class RequiredOperationSkipAttemptedException extends ExecutionException {
	public RequiredOperationSkipAttemptedException(String message) {
		super(message);
	}
}
