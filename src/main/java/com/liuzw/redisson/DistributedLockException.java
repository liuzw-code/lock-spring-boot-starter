package com.liuzw.redisson;

/**
 * 自定义异常
 *
 * @author liuzw
 */
public class DistributedLockException extends RuntimeException {
    private static final long serialVersionUID = 6610083281801529147L;

    public DistributedLockException(String message) {
        super(message);
    }
}