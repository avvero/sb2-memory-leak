package pw.avvero.sb2memoryleak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class Sb2MemoryLeakApplication {

	public static void main(String[] args) {
		SpringApplication.run(Sb2MemoryLeakApplication.class, args);
	}

}
