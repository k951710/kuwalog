package com.example.kuwalog.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatusException(ResponseStatusException ex, Model model) {
        log.warn("ResponseStatusException: {} {}", ex.getStatusCode(), ex.getReason());
        model.addAttribute("message", ex.getReason());

        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            return "error/404";
        }
        if (ex.getStatusCode() == HttpStatus.FORBIDDEN) {
            return "error/403";
        }
        return "error/500";
    }

    @ExceptionHandler({NoResourceFoundException.class, MethodArgumentTypeMismatchException.class})
    public String handleNoResource(Model model) {
        model.addAttribute("message", null);
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
        log.error("Unhandled exception", ex);
        model.addAttribute("message", null);
        return "error/500";
    }
}
