# A2Z — Student Management System (CIS 216 Exam) — ONE FILE, START TO FINISH

---

## ধাপ ১ — Project Setup

1. **start.spring.io** এ যাও
2. Maven, Java, Spring Boot latest সিলেক্ট করো
3. Dependencies: **Spring Web, Thymeleaf, Spring Data JPA, MySQL Driver (বা H2), Validation**
4. Generate → Extract → VS Code এ Open Folder

## ধাপ ২ — Database Config

`src/main/resources/application.properties` এ (MySQL হলে):
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/student_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```
MySQL Workbench এ আগে থেকে বানাও: `CREATE DATABASE student_db;`

**অথবা সহজ বিকল্প (H2, MySQL সমস্যা করলে):**
```properties
spring.datasource.url=jdbc:h2:mem:studentdb
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
```

## ধাপ ৩ — Package বানাও

`src/main/java/com/example/demo/` এর ভেতরে:
```
model
repository
service
controller
```

---

## ধাপ ৪ — Entity: `model/Student.java`

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

---

## ধাপ ৫ — Repository: `repository/StudentRepository.java`

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

## ধাপ ৬ — Service: `service/StudentService.java`

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

## ধাপ ৭ — Controller: `controller/StudentController.java`

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

    @GetMapping
    public String listStudents(Model model) {
        model.addAttribute("studentList", studentService.getAllStudents());
        return "student-list";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("student", new Student());
        return "student-form";
    }

    @PostMapping("/save")
    public String saveStudent(@Valid @ModelAttribute("student") Student student,
                               BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "student-form";
        }
        studentService.addStudent(student);
        return "redirect:/students";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.getStudentById(id));
        return "student-form";
    }

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

    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return "redirect:/students";
    }
}
```

---

## ধাপ ৮ — Templates: `src/main/resources/templates/`

### `student-list.html`
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

### `student-form.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <title>Student Form</title>
</head>
<body class="container mt-5">
    <h2>Student Form</h2>

    <!-- id na thakle Add (save e jabe) -->
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

    <!-- id thakle Edit (update/{id} e jabe) -->
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

---

## ধাপ ৯ — Run

`DemoApplication.java` খোলো → উপরের ▷ (play) বাটনে ক্লিক করো
Terminal এ `Tomcat started on port 8080` দেখলে ঠিক আছে

## ধাপ ১০ — Test (browser: `http://localhost:8080/students`)

| # | কাজ | দেখবা কী |
|---|-----|----------|
| 1 | `/students` এ যাও | খালি table |
| 2 | "Add Student" ক্লিক | ফর্ম আসবে |
| 3 | Name খালি রেখে Save করার চেষ্টা | "Name cannot be empty" error দেখাবে |
| 4 | সঠিক data দিয়ে Save | list এ ফেরত, নতুন row |
| 5 | "Edit" ক্লিক | পুরোনো data pre-filled |
| 6 | বদলে "Update" | পরিবর্তন সহ list এ ফেরত |
| 7 | "Delete" ক্লিক | row চলে যাবে |

---

## Common Errors ও Fix

| Error | কারণ | Fix |
|---|---|---|
| MySQL connection error | database বানানো হয়নি বা password ভুল | `CREATE DATABASE student_db;` চালাও MySQL এ, password মিলাও |
| Whitelabel Error Page | template name mismatch | `return "student-list"` আর `student-list.html` মিলাও |
| Validation error না দেখানো | `@Valid` বা `BindingResult` মিসিং | Controller method এ `@Valid @ModelAttribute` + `BindingResult result` একসাথে থাকতে হবে, ঠিক এই order এ |
| Update করলে notun row হয়ে যাচ্ছে | Edit form এ `student.id` null হয়ে গেছে | `getStudentById()` ঠিকমতো object পাঠাচ্ছে কিনা check করো |
| Port 8080 already in use | আগের run বন্ধ হয়নি | Terminal এ Ctrl+C, আবার run করো |

---

## Viva — এক লাইনে উত্তর
- **Entity** = database টেবিলের সাথে মেলানো Java class
- **Repository** = database এর সাথে সরাসরি কথা বলে (save, findAll, delete)
- **Service** = business logic রাখে, Repository আর Controller এর মাঝে থাকে
- **Controller** = web request ধরে, Service কে call করে, কোন page দেখাবে ঠিক করে
- **Thymeleaf** = Java এর data নিয়ে HTML এ বসিয়ে actual webpage বানায়

## URL মনে রাখো
```
GET  /students              → list
GET  /students/new          → add form
POST /students/save         → save new
GET  /students/edit/{id}    → edit form
POST /students/update/{id}  → update (save না!)
GET  /students/delete/{id}  → delete
```
