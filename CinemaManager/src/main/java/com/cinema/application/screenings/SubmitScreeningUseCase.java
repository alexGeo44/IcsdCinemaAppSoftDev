package com.cinema.application.screenings;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class SubmitScreeningUseCase {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    public SubmitScreeningUseCase(ScreeningRepository screeningRepository,
                                  ProgramRepository programRepository) {
        this.screeningRepository = Objects.requireNonNull(screeningRepository);
        this.programRepository = Objects.requireNonNull(programRepository);
    }


    @Transactional
    public void submit(UserId callerId, ScreeningId screeningId) {
        if (callerId == null) throw new AuthorizationException("Unauthorized");
        if (screeningId == null) throw new ValidationException("screeningId", "screeningId is required");

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening", "Screening not found"));


        if (!screening.isOwner(callerId)) {
            throw new AuthorizationException("Only the submitter can submit this screening");
        }

        ProgramId programId = screening.programId();

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));


        if (program.creatorUserId().equals(callerId)) {
            throw new AuthorizationException("Creator cannot submit screenings to own program");
        }


        if (program.state() != ProgramState.SUBMISSION) {
            throw new ValidationException("programState", "Screening submission allowed only in SUBMISSION");
        }


        if (screening.state() == ScreeningState.SUBMITTED) {
            throw new ValidationException("screeningState", "Screening already submitted");
        }
        if (screening.state() != ScreeningState.CREATED) {
            throw new ValidationException("screeningState", "Only CREATED screenings can be submitted");
        }


        if (!screening.isCompleteForSubmission()) {
            throw new ValidationException("screening", "Screening is incomplete for submission");
        }

        try {
            screening.submit();
        } catch (IllegalStateException ex) {
            throw new ValidationException("screeningState", ex.getMessage());
        }

        screeningRepository.save(screening);
    }
}
