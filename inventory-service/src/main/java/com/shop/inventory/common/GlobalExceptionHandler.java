package com.shop.inventory.common;

import com.shop.inventory.stock.InventoryNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InventoryNotFound.class)
    public ProblemDetail handleInventoryNotFound(InventoryNotFound ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Inventory not found");
        problem.setDetail(ex.getMessage());
        return problem;
    }

}
