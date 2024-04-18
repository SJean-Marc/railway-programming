package fr.railway.shared;

public class ExpectedFailedReason extends Reason {
    private final String failedReason;

    public ExpectedFailedReason(String failedReason) {
        this.failedReason = failedReason;
    }

    @Override
    public String failedReason() {
        return failedReason;
    }

}
