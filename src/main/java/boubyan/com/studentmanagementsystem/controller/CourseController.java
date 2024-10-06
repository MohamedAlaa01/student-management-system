package boubyan.com.studentmanagementsystem.controller;

import boubyan.com.studentmanagementsystem.dto.CourseDTO;
import boubyan.com.studentmanagementsystem.dto.CreateCourseRequest;
import boubyan.com.studentmanagementsystem.model.SecurityUser;
import boubyan.com.studentmanagementsystem.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        List<CourseDTO> courses = courseService.getAllCoursesWithStudents();
        return ResponseEntity.ok(courses);
    }

    @PostMapping
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CreateCourseRequest createCourseRequest) {
        CourseDTO createdCourse = courseService.createCourse(createCourseRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
    }

    @PostMapping("/{courseId}/register")
    public ResponseEntity<?> registerToCourse(@PathVariable Long courseId, Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            courseService.registerToCourse(userId, courseId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{courseId}/cancel")
    public ResponseEntity<?> cancelCourseRegistration(@PathVariable Long courseId, Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            courseService.cancelCourseRegistration(userId, courseId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/schedule")
    public ResponseEntity<byte[]> getCourseSchedule(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            byte[] pdfBytes = courseService.getCourseScheduleAsPdf(userId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "course_schedule.pdf");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Error generating PDF: " + e.getMessage()).getBytes());
        }
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication.getPrincipal() instanceof SecurityUser) {
            return ((SecurityUser) authentication.getPrincipal()).getId();
        }
        throw new IllegalStateException("Unexpected principal type in Authentication object");
    }
}