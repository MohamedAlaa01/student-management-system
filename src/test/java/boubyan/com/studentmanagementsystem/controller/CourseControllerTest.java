package boubyan.com.studentmanagementsystem.controller;

import boubyan.com.studentmanagementsystem.Application;
import boubyan.com.studentmanagementsystem.dto.CourseDTO;
import boubyan.com.studentmanagementsystem.dto.CreateCourseRequest;
import boubyan.com.studentmanagementsystem.model.SecurityUser;
import boubyan.com.studentmanagementsystem.model.UserProfile;
import boubyan.com.studentmanagementsystem.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    private List<CourseDTO> courseDTOs;

    @BeforeEach
    void setUp() {
        courseDTOs = Arrays.asList(
                new CourseDTO(1L, "Math", "Mathematics course", LocalDateTime.now(), LocalDateTime.now().plusMonths(3), new HashSet<>()),
                new CourseDTO(2L, "Science", "Science course", LocalDateTime.now(), LocalDateTime.now().plusMonths(3), new HashSet<>())
        );

        CreateCourseRequest createCourseRequest = new CreateCourseRequest();
        createCourseRequest.setName("New Course");
        createCourseRequest.setDescription("New course description");

        UserProfile userProfile = new UserProfile();
        userProfile.setId(1L);
        userProfile.setUsername("user");
        userProfile.setRoles(Stream.of("USER").toList());
        SecurityUser securityUser = new SecurityUser(userProfile);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                securityUser,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void getAllCourses_ShouldReturnListOfCourses() throws Exception {
        when(courseService.getAllCoursesWithStudents()).thenReturn(courseDTOs);

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Math"))
                .andExpect(jsonPath("$[1].name").value("Science"));
    }



    @Test
    void registerToCourse_WithValidData_ShouldRegisterSuccessfully() throws Exception {
        doNothing().when(courseService).registerToCourse(anyLong(), anyLong());

        mockMvc.perform(post("/api/courses/1/register"))
                .andExpect(status().isOk());
    }

    @Test
    void cancelCourseRegistration_WithValidData_ShouldCancelSuccessfully() throws Exception {
        doNothing().when(courseService).cancelCourseRegistration(anyLong(), anyLong());

        mockMvc.perform(delete("/api/courses/1/cancel"))
                .andExpect(status().isOk());
    }

    @Test
    void getCourseSchedule_ShouldReturnPdfFile() throws Exception {
        byte[] pdfContent = "PDF content".getBytes();
        when(courseService.getCourseScheduleAsPdf(anyLong())).thenReturn(pdfContent);

        mockMvc.perform(get("/api/courses/schedule"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"filename\"; filename=\"course_schedule.pdf\""))
                .andExpect(content().bytes(pdfContent));
    }
}