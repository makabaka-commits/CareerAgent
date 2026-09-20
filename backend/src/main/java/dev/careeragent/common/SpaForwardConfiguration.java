package dev.careeragent.common;
import org.springframework.context.annotation.Configuration;import org.springframework.web.servlet.config.annotation.*;
@Configuration public class SpaForwardConfiguration implements WebMvcConfigurer{
 @Override public void addViewControllers(ViewControllerRegistry registry){for(String path:new String[]{"/login","/demo","/app","/app/**"})registry.addViewController(path).setViewName("forward:/index.html");}
}
