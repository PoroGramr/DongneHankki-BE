package org.netway.dongnehankki.global.exception.user;

import org.netway.dongnehankki.global.exception.CustomException;
import org.netway.dongnehankki.global.exception.ErrorCode;

public class UnregisteredUserException extends CustomException {

    public UnregisteredUserException() {
        super(ErrorCode.UNREGISTERED_USER);
    }
}
