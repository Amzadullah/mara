package com.diu.studentmanagement.repository;

import com.diu.studentmanagement.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
    // Kichu lekhar dorkar nai! JpaRepository already dey:
    // save(), findAll(), findById(), deleteById()
}