package it.gov.pagopa.pu.pagopapayments;

import it.gov.pagopa.pu.pagopapayments.util.Constants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration;

import java.util.TimeZone;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
public class PagoPaPaymentsApplication {

	public static void main(String[] args) {
    TimeZone.setDefault(Constants.DEFAULT_TIMEZONE);
		SpringApplication.run(PagoPaPaymentsApplication.class, args);
	}

}
