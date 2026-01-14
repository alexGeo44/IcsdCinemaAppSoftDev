package application.screenings;

import com.cinema.application.screenings.ScheduleScreeningUseCase;
import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.enums.ScreeningState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScheduleScreeningUseCaseTest {

    private ScreeningRepository screeningRepo;
    private ProgramRepository programRepo;
    private ScheduleScreeningUseCase useCase;

    @BeforeEach
    void setup() {
        screeningRepo = mock(ScreeningRepository.class);
        programRepo = mock(ProgramRepository.class);
        useCase = new ScheduleScreeningUseCase(screeningRepo, programRepo);
    }

    private Program program(UserId creator, ProgramState state) {
        return new Program(
                null,
                null,
                "P",
                "D",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                creator,
                state
        );
    }

    @Test
    void schedule_saves_whenProgrammerAndDecisionAndFinalSubmitted() {
        UserId actor = new UserId(1L);
        ScreeningId sid = new ScreeningId(7L);
        ProgramId pid = new ProgramId(1L);

        Screening screening = ScreeningTestFactory.rehydrate(
                sid, pid, new UserId(50L), ScreeningState.FINAL_SUBMITTED, null
        );

        when(screeningRepo.findById(sid)).thenReturn(Optional.of(screening));
        when(programRepo.findById(pid)).thenReturn(Optional.of(program(new UserId(999L), ProgramState.DECISION)));
        when(programRepo.isProgrammer(pid, actor)).thenReturn(true);

        useCase.schedule(actor, sid, LocalDate.now().plusDays(2), "Room 1");

        verify(screeningRepo).save(screening);
    }

    @Test
    void schedule_throws_whenNotProgrammerAndNotCreator() {
        UserId actor = new UserId(1L);
        ScreeningId sid = new ScreeningId(7L);
        ProgramId pid = new ProgramId(1L);

        Screening screening = ScreeningTestFactory.rehydrate(
                sid, pid, new UserId(50L), ScreeningState.FINAL_SUBMITTED, null
        );

        when(screeningRepo.findById(sid)).thenReturn(Optional.of(screening));
        when(programRepo.findById(pid)).thenReturn(Optional.of(program(new UserId(999L), ProgramState.DECISION)));
        when(programRepo.isProgrammer(pid, actor)).thenReturn(false);

        assertThrows(AuthorizationException.class,
                () -> useCase.schedule(actor, sid, LocalDate.now().plusDays(2), "Room 1"));

        verify(screeningRepo, never()).save(any());
    }

    @Test
    void schedule_throws_whenWrongProgramState() {
        UserId actor = new UserId(1L);
        ScreeningId sid = new ScreeningId(7L);
        ProgramId pid = new ProgramId(1L);

        Screening screening = ScreeningTestFactory.rehydrate(
                sid, pid, new UserId(50L), ScreeningState.FINAL_SUBMITTED, null
        );

        when(screeningRepo.findById(sid)).thenReturn(Optional.of(screening));
        when(programRepo.findById(pid)).thenReturn(Optional.of(program(new UserId(999L), ProgramState.SUBMISSION)));
        when(programRepo.isProgrammer(pid, actor)).thenReturn(true);

        assertThrows(ValidationException.class,
                () -> useCase.schedule(actor, sid, LocalDate.now().plusDays(2), "Room 1"));

        verify(screeningRepo, never()).save(any());
    }

    @Test
    void schedule_throws_whenScreeningMissing() {
        UserId actor = new UserId(1L);
        ScreeningId sid = new ScreeningId(7L);

        when(screeningRepo.findById(sid)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> useCase.schedule(actor, sid, LocalDate.now().plusDays(2), "Room 1"));
    }
}
