package br.com.sicredi.votacao.common.error;

import br.com.sicredi.votacao.common.exception.PautaNaoEncontradaException;
import br.com.sicredi.votacao.common.exception.SessaoFechadaException;
import br.com.sicredi.votacao.common.exception.SessaoJaAbertaException;
import br.com.sicredi.votacao.common.exception.SessaoNaoEncontradaException;
import br.com.sicredi.votacao.common.exception.VotoDuplicadoException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void deveRetornarErroPadronizadoParaValidacaoDeRequest() throws Exception {
        mockMvc.perform(post("/handler/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("nome: must not be blank"))
                .andExpect(jsonPath("$.path").value("/handler/validacao"));
    }

    @Test
    void deveRetornarErroPadronizadoParaJsonInvalido() throws Exception {
        mockMvc.perform(post("/handler/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{nome}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Requisicao invalida"))
                .andExpect(jsonPath("$.path").value("/handler/validacao"));
    }

    @Test
    void deveRetornarErroPadronizadoParaUuidInvalido() throws Exception {
        mockMvc.perform(get("/handler/uuid/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Parametro invalido"))
                .andExpect(jsonPath("$.path").value("/handler/uuid/abc"));
    }

    @Test
    void deveRetornarErroPadronizadoParaConstraintViolation() throws Exception {
        mockMvc.perform(get("/handler/constraint-violation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Requisicao invalida"))
                .andExpect(jsonPath("$.path").value("/handler/constraint-violation"));
    }

    @Test
    void deveRetornarNotFoundParaPautaNaoEncontrada() throws Exception {
        mockMvc.perform(get("/handler/pauta-nao-encontrada"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Pauta nao encontrada"))
                .andExpect(jsonPath("$.path").value("/handler/pauta-nao-encontrada"));
    }

    @Test
    void deveRetornarConflictParaSessaoJaAberta() throws Exception {
        mockMvc.perform(get("/handler/sessao-ja-aberta"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Sessao ja aberta para esta pauta"))
                .andExpect(jsonPath("$.path").value("/handler/sessao-ja-aberta"));
    }

    @Test
    void deveRetornarConflictParaSessaoNaoEncontrada() throws Exception {
        mockMvc.perform(get("/handler/sessao-nao-encontrada"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Sessao de votacao nao aberta"))
                .andExpect(jsonPath("$.path").value("/handler/sessao-nao-encontrada"));
    }

    @Test
    void deveRetornarConflictParaSessaoForaDoPeriodo() throws Exception {
        mockMvc.perform(get("/handler/sessao-fechada"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Sessao de votacao nao esta aberta"))
                .andExpect(jsonPath("$.path").value("/handler/sessao-fechada"));
    }

    @Test
    void deveRetornarConflictParaVotoDuplicado() throws Exception {
        mockMvc.perform(get("/handler/voto-duplicado"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Associado ja votou nesta pauta"))
                .andExpect(jsonPath("$.path").value("/handler/voto-duplicado"));
    }

    @Test
    void deveRetornarInternalServerErrorParaErroInesperado() throws Exception {
        mockMvc.perform(get("/handler/erro-inesperado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Erro interno inesperado"))
                .andExpect(jsonPath("$.path").value("/handler/erro-inesperado"));
    }

    record TestRequest(@NotBlank String nome) {
    }

    @RestController
    @RequestMapping("/handler")
    public static class TestController {

        @PostMapping("/validacao")
        void validacao(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/uuid/{id}")
        void uuid(@PathVariable UUID id) {
        }

        @GetMapping("/constraint-violation")
        void constraintViolation() {
            throw new ConstraintViolationException("Requisicao invalida", Set.of());
        }

        @GetMapping("/pauta-nao-encontrada")
        void pautaNaoEncontrada() {
            throw new PautaNaoEncontradaException();
        }

        @GetMapping("/sessao-ja-aberta")
        void sessaoJaAberta() {
            throw new SessaoJaAbertaException();
        }

        @GetMapping("/sessao-nao-encontrada")
        void sessaoNaoEncontrada() {
            throw new SessaoNaoEncontradaException();
        }

        @GetMapping("/sessao-fechada")
        void sessaoFechada() {
            throw new SessaoFechadaException();
        }

        @GetMapping("/voto-duplicado")
        void votoDuplicado() {
            throw new VotoDuplicadoException();
        }

        @GetMapping("/erro-inesperado")
        void erroInesperado() {
            throw new IllegalStateException("Erro sensivel");
        }
    }
}
