package org.luckyjourney.exception;

import org.luckyjourney.util.R;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-27 19:19
 */
@RestController
@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(Exception.class)
    public R ex(Exception e){
        e.printStackTrace();
        return R.error().message(e.toString());
    }
}
