package org.netway.dongnehankki.global.exception.user;

import org.netway.dongnehankki.global.exception.CustomException;
import org.netway.dongnehankki.global.exception.ErrorCode;

public class UnregisteredStoreException extends CustomException {

    public UnregisteredStoreException() {
        super(ErrorCode.UNREGISTERED_STORE);
    }
}
