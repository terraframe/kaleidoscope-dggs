package ai.terraframe.kaleidoscope.dggs.core.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan("ai.terraframe.kaleidoscope.dggs.core")
public class CoreConfiguration
{


}