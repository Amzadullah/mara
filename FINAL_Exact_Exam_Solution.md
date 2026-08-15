# FINAL EXACT SOLUTION — CIS 216 Student Management System

তোমার আসল question paper অনুযায়ী পুরো সমাধান। ৬টা অংশ, ঠিক marks distribution অনুযায়ী।

---

## 1. Project Setup & Database (5 Marks)

Dependencies (start.spring.io): **Spring Web, Thymeleaf, Spring Data JPA, MySQL Driver, Validation**

`application.properties` এ (MySQL এর জন্য):
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/student_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```
⚠️ **MySQL আগে থেকে চালু থাকতে হবে**, আর `student_db` নামে একটা database MySQL Workbench/phpMyAdmin দিয়ে আগে বানিয়ে রাখতে হবে (`CREATE DATABASE student_db;`)।

**যদি ল্যাবে MySQL সেট করা না থাকে বা সময় কম থাকে**, বিকল্প হিসেবে H2 ব্যবহার করতে পারো (dependency তে MySQL এর বদলে H2 নাও):
```properties
spring.datasource.url=jdbc:h2:mem:studentdb
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
```
(শিক্ষককে জিজ্ঞেস করে নিশ্চিত হয়ে নিও কোনটা expected, exam শুরুর আগেই)

Package বানাও: `model`, `repository`, `service`, `controller`

---

## 2. Entity & Repository (7 Marks)

### `model/Student.java`
```java
package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email must be valid")
    private String email;

    private String department;

    @DecimalMin(value = "0.00", message = "CGPA must be at least 0.00")
    @DecimalMax(value = "4.00", message = "CGPA must be at most 4.00")
    private Double cgpa;
}
```

### `repository/StudentRepository.java`
```java
package com.example.demo.repository;

import com.example.demo.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
}
```

---

## 3. Service Layer (6 Marks)

### `service/StudentService.java`
```java
package com.example.demo.service;

import com.example.demo.model.Student;
import com.example.demo.repository.StudentRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student addStudent(Student student) {
        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
    }

    public Student updateStudent(Long id, Student updatedStudent) {
        Student student = getStudentById(id);
        student.setName(updatedStudent.getName());
        student.setEmail(updatedStudent.getEmail());
        student.setDepartment(updatedStudent.getDepartment());
        student.setCgpa(updatedStudent.getCgpa());
        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
}
```

---

## 4. Controller — Thymeleaf UI & CRUD (15 Marks)

⚠️ **খেয়াল করো — Update এর জন্য আলাদা URL** (`/update/{id}`), Save এর মতো না।

### `controller/StudentController.java`
```java
package com.example.demo.controller;

import com.example.demo.model.Student;
import com.example.demo.service.StudentService;
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

    // /students → Student List Page
    @GetMapping
    public String listStudents(Model model) {
        model.addAttribute("studentList", studentService.getAllStudents());
        return "student-list";
    }

    // /students/new → Add Student Page (empty form)
    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("student", new Student());
        return "student-form";
    }

    // /students/save → Submit new student
    @PostMapping("/save")
    public String saveStudent(@Valid @ModelAttribute("student") Student student,
                               BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "student-form";   // errors thakle abar form dekhabe, error soho
        }
        studentService.addStudent(student);
        return "redirect:/students";
    }

    // /students/edit/{id} → Edit Student Page (pre-filled form)
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.getStudentById(id));
        return "student-form";
    }

    // /students/update/{id} → Submit updated student
    @PostMapping("/update/{id}")
    public String updateStudent(@PathVariable Long id,
                                 @Valid @ModelAttribute("student") Student student,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "student-form";
        }
        studentService.updateStudent(id, student);
        return "redirect:/students";
    }

    // /students/delete/{id} → Delete student
    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return "redirect:/students";
    }
}
```

---

## 5. Templates

### `student-list.html` (A. Student List Page)
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <title>Students</title>
</head>
<body class="container mt-5">
    <h2>Student List</h2>
    <a th:href="@{/students/new}" class="btn btn-success mb-3">Add Student</a>
    <table class="table table-striped">
        <thead class="table-dark">
            <tr><th>ID</th><th>Name</th><th>Email</th><th>Department</th><th>CGPA</th><th>Actions</th></tr>
        </thead>
        <tbody>
            <tr th:each="s : ${studentList}">
                <td th:text="${s.id}"></td>
                <td th:text="${s.name}"></td>
                <td th:text="${s.email}"></td>
                <td th:text="${s.department}"></td>
                <td th:text="${s.cgpa}"></td>
                <td>
                    <a th:href="@{/students/edit/{id}(id=${s.id})}" class="btn btn-sm btn-warning">Edit</a>
                    <a th:href="@{/students/delete/{id}(id=${s.id})}" class="btn btn-sm btn-danger">Delete</a>
                </td>
            </tr>
        </tbody>
    </table>
</body>
</html>
```

### `student-form.html` (B + C — Add ও Edit দুটোর জন্যই একই ফর্ম)

