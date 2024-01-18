package com.prj.chatgpt.application;

/**
 * @description: Supply the authentication service for wechat official account.
 */
public interface IWeiXinValidateService {
    boolean checkSign(String signature, String timestamp, String nonce);
}
