package com.cinema.application.screenings;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.Screening;
import com.cinema.domain.entity.value.ScreeningId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.enums.ScreeningState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.ScreeningRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class RejectScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public RejectScreeningUseCase(ScreeningRepository screeningRepository,
                                  ProgramRepository programRepository) {
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
        this.programRepository = Objects.requireNonNull(programRepository);
    }


    @Transactional
    public void reject(UserId callerId, ScreeningId screeningId, String reason) {

        if (callerId == null) throw new AuthorizationException("Unauthorized");
        if (screeningId == null) throw new ValidationException("screeningId", "screeningId is required");
        if (reason == null || reason.isBlank()) {
            throw new ValidationException("reason", "Rejection reason is required");
        }

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));

        Program program = programRepository.findById(screening.programId())
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));

        ProgramState ps = program.state();
        ScreeningState ss = screening.state();


        if (ss == ScreeningState.SCHEDULED || ss == ScreeningState.REJECTED) {
            throw new ValidationException("screeningState", "Screening is already in final state");
        }


        if (ps == ProgramState.REVIEW) {


            if (!programRepository.isStaff(program.id(), callerId)) {
                throw new AuthorizationException("Only STAFF of the program can reject in REVIEW");
            }


            if (screening.staffMemberId() == null || !screening.isAssignedTo(callerId)) {
                throw new AuthorizationException("Only assigned STAFF can reject this screening");
            }


            if (ss != ScreeningState.SUBMITTED) {
                throw new ValidationException("screeningState", "Only SUBMITTED screenings can be rejected in REVIEW");
            }

            screening.reject(reason);
            screeningRepository.save(screening);
            return;
        }


        if (ps == ProgramState.SCHEDULING || ps == ProgramState.DECISION) {

            if (!programRepository.isProgrammer(program.id(), callerId)) {
                throw new AuthorizationException("Only PROGRAMMER can reject screenings in this program");
            }

            screening.reject(reason);
            screeningRepository.save(screening);
            return;
        }


        throw new ValidationException("programState", "Rejection allowed only in REVIEW (STAFF) or in SCHEDULING/DECISION (PROGRAMMER)");
    }
}
