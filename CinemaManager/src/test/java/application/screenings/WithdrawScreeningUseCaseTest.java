package application.screenings;

import com.cinema.application.screenings.WithdrawScreeningUseCase;
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

class WithdrawScreeningUseCaseTest {

    private ScreeningRepository screeningRepo;
    private ProgramRepository programRepo;
    private WithdrawScreeningUseCase useCase;

    @BeforeEach
    void setup() {
        screeningRepo = mock(ScreeningRepository.class);
        programRepo = mock(ProgramRepository.class);
        useCase = new WithdrawScreeningUseCase(screeningRepo, programRepo);
    }

    private Program program(UserId creator) {
        return new Program(
                null,
                null,
                "P",
                "D",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                creator,
                ProgramState.CREATED
        );
    }

    @Test
    void withdraw_deletes_whenOwnerAndCreated() {
        UserId owner = new UserId(4L);
        ScreeningId sid = new ScreeningId(11L);
        ProgramId pid = new ProgramId(1L);

        Screening screening = ScreeningTestFactory.rehydrate(
                sid, pid, owner, ScreeningState.CREATED, null
        );

        when(screeningRepo.findById(sid)).thenReturn(Optional.of(screening));
        when(programRepo.findById(pid)).thenReturn(Optional.of(program(new UserId(999L))));

        useCase.withdraw(owner, sid);

        verify(screeningRepo).deleteById(sid);
    }

    @Test
    void withdraw_deletes_whenCreatorAndCreated_evenIfNotOwner() {
        UserId creator = new UserId(77L);
        UserId owner = new UserId(4L);
        ScreeningId sid = new ScreeningId(11L);
        ProgramId pid = new ProgramId(1L);

        Screening screening = ScreeningTestFactory.rehydrate(
                sid, pid, owner, ScreeningState.CREATED, null
        );

        when(screeningRepo.findById(sid)).thenReturn(Optional.of(screening));
        when(programRepo.findById(pid)).thenReturn(Optional.of(program(creator)));

        useCase.withdraw(creator, sid);

        verify(screeningRepo).deleteById(sid);
    }

    @Test
    void withdraw_throws_whenNotOwnerNorCreator() {
        UserId caller = new UserId(9L);
        UserId creator = new UserId(77L);
        UserId owner = new UserId(4L);
        ScreeningId sid = new ScreeningId(11L);
        ProgramId pid = new ProgramId(1L);

        Screening screening = ScreeningTestFactory.rehydrate(
                sid, pid, owner, ScreeningState.CREATED, null
        );

        when(screeningRepo.findById(sid)).thenReturn(Optional.of(screening));
        when(programRepo.findById(pid)).thenReturn(Optional.of(program(creator)));

        assertThrows(AuthorizationException.class, () -> useCase.withdraw(caller, sid));
        verify(screeningRepo, never()).deleteById(any());
    }

    @Test
    void withdraw_throws_whenNotCreated() {
        UserId owner = new UserId(4L);
        ScreeningId sid = new ScreeningId(11L);
        ProgramId pid = new ProgramId(1L);

        Screening screening = ScreeningTestFactory.rehydrate(
                sid, pid, owner, ScreeningState.SUBMITTED, null
        );

        when(screeningRepo.findById(sid)).thenReturn(Optional.of(screening));
        when(programRepo.findById(pid)).thenReturn(Optional.of(program(new UserId(999L))));

        assertThrows(ValidationException.class, () -> useCase.withdraw(owner, sid));
        verify(screeningRepo, never()).deleteById(any());
    }

    @Test
    void withdraw_throws_whenProgramMissing() {
        UserId owner = new UserId(4L);
        ScreeningId sid = new ScreeningId(11L);
        ProgramId pid = new ProgramId(1L);

        Screening screening = ScreeningTestFactory.rehydrate(
                sid, pid, owner, ScreeningState.CREATED, null
        );

        when(screeningRepo.findById(sid)).thenReturn(Optional.of(screening));
        when(programRepo.findById(pid)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> useCase.withdraw(owner, sid));
    }
}
