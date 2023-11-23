package pw.avvero.sb2memoryleak.clones.a0_9.a3.e.account

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import pw.avvero.sb2memoryleak.account.DataSourceStub
import spock.lang.Specification

import static org.hamcrest.Matchers.*
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static pw.avvero.sb2memoryleak.account.TestUtils.json

@SpringBootTest
@ActiveProfiles(profiles = "test")
@Import([DataSourceStub])
@AutoConfigureMockMvc
class T1CreationTests extends Specification {

    @Autowired
    MockMvc mockMvc

    def "Account could be created with method"() {
        expect:
        mockMvc.perform(post("/api/v1/account/create")
                .contentType(APPLICATION_JSON_VALUE)
                .content()
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath('$.resultCode', is("Ok")))
                .andExpect(jsonPath('$.id', not(emptyOrNullString())))
    }

    def "Account balance could be obtained with info method"() {
        when:
        def createAccountResponseString = mockMvc.perform(post("/api/v1/account/create")
                .contentType(APPLICATION_JSON_VALUE)
                .content()
                .accept(APPLICATION_JSON_VALUE))
                .andReturn().response.contentAsString
        def createAccountResponse = json(createAccountResponseString)
        def accountId = createAccountResponse.id as Integer
        then:
        mockMvc.perform(post("/api/v1/account/info")
                .contentType(APPLICATION_JSON_VALUE)
                .content("""{"id": $accountId}""".toString())
                .accept(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath('$.resultCode', is("Ok")))
                .andExpect(jsonPath('$.id', is(accountId)))
                .andExpect(jsonPath('$.balance', is(0)))
    }
}
