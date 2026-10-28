package be.duft.easier_household.bll.exceptions;

/**
 * La requête est bien formée mais métierement invalide (renvoyée en 400 Bad Request).
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