⚠️ **এই form টা একটু tricky** — Add এর সময় `/students/save` এ যাবে, Edit এর সময় `/students/update/{id}` এ যাবে। তাই action URL কে conditionally ঠিক করতে হবে `th:if`/`th:unless` দিয়ে, `student.id` আছে কিনা তার উপর ভিত্তি করে:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <title>Student Form</title>
</head>
<body class="container mt-5">
    <h2>Student Form</h2>

    <!-- id na thakle notun (save), thakle update -->
    <form th:if="${student.id == null}" th:action="@{/students/save}" th:object="${student}" method="post">
        <div class="mb-3">
            <label class="form-label">Name</label>
            <input type="text" th:field="*{name}" class="form-control">
            <span th:if="${#fields.hasErrors('name')}" th:errors="*{name}" class="text-danger"></span>
        </div>
        <div class="mb-3">
            <label class="form-label">Email</label>
            <input type="email" th:field="*{email}" class="form-control">
            <span th:if="${#fields.hasErrors('email')}" th:errors="*{email}" class="text-danger"></span>
        </div>
        <div class="mb-3">
            <label class="form-label">Department</label>
            <input type="text" th:field="*{department}" class="form-control">
        </div>
        <div class="mb-3">
            <label class="form-label">CGPA</label>
            <input type="number" step="0.01" th:field="*{cgpa}" class="form-control">
            <span th:if="${#fields.hasErrors('cgpa')}" th:errors="*{cgpa}" class="text-danger"></span>
        </div>
        <button type="submit" class="btn btn-primary">Save</button>
    </form>

    <form th:unless="${student.id == null}" th:action="@{/students/update/{id}(id=${student.id})}" th:object="${student}" method="post">
        <div class="mb-3">
            <label class="form-label">Name</label>
            <input type="text" th:field="*{name}" class="form-control">
            <span th:if="${#fields.hasErrors('name')}" th:errors="*{name}" class="text-danger"></span>
        </div>
        <div class="mb-3">
            <label class="form-label">Email</label>
            <input type="email" th:field="*{email}" class="form-control">
            <span th:if="${#fields.hasErrors('email')}" th:errors="*{email}" class="text-danger"></span>
        </div>
        <div class="mb-3">
            <label class="form-label">Department</label>
            <input type="text" th:field="*{department}" class="form-control">
        </div>
        <div class="mb-3">
            <label class="form-label">CGPA</label>
            <input type="number" step="0.01" th:field="*{cgpa}" class="form-control">
            <span th:if="${#fields.hasErrors('cgpa')}" th:errors="*{cgpa}" class="text-danger"></span>
        </div>
        <button type="submit" class="btn btn-primary">Update</button>
    </form>
</body>
</html>
```

**সময় কম থাকলে সহজ বিকল্প:** যদি উপরের `th:if`/`th:unless` জটিল লাগে, একটা **সাধারণ ফর্ম** বানাও যেটা সবসময় `/students/save` এ যায়, আর `updateStudent` কে `saveStudent` এর ভেতরেই merge করে দাও (id থাকলে update, না থাকলে insert — Service এর `addStudent` কেই ব্যবহার করো উভয় জায়গায়)। **এতে Controller এর `/update/{id}` marks কমতে পারে, কিন্তু পুরো app কাজ করবে, যেটা বেশি গুরুত্বপূর্ণ যদি সময় কম থাকে।**

---

## 6. Validation & Error Handling (4 Marks)

এইটা ইতিমধ্যে উপরে করা হয়ে গেছে:
- `@NotBlank` — name খালি থাকলে error
- `@Email` — email ভুল format হলে error
- `@DecimalMin`/`@DecimalMax` — cgpa 0-4 এর বাইরে হলে error
- `@Valid` + `BindingResult` — Controller এ error catch করা, `student-form` এ ফেরত পাঠানো
- `#fields.hasErrors()` + `th:errors` — HTML এ error দেখানো
- Invalid ID হ্যান্ডেল করা হয়েছে `getStudentById()` এ `orElseThrow()` দিয়ে

---

## 7. Viva প্রশ্নের উত্তর (মুখস্থ করে রাখো, ৩ মার্কস)

**Entity কী করে?** Database এর একটা টেবিলকে represent করে — প্রতিটা field একটা column।

**Repository কী করে?** Database এর সাথে সরাসরি কথা বলে — CRUD এর basic operation (`save`, `findAll`, `deleteById`) দেয়, কোনো SQL লিখতে হয় না।

**Service কী করে?** Repository আর Controller এর মাঝে থাকে — business logic (যেমন update এর সময় কোন field বদলাবে) এখানে থাকে, Controller কে পরিষ্কার রাখে।

**Controller কী করে?** Browser থেকে আসা request ধরে, Service কে call করে, কোন HTML page দেখাবে সেটা ঠিক করে।

**Thymeleaf কী করে?** Java এর data নিয়ে HTML এর ভেতরে বসিয়ে দেয় (`th:text`, `th:each`), Controller এর পাঠানো Model কে actual webpage বানায়।

---

## URL সারাংশ (মনে রাখো, exact এইভাবেই)
```
GET  /students              → list
GET  /students/new          → add form
POST /students/save         → save new
GET  /students/edit/{id}    → edit form
POST /students/update/{id}  → update existing   ⚠️ আলাদা URL, save না
GET  /students/delete/{id}  → delete
```
