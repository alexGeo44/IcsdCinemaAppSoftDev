package com.cinema.application.programs;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.DuplicateException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.Program;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.ProgramState;
import com.cinema.domain.port.ProgramRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;

@Service
public class CreateProgramUseCase {

    private final ProgramRepository programRepository;

    public CreateProgramUseCase(ProgramRepository programRepository) {
        this.programRepository = Objects.requireNonNull(programRepository);
    }


    @Transactional
    public Program create(
            UserId creatorId,
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (creatorId == null) throw new AuthorizationException("Unauthorized");

        String n = name == null ? null : name.trim();
        String d = description == null ? null : description.trim();

        if (n == null || n.isBlank()) throw new ValidationException("name", "Program name is required");
        if (d == null || d.isBlank()) throw new ValidationException("description", "Program description is required");
        if (startDate == null) throw new ValidationException("startDate", "Start date is required");
        if (endDate == null) throw new ValidationException("endDate", "End date is required");
        if (endDate.isBefore(startDate)) throw new ValidationException("dates", "End date must be on/after start date");


        if (programRepository.existsByName(n)) {
            throw new DuplicateException("program.name", "Program name already exists");
        }

        Program program = new Program(
                null,
                null,
                n,
                d,
                startDate,
                endDate,
                creatorId,
                ProgramState.CREATED
        );



        return programRepository.save(program);
    }
}
