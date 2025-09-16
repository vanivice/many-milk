package com.application.manymilk.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex,
                                        HttpServletRequest request,
                                        RedirectAttributes redirectAttributes) {
        log.warn("Ошибка валидации: {}", ex.getMessage());

        // Сохраняем flash attribute
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());

        // Редиректим обратно на форму редактирования
        String referer = request.getHeader("Referer");
        if (referer == null) {
            referer = "/clients"; // fallback
        }
        return "redirect:" + referer;
    }
}
