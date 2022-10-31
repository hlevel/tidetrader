package tide.trader.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application start.
 */
@SuppressWarnings({ "checkstyle:FinalClass", "checkstyle:HideUtilityClassConstructor" })
@SpringBootApplication
public class TideTraderApplication {

	public static void main(final String[] args) {
		SpringApplication.run(TideTraderApplication.class, args);
	}

}
