package com.kasyan313.Mayak.Aspects;

import com.kasyan313.Mayak.Exceptions.ResourceNotFoundException;
import com.kasyan313.Mayak.Exceptions.UserAlreadyExistsException;
import com.kasyan313.Mayak.Exceptions.UserNotFoundException;
import org.aspectj.lang.annotation.*;
import org.springframework.core.Ordered;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;

@Component
@Aspect
public class ExceptionHandler implements Ordered {
    @Pointcut("execution(* com.kasyan313.Mayak.Services.IMessageService.*(..))")
    public void selectAllMessageServiceMethods() {

    }

    @AfterThrowing(pointcut = "selectAllMessageServiceMethods()", throwing = "e")
    public void afterThrowingNoResultExceptionInMessageService(NoResultException e) {
        throw new ResourceNotFoundException();
    }

    @Pointcut("execution(* com.kasyan313.Mayak.Services.IUserService.*(..))")
    public void selectAllUserServiceMethods() {

    }

    @AfterThrowing(pointcut = "selectAllUserServiceMethods()", throwing = "e")
    public void afterThrowingNoResultExceptionInUserService(NoResultException e) {
        throw new UserNotFoundException();
    }

    @Pointcut("execution(* com.kasyan313.Mayak.Services.IUserInfoService.*(..))")
    public void selectAllUserInfoServiceMethods() {}

    @AfterThrowing(pointcut = "selectAllUserInfoServiceMethods()", throwing = "e")
    public void afterThrowingNoResultExceptionInUserInfoService(NoResultException e) {
        throw new UserNotFoundException();
    }

    @AfterThrowing(pointcut = "selectAllUserInfoServiceMethods()", throwing = "e")
    public void afterThrowingDataIntegrityViolationExceptionInUserInfoService(DataIntegrityViolationException e) {
        throw new UserAlreadyExistsException("nickname is already used");
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
