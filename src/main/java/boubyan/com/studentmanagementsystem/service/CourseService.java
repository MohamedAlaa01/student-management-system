package boubyan.com.studentmanagementsystem.service;

import boubyan.com.studentmanagementsystem.dto.CourseDTO;
import boubyan.com.studentmanagementsystem.dto.CreateCourseRequest;
import boubyan.com.studentmanagementsystem.dto.UserProfileDTO;
import boubyan.com.studentmanagementsystem.model.Course;
import boubyan.com.studentmanagementsystem.model.UserProfile;
import boubyan.com.studentmanagementsystem.repository.CourseRepository;
import boubyan.com.studentmanagementsystem.repository.UserRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CourseService(CourseRepository courseRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "courses", key = "'allCourses'")
    public List<CourseDTO> getAllCoursesWithStudents() {
        return courseRepository.findAllWithStudents().stream()
                .map(this::convertToCourseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = {"courses", "courseSchedule"}, allEntries = true)
    public void registerToCourse(Long userId, Long courseId) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Course course = courseRepository.findByIdWithStudents(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        course.addStudent(user);
        courseRepository.save(course);
    }

    @Transactional
    @CacheEvict(value = {"courses", "courseSchedule"}, allEntries = true)
    public void cancelCourseRegistration(Long userId, Long courseId) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Course course = courseRepository.findByIdWithStudents(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        course.removeStudent(user);
        courseRepository.save(course);
    }

    @Cacheable(value = "courseSchedule", key = "#userId")
    public byte[] getCourseScheduleAsPdf(Long userId) {
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Course> userCourses = courseRepository.findAllByStudentId(userId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);

        try (Document document = new Document(pdf, PageSize.A4)) {
            addTitle(document, user);
            addSubtitle(document);
            addCourseTable(document, userCourses);
            addFooter(document);
        }

        return baos.toByteArray();
    }

    private void addTitle(Document document, UserProfile user) {
        Paragraph title = new Paragraph("Course Schedule for " + user.getFirstName() + " " + user.getLastName())
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);
    }

    private void addSubtitle(Document document) {
        Paragraph subtitle = new Paragraph("Generated on: " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(subtitle);
    }

    private void addCourseTable(Document document, List<Course> courses) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 30, 20, 20, 25}))
                .useAllAvailableWidth();

        addTableHeaders(table);
        addTableData(table, courses);

        document.add(table);
    }

    private void addTableHeaders(Table table) {
        String[] headers = {"#", "Course Name", "Start Date", "End Date", "Description"};
        for (String header : headers) {
            table.addHeaderCell(
                    new Cell().add(new Paragraph(header).setBold())
                            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                            .setTextAlignment(TextAlignment.CENTER)
            );
        }
    }

    private void addTableData(Table table, List<Course> courses) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        for (int i = 0; i < courses.size(); i++) {
            Course course = courses.get(i);
            table.addCell(new Cell().add(new Paragraph(String.valueOf(i + 1)).setTextAlignment(TextAlignment.CENTER)));
            table.addCell(new Cell().add(new Paragraph(course.getName())));
            table.addCell(new Cell().add(new Paragraph(course.getStartDate().format(dateFormatter)).setTextAlignment(TextAlignment.CENTER)));
            table.addCell(new Cell().add(new Paragraph(course.getEndDate().format(dateFormatter)).setTextAlignment(TextAlignment.CENTER)));
            table.addCell(new Cell().add(new Paragraph(course.getDescription())));
        }
    }

    private void addFooter(Document document) {
        Paragraph footer = new Paragraph("This schedule is subject to change. Please check regularly for updates.")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        document.add(footer);
    }

    @Transactional
    public CourseDTO createCourse(CreateCourseRequest createCourseRequest) {
        Course course = new Course();
        course.setName(createCourseRequest.getName());
        course.setDescription(createCourseRequest.getDescription());
        course.setStartDate(createCourseRequest.getStartDate());
        course.setEndDate(createCourseRequest.getEndDate());

        Course savedCourse = courseRepository.save(course);
        return convertToCourseDTO(savedCourse);
    }

    private CourseDTO convertToCourseDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        dto.setStartDate(course.getStartDate());
        dto.setEndDate(course.getEndDate());
        dto.setStudents(course.getStudents().stream()
                .map(this::convertToUserProfileDTO)
                .collect(Collectors.toSet()));
        return dto;
    }

    private UserProfileDTO convertToUserProfileDTO(UserProfile userProfile) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(userProfile.getId());
        dto.setUsername(userProfile.getUsername());
        dto.setEmail(userProfile.getEmail());
        dto.setFirstName(userProfile.getFirstName());
        dto.setLastName(userProfile.getLastName());
        return dto;
    }
}