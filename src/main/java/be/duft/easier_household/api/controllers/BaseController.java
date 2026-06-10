package be.duft.easier_household.api.controllers;

import be.duft.easier_household.dl.entities.User;
import be.duft.easier_household.exceptions.UnauthorizedAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class BaseController {

//    protected Long getCurrentUserId() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new UnauthorizedAccessException("No authenticated user found");
//        }
//        // On récupère le name (= le subject du JWT = l'userId)
//        return Long.parseLong(authentication.getName());
//    }

    protected Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedAccessException("No authenticated user found");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user.getId();
        }
        throw new UnauthorizedAccessException("No authenticated user found");
    }
}
