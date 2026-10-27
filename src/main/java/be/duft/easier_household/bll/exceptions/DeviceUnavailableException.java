package be.duft.easier_household.bll.exceptions;

public class DeviceUnavailableException extends RuntimeException {
    public DeviceUnavailableException(String message) {
        super(message);
    }
}
