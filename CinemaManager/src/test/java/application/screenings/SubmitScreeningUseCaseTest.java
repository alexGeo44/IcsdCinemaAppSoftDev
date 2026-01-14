package application.screenings;

import com.cinema.application.screenings.SubmitScreeningUseCase;
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

class SubmitScreeningUseCaseTest {

    private ScreeningRepository screeningRepo;
    private ProgramRepository programRepo;
    private SubmitScreeningUseCase useCase;

    @BeforeEach
    void setup() {
        screeningRepo = mock(ScreeningRepository.class);
        programRepo = mock(ProgramRepository.class);
        useCase = new SubmitScreeningUseCase(screeningRepo, programRepo);
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
    void submit_throws_whenCallerNotOwner() {
        UserId caller = new UserId(1L);
        ScreeningId sid = new ScreeningId(10L);
        ProgramId pid = new ProgramId(1L);


        Screening screening = ScreeningTestFactory.rehydrate(
                sid, pid, new UserId(999L), ScreeningState.CREATED, null
        );

        when(screeningRepo.findById(sid)).thenReturn(Optional.of(screening));

        assertThrows(AuthorizationException.class,
                () -> useCase.submit(caller, sid));
    }

    @Test
    void submit_throws_whenProgramNotInSubmission() {
        UserId caller = new UserId(1L);
        ScreeningId sid = new ScreeningId(10L);
        ProgramId pid = new ProgramId(1L);

        Screening screening = ScreeningTestFactory.rehydrate(
                sid, pid, caller, ScreeningState.CREATED, null
        );

        when(screeningRepo.findById(sid)).thenReturn(Optional.of(screening));
        when(programRepo.findById(pid)).thenReturn(Optional.of(program(new UserId(999L), ProgramState.CREATED)));

        assertThrows(ValidationException.class,
                () -> useCase.submit(caller, sid));
    }

    @Test
    void submit_throws_whenCreatorConflict_onlyCreatorBlocked() {
        UserId caller = new UserId(1L);
        ScreeningId sid = new ScreeningId(10L);
        ProgramId pid = new ProgramId(1L);

        Screening screening = ScreeningTestFactory.rehydrate(
                sid, pid, caller, ScreeningState.CREATED, null
        );

        when(screeningRepo.findById(sid)).thenReturn(Optional.of(screening));
        when(programRepo.findById(pid)).thenReturn(Optional.of(program(caller, ProgramState.SUBMISSION)));


        assertThrows(AuthorizationException.class,
                () -> useCase.submit(caller, sid));

        verify(screeningRepo, never()).save(any());
    }

    @Test
    void submit_throws_whenScreeningMissing() {
        UserId caller = new UserId(1L);
        ScreeningId sid = new ScreeningId(10L);

        when(screeningRepo.findById(sid)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> useCase.submit(caller, sid));
    }
}
