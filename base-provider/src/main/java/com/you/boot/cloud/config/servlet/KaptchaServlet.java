package com.you.boot.cloud.config.servlet;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.you.base.BaseResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shicz
 */
public class KaptchaServlet extends HttpServlet {



    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String widthStr = "width";
        String heightStr = "height";
        String codeCountStr = "codeCount";
        String lineCountStr = "lineCount";
        response.setContentType("application/json; charset=UTF-8");
        String content =  IoUtil.read(request.getInputStream(),"UTF-8");
        int width = 150;
        int height = 32;
        int codeCount = 4;
        int lineCount = 4;
        if (StrUtil.isNotEmpty(content)){
            JSONObject jsonObject = JSONObject.parseObject(content);
            if (jsonObject.containsKey(widthStr)){
                width = jsonObject.getInteger(widthStr);
            }
            if (jsonObject.containsKey(heightStr)){
                height = jsonObject.getInteger(heightStr);
            }
            if (jsonObject.containsKey(codeCountStr)){
                codeCount = jsonObject.getInteger(codeCountStr);
            }
            if (jsonObject.containsKey(lineCountStr)){
                lineCount = jsonObject.getInteger(lineCountStr);
            }
        }
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(width, height,codeCount,lineCount);
        String code = Base64.encode(lineCaptcha.getCode());
        String image = lineCaptcha.getImageBase64();
        Map<String,String> map = new HashMap<String,String>(16);
        map.put("code",code);
        map.put("image",image);
        BaseResponse baseResponse =  BaseResponse.successResult(map);
        PrintWriter out = response.getWriter();
        out.write(JSONObject.toJSONString(baseResponse));
    }
}
