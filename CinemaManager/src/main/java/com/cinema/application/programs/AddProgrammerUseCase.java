package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.DuplicateException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.ProgramId;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.port.ProgramRepository;
import com.cinema.domain.port.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class AddProgrammerUseCase {

    private final ProgramRepository programRepository;
    private final UserRepository userRepository;

    public AddProgrammerUseCase(ProgramRepository programRepository, UserRepository userRepository) {
        this.programRepository = Objects.requireNonNull(programRepository);
        this.userRepository = Objects.requireNonNull(userRepository);
    }


    @Transactional
    public void addProgrammer(UserId actorId, ProgramId programId, UserId newProgrammerId) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");
        if (programId == null) throw new ValidationException("programId", "programId is required");
        if (newProgrammerId == null) throw new ValidationException("newProgrammerId", "newProgrammerId is required");

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException("Program", "Program not found"));


        if (!programRepository.isProgrammer(programId, actorId)) {
            throw new AuthorizationException("Only PROGRAMMER of this program can add programmers");
        }


        if (program.state() == ProgramState.ANNOUNCED) {
            throw new ValidationException("programState", "Program cannot be modified after ANNOUNCED");
        }


        User user = userRepository.findById(newProgrammerId)
                .orElseThrow(() -> new NotFoundException("User", "Programmer user not found"));

        if (!user.isActive()) {
            throw new ValidationException("newProgrammerId", "User account is inactive");
        }


        if (program.isProgrammer(newProgrammerId)) {
            throw new DuplicateException("programmer", "User is already a PROGRAMMER of this program");
        }


        if (program.isStaff(newProgrammerId)) {
            throw new ValidationException("newProgrammerId", "User is already STAFF of this program");
        }

        program.addProgrammer(newProgrammerId);
        programRepository.save(program);
    }
}
