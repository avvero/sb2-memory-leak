package pw.avvero.sb2memoryleak.clones.e0_9.a9.c.account

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import pw.avvero.sb2memoryleak.account.DataSourceStub
import spock.lang.Specification
import spock.lang.Unroll

import static org.hamcrest.Matchers.is
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static pw.avvero.sb2memoryleak.account.TestUtils.json

@SpringBootTest
@ActiveProfiles(profiles = "test")
@Import([DataSourceStub])
@AutoConfigureMockMvc
@TestPropertySource(properties = [
        'app.by-test-property=${random[100,200]}'
])
class T6WithdrawalConcurrentTests extends Specification {

    @Autowired
    MockMvc mockMvc

    @Unroll
    def "Account could be withdrawn with deposit method even in a highly concurrent environment"() {
        when: "Create account"
        def createAccountResponse = mockMvc.perform(post("/api/v1/account/create")
                .contentType(APPLICATION_JSON_VALUE)
                .content()
                .accept(APPLICATION_JSON_VALUE))
                .andReturn().response
        def accountId = json(createAccountResponse.contentAsString).id as Integer
        and: "Replenish on 50"
        mockMvc.perform(post("/api/v1/account/deposit")
                .contentType(APPLICATION_JSON_VALUE)
                .content("""{"id": $accountId, "amount": "50"}""".toString())
                .accept(APPLICATION_JSON_VALUE))
        then: "Check info"
        mockMvc.perform(post("/api/v1/account/info")
                .contentType(APPLICATION_JSON_VALUE)
                .content("""{"id": $accountId}""".toString())
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath('$.balance', is(50)))
        when: "Withdrawal concurrently"
        List<Thread> threads = new LinkedList<>()
        10.times { i ->
            Thread t = new Thread(new Runnable() {
                @Override
                void run() {
                    try {
                        Thread.sleep(new Random().nextInt(100) + 1)
                        String result = mockMvc.perform(post("/api/v1/account/withdraw")
                                .contentType(APPLICATION_JSON_VALUE)
                                .content("""{"id": $accountId, "amount": "3"}""".toString())
                                .accept(APPLICATION_JSON_VALUE))
                                .andReturn().response.contentAsString
                        System.out.println("Result: " + i + " : " + result)
                    } catch (Exception e) {
                        System.out.println(e.getLocalizedMessage())
                    }
                }
            });
            t.start()
            if (!concurrentlly) {
                t.join() // enable to make sequence
            }
            threads.add(t)
        }
        for (Thread thread : threads) {
            thread.join()
        }
        then: "Balance is changed"
        mockMvc.perform(post("/api/v1/account/info")
                .contentType(APPLICATION_JSON_VALUE)
                .content("""{"id": $accountId}""".toString())
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath('$.balance', is(20)))
        where:
        concurrentlly | _
        false         | _
        true          | _
    }
}
