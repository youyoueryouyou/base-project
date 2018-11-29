package com.you.boot.cloud.config;

import com.you.boot.cloud.config.filter.XssFilter;
import com.you.boot.cloud.config.listener.ServerListener;
import com.you.boot.cloud.config.servlet.KaptchaServlet;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.interceptor.*;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.*;

/**
 * @author shicz
 */
@Configuration
    public class WebConfig implements WebMvcConfigurer
{
    @Autowired
    private DataSourceTransactionManager transactionManager;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new TestInterceptor());
    }

    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverters()
    {
        FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat);
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        List<MediaType> fastMediaTypes = new ArrayList<MediaType>();
        fastMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        fastJsonHttpMessageConverter.setFastJsonConfig(fastJsonConfig);
        fastJsonHttpMessageConverter.setSupportedMediaTypes(fastMediaTypes);
        HttpMessageConverter converter = fastJsonHttpMessageConverter;
         return new HttpMessageConverters(converter);
    }


    /**
     * //@Bean
     * */
    public ServletRegistrationBean kaptchaServletBean() {
        return new ServletRegistrationBean(new KaptchaServlet(),"/kaptcha");
    }

    @Bean
    public FilterRegistrationBean charsetFilter() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter("utf-8",true);
        registrationBean.setFilter(characterEncodingFilter);
        List<String> urls = new ArrayList<>();
        urls.add("/*");
        registrationBean.setUrlPatterns(urls);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean xssFilter() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        XssFilter xssFilter = new XssFilter();
        registrationBean.setFilter(xssFilter);
        List<String> urls = new ArrayList<>();
        urls.add("/*");
        registrationBean.setUrlPatterns(urls);
        return registrationBean;
    }

    /**
     * //@Bean
     * */
    public ServletListenerRegistrationBean<ServerListener> serverListenerBean() {
        return new ServletListenerRegistrationBean<ServerListener>(new ServerListener());
    }

    @Bean
    public ServletRegistrationBean dispatcherRegistration(DispatcherServlet dispatcherServlet) {
        ServletRegistrationBean registration = new ServletRegistrationBean(
                dispatcherServlet);
        dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);
        return registration;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //registry.addMapping("/**").allowedOrigins("http://localhost:8080").allowedMethods("POST", "GET").allowCredentials(true);
    }


    @Bean(name = "txAdvice")
    public TransactionInterceptor getAdvisor() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("add*", "PROPAGATION_REQUIRED,-Exception");
        properties.setProperty("insert*", "PROPAGATION_REQUIRED,-Exception");
        properties.setProperty("save*", "PROPAGATION_REQUIRED,-Exception");
        properties.setProperty("update*", "PROPAGATION_REQUIRED,-Exception");
        properties.setProperty("modify*", "PROPAGATION_REQUIRED,-Exception");
        properties.setProperty("del*", "PROPAGATION_REQUIRED,-Exception");
        properties.setProperty("remove*", "PROPAGATION_REQUIRED,-Exception");
        properties.setProperty("get*", "PROPAGATION_REQUIRED,-Exception,readOnly");
        properties.setProperty("query*", "PROPAGATION_REQUIRED,-Exception,readOnly");
        properties.setProperty("select*", "PROPAGATION_REQUIRED,-Exception,readOnly");
        properties.setProperty("find*", "PROPAGATION_REQUIRED,-Exception,readOnly");
        properties.setProperty("*", "PROPAGATION_REQUIRED,-Exception");
        TransactionInterceptor tsi = new TransactionInterceptor(transactionManager,properties);
        return tsi;
    }

    @Bean
    public BeanNameAutoProxyCreator txProxy() {
        BeanNameAutoProxyCreator creator = new BeanNameAutoProxyCreator();
        creator.setInterceptorNames("txAdvice");
        creator.setBeanNames("*ServiceImpl");
        creator.setProxyTargetClass(true);
        return creator;
    }
}
