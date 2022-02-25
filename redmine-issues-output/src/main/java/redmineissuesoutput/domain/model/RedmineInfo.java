package redmineissuesoutput.domain.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.Setter;

// application.yml以外から読込む場合
@Configuration
@PropertySource("classpath:/config/confidential.yml")
@Component
@ConfigurationProperties(prefix = "app.config.redmine-develop")
public class RedmineInfo {
	@Autowired
	private Environment env;
	
	@Setter
	private String url;
	@Setter
	private String apiKey;
	
	public String getUrl() {
		return env.getProperty("url");
	}
	public String getApiKey() {
		return env.getProperty("apiKey");
	}
}
