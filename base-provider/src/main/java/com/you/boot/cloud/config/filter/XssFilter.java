package com.you.boot.cloud.config.filter;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * @author shicz
 */
public class XssFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        chain.doFilter(new XssHttpServletRequestWrapper((HttpServletRequest) request), response);
    }

    @Override
    public void destroy() {
    }

    private class XssHttpServletRequestWrapper extends HttpServletRequestWrapper
    {
        private String requestBody = null;

        private final String UTF8 = "utf-8";

        private Pattern scriptPattern1 = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE);
        private Pattern scriptPattern2 = Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'",Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        private Pattern scriptPattern3 = Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"",Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        private Pattern scriptPattern4 = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);
        private Pattern scriptPattern5 = Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        private Pattern scriptPattern6 = Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        private Pattern scriptPattern7 = Pattern.compile("e­xpression\\((.*?)\\)",Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        private Pattern scriptPattern8 = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
        private Pattern scriptPattern9 = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
        private Pattern scriptPattern10 = Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

        public XssHttpServletRequestWrapper(HttpServletRequest request)
        {
            super(request);
        }

        @Override
        public String getHeader(String name)
        {
            return escapeHtml(super.getHeader(name));
        }

        @Override
        public String getQueryString()
        {
            return escapeHtml(super.getQueryString());
        }


        @Override
        public String getParameter(String name)
        {
            return escapeHtml(super.getParameter(name));
        }

        @Override
        public String[] getParameterValues(String name)
        {
            String[] values = super.getParameterValues(name);
            if (values != null)
            {
                int length = values.length;
                String[] escapseValues = new String[length];
                for (int i = 0; i < length; i++)
                {
                    escapseValues[i] = escapeHtml(values[i]);
                }
                return escapseValues;
            }
            return super.getParameterValues(name);
        }
        
        @Override
        public BufferedReader getReader()
            throws IOException
        {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        }
        
        @Override
        public ServletInputStream getInputStream()
            throws IOException
        {
            if (requestBody == null)
            {
                ServletInputStream inputStream = super.getInputStream();
                requestBody = cleanXSS(readBody(inputStream));
            }
            return new CustomServletInputStream(requestBody);
        }
        
        private String escapeHtml(String value)
        {
            if (value == null || "".equals(value))
            {
                return value;
            }
            try
            {
                String encoding = super.getRequest().getCharacterEncoding();
                if (encoding == null || !"".equals(encoding))
                {
                    encoding = "ISO-8859-1";
                }
                if (!UTF8.equalsIgnoreCase(encoding))
                {
                    String temp = new String(value.getBytes(encoding), UTF8);
                    temp = StringEscapeUtils.escapeHtml4(temp);
                    temp = new String(temp.getBytes(UTF8), encoding);
                    return temp;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return StringEscapeUtils.escapeHtml4(value);
        }

        private String replaceValue(Pattern pattern, String value)
        {
            Matcher matcher = pattern.matcher(value);
            while (matcher.find())
            {
                String group = matcher.group();
                value = value.replace(group, escapeHtml(group));
            }
            return value;
        }

        private String cleanXSS(String value)
        {
            if (value != null && !"".equals(value))
            {
                // NOTE: It's highly recommended to use the ESAPI library and uncomment the following line to
                value = value.replaceAll("", "");
                // Avoid anything between script tags
                 value = replaceValue(scriptPattern1, value);
                // Avoid anything in a src="..." type of e­xpression
                value = replaceValue(scriptPattern2, value);
                value = replaceValue(scriptPattern3, value);
                // Remove any lonesome </script> tag
                value = replaceValue(scriptPattern4, value);
                // Remove any lonesome <script ...> tag
                value = replaceValue(scriptPattern5, value);
                // Avoid eval(...) e­xpressions
                value = replaceValue(scriptPattern6, value);
                // Avoid e­xpression(...) e­xpressions
                value = replaceValue(scriptPattern7, value);
                // Avoid javascript:... e­xpressions
                value = replaceValue(scriptPattern8, value);
                // Avoid vbscript:... e­xpressions
                value = replaceValue(scriptPattern9, value);
                // Avoid onload= e­xpressions
                value = replaceValue(scriptPattern10, value);
            }
            return value;
        }

        private String readBody(InputStream is)
        {
            StringBuilder sb = new StringBuilder();
            String inputLine;
            BufferedReader br = null;
            try
            {
                br = new BufferedReader(new InputStreamReader(is));
                while ((inputLine = br.readLine()) != null)
                {
                    sb.append(inputLine);
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException("Failed to read body.", e);
            }
            finally
            {
                if (br != null)
                {
                    try
                    {
                        br.close();
                    }
                    catch (IOException e)
                    {
                    }
                }
            }
            return sb.toString();
        }

        private class CustomServletInputStream extends ServletInputStream
        {
            private ByteArrayInputStream buffer;

            public CustomServletInputStream(String body)
            {
                body = body == null ? "" : body;
                this.buffer = new ByteArrayInputStream(body.getBytes());
            }

            @Override
            public int read()
                throws IOException
            {
                return buffer.read();
            }

            @Override
            public boolean isFinished()
            {
                return buffer.available() == 0;
            }

            @Override
            public boolean isReady()
            {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener)
            {
                throw new RuntimeException("Not implemented");
            }
        }
    }
}

