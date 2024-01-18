package com.prj.chatgpt.interfaces;

import com.prj.chatgpt.application.IWeiXinValidateService;
import com.prj.chatgpt.common.Constants;
import com.prj.chatgpt.domain.chat.ChatCompletionRequest;
import com.prj.chatgpt.domain.chat.ChatCompletionResponse;
import com.prj.chatgpt.domain.chat.Message;
import com.prj.chatgpt.domain.receive.model.MessageTextEntity;
import com.prj.chatgpt.infrastructure.util.XmlUtil;
import com.prj.chatgpt.session.Configuration;
import com.prj.chatgpt.session.OpenAiSession;
import com.prj.chatgpt.session.OpenAiSessionFactory;
import com.prj.chatgpt.session.defaults.DefaultOpenAiSessionFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @descriptrion: Connect to Wechat official account, deal with the request service
 */
@RestController
@RequestMapping("/wx/portal/{appid}")
public class WeiXinPortalController {

    private Logger logger = LoggerFactory.getLogger(WeiXinPortalController.class);

    @Value("${wx.config.originalid:gh_c5f3aefcb7c7}")
    private String originalId;

    @Resource
    private IWeiXinValidateService weiXinValidateService;

    //Upgraded here
    private final OpenAiSession openAiSession;

    @Resource
    private ThreadPoolTaskExecutor taskExecutor;

    //Upgraded here
    //private Map<String, String> chatGPTMap = new ConcurrentHashMap<>();
    // Store the return data of OpenAi
    private final Map<String, String> openAiDataMap = new ConcurrentHashMap<>();
    // Store the invoke times of OpenAi
    private final Map<String, Integer> openAiRetryCountMap = new ConcurrentHashMap<>();

