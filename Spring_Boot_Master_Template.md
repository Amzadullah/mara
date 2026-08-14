# Spring Boot CRUD — Master Template (যেকোনো Entity এর জন্য)

**নিয়ম: যেখানেই `// CHANGE:` কমেন্ট আছে, শুধু ঐটুকু বদলাও। বাকি সব লাইন হুবহু এক থাকে, প্রতিটা exam এ, প্রতিটা entity তে।**

---

## ধাপ ১ — Project Setup

1. start.spring.io এ যাও
2. Maven + Java + Spring Boot (latest) সিলেক্ট করো
3. Dependencies: **Spring Web, Thymeleaf, Lombok, Spring Data JPA, H2 Database**
4. Generate → zip download → extract → VS Code এ Open Folder

## ধাপ ২ — Package বানাও

`src/main/java/com/example/demo/` এর ভেতরে:
```
model/
repository/
controller/
```

---

## ধাপ ৩ — Entity (`model/EntityName.java`)

```java
package com.example.demo.model;              // CHANGE: শুধু যদি project name "demo" না হয়

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "tablename")                     // CHANGE: table এর নাম (lowercase, plural — যেমন "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityName {                      // CHANGE: class নাম (যেমন "Employee")

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                            // এই ৪ লাইন (Id block) সবসময় অবিকল একই থাকে

    private String field1;                      // CHANGE: field নাম + type (যেমন "name")
    private Double field2;                      // CHANGE: field নাম + type (যেমন "salary")
    // দরকার হলে আরো field যোগ করো এই প্যাটার্নে: private <Type> <name>;
}
```

**মনে রাখার জিনিস:** `@Entity`, `@Table`, `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)` — এই ৭টা annotation **শব্দে শব্দে একই**, কোনো entity তেই বদলায় না।

---

## ধাপ ৪ — Repository (`repository/EntityNameRepository.java`)

```java
package com.example.demo.repository;

import com.example.demo.model.EntityName;       // CHANGE: entity নাম
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntityNameRepository extends JpaRepository<EntityName, Long> {
    // CHANGE: "EntityNameRepository" আর <EntityName, Long> এ entity নাম বসাও
    // body সবসময় ফাঁকা — এখানে কিছুই লিখতে হয় না
}
```

---

## ধাপ ৫ — Controller (`controller/EntityNameController.java`)

```java
package com.example.demo.controller;

import com.example.demo.model.EntityName;                 // CHANGE
import com.example.demo.repository.EntityNameRepository;  // CHANGE
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/entities")                    // CHANGE: URL path (যেমন "/employees")
public class EntityNameController {              // CHANGE: class নাম

    private final EntityNameRepository repository;   // CHANGE: repository type

    public EntityNameController(EntityNameRepository repository) {  // CHANGE
        this.repository = repository;
    }

    // ---- READ (list) ----
    @GetMapping
    public String list(Model model) {
        model.addAttribute("list", repository.findAll());   // "list" নাম রাখলেই simplicity থাকে
        return "list";                            // এই string = templates/list.html
    }

    // ---- CREATE (empty form) ----
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("item", new EntityName());       // CHANGE: new EntityName()
        return "form";                            // এই string = templates/form.html
    }

    // ---- CREATE + UPDATE উভয়ের জন্য একই save() ----
    @PostMapping("/save")
    public String save(@ModelAttribute EntityName item) {    // CHANGE: parameter type
        repository.save(item);
        return "redirect:/entities";              // CHANGE: URL path (ধাপ ৫ এর RequestMapping এর সাথে মিলিয়ে)
    }

    // ---- UPDATE (pre-filled form) ----
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("item", repository.findById(id).orElseThrow());
        return "form";
    }

    // ---- DELETE ----
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        repository.deleteById(id);
        return "redirect:/entities";              // CHANGE: উপরের মতোই URL path মিলাও
    }
}
```

**মনে রাখার জিনিস:** ৫টা method এর **নাম, annotation, structure** সবসময় একই থাকে — শুধু entity type আর URL path বদলায়। এই ৫টাই সব সময় লাগবে: list → add form → save → edit form → delete।

