package fr.railway.shared;

import org.apache.commons.lang3.StringUtils;

public abstract class Reason {
    public abstract String failedReason();

    public boolean hasMessage() {
        return StringUtils.isNotBlank(failedReason());
    }

    @Override
    public String toString() {
        return failedReason();
    }
}