    public WeiXinPortalController() {
        // 1.Configuration file:
        Configuration configuration = new Configuration();
        configuration.setApiHost("https://api.openai.com/");
        configuration.setApiKey("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");
        // 测试时候，需要先获得授权token：http://api.xfg.im:8080/authorize?username=xfg&password=123 - 此地址暂时有效，后续根据课程首页说明获取token；https://t.zsxq.com/0d3o5FKvc
        //configuration.setAuthToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ4ZmciLCJleHAiOjE2ODMyODE2NzEsImlhdCI6MTY4MzI3ODA3MSwianRpIjoiMWUzZTkwYjYtY2UyNy00NzNlLTk5ZTYtYWQzMWU1MGVkNWE4IiwidXNlcm5hbWUiOiJ4ZmcifQ.YgQRJ2U5-9uydtd6Wbkg2YatsoX-y8mS_OJ3FdNRaX0");
//        configuration.setApiHost("https://api.xfg.im/b8b6/");
//        configuration.setApiKey("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");
        // 2. Session factory
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);
        // 3. Open the session
        this.openAiSession = factory.openSession();
        logger.info("Start openAiSession");
    }

    /**
     * Process the get request sent from the WeChat server and verify the signature
     * http://chatgpt-wechat.cpolar.top/wx/portal/wxcd4029157fc00b47
     * (domain_name_in_intranet_penetrate/wx/portal/{appid})
     * <p>
     * appid WeChat AppID
     * signature The signature sent from WeChat
     * timestamp timestamp sent by WeChat
     * nonce random string sent from WeChat
     * echostr Verification string sent from WeChat
     */
    @GetMapping(produces = "text/plain;charset=utf-8")
    public String validate(@PathVariable String appid,
                           @RequestParam(value = "signature", required = false) String signature,
                           @RequestParam(value = "timestamp", required = false) String timestamp,
                           @RequestParam(value = "nonce", required = false) String nonce,
                           @RequestParam(value = "echostr", required = false) String echostr) {
        try {
            logger.info("WeChat official account verification signature information{}Start [{}, {}, {}, {}]", appid, signature, timestamp, nonce, echostr);
            if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
                throw new IllegalArgumentException("Illegal request parameters, please verify!");
            }
            boolean check = weiXinValidateService.checkSign(signature, timestamp, nonce);
            logger.info("WeChat official account verification signature information{}Finished check：{}", appid, check);
            if (!check) {
                return null;
            }
            return echostr;
        } catch (Exception e) {
            logger.error("WeChat official account verification signature information{}Failed [{}, {}, {}, {}]", appid, signature, timestamp, nonce, echostr, e);
            return null;
        }
    }

    public void doChatGPTTask(String content) {
        //Upgraded
        //chatGPTMap.put(content, "NULL");
        openAiDataMap.put(content, "NULL");
        taskExecutor.execute(() -> {
            // OpenAI request
            // 1. Create parameter
            ChatCompletionRequest chatCompletion = ChatCompletionRequest
                    .builder()
                    .messages(Collections.singletonList(Message.builder().role(Constants.Role.USER).content(content).build()))
                    .model(ChatCompletionRequest.Model.GPT_3_5_TURBO.getCode())
                    .build();
            //Upgraded
            // 2. Make a request
//            ChatCompletionResponse chatCompletionResponse = openAiSession.completions(chatCompletion);
            // 2. Make a request asynchronously
            CompletableFuture<ChatCompletionResponse> future = CompletableFuture.supplyAsync(() -> openAiSession.completions(chatCompletion));

            //Upgraded
            /*
            // 3. Parse result
            StringBuilder messages = new StringBuilder();
            chatCompletionResponse.getChoices().forEach(e -> {
                messages.append(e.getMessage().getContent());
            });
            chatGPTMap.put(content, messages.toString());
             */
            // 3. Parse result asynchronously
            future.thenAccept(chatCompletionResponse -> {
                StringBuilder messages = new StringBuilder();
                chatCompletionResponse.getChoices().forEach(e -> {
                    messages.append(e.getMessage().getContent());
                });
                openAiDataMap.put(content, messages.toString());
            }).exceptionally(e -> {
                // Handle exceptions
                e.printStackTrace();
                // Update retry count
                openAiRetryCountMap.merge(content, 1, Integer::sum);
                return null;
            });
        });
    }

    /**
     * Here processes message forwarding from the WeChat server
     */
    @PostMapping(produces = "application/xml; charset=UTF-8")
    public String post(@PathVariable String appid,
                       @RequestBody String requestBody,
                       @RequestParam("signature") String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce,
                       @RequestParam("openid") String openid,
                       @RequestParam(name = "encrypt_type", required = false) String encType,
                       @RequestParam(name = "msg_signature", required = false) String msgSignature) {
        try {
            logger.info("Receive WeChat public account information requests{}Start {}", openid, requestBody);
            MessageTextEntity message = XmlUtil.xmlToBean(requestBody, MessageTextEntity.class);
            //Upgraded
            logger.info("Request times：{}", null == openAiRetryCountMap.get(message.getContent().trim()) ? 1 : openAiRetryCountMap.get(message.getContent().trim()));
            /*
            // Asynchronous tasks
            if (chatGPTMap.get(message.getContent().trim()) == null || "NULL".equals(chatGPTMap.get(message.getContent().trim()))) {
                // Feedback message [text]
                MessageTextEntity res = new MessageTextEntity();
                res.setToUserName(openid);
                res.setFromUserName(originalId);
                res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
                res.setMsgType("text");
                res.setContent("The message is being processed, please reply to me again【" + message.getContent().trim() + "】");
                if (chatGPTMap.get(message.getContent().trim()) == null) {
                    doChatGPTTask(message.getContent().trim());
                }

                return XmlUtil.beanToXml(res);
            }
             */
            // Asynchronous tasks [add timeout retry, for small-volume call feedback, the result can be returned within the valid number of retries]
            if (openAiDataMap.get(message.getContent().trim()) == null || "NULL".equals(openAiDataMap.get(message.getContent().trim()))) {
                String data = "Message processing, please sent me again with【" + message.getContent().trim() + "】";
                // Sleep for waiting
                Integer retryCount = openAiRetryCountMap.get(message.getContent().trim());
                if (null == retryCount) {
                    if (openAiDataMap.get(message.getContent().trim()) == null) {
                        doChatGPTTask(message.getContent().trim());
                    }
                    logger.info("Time out and resend：{}", 1);
                    openAiRetryCountMap.put(message.getContent().trim(), 1);
                    TimeUnit.SECONDS.sleep(5);
                    new CountDownLatch(1).await();
                } else if (retryCount < 2) {
                    retryCount = retryCount + 1;
                    logger.info("Time out and resend：{}", retryCount);
                    openAiRetryCountMap.put(message.getContent().trim(), retryCount);
                    TimeUnit.SECONDS.sleep(5);
                    new CountDownLatch(1).await();
                } else {
                    retryCount = retryCount + 1;
                    logger.info("Time out and resend：{}", retryCount);
                    openAiRetryCountMap.put(message.getContent().trim(), retryCount);
                    TimeUnit.SECONDS.sleep(3);
                    if (openAiDataMap.get(message.getContent().trim()) != null && !"NULL".equals(openAiDataMap.get(message.getContent().trim()))) {
                        data = openAiDataMap.get(message.getContent().trim());
                    }
                }

                // Feedback message [text]
                MessageTextEntity res = new MessageTextEntity();
                res.setToUserName(openid);
                res.setFromUserName(originalId);
                res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
                res.setMsgType("text");
                res.setContent(data);

                return XmlUtil.beanToXml(res);
            }

            // Feedback message [text]
            MessageTextEntity res = new MessageTextEntity();
            res.setToUserName(openid);
            res.setFromUserName(originalId);
            res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
            res.setMsgType("text");
            res.setContent(openAiDataMap.get(message.getContent().trim()));
            String result = XmlUtil.beanToXml(res);
            logger.info("Receive WeChat public account information requests{}Finished {}", openid, result);
            openAiDataMap.remove(message.getContent().trim());
            return result;
        } catch (Exception e) {
            logger.error("Receive WeChat public account information requests{}Failed {}", openid, requestBody, e);
            return "";
        }
    }

}
