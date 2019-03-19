package vit.controller;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import vit.domain.Team;
import vit.rabbitmq.Publisher;
import vit.repository.TeamRepository;

@RunWith(SpringRunner.class)
@WebMvcTest(TeamController.class)
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;                    // инжекция объекта для тестирования контроллеров MVC без запуска полноценного HTTP-сервера

    @MockBean
    private TeamRepository teamRepository;      // мок-репозиторий – заменит реальный репозиторий в контроллере

    @MockBean
    private Publisher publisher;

    private Team team;                         // команда c капитаном

    @BeforeEach
    public void setUp() throws Exception {
        this.team = new Team(8L, 88L, "Иван");
    }

    @Test
    void setCommander_exist() throws Exception {                                    // проверка присвоения капитанства команде у которой есть капитан => ничего не делать
        given(teamRepository.findById(8L)).willReturn(Optional.of(team));           // при запросе как бы к БД - такой участник будет как бы найден
        mockMvc.perform(post("/team")                                    // выполнение как-бы HTTP-запроса - на него срабатывает метод  контроллера
                .param("teamId", String.valueOf(8L))                               // параметры запроса: команда и участник (который хочет стать капитаном)
                .param("participantId", String.valueOf(88l))
                .param("participantIdentifier", "Иван")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value("false"))   // тело HTTP-ответа (json): {"success": "false"}
                .andExpect(status().isOk());                                             // статус HTTP-ответа (=200)
    }

    @Test
    void setCommander_put() throws Exception {                                      // проверка присвоения капитанства команде у которой нет капитана => задать
        Team emptyTeam = new Team(8L, null, null);   // команда у которой нет капитана
        given(teamRepository.findById(8L)).willReturn(Optional.of(emptyTeam));      // при запросе как бы к БД - такая команда будет как бы найдена
        mockMvc.perform(post("/team")                                    // выполнение как-бы HTTP-запроса (на него срабатывает метод  контроллера)
                .param("teamId", String.valueOf(8L))                               // параметры запроса: команда и участник (который хочет стать капитаном)
                .param("participantId", String.valueOf(88L))
                .param("participantIdentifier", "Иван")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value("true"))    // тело HTTP-ответа (json): {"success": "true"}
                .andExpect(status().isOk());                                             // статус HTTP-ответа =200
        verify(teamRepository).save(any(Team.class));                                    // personRepository.save() был успешно вызван (один раз)
    }

    @Test
    void setCommander_new() throws Exception {                                      // проверка добавления новой команды (и капитана для неё): нет команды => добавить
        given(teamRepository.findById(8L)).willReturn(Optional.empty());            // при запросе как бы к БД - команда как бы не будет найдена
        given(teamRepository.save(team)).willReturn(team);                          // при как бы сохранении как бы вернётся новая команда и капитан
        mockMvc.perform(post("/team")                                    // выполнение как-бы HTTP-запроса (на него срабатывает метод  контроллера)
                .param("teamId", String.valueOf(8L))                               // параметры запроса: команда и участник (который хочет стать капитаном)
                .param("participantId", String.valueOf(88L))
                .param("participantIdentifier", "Иван")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value("true"))    // тело HTTP-ответа (json): {"success": "true"}
                .andExpect(header().string("Location", "team/8"))            // в заголовке URI созданного ресурса
                .andExpect(status().isCreated());                                        // статус HTTP-ответа =201
        verify(teamRepository).save(any(Team.class));                                    // personRepository.save() был успешно вызван (один раз)
    }

    @Test
    void getCommander() throws Exception {                                           // проверка поиска команды
        given(teamRepository.findById(8L)).willReturn(Optional.of(team));            // при запросе как бы к БД - такой участник будет как бы найден
        mockMvc.perform(get("/team/8")                                    // выполнение как-бы HTTP-запроса - на него срабатывает метод  контроллера
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.participantIdentifier").value("Иван"))  // тело HTTP-ответа (json): {"participantId":"88","participantIdentifier":"Иван"}
                .andExpect(status().isOk());                                                          //  статус HTTP-ответа =200
    }

    @Test
    void refuseCommander() throws Exception {                                        // проверка отказа от капитанства  => убрать капитана
        given(teamRepository.findById(8L)).willReturn(Optional.of(team));            // при запросе как бы к БД - такая команда будет как бы найдена
        mockMvc.perform(put("/team")                                      // выполнение как-бы HTTP-запроса (на него срабатывает метод  контроллера)
                .param("teamId", String.valueOf(8L))                               // параметры запроса: команда и участник (который не хочет быть капитаном)
                .param("participantId", String.valueOf(88L))
                .param("participantIdentifier", "Иван")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());                                         // статус HTTP-ответа =200
        verify(teamRepository).save(any(Team.class));                                // personRepository.save() был успешно вызван (один раз)
    }
}