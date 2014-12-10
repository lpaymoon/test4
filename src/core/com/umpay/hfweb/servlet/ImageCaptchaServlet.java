package com.umpay.hfweb.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.octo.captcha.service.CaptchaServiceException;
import com.umpay.hfweb.util.CaptchaServiceSingleton;

public class ImageCaptchaServlet extends HttpServlet{

	public void init(ServletConfig servletConfig) throws ServletException{

		super.init(servletConfig);

	}

	protected void doGet(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws ServletException,
			IOException{

		try {
            CaptchaServiceSingleton.getInstance().writeCaptchaImage(
                    httpServletRequest, httpServletResponse);

        } catch (IllegalArgumentException e) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } catch (CaptchaServiceException e) {
            httpServletResponse
                    .sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
	}
}
