package com.example.kuwalog.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatusException(ResponseStatusException ex, Model model) {
        model.addAttribute("message", ex.getReason());

        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            return "error/404";
        }
        if (ex.getStatusCode() == HttpStatus.FORBIDDEN) {
            return "error/403";
        }
        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Model model) {
        model.addAttribute("message", null);
        return "error/500";
    }
}
