package org.netway.dongnehankki.global.exception.user;

import org.netway.dongnehankki.global.exception.CustomException;
import org.netway.dongnehankki.global.exception.ErrorCode;

public class DuplicateUserNameException extends CustomException {

    public DuplicateUserNameException() {
        super(ErrorCode.DUPLICATE_USERNAME);
    }
}
