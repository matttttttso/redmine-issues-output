package redmineissuesoutput.domain.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

// application.ymlから読込む場合
@Component
@ConfigurationProperties(prefix = "app.config.redmine")
public class RedmineInfo {
	@Getter
	@Setter
	private String url;
	@Getter
	@Setter
	private String apiKey;
}
