package boubyan.com.studentmanagementsystem.service;

import boubyan.com.studentmanagementsystem.dto.CourseDTO;
import boubyan.com.studentmanagementsystem.dto.CreateCourseRequest;
import boubyan.com.studentmanagementsystem.model.Course;
import boubyan.com.studentmanagementsystem.model.UserProfile;
import boubyan.com.studentmanagementsystem.repository.CourseRepository;
import boubyan.com.studentmanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CourseService courseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllCoursesWithStudents_shouldReturnListOfCourseDTOs() {
        // Arrange
        List<Course> courses = Arrays.asList(
                createMockCourse(1L, "Course 1", Collections.emptySet()),
                createMockCourse(2L, "Course 2", Collections.emptySet())
        );
        when(courseRepository.findAllWithStudents()).thenReturn(courses);

        // Act
        List<CourseDTO> result = courseService.getAllCoursesWithStudents();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Course 1", result.get(0).getName());
        assertEquals("Course 2", result.get(1).getName());
        verify(courseRepository, times(1)).findAllWithStudents();
    }

    @Test
    void registerToCourse_shouldAddStudentToCourse() {
        // Arrange
        Long userId = 1L;
        Long courseId = 1L;
        UserProfile user = createMockUser(userId, "John");
        Course course = spy(createMockCourse(courseId, "Test Course", new HashSet<>()));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findByIdWithStudents(courseId)).thenReturn(Optional.of(course));

        // Act
        courseService.registerToCourse(userId, courseId);

        // Assert
        verify(course).addStudent(user);
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void cancelCourseRegistration_shouldRemoveStudentFromCourse() {
        // Arrange
        Long userId = 1L;
        Long courseId = 1L;
        UserProfile user = createMockUser(userId, "John");
        Course course = createMockCourse(courseId, "Test Course", new HashSet<>(Collections.singletonList(user)));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findByIdWithStudents(courseId)).thenReturn(Optional.of(course));

        // Act
        courseService.cancelCourseRegistration(userId, courseId);

        // Assert
        assertFalse(course.getStudents().contains(user));
        verify(courseRepository, times(1)).save(course);
    }

    @Test
    void getCourseScheduleAsPdf_shouldReturnByteArray() {
        // Arrange
        Long userId = 1L;
        UserProfile user = createMockUser(userId, "John");
        List<Course> userCourses = Arrays.asList(
                createMockCourse(1L, "Course 1", Collections.emptySet()),
                createMockCourse(2L, "Course 2", Collections.emptySet())
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findAllByStudentId(userId)).thenReturn(userCourses);

        // Act
        byte[] result = courseService.getCourseScheduleAsPdf(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void createCourse_shouldReturnCourseDTO() {
        // Arrange
        CreateCourseRequest request = new CreateCourseRequest();
        request.setName("New Course");
        request.setDescription("Description");
        request.setStartDate(LocalDateTime.now());
        request.setEndDate(LocalDateTime.now().plusDays(30));

        Course savedCourse = createMockCourse(1L, "New Course", Collections.emptySet());
        when(courseRepository.save(any(Course.class))).thenReturn(savedCourse);

        // Act
        CourseDTO result = courseService.createCourse(request);

        // Assert
        assertNotNull(result);
        assertEquals("New Course", result.getName());
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    private Course createMockCourse(Long id, String name, Set<UserProfile> students) {
        Course course = new Course();
        course.setId(id);
        course.setName(name);
        course.setDescription("Description for " + name);
        course.setStartDate(LocalDateTime.now());
        course.setEndDate(LocalDateTime.now().plusDays(30));
        course.setStudents(students);
        return course;
    }

    private UserProfile createMockUser(Long id, String firstName) {
        UserProfile user = new UserProfile();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName("Doe");
        user.setEmail(firstName.toLowerCase() + "@example.com");
        user.setUsername(firstName.toLowerCase());
        return user;
    }
}