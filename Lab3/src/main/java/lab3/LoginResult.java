package lab3;

/** Immutable message passed from a worker to a login task and then to the UI. */
public record LoginResult(Status status, int attemptsLeft, long secondsLeft) {
    public enum Status { SUCCESS, ERROR, BLOCKED }
    public static LoginResult success() { return new LoginResult(Status.SUCCESS, 0, 0); }
    public static LoginResult error(int left) { return new LoginResult(Status.ERROR, left, 0); }
    public static LoginResult blocked(long seconds) { return new LoginResult(Status.BLOCKED, 0, seconds); }
}
