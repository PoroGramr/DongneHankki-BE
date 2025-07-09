package org.netway.dongnehankki.user.exception;

import org.netway.dongnehankki.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class DuplicateUserNameException extends CustomException {
    private static final String MESSAGE = "이미 사용 중인 유저 이름입니다.";

    public DuplicateUserNameException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
