package be.duft.easier_household.bll.exceptions;

/**
 * La ressource demandée entre en conflit avec une ressource existante (renvoyée en 409 Conflict).
 */
public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
