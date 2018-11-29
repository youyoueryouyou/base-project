package com.you.boot.cloud.config.exception;

import com.you.base.BaseResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.sql.SQLException;

/**
 * @author shicz
 */
@ControllerAdvice
public class GlobalDefaultExceptionHandler {
    /**
     * 处理 Exception 类型的异常
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public BaseResponse defaultExceptionHandler(Exception e) {
        e.printStackTrace();
        return BaseResponse.failureMessage(e.toString());
    }

    @ExceptionHandler(SQLException.class)
    @ResponseBody
    public BaseResponse sqlExceptionHandler(Exception e) {
        e.printStackTrace();
        return BaseResponse.failureMessage("sqlException");
    }

    @ExceptionHandler(value = NoHandlerFoundException.class)
    @ResponseBody
    public BaseResponse noFoundHandler(Exception e) throws Exception {
        return BaseResponse.failureMessage(e.toString());
    }
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public BaseResponse notSupportedErrorHandler(Exception e) throws Exception {
        return BaseResponse.failureMessage(e.toString());
    }
}
