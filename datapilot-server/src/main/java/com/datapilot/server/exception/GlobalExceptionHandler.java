package com.datapilot.server.exception;

import com.datapilot.common.exception.BusinessException;
import com.datapilot.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log= LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     *业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e){
        log.warn("业务异常：{}",e.getMessage());
        return Result.error(e.getMessage());
    }


    /**
     *未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e){
        log.error("系统异常",e);
        return Result.error("系统异常，请稍后再试");
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        String message=e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("参数校验异常：{}",message);
        return Result.error(message);
    }
}
