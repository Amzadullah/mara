package com.diu.studentmanagement.controller;

import com.diu.studentmanagement.entity.Student;
import com.diu.studentmanagement.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // 1. Student List Page
    @GetMapping
    public String listStudents(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        return "student-list"; // student-list.html
    }

    // 2. Add Student Form Page
    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("student", new Student());
        return "student-form"; // student-form.html
    }

    // 3. Save Student (from Add form)
    @PostMapping("/save")
    public String saveStudent(@Valid @ModelAttribute("student") Student student,
                               BindingResult result) {
        if (result.hasErrors()) {
            return "student-form"; // errors thakle abar form e ferot pathao
        }
        studentService.saveStudent(student);
        return "redirect:/students";
    }

    // 4. Edit Form Page
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("student", studentService.getStudentById(id));
        return "student-form"; // same form, but pre-filled
    }

    // 5. Update Student (from Edit form) — same URL/method style as save
    @PostMapping("/update/{id}")
    public String updateStudent(@PathVariable("id") Long id,
                                 @Valid @ModelAttribute("student") Student student,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return "student-form";
        }
        student.setId(id);
        studentService.saveStudent(student);
        return "redirect:/students";
    }

    // 6. Delete Student
    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return "redirect:/students";
    }
}