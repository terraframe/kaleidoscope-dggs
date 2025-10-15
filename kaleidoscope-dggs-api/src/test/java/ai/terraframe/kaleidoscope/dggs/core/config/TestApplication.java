package ai.terraframe.kaleidoscope.dggs.core.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "ai.terraframe.kaleidoscope.dggs.core", "ai.terraframe.kaleidoscope.dggs.web" })
public class TestApplication extends SpringBootServletInitializer
{
  public static void main(String[] args)
  {
    SpringApplication.run(TestApplication.class, args);
  }
}
