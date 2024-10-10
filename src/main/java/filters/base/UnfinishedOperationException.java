package filters.base;

import java.util.concurrent.ExecutionException;

public class UnfinishedOperationException extends ExecutionException {
	public UnfinishedOperationException(String message) {
		super(message);
	}
}