---

## ধাপ ৬ — Templates (`src/main/resources/templates/`)

### `list.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>List</title></head>
<body>
<h2>List</h2>
<a th:href="@{/entities/add}">Add New</a>          <!-- CHANGE: URL path -->
<table border="1">
<tr><th>ID</th><th>Field1</th><th>Actions</th></tr>  <!-- CHANGE: কলাম হেডার -->
<tr th:each="x : ${list}">                          <!-- "list" = Controller এ addAttribute করা নাম -->
<td th:text="${x.id}"></td>
<td th:text="${x.field1}"></td>                     <!-- CHANGE: field নাম -->
<td>
<a th:href="@{/entities/edit/{id}(id=${x.id})}">Edit</a>     <!-- CHANGE: URL path -->
<a th:href="@{/entities/delete/{id}(id=${x.id})}">Delete</a> <!-- CHANGE: URL path -->
</td>
</tr>
</table>
</body>
</html>
```

### `form.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>Form</title></head>
<body>
<form th:action="@{/entities/save}" th:object="${item}" method="post">  <!-- CHANGE: URL path -->
<input type="hidden" th:field="*{id}">              <!-- এই লাইন সবসময় থাকবেই, Update ঠিক করার জন্য -->
<label>Field1</label>
<input type="text" th:field="*{field1}">            <!-- CHANGE: field নাম, দরকারে প্রতিটা field এর জন্য একটা input -->
<button type="submit">Save</button>
</form>
</body>
</html>
```

**মনে রাখার জিনিস:** `th:each`, `th:text`, `th:field`, `th:href`, `th:object`, `th:action` — এই ৬টা attribute সব template এ ঘুরেফিরে আসে। `<input type="hidden" th:field="*{id}">` **কখনোই বাদ দেওয়া যাবে না** — এটা ছাড়া Update কাজ করবে না, নতুন row তৈরি হয়ে যাবে।

---

## ধাপ ৭ — Run + Test

1. `DemoApplication.java` খোলো → ▷ (play button) চাপো
2. Terminal এ `Tomcat started on port 8080` দেখলে ঠিক আছে
3. Browser: `http://localhost:8080/entities` (তোমার URL path বসাও)
4. Test flow: list দেখাও (খালি) → Add New → form পূরণ → Save → list এ নতুন row → Edit → বদলাও → Save → Delete

---

## 🔑 এক নজরে — কী বদলায়, কী বদলায় না

| জিনিস | বদলায়? |
|---|---|
| `@Entity`, `@Table`, `@Data`, `@Id`, `@GeneratedValue` annotation গুলা | ❌ না, সবসময় same |
| Class নাম (Product/Employee/Book...) | ✅ হ্যাঁ |
| Field নাম আর type | ✅ হ্যাঁ |
| Repository এর `extends JpaRepository<X, Long>` structure | ❌ না, শুধু `X` বদলায় |
| Controller এর ৫টা method এর নাম, annotation, return logic | ❌ না, সবসময় same |
| URL path (`/entities`) | ✅ হ্যাঁ (সব জায়গায় consistent রাখতে হবে) |
| `th:each`, `th:text`, `th:field` ইত্যাদি Thymeleaf syntax | ❌ না, সবসময় same |
| Template এর field নাম, table column | ✅ হ্যাঁ |

## 🩹 সবচেয়ে বেশি হওয়া ভুল
1. Controller এর URL (`@RequestMapping("/entities")`) আর template এর `th:href="@{/entities/...}"` এর নাম না মেলা
2. `return "list"` লেখা কিন্তু file এর নাম `list.html` না রাখা (বা উল্টো)
3. Edit form এ hidden `id` field বাদ দেওয়া
4. `@Data` ভুলে বাদ দেওয়া (getter/setter কাজ করবে না)
5. Entity, Repository, Controller — ৩টাই আলাদা package এ না রাখা (import miss হবে)
