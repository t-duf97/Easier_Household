package be.duft.easier_household.bll.exceptions;

/**
 * Identifiants de connexion refusés (renvoyée en 401 Unauthorized). Le message est le même que l'email
 * soit inconnu ou le mot de passe faux, pour ne pas révéler quels comptes existent.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
