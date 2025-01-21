package it.gov.pagopa.pu.pagopapayments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
public class PagoPaPaymentsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PagoPaPaymentsApplication.class, args);
	}

}
