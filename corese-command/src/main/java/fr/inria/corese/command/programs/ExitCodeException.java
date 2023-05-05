package fr.inria.corese.command.programs;

import java.util.concurrent.Callable;

public class ExitCodeException extends RuntimeException implements Callable<Integer> {
    private final int exitCode;

    public ExitCodeException(int exitCode, String message) {
        super(message);
        this.exitCode = exitCode;
    }

    @Override
    public Integer call() {
        return exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}
